// 签名帮助:定位光标所在调用,返回该指令在各自 .ts 中定义的参数提示(params)
import { TextDocument } from "vscode-languageserver-textdocument";
import { Position, SignatureHelp, SignatureInformation, ParameterInformation } from "vscode-languageserver";
import type { InstructionData, ChainKeyInfo } from "./data";

/** 调用点:某个 "(" 的位置及其头部键(如 "print" / ".target") */
interface CallSite {
  paren: number;
  head: string; // 不含 "("
}

/** 从 line 中向前找到光标前最近的调用点(跳过字符串/注释) */
function lastCallBefore(line: string, pos: number): CallSite | null {
  let inStr = false;
  for (let i = pos - 1; i >= 0; i--) {
    const c = line[i];
    if (c === '"') { inStr = !inStr; continue; }
    if (inStr) continue;
    if (c === ':' && i > 0 && line[i - 1] === ':') return null; // 注释
    if (c === ')') { continue; }
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

/** 找到 line[from] 处 "(" 的匹配右括号;无匹配返回 -1 */
function matchingClose(line: string, from: number): number {
  let depth = 0, inStr = false;
  for (let i = from; i < line.length; i++) {
    const c = line[i];
    if (c === '"') { inStr = !inStr; continue; }
    if (inStr) continue;
    if (c === '(') depth++;
    else if (c === ')') { depth--; if (depth === 0) return i; }
  }
  return -1;
}

/** 光标所在调用的顶层逗号数(activeParameter 用) */
function topLevelCommas(line: string, open: number, pos: number): number {
  let depth = 0, commas = 0, inStr = false;
  for (let i = open + 1; i < pos; i++) {
    const c = line[i];
    if (c === '"') { inStr = !inStr; continue; }
    if (inStr) continue;
    if (c === '(') depth++;
    else if (c === ')') depth--;
    else if (c === ',' && depth === 0) commas++;
  }
  return commas;
}

/** 解析链键所在母指令的链键表(如 ".target" → ushoot 的 chain) */
function resolveChainKey(data: InstructionData, call: CallSite, line: string): { params: string[]; parentKey: string } | null {
  // 从该链键调用点继续向前找母指令调用
  for (let i = call.paren - 1; i >= 0; i--) {
    if (line[i] !== '(') continue;
    let j = i - 1;
    while (j >= 0 && /[A-Za-z0-9_.-]/.test(line[j])) j--;
    const head = line.slice(j + 1, i);
    const fullKey = head + "(";
    const chain = data.chainByParent.get(fullKey);
    if (chain) {
      const ck = chain.find((c) => "." + c.key === call.head);
      if (ck) return { params: ck.params, parentKey: fullKey };
      return null;
    }
    i = j + 1;
  }
  return null;
}

export function signatureAt(doc: TextDocument, pos: Position, data: InstructionData): SignatureHelp | null {
  const lines = doc.getText().split("\n");
  if (pos.line < 0 || pos.line >= lines.length) return null;
  const line = lines[pos.line];
  const call = lastCallBefore(line, pos.character);
  if (!call) return null;

  let params: string[] = [];
  let labelPrefix = call.head;
  if (call.head.startsWith(".")) {
    const r = resolveChainKey(data, call, line);
    if (!r) return null;
    params = r.params;
  } else {
    const item = data.items.find((i) => i.fullKey === call.head + "(");
    if (!item) return null;
    params = item.params;
  }

  const sig: SignatureInformation = {
    label: call.head + "(" + params.join(", ") + ")",
    parameters: params.map((p) => ({ label: p } as ParameterInformation)),
  };
  const commas = topLevelCommas(line, call.paren, pos.character);
  let active = commas;
  // 刚输入逗号或没有参数时不显示 activeParameter 指向越界
  if (active >= params.length) active = Math.max(0, params.length - 1);
  if (params.length > 0) sig.activeParameter = active;
  return { signatures: [sig], activeSignature: 0, activeParameter: active };
}
