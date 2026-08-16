/**
 * log — 任意底对数(规范见 docs/instructions/front.md;两参交换)
 */
namespace Builtins {
  export namespace Front {
    import H = Builtins.Helpers;

    export const log: InstrDef = {
      key: 'log',
      compile: function (s, ctx) {
        const w = ctx.parts(s);
        return 'op logn ' + ctx.mid() + ' ' + H.getOr(w, 1, 'null') + ' ' + H.getOr(w, 0, 'null');
      }
    };
  }
}
