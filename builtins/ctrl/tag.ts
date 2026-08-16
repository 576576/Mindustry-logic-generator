/**
 * tag — 标签指令;无 restore(`::` 行由 decompile 管道原样保留)。
 *
 * 语法:`tag(<标签名>)`
 * 输出:`::<s>`(标签行;由 convertLink 解析)
 */

namespace Builtins {
  export namespace Ctrl {
    export const tag: InstrDef = {
      desc: '定义跳转标签',
      key: 'tag',
      params: ['标签名'],
      compile: function (s) { return '::' + s; }
    };
  }
}
