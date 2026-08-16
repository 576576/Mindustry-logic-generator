/**
 * draw — 固定填充指令(规范见 docs/instructions/ctrl.md)
 */
namespace Builtins {
  export namespace Ctrl {
    import H = Builtins.Helpers;

    export const draw: InstrDef = {
      key: 'draw',
      compile: function (s, ctx) { return 'draw ' + H.padZero(7, ctx.parts(s)); },
      restore: function (s) { return 'draw(' + H.reduce('0', s) + ')'; }
    };
  }
}
