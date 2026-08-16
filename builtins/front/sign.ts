/**
 * sign — 一元运算。
 *
 * 语法:`sign(x)`
 * 输出:`op sign mid.<ref> <s> 0`
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=sign(<参数>)`)。
 */

namespace Builtins {
  export namespace Front {
    export const sign: InstrDef = {
      desc: '取符号(1/-1/0)',
      key: 'sign',
      params: ['x'],
      compile: function (s, ctx) { return 'op sign ' + ctx.mid() + ' ' + s + ' 0'; }
    };
  }
}
