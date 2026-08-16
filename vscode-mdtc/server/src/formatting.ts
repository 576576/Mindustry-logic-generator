// 格式化:写临时文件调用 CLI -fo,读回结果
import * as fs from "node:fs";
import * as os from "node:os";
import * as path from "node:path";
import { execFileSync } from "node:child_process";
import { TextDocument } from "vscode-languageserver-textdocument";
import { Position, Range, TextEdit } from "vscode-languageserver";

export function formatDocument(doc: TextDocument, cliJar: string): TextEdit[] {
  const text = doc.getText();
  const tmp = path.join(os.tmpdir(), `mdtc_fmt_${process.pid}_${Date.now()}.mdtc`);
  try {
    fs.writeFileSync(tmp, text, "utf8");
    execFileSync("java", ["-jar", cliJar, "-i", tmp, "-fo"],
      { encoding: "utf8", timeout: 30000, stdio: ["ignore", "pipe", "pipe"] });
    const formatted = fs.readFileSync(tmp, "utf8");
    if (!formatted || formatted === text) return [];
    const lines = text.split("\n");
    const last = Math.max(0, lines.length - 1);
    const lastChar = lines.length ? lines[lines.length - 1].length : 0;
    return [TextEdit.replace(Range.create(Position.create(0, 0), Position.create(last, lastChar)), formatted)];
  } finally {
    try { fs.unlinkSync(tmp); } catch { /* ignore */ }
  }
}
