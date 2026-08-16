/**
 * end — 零参指令(规范见 docs/instructions/ctrl.md)
 * 无 restore:裸 "end" 行由 decompile 管道还原为 "end()"。
 */
namespace Builtins {
  export namespace Ctrl {
    export const end: InstrDef = {
      key: 'end',
      compile: function () { return 'end'; }
    };
  }
}
