/**
 * uctrl — 固定填充指令(规范见 docs/instructions/ctrl.md)
 * mcode 为共享指令字 "ucontrol"(兜底:无 mcodeSelect 命中时)。
 */
namespace Builtins {
  export namespace Ctrl {
    import H = Builtins.Helpers;

    export const uctrl: InstrDef = {
      key: 'uctrl',
      mcode: 'ucontrol',
      compile: function (s, ctx) { return 'ucontrol ' + H.padZero(6, ctx.parts(s)); },
      restore: function (s) {
        const parts = H.splitInto(s, 2);
        return 'uctrl(' + H.reduce('0', parts[0] + ' ' + parts[1]) + ')';
      }
    };
  }
}
