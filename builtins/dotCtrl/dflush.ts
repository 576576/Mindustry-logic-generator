/**
 * .dflush — 绘制冲刷指令。
 *
 * 语法:`<block>.dflush()`
 * 输出:`drawflush <block>`
 * 反编译:`drawflush ` → `<block>.dflush()`
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
