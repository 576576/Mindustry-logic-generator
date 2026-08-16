/**
 * stop — 零参指令(规范见 docs/instructions/ctrl.md)
 * 无 restore:裸 "stop" 行由 decompile 管道还原为 "stop()"。
 */
namespace Builtins {
  export namespace Ctrl {
    export const stop: InstrDef = {
      key: 'stop',
      compile: function () { return 'stop'; }
    };
  }
}
