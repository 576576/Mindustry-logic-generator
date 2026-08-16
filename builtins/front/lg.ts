/**
 * lg — 常用对数(op 名 log10)。
 *
 * 语法:`lg(x)`
 * 输出:`op log10 mid.<ref> <s> 0`
 * 反编译:op 行通用还原,log10 按别名表还原为 lg(<参数>)。
 */

namespace Builtins {
  export namespace Front {
    export const lg: InstrDef = {
      key: 'lg',
      params: ['x'],
      compile: function (s, ctx) { return 'op log10 ' + ctx.mid() + ' ' + s + ' 0'; }
    };
  }
}
