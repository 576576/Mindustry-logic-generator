// .mdtcode(Mindustry 逻辑汇编)支持:语义高亮 / 诊断 / 悬停 / goto / 补全
import { TextDocument } from "vscode-languageserver-textdocument";
import { Diagnostic, DiagnosticSeverity, Position, Range, Hover, Location, CompletionItem, CompletionItemKind } from "vscode-languageserver";
import type { InstructionData, InstrInfo } from "./data";
import { lookupDoc } from "./hover";
import { mdtcodeError } from "./messages";

const WORD_RE = /[A-Za-z0-9_.@-]/;

/** 取行内第 n 个 token(跳过字符串);返回 {start, end, text} 或 null */
function tokenAt(line: string, n: number): { start: number; end: number; text: string } | null {
  let i = 0, inStr = false, count = 0;
  while (i < line.length) {
    const c = line[i];
    if (c === '"') { inStr = !inStr; i++; continue; }
    if (inStr) { i++; continue; }
    if (/\s/.test(c)) { i++; continue; }
    const start = i;
    while (i < line.length && !/\s/.test(line[i]) && line[i] !== '"') i++;
    if (count === n) return { start, end: i, text: line.slice(start, i) };
    count++;
  }
  return null;
}

/** 行首 token(忽略前导空白) */
function headToken(line: string): string {
  const m = line.match(/^\s*(\S+)/);
  return m ? m[1] : "";
}

// ==================== 语义高亮 ====================

export function mdtcodeSemanticTokens(doc: TextDocument, data: InstructionData): number[] {
  const lines = doc.getText().split("\n");
  const out: Array<[number, number, number, number]> = [];
  const add = (li: number, s: number, l: number, t: number) => { if (l > 0) out.push([li, s, l, t]); };
  const T = { COMMENT: 0, STRING: 1, NUMBER: 2, OPERATOR: 3, KEYWORD: 4, FUNCTION: 5, METHOD: 6, VARIABLE: 7, ENUM: 8, LABEL: 9 };
  for (let li = 0; li < lines.length; li++) {
    const line = lines[li];
    if (line.startsWith("::")) { add(li, 0, line.length, T.LABEL); continue; }
    let pos = 0;
    const len = line.length;
    let first = true;
    let tokIdx = 0;
    const head = headToken(line);
    const headSelects = data.selectsByMcode.get(head);
    while (pos < len) {
      const c = line[pos];
      if (/\s/.test(c)) { pos++; continue; }
      if (c === '"') {
        let e = pos + 1;
        while (e < len && line[e] !== '"') e++;
        add(li, pos, e - pos + 1, T.STRING);
        pos = e + 1; first = false; continue;
      }
      if (c === "@") {
        let e = pos + 1;
        while (e < len && WORD_RE.test(line[e])) e++;
        add(li, pos, e - pos, T.ENUM);
        pos = e; first = false; continue;
      }
      if (/\d/.test(c) || (c === "-" && /\d/.test(line[pos + 1] ?? ""))) {
        let e = pos + 1;
        while (e < len && (/[\d.]/.test(line[e]) || line[e] === "e")) e++;
        add(li, pos, e - pos, T.NUMBER);
        pos = e; first = false; continue;
      }
      let e = pos;
      while (e < len && WORD_RE.test(line[e])) e++;
      const w = line.slice(pos, e);
      if (e > pos) {
        if (first && data.mcodes.has(w)) add(li, pos, e - pos, T.FUNCTION);
        else if (tokIdx === 1 && (data.opNames.has(w) || headSelects?.has(w))) {
          // op 运算符名 / control·lookup·ucontrol 等分派名
          add(li, pos, e - pos, T.FUNCTION);
        } else add(li, pos, e - pos, T.VARIABLE);
        first = false;
        tokIdx++;
        pos = e;
        continue;
      }
      pos++;
    }
  }
  const rel: number[] = [];
  let prevLine = 0, prevChar = 0;
  for (const [l, s, len2, t] of out) {
    const dl = l - prevLine;
    rel.push(dl, dl === 0 ? s - prevChar : s, len2, t, 0);
    prevLine = l;
    prevChar = s;
  }
  return rel;
}

// ==================== 诊断 ====================

