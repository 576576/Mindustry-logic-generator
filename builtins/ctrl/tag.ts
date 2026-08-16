/**
 * tag — 标签指令;无 restore(`::` 行由 decompile 管道原样保留)。
 *
 * 语法:`tag(<标签名>)`
 * 输出:`::<s>`(标签行;由 convertLink 解析)
 */

namespace Builtins {
  export namespace Ctrl {
    export const tag: InstrDef = {
      key: 'tag',
      compile: function (s) { return '::' + s; }
    };
  }
}
