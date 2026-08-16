/**
 * raw — 原生透传指令(规范见 docs/instructions/ctrl.md)
 * 无 restore:未知指令行由 decompile 管道还原为 raw("…")。
 * 注意:raw( 必须排在 draw( 之后(注册顺序即匹配顺序)。
 */
namespace Builtins {
  export namespace Ctrl {
    export const raw: InstrDef = {
      key: 'raw',
      compile: function (s) { return s; }
    };
  }
}
