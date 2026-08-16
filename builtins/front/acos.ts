/**
 * acos — 一元运算。
 *
 * 语法:`acos(x)`
 * 输出:`op acos mid.<ref> <s> 0`
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=acos(<参数>)`)。
 */

namespace Builtins {
  export namespace Front {
    export const acos: InstrDef = {
      key: 'acos',
      params: ['x'],
      compile: function (s, ctx) { return 'op acos ' + ctx.mid() + ' ' + s + ' 0'; }
    };
  }
}
