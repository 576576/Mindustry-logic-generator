/**
 * sin — 三角函数(规范见 docs/instructions/front.md;输出无尾随 0)
 */
namespace Builtins {
  export namespace Front {
    export const sin: InstrDef = {
      key: 'sin',
      compile: function (s, ctx) { return 'op sin ' + ctx.mid() + ' ' + s; }
    };
  }
}
