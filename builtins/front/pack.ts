/**
 * pack — 颜色打包(规范见 docs/instructions/front.md)
 */
namespace Builtins {
  export namespace Front {
    import H = Builtins.Helpers;

    export const pack: InstrDef = {
      key: 'pack',
      mcode: 'packcolor',
      compile: function (s, ctx) { return 'packcolor ' + ctx.mid() + ' ' + H.padZero(4, ctx.parts(s)); },
      restore: function (s) {
        const idx = s.indexOf(' ');
        const var0 = idx === -1 ? s : s.slice(0, idx);
        const rest = idx === -1 ? '' : s.slice(idx + 1);
        return var0 + '=pack(' + rest.replace(/ /g, ',') + ')';
      }
    };
  }
}
