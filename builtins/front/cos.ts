/**
 * cos — 三角函数(输出无尾随 0)。
 *
 * 语法:`cos(x)`
 * 输出:`op cos mid.<ref> <s>`
 * 反编译:op 行通用还原。
 */

namespace Builtins {
  export namespace Front {
    export const cos: InstrDef = {
      key: 'cos',
      compile: function (s, ctx) { return 'op cos ' + ctx.mid() + ' ' + s; }
    };
  }
}
