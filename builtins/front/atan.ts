/**
 * atan — 一元运算。
 *
 * 语法:`atan(x)`
 * 输出:`op atan mid.<ref> <s> 0`
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=atan(<参数>)`)。
 */

namespace Builtins {
  export namespace Front {
    export const atan: InstrDef = {
      key: 'atan',
      compile: function (s, ctx) { return 'op atan ' + ctx.mid() + ' ' + s + ' 0'; }
    };
  }
}
