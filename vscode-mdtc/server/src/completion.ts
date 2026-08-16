// 补全:指令键 + 关键字;链式指令协同母指令(如 ushoot(...) 后提示 .target( )
import type { CompletionItem, CompletionItemKind } from "vscode-languageserver";
import type { TextDocument } from "vscode-languageserver-textdocument";
import type { InstructionData, ChainKeyInfo } from "./data";

export function buildCompletion(data: InstructionData, doc?: TextDocument, pos?: { line: number; character: number }): CompletionItem[] {
  const items: CompletionItem[] = [];

  // 光标紧贴 "." 之后(且非数字小数点):点链指令的 insertText 去掉前导 ".",
  // 避免用户已键入的 "." 与补全内容拼出 "..config(" 双重点
  const line = doc && pos ? (doc.getText().split("\n")[pos.line] ?? "") : "";
  const afterDot = pos ? afterDotState(line, pos.character) : false;
  const close = (s: string) => (s.endsWith("(") ? s + ")" : s);
  const insert = (s: string) => close(afterDot && s.startsWith(".") ? s.slice(1) : s);

  // 标签带参数提示:如 .color(hex color) / print(内容);插入文本仍为 .color()
  const labelWithParams = (head: string, params: string[]) =>
    (head.endsWith("(") ? head.slice(0, -1) : head) + "(" + params.join(", ") + ")";

  // 链式上下文:光标位于可链母指令调用内/其后 → 优先提示该母指令的链键
  const chainCtx = doc && pos ? chainContextAt(doc, pos, data) : null;
  if (chainCtx) {
    for (const ck of chainCtx.chain) {
      const full = "." + ck.key + "(";
      items.push({
        label: labelWithParams(full, ck.params),
        kind: 6 as CompletionItemKind, // Method
        detail: "链式参数(母指令 " + chainCtx.parentKey.slice(0, -1) + ")",
        insertText: insert(full),
        sortText: "0" + ck.key,
      });
    }
  }

  for (const i of data.items) {
    items.push({
      label: labelWithParams(i.fullKey, i.params),
      kind: (i.category.includes("链式") ? 6 : 3) as CompletionItemKind, // Method | Function
      detail: i.desc || i.category,
      insertText: insert(i.fullKey),
    });
  }
  for (const kw of ["if(", "else{", "for(", "while(", "do{", "function ", "import ",
    "repeat(", "raw(", "return", "::"]) {
    items.push({ label: close(kw), kind: 14 as CompletionItemKind, detail: "关键字", insertText: close(kw) });
  }
  return items;
}

/** 光标是否紧贴 "." 之后(排除 3. / 10. 之类数字小数点) */
function afterDotState(line: string, pos: number): boolean {
  if (pos <= 0) return false;
  const c = line[pos - 1];
  if (c !== ".") return false;
  const prev = line[pos - 2] ?? "";
  if (/\d/.test(prev)) return false; // 数字小数点:3. / 10. / 10.^2
  return true;
}

/** 从 line 中向前找光标前最近的调用点(跳过字符串/注释) */
function lastCallBefore(line: string, pos: number): { paren: number; head: string } | null {
  let inStr = false;
  for (let i = pos - 1; i >= 0; i--) {
    const c = line[i];
    if (c === '"') { inStr = !inStr; continue; }
    if (inStr) continue;
    if (c === ':' && i > 0 && line[i - 1] === ':') return null; // 注释
    if (c === '(') {
      let j = i - 1;
      while (j >= 0 && /[A-Za-z0-9_.-]/.test(line[j])) j--;
      const head = line.slice(j + 1, i);
      if (!head) return null;
      return { paren: i, head };
    }
  }
  return null;
}

interface ChainCtx {
  parentKey: string; // 如 "ushoot("
  chain: ChainKeyInfo[];
}

/**
 * 链式上下文:从光标向前找最近的可链母指令调用。
 * - 光标在母指令括号内(未闭合)或闭合括号之后 → 返回其链键
 * - 链键调用(如 .target() )跳过,继续向前找母指令
 * - 最近的调用是普通指令(如 print() )→ 返回 null
 */
export function chainContextAt(doc: TextDocument, pos: { line: number; character: number }, data: InstructionData): ChainCtx | null {
  const lines = doc.getText().split("\n");
  if (pos.line < 0 || pos.line >= lines.length) return null;
  const line = lines[pos.line];
  let scan = pos.character;
  for (;;) {
    const call = lastCallBefore(line, scan);
    if (!call) return null;
    if (call.head.startsWith(".")) { scan = call.paren; continue; }
    const fullKey = call.head + "(";
    const chain = data.chainByParent.get(fullKey);
    if (chain) return { parentKey: fullKey, chain };
    return null;
  }
}
