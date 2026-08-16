// 诊断:调用 Java CLI 编译临时文件,结合括号/负数扫描,按 locale 输出
import * as fs from "node:fs";
import * as os from "node:os";
import * as path from "node:path";
import { execFileSync } from "node:child_process";
import { TextDocument } from "vscode-languageserver-textdocument";
import { Diagnostic, DiagnosticSeverity, Position, Range } from "vscode-languageserver";
import { negativeWarning, bracketError, translate, compileException } from "./messages";

const LINE_RE = /line\s*(\d+)/;
const ANSI_RE = /\x1B\[[;\d]*m/g;

export interface ValidateResult {
  diagnostics: Diagnostic[];
  output: string;
}

export function validateDocument(doc: TextDocument, cliJar: string): ValidateResult {
  const text = doc.getText();
  const out: Diagnostic[] = [];

  // ---- 1. 编译(临时文件) ----
  let errText = "";
  let output = "";
  let tmp: string | null = null;
  try {
    tmp = path.join(os.tmpdir(), `mdtc_lsp_${process.pid}_${Date.now()}.mdtc`);
    fs.writeFileSync(tmp, text, "utf8");
    const outTmp = tmp.replace(/\.mdtc$/, ".mdtcode");
    try {
      const res = execFileSync("java", ["-jar", cliJar, "-i", tmp, "-o", outTmp],
        { encoding: "utf8", timeout: 30000, stdio: ["ignore", "pipe", "pipe"] });
      output = res ?? "";
    } catch (e: any) {
      errText = (e?.stderr ?? "") + "\n" + (e?.stdout ?? "");
      if (e?.status === undefined) {
        out.push({ severity: DiagnosticSeverity.Error, range: docLineRange(doc, 0),
          message: compileException(e), source: "mdtc" });
      }
    } finally {
      try { fs.unlinkSync(outTmp); } catch { /* ignore */ }
    }
  } finally {
    if (tmp) { try { fs.unlinkSync(tmp); } catch { /* ignore */ } }
  }

  // ---- 2. 编译错误(stderr;括号错误由扫描覆盖,> 续行丢弃) ----
  for (const raw of errText.split("\n")) {
    const line = raw.replace(ANSI_RE, "").trim();
    if (!line || line.includes("Compile Warning:")
        || line.includes("Syntax error on token") || line.startsWith(">")) continue;
    out.push(errorFrom(line, doc));
  }

  // ---- 3. 括号配对扫描(精确标红 token) ----
  out.push(...scanBrackets(doc));

  // ---- 4. 负数守卫警告(精确标黄 token) ----
  out.push(...scanNegativeWarnings(doc));

  // ---- 5. 其他警告(CLI 的 "Compile Warning:" 行;负数守卫由扫描覆盖) ----
  for (const raw of errText.split("\n")) {
    const line = raw.replace(ANSI_RE, "").trim();
    if (!line.startsWith("Compile Warning:")) continue;
    const msg = translate(line.substring("Compile Warning:".length).trim());
    if (msg.includes("is not wrapped in parentheses") || msg.includes("未被 () 包裹")) continue;
    out.push({ severity: DiagnosticSeverity.Warning, range: docLineRange(doc, 0), message: msg, source: "mdtc" });
  }

  return { diagnostics: out, output };
}

function errorFrom(msg: string, doc: TextDocument): Diagnostic {
  let ln = -1;
  const m = msg.match(LINE_RE);
  if (m) ln = parseInt(m[1], 10);
  if (ln < 0) {
    // 消息中的原文行片段定位
    const lines = doc.getText().split("\n");
    for (let i = 0; i < lines.length; i++) {
      const src = lines[i].trim();
      if (src.length >= 3 && !src.startsWith("::") && msg.includes(src)) { ln = i; break; }
    }
  }
  return { severity: DiagnosticSeverity.Error, range: docLineRange(doc, ln), message: translate(msg.trim()), source: "mdtc" };
}

function docLineRange(doc: TextDocument, line: number): Range {
  if (line >= 0) {
    return Range.create(Position.create(line, 0), Position.create(line, 2147483647));
  }
  return Range.create(Position.create(0, 0), Position.create(2147483647, 0));
}

/** 括号配对扫描:range 仅覆盖不匹配的括号字符 */
function scanBrackets(doc: TextDocument): Diagnostic[] {
  const out: Diagnostic[] = [];
  const lines = doc.getText().split("\n");
  const stack: Array<[number, number, string]> = [];
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    let inString = false;
    for (let j = 0; j < line.length; j++) {
      const c = line[j];
      if (c === ":" && j + 1 < line.length && line[j + 1] === ":") break;
      if (c === '"') { inString = !inString; continue; }
      if (inString) continue;
      if (c === "(" || c === "{") {
        stack.push([i, j, c]);
      } else if (c === ")" || c === "}") {
        const want = c === ")" ? "(" : "{";
        const top = stack[stack.length - 1];
        if (!top || top[2] !== want) {
          out.push({ severity: DiagnosticSeverity.Error, range: Range.create(Position.create(i, j), Position.create(i, j + 1)), message: bracketError(c), source: "mdtc" });
        } else {
          stack.pop();
        }
      }
    }
  }
  while (stack.length > 0) {
    const t = stack.pop()!;
    out.push({ severity: DiagnosticSeverity.Error, range: Range.create(Position.create(t[0], t[1]), Position.create(t[0], t[1] + 1)), message: bracketError(t[2]), source: "mdtc" });
  }
  return out;
}

