/**
 * tag — 标签指令(规范见 docs/instructions/ctrl.md)
 * 无 restore:"::" 行由 decompile 管道原样保留。
 */
namespace Builtins {
  export namespace Ctrl {
    export const tag: InstrDef = {
      key: 'tag',
      compile: function (s) { return '::' + s; }
    };
  }
}
