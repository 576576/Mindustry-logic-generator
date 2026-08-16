/**
 * round — 一元运算。
 *
 * 语法:`round(x)`
 * 输出:`op round mid.<ref> <s> 0`
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=round(<参数>)`)。
 */

namespace Builtins {
  export namespace Front {
    export const round: InstrDef = {
      desc: '四舍五入',
      key: 'round',
      params: ['x'],
      compile: function (s, ctx) { return 'op round ' + ctx.mid() + ' ' + s + ' 0'; }
    };
  }
}
