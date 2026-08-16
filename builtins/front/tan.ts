/**
 * tan — 三角函数(规范见 docs/instructions/front.md;输出无尾随 0)
 */
namespace Builtins {
  export namespace Front {
    export const tan: InstrDef = {
      key: 'tan',
      compile: function (s, ctx) { return 'op tan ' + ctx.mid() + ' ' + s; }
    };
  }
}
