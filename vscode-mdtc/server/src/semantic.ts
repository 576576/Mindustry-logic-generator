// 语义高亮:词法扫描生成 LSP semantic tokens(相对编码)
// 链式指令协同母指令:如 ushoot(1).target(...) 中 .target 依母指令 ushoot 标为 method
import { TextDocument } from "vscode-languageserver-textdocument";
import type { InstructionData } from "./data";

export const TOKEN_TYPES = [
  "comment", "string", "number", "operator", "keyword",
  "function", "method", "variable", "enumMember", "label",
];

const COMMENT = 0, STRING = 1, NUMBER = 2, OPERATOR = 3, KEYWORD = 4,
  FUNCTION = 5, METHOD = 6, VARIABLE = 7, ENUM_MEMBER = 8, LABEL = 9;

const KEYWORDS = [
  "if(", "else{", "for(", "while(", "do{", "function ", "import ", "repeat(", "raw(", "return",
];

export function encodeSemanticTokens(doc: TextDocument, data: InstructionData): number[] {
  const tokens: Array<[number, number, number, number]> = [];
  const lines = doc.getText().split("\n");
  const keys = [...data.items.map(i => i.fullKey)].sort((a, b) => b.length - a.length);
  const ops = data.operators;
  for (let li = 0; li < lines.length; li++) {
    scanLine(lines[li], li, keys, ops, data, tokens);
  }
  const out: number[] = [];
  let prevLine = 0, prevChar = 0;
  for (const [l, s, len, t] of tokens) {
    const dl = l - prevLine;
    out.push(dl, dl === 0 ? s - prevChar : s, len, t, 0);
    prevLine = l;
    prevChar = s;
  }
  return out;
}

function scanLine(line: string, li: number, keys: string[], ops: string[], data: InstructionData,
  out: Array<[number, number, number, number]>): void {
  let pos = 0;
  const len = line.length;
  const add = (s: number, l: number, t: number) => { if (l > 0) out.push([li, s, l, t]); };
  const isWord = (c: string) => /[A-Za-z0-9_.-]/.test(c);
  // 本行出现的可链母指令 → 其后紧跟的链键(.key)标为 method
  let pendingChain: Set<string> | null = null;
  while (pos < len) {
    const c = line[pos];
    if (/\s/.test(c)) { pos++; continue; }
    if (c === ":" && line[pos + 1] === ":") { add(pos, len - pos, COMMENT); return; }
    if (c === '"') {
      let e = pos + 1;
      while (e < len && line[e] !== '"') e++;
      add(pos, e - pos + 1, STRING);
      pos = e + 1;
      continue;
    }
    if (c === "@") {
      let e = pos + 1;
      while (e < len && isWord(line[e])) e++;
      add(pos, e - pos, ENUM_MEMBER);
      pos = e;
      continue;
    }
    if (/\d/.test(c) || (c === "." && /\d/.test(line[pos + 1] ?? ""))) {
      let e = pos + 1;
      while (e < len && (/\d/.test(line[e]) || line[e] === ".")) e++;
      add(pos, e - pos, NUMBER);
      pos = e;
      continue;
    }
    const key = keys.find(k => line.startsWith(k, pos));
    if (key) {
      add(pos, key.length, key.startsWith(".") ? METHOD : FUNCTION);
      const chain = data.chainByParent.get(key);
      pendingChain = chain ? new Set(chain.map((c) => c.key)) : null;
      pos += key.length;
      continue;
    }
    const kw = KEYWORDS.find(k => line.startsWith(k, pos));
    if (kw) {
      add(pos, kw.length, KEYWORD);
      pos += kw.length;
      continue;
    }
    // 链键:母指令出现后,紧随其后的 .key 标为 method(即使不在指令表内)
    if (c === "." && pendingChain) {
      let e = pos + 1;
      while (e < len && /[A-Za-z0-9]/.test(line[e])) e++;
      const w = line.slice(pos + 1, e);
      if (pendingChain.has(w)) {
        add(pos, e - pos, METHOD);
        pos = e;
        continue;
      }
    }
    const op = ops.find(o => line.startsWith(o, pos));
    if (op) {
      add(pos, op.length, OPERATOR);
      pos += op.length;
      continue;
    }
    let e = pos;
    while (e < len && isWord(line[e])) e++;
    if (e > pos) { add(pos, e - pos, VARIABLE); pos = e; continue; }
    pos++;
  }
}
