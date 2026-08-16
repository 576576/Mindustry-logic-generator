/**
 * ceil — 一元运算。
 *
 * 语法:`ceil(x)`
 * 输出:`op ceil mid.<ref> <s> 0`
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=ceil(<参数>)`)。
 */

namespace Builtins {
  export namespace Front {
    export const ceil: InstrDef = {
      desc: '向上取整',
      key: 'ceil',
      params: ['x'],
      compile: function (s, ctx) { return 'op ceil ' + ctx.mid() + ' ' + s + ' 0'; }
    };
  }
}
