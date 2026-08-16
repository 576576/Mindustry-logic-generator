/**
 * floor — 一元运算。
 *
 * 语法:`floor(x)`
 * 输出:`op floor mid.<ref> <s> 0`
 * 反编译:op 行由注册表通用 opRestore 还原(如 `<结果>=floor(<参数>)`)。
 */

namespace Builtins {
  export namespace Front {
    export const floor: InstrDef = {
      desc: '向下取整',
      key: 'floor',
      params: ['x'],
      compile: function (s, ctx) { return 'op floor ' + ctx.mid() + ' ' + s + ' 0'; }
    };
  }
}
