/**
 * lb — 以 2 为底对数(规范见 docs/instructions/front.md,op 名 logn)
 */
namespace Builtins {
  export namespace Front {
    export const lb: InstrDef = {
      key: 'lb',
      compile: function (s, ctx) { return 'op logn ' + ctx.mid() + ' ' + s + ' 2'; }
    };
  }
}
