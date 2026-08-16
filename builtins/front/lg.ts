/**
 * lg — 常用对数(规范见 docs/instructions/front.md,op 名 log10)
 */
namespace Builtins {
  export namespace Front {
    export const lg: InstrDef = {
      key: 'lg',
      compile: function (s, ctx) { return 'op log10 ' + ctx.mid() + ' ' + s + ' 0'; }
    };
  }
}
