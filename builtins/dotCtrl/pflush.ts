/**
 * .pflush — 打印冲刷指令。
 *
 * 语法:`<block>.pflush()`
 * 输出:`printflush <block>`
 * 反编译:`printflush ` → `<block>.pflush()`
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
