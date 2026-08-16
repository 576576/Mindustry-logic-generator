// goto:jump/jump2 标签 ↔ ::标签 定义行
import { Location, Position, Range } from "vscode-languageserver";
import { TextDocument } from "vscode-languageserver-textdocument";

export function definitionAt(doc: TextDocument, lineIdx: number, col: number): Location[] {
  const lines = doc.getText().split("\n");
  if (lineIdx < 0 || lineIdx >= lines.length) return [];
  const cursorLine = lines[lineIdx];

  // 1. 光标在 ::标签 定义行 → 返回所有引用行
  const labelDef = labelDefAt(cursorLine);
  if (labelDef) {
    const refs: Location[] = [];
    for (let i = 0; i < lines.length; i++) {
      const l = lines[i];
      let idx = l.indexOf("jump");
      while (idx !== -1) {
        const open = l.indexOf("(", idx);
        if (open === -1) break;
        const close = l.indexOf(")", open);
        if (close === -1) break;
        const arg = l.substring(open + 1, close).split(",", 2)[0].trim();
        if (arg === labelDef) refs.push(loc(i));
        idx = l.indexOf("jump", close);
      }
    }
    return refs;
  }

  // 2. 光标在 jump/jump2 引用上 → 标签定义行
  const target = jumpTargetAt(cursorLine, col);
  if (target) {
    for (let i = 0; i < lines.length; i++) {
      if (lines[i].trim().startsWith("::" + target)) return [loc(i)];
    }
  }
  return [];
}

function loc(line: number): Location {
  return Location.create("", Range.create(Position.create(line, 0), Position.create(line, 2147483647)));
}

function labelDefAt(line: string): string | null {
  const t = line.trim();
  if (!t.startsWith("::")) return null;
  const name = t.substring(2).trim();
  return name ? name.split(/\s+/)[0] : null;
}

function jumpTargetAt(line: string, column: number): string | null {
  let from = 0;
  while (true) {
    const idx = line.indexOf("jump", from);
    if (idx === -1) return null;
    const open = line.indexOf("(", idx);
    if (open !== -1 && open - idx <= 6) {
      const close = line.indexOf(")", open);
      if (close !== -1) {
        const arg = line.substring(open + 1, close).split(",", 2)[0].trim();
        if (arg && column >= open + 1 && column <= close) return arg;
      }
    }
    from = idx + 4;
  }
}
