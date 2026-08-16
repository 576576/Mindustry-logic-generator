/**
 * ln — 自然对数(op 名 log)。
 *
 * 语法:`ln(x)`
 * 输出:`op log mid.<ref> <s> 0`
 * 反编译:op 行通用还原,log 按别名表还原为 ln(<参数>)。
 */

namespace Builtins {
  export namespace Front {
    export const ln: InstrDef = {
      key: 'ln',
      compile: function (s, ctx) { return 'op log ' + ctx.mid() + ' ' + s + ' 0'; }
    };
  }
}
