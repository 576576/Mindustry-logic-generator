/**
 * printf — 多行展开指令(规范见 docs/instructions/ctrl.md)
 * 无 restore:多行(print+format)不折叠回 printf,由 decompile 逐行还原。
 */
namespace Builtins {
  export namespace Ctrl {
    export const printf: InstrDef = {
      key: 'printf',
      compile: function (s, ctx) {
        const w = ctx.parts(s);
        if (w.length < 2) return 'print ' + s;
        let out = 'print ' + w[0];
        for (let i = 1; i < w.length; i++) out += '\nformat ' + w[i];
        return out;
      }
    };
  }
}
