/**
 * sqrt — 一元运算。
 *
 * 语法:`sqrt(x)`
 * 输出:`op sqrt mid.<ref> <s> 0`
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=sqrt(<参数>)`)。
 */

namespace Builtins {
  export namespace Front {
    export const sqrt: InstrDef = {
      desc: '平方根',
      key: 'sqrt',
      params: ['x'],
      compile: function (s, ctx) { return 'op sqrt ' + ctx.mid() + ' ' + s + ' 0'; }
    };
  }
}
