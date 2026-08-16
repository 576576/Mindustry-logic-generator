/**
 * max — 二元运算(规范见 docs/instructions/front.md)
 */
namespace Builtins {
  export namespace Front {
    import H = Builtins.Helpers;

    export const max: InstrDef = {
      key: 'max',
      compile: function (s, ctx) {
        const w = ctx.parts(s);
        return 'op max ' + ctx.mid() + ' ' + H.getOr(w, 0, 'null') + ' ' + H.getOr(w, 1, 'null');
      }
    };
  }
}
