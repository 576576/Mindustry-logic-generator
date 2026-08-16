/**
 * not — 一元运算。
 *
 * 语法:`not(x)`
 * 输出:`op not mid.<ref> <s> 0`
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=not(<参数>)`)。
 */

namespace Builtins {
  export namespace Front {
    export const not: InstrDef = {
      desc: '逻辑非',
      key: 'not',
      params: ['x'],
      compile: function (s, ctx) { return 'op not ' + ctx.mid() + ' ' + s + ' 0'; }
    };
  }
}
