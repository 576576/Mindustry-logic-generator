/**
 * rand — 一元运算。
 *
 * 语法:`rand(x)`
 * 输出:`op rand mid.<ref> <s> 0`
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=rand(<参数>)`)。
 */

namespace Builtins {
  export namespace Front {
    export const rand: InstrDef = {
      key: 'rand',
      compile: function (s, ctx) { return 'op rand ' + ctx.mid() + ' ' + s + ' 0'; }
    };
  }
}
