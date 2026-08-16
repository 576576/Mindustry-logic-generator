// 悬停:从 docs/instructions/*.md 预解析 "### 键" 段落
import * as fs from "node:fs";
import * as path from "node:path";

const DOCS = new Map<string, string>();

export function loadDocs(docsDir: string): void {
  DOCS.clear();
  for (const f of ["ctrl.md", "front.md", "dot.md", "dotCtrl.md", "domain.md", "operators.md"]) {
    const p = path.join(docsDir, f);
    if (!fs.existsSync(p)) continue;
    const lines = fs.readFileSync(p, "utf8").split("\n");
    let key: string | null = null;
    const buf: string[] = [];
    for (const l of lines) {
      if (l.startsWith("### ")) {
        if (key && buf.length) DOCS.set(key, buf.join("\n").trim());
        key = l.substring(4).trim();
        buf.length = 0;
      } else if (key) {
        buf.push(l);
      }
    }
    if (key && buf.length) DOCS.set(key, buf.join("\n").trim());
  }
}

export function lookupDoc(word: string): string | null {
  if (!word) return null;
  if (DOCS.has(word)) return DOCS.get(word)!;
  for (const [k, v] of DOCS) {
    if (k.startsWith(word) || word.startsWith(k)) return v;
  }
  return null;
}
