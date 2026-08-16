/**
 * cos — 三角函数(规范见 docs/instructions/front.md;输出无尾随 0)
 */
namespace Builtins {
  export namespace Front {
    export const cos: InstrDef = {
      key: 'cos',
      compile: function (s, ctx) { return 'op cos ' + ctx.mid() + ' ' + s; }
    };
  }
}
