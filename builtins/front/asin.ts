/**
 * asin — 一元运算。
 *
 * 语法:`asin(x)`
 * 输出:`op asin mid.<ref> <s> 0`
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=asin(<参数>)`)。
 */

namespace Builtins {
  export namespace Front {
    export const asin: InstrDef = {
      desc: '反正弦',
      key: 'asin',
      params: ['x'],
      compile: function (s, ctx) { return 'op asin ' + ctx.mid() + ' ' + s + ' 0'; }
    };
  }
}
