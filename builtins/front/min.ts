/**
 * min — 二元运算(规范见 docs/instructions/front.md)
 */
namespace Builtins {
  export namespace Front {
    import H = Builtins.Helpers;

    export const min: InstrDef = {
      key: 'min',
      compile: function (s, ctx) {
        const w = ctx.parts(s);
        return 'op min ' + ctx.mid() + ' ' + H.getOr(w, 0, 'null') + ' ' + H.getOr(w, 1, 'null');
      }
    };
  }
}
