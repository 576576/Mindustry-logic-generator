/**
 * .dflush — 绘制冲刷指令(规范见 docs/instructions/dot.md)
 */
namespace Builtins {
  export namespace DotCtrl {
    export const dflush: InstrDef = {
      key: 'dflush',
      mcode: 'drawflush',
      compile: function (_s, ctx) { return 'drawflush ' + ctx.block(); },
      restore: function (s) { return s + '.dflush()'; }
    };
  }
}