export function mdtcodeDiagnostics(doc: TextDocument, data: InstructionData): Diagnostic[] {
  const out: Diagnostic[] = [];
  const lines = doc.getText().split("\n");
  const labels = new Set<string>();
  for (const l of lines) {
    if (l.startsWith("::")) labels.add(l.slice(2).trim());
  }
  const range = (li: number, s: number, e: number) => Range.create(Position.create(li, s), Position.create(li, Math.max(e, s + 1)));
  for (let li = 0; li < lines.length; li++) {
    const line = lines[li];
    if (!line.trim() || line.startsWith("::")) continue;
    const head = headToken(line);
    if (head && !data.mcodes.has(head)) {
      out.push({ severity: DiagnosticSeverity.Error, range: range(li, 0, head.length), message: mdtcodeError("unknownInstr", head), source: "mdtc" });
    }
    if (head === "jump") {
      const tgt = tokenAt(line, 1);
      if (tgt && /^-?\d+$/.test(tgt.text)) {
        const n = parseInt(tgt.text, 10);
        if (n < 0 || n >= lines.length) {
          out.push({ severity: DiagnosticSeverity.Error, range: range(li, tgt.start, tgt.end), message: mdtcodeError("jumpRange", String(n)), source: "mdtc" });
        }
      }
    }
    if (head === "set") {
      const t2 = tokenAt(line, 1);
      if (t2?.text === "@counter") {
        const t3 = tokenAt(line, 2);
        if (t3 && !labels.has(t3.text)) {
          out.push({ severity: DiagnosticSeverity.Error, range: range(li, t3.start, t3.end), message: mdtcodeError("labelNotFound", t3.text), source: "mdtc" });
        }
      }
    }
    if (head === "op") {
      const t2 = tokenAt(line, 1);
      if (t2 && !data.opNames.has(t2.text)) {
        out.push({ severity: DiagnosticSeverity.Error, range: range(li, t2.start, t2.end), message: mdtcodeError("unknownOp", t2.text), source: "mdtc" });
      }
    }
  }
  return out;
}

// ==================== 悬停 ====================

function wordAt(line: string, col: number): string {
  let s = col, e = col;
  while (s > 0 && WORD_RE.test(line[s - 1])) s--;
  while (e < line.length && WORD_RE.test(line[e])) e++;
  return line.slice(s, e);
}

export function mdtcodeHover(doc: TextDocument, pos: Position, data: InstructionData): Hover | null {
  const lines = doc.getText().split("\n");
  if (pos.line < 0 || pos.line >= lines.length) return null;
  const line = lines[pos.line];
  const word = wordAt(line, pos.character);
  if (!word) return null;
  let cands: InstrInfo[] = [];
  const head = headToken(line);
  if (head === "op" && data.opNames.has(word)) {
    cands = data.items.filter((i) => i.key === word); // op 运算符名 → front 指令
  } else if (data.selectsByMcode.get(head)?.has(word)) {
    // control/lookup/ucontrol 分派名 → 对应指令(如 enabled → enable)
    cands = data.items.filter((i) => i.select.includes(word));
  } else {
    cands = data.items.filter((i) => i.mcode === word);
  }
  if (cands.length === 0) return null;
  const docs = cands.map((i) => lookupDoc(i.key + "(")).filter(Boolean);
  if (docs.length === 0) return null;
  return {
    contents: { kind: "markdown", value: "**" + word + "** — " + cands[0].desc + "\n\n" + docs.join("\n---\n") },
    range: Range.create(Position.create(pos.line, 0), Position.create(pos.line, line.length)),
  };
}

// ==================== goto(标签 / jump 行号) ====================

export function mdtcodeDefinitionAt(doc: TextDocument, lineIdx: number, char: number, data: InstructionData): Location[] {
  const lines = doc.getText().split("\n");
  if (lineIdx < 0 || lineIdx >= lines.length) return [];
  const line = lines[lineIdx];
  const loc = (l: number, s: number, e: number): Location => ({
    uri: "",
    range: Range.create(Position.create(l, s), Position.create(l, Math.max(e, s + 1))),
  });
  // 1) ::标签 → 引用(set @counter 标签 / jump 标签?)
  if (line.startsWith("::")) {
    const name = line.slice(2).trim();
    const refs: Location[] = [];
    for (let i = 0; i < lines.length; i++) {
      const m = lines[i].match(/set\s+@counter\s+(\S+)/);
      if (m && m[1] === name) refs.push(loc(i, 0, lines[i].length));
    }
    return refs;
  }
  // 2) set @counter 标签 → ::标签
  const sc = line.match(/set\s+@counter\s+(\S+)/);
  if (sc) {
    const name = sc[1];
    const w = wordAt(line, char);
    if (w === name || w === "@counter") {
      for (let i = 0; i < lines.length; i++) {
        if (lines[i].startsWith("::" + name)) return [loc(i, 0, lines[i].length)];
      }
    }
  }
  // 3) jump 行号 → 目标行
  if (headToken(line) === "jump") {
    const tgt = tokenAt(line, 1);
    if (tgt && /^-?\d+$/.test(tgt.text)) {
      const w = wordAt(line, char);
      if (w === tgt.text) {
        const n = parseInt(tgt.text, 10);
        if (n >= 0 && n < lines.length) return [loc(n, 0, lines[n].length)];
      }
    }
  }
  return [];
}

// ==================== 补全 ====================

export function mdtcodeCompletion(data: InstructionData): CompletionItem[] {
  const out: CompletionItem[] = [];
  for (const m of data.mcodes) {
    out.push({ label: m, kind: 3 as CompletionItemKind, detail: "mdtcode 指令", insertText: m });
  }
  return out;
}