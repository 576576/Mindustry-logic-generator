/**
 * .pflush — 打印冲刷指令(规范见 docs/instructions/dot.md)
 */
namespace Builtins {
  export namespace DotCtrl {
    export const pflush: InstrDef = {
      key: 'pflush',
      mcode: 'printflush',
      compile: function (_s, ctx) { return 'printflush ' + ctx.block(); },
      restore: function (s) { return s + '.pflush()'; }
    };
  }
}
