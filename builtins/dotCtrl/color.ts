/**
 * .color — 控制指令(规范见 docs/instructions/dot.md)
 * mcode 为共享指令字 "control",按 "color" 分派。
 */
namespace Builtins {
  export namespace DotCtrl {
    import H = Builtins.Helpers;

    export const color: InstrDef = {
      key: 'color',
      mcode: 'control',
      mcodeSelect: ['color'],
      compile: function (s, ctx) { return 'control color ' + ctx.block() + ' ' + H.padZero(4, ctx.parts(s)); },
      restore: function (s) {
        const parts = H.splitInto(s, 3);
        const ps = (parts[2] == null ? '' : parts[2]).split(' ');
        return parts[1] + '.color(' + (ps[0] == null ? '' : ps[0]) + ')';
      }
    };
  }
}
