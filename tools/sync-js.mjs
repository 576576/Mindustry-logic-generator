// 将 builtins/*.ts 编译为 builtins/gen/builtins.js(单文件 bundle,Rhino 运行期加载)。
// 生命周期见 docs/instructions/README.md;由 Gradle(syncBuiltinJs)或手工调用。
import { execFileSync } from 'node:child_process';
import { existsSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const localTsc = path.join(root, 'node_modules', 'typescript', 'bin', 'tsc');

let cmd;
let args;
if (existsSync(localTsc)) {
  cmd = process.execPath;
  args = [localTsc, '-p', path.join(root, 'builtins')];
} else {
  cmd = 'npx';
  args = ['-y', '-p', 'typescript', 'tsc', '-p', path.join(root, 'builtins')];
}
execFileSync(cmd, args, { stdio: 'inherit', cwd: root });
console.log('OK: builtins/gen/builtins.js generated.');
