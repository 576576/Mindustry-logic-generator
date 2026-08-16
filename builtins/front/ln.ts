/**
 * ln — 自然对数(规范见 docs/instructions/front.md,op 名 log)
 */
namespace Builtins {
  export namespace Front {
    export const ln: InstrDef = {
      key: 'ln',
      compile: function (s, ctx) { return 'op log ' + ctx.mid() + ' ' + s + ' 0'; }
    };
  }
}
