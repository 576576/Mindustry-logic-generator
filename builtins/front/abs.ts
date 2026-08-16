/**
 * abs — 一元运算。
 *
 * 语法:`abs(x)`
 * 输出:`op abs mid.<ref> <s> 0`
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=abs(<参数>)`)。
 */

namespace Builtins {
  export namespace Front {
    export const abs: InstrDef = {
      key: 'abs',
      compile: function (s, ctx) { return 'op abs ' + ctx.mid() + ' ' + s + ' 0'; }
    };
  }
}
