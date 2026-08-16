/**
 * lb — 以 2 为底对数(op 名 logn,尾参 2)。
 *
 * 语法:`lb(x)`
 * 输出:`op logn mid.<ref> <s> 2`
 * 反编译:op logn 且末参为 2 → `<结果>=lb(<参2>)`。
 */

namespace Builtins {
  export namespace Front {
    export const lb: InstrDef = {
      key: 'lb',
      compile: function (s, ctx) { return 'op logn ' + ctx.mid() + ' ' + s + ' 2'; }
    };
  }
}
