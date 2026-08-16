// prepare-data.mjs — 拷贝 LSP 数据到 server/data(builtins.js、docs/instructions、Cli.jar)
// 数据源根:主仓库(含 builtins/ docs/ build/libs);独立仓库用环境变量 MDTC_DATA_SOURCE 指定
import * as fs from "node:fs";
import * as path from "node:path";
import { fileURLToPath } from "node:url";

const startDir = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");
// 两种仓库布局下,数据目标目录均为 scripts 上一级/server/data
const dataDir = path.join(startDir, "server", "data");

let sourceRoot = process.env.MDTC_DATA_SOURCE || null;
if (!sourceRoot) {
  let dir = startDir;
  for (let i = 0; i < 4; i++) {
    if (fs.existsSync(path.join(dir, "builtins", "gen", "builtins.js"))) { sourceRoot = dir; break; }
    dir = path.resolve(dir, "..");
  }
}
if (!sourceRoot) {
  console.error("prepare-data: 找不到数据源仓库根(请设置 MDTC_DATA_SOURCE 指向 mdtC 主仓库)");
  process.exit(1);
}

fs.mkdirSync(dataDir, { recursive: true });
fs.mkdirSync(path.join(dataDir, "docs"), { recursive: true });

fs.copyFileSync(path.join(sourceRoot, "builtins", "gen", "builtins.js"), path.join(dataDir, "builtins.js"));
for (const f of ["ctrl.md", "front.md", "dot.md", "dotCtrl.md", "domain.md", "operators.md", "README.md"]) {
  const src = path.join(sourceRoot, "docs", "instructions", f);
  if (fs.existsSync(src)) fs.copyFileSync(src, path.join(dataDir, "docs", f));
}
const cliJar = fs.readdirSync(path.join(sourceRoot, "build", "libs")).find(f => f.endsWith("-Cli.jar"));
if (cliJar) {
  fs.copyFileSync(path.join(sourceRoot, "build", "libs", cliJar), path.join(dataDir, "mdtc-Cli.jar"));
} else {
  console.error("Cli.jar not found — run gradlew shadowJar first");
  process.exit(1);
}
console.log("server/data prepared -> " + dataDir + " (source: " + sourceRoot + ")");