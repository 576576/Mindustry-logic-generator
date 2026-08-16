// prepare-data.mjs — 拷贝 LSP 数据到 server/data(builtins.js、docs/instructions、Cli.jar)
import * as fs from "node:fs";
import * as path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..", "..");
const dataDir = path.join(root, "vscode-mdtc", "server", "data");
fs.mkdirSync(dataDir, { recursive: true });
fs.mkdirSync(path.join(dataDir, "docs"), { recursive: true });

fs.copyFileSync(path.join(root, "builtins", "gen", "builtins.js"), path.join(dataDir, "builtins.js"));
for (const f of ["ctrl.md", "front.md", "dot.md", "dotCtrl.md", "domain.md", "operators.md", "README.md"]) {
  const src = path.join(root, "docs", "instructions", f);
  if (fs.existsSync(src)) fs.copyFileSync(src, path.join(dataDir, "docs", f));
}
const cliJar = fs.readdirSync(path.join(root, "build", "libs")).find(f => f.endsWith("-Cli.jar"));
if (cliJar) {
  fs.copyFileSync(path.join(root, "build", "libs", cliJar), path.join(dataDir, "mdtc-Cli.jar"));
} else {
  console.error("Cli.jar not found — run gradlew shadowJar first");
  process.exit(1);
}
console.log("server/data prepared");
