/**
 * .ctrl — 控制指令兜底(规范见 docs/instructions/dot.md)
 * mcode 为共享指令字 "control",无 mcodeSelect = 兜底(未知控制类型)。
 */
namespace Builtins {
  export namespace DotCtrl {
    import H = Builtins.Helpers;
    import D = Builtins.Domain;

    export const ctrl: InstrDef = {
      key: 'ctrl',
      mcode: 'control',
      compile: function (s, ctx) {
        const w = ctx.parts(s);
        const type = H.getOr(w, 0, D.CONTROL_DEFAULT);
        return 'control' + type + ctx.block() + ' ' + H.padZero(4, [H.getOr(w, 1, '')]);
      },
      restore: function (s) {
        const parts = H.splitInto(s, 3);
        return parts[1] + '.ctrl(' + H.reduce('0', parts[0] + ' ' + (parts[2] == null ? '' : parts[2])) + ')';
      }
    };
  }
}