/** 负数守卫扫描:中置运算符后无空格负数,range 仅覆盖负数 token */
function scanNegativeWarnings(doc: TextDocument): Diagnostic[] {
  const out: Diagnostic[] = [];
  const lines = doc.getText().split("\n");
  for (let i = 0; i < lines.length; i++) {
    for (const r of findInfixNegativeRanges(lines[i])) {
      const token = lines[i].substring(r[0], r[1]);
      out.push({ severity: DiagnosticSeverity.Warning,
        range: Range.create(Position.create(i, r[0]), Position.create(i, r[1])),
        message: negativeWarning(token), source: "mdtc" });
    }
  }
  return out;
}

/** 中置运算符集合(与 builtins/operators.ts 一致;排除 = 与括号) */
const INFIX_OPS = new Set([
  "+", "-", "*", "/", "//", ".%", ".^", "%%",
  "<<", ">>", ">>>", "&", "|", "^", "&&", "||",
  "==", "!=", "<=", ">=", "===", "<", ">",
]);

/** 负数守卫:中置运算符后无空格负数的 token 区间 */
function findInfixNegativeRanges(line: string): Array<[number, number]> {
  const tokens = tokenize(line);
  const out: Array<[number, number]> = [];
  let searchFrom = 0;
  for (let i = 1; i < tokens.length; i++) {
    const t = tokens[i];
    if (t.startsWith("-") && t.length > 1 && !/\s/.test(t[1]) && INFIX_OPS.has(tokens[i - 1])) {
      const idx = line.indexOf(t, searchFrom);
      if (idx >= 0) {
        out.push([idx, idx + t.length]);
        searchFrom = idx + t.length;
      }
    }
  }
  return out;
}

/** 分词:与 Java stringSplit 同语义(负号无空格并入 token;spaced 减号独立) */
function tokenize(line: string): string[] {
  const out: string[] = [];
  let cur = "";
  const flush = () => { if (cur) { out.push(cur); cur = ""; } };
  for (let i = 0; i < line.length; i++) {
    const c = line[i];
    if (/[\s,;]/.test(c)) {
      flush();
    } else if (c === "(" || c === ")") {
      flush();
      out.push(c);
    } else if (c === "-") {
      const prev = i > 0 ? line[i - 1] : " ";
      const next = i + 1 < line.length ? line[i + 1] : " ";
      if (/\s/.test(prev) && /\s/.test(next)) {
        flush();
        out.push("-");
      } else {
        // 负数/连字符标识符:并入当前 token
        cur += c;
      }
    } else if (/[+*/<>!=&|^%]/.test(c)) {
      flush();
      let j = i;
      while (j < line.length && /[+*/<>!=&|^%.]/.test(line[j])) j++;
      out.push(line.substring(i, j));
      i = j - 1;
    } else {
      cur += c;
    }
  }
  flush();
  return out;
}
