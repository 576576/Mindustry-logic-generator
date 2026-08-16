/**
 * sin — 三角函数(输出无尾随 0)。
 *
 * 语法:`sin(x)`
 * 输出:`op sin mid.<ref> <s>`
 * 反编译:op 行通用还原(如 `<结果>=sin(<参数>)`)。
 */

namespace Builtins {
  export namespace Front {
    export const sin: InstrDef = {
      key: 'sin',
      compile: function (s, ctx) { return 'op sin ' + ctx.mid() + ' ' + s; }
    };
  }
}
