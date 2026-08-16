/**
 * .unpack — 取色指令(规范见 docs/instructions/dot.md)
 */
namespace Builtins {
  export namespace DotCtrl {
    import H = Builtins.Helpers;

    export const unpack: InstrDef = {
      key: 'unpack',
      mcode: 'unpackcolor',
      compile: function (s, ctx) { return 'unpackcolor ' + H.padZero(4, ctx.parts(s)) + ' ' + ctx.block(); },
      restore: function (s) {
        const p = s.split(' ');
        return p[4] + '.unpack(' + H.reduceArr('0', 4, p.slice(0, 4)) + ')';
      }
    };
  }
}
