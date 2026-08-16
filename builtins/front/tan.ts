/**
 * tan — 三角函数(输出无尾随 0)。
 *
 * 语法:`tan(x)`
 * 输出:`op tan mid.<ref> <s>`
 * 反编译:op 行通用还原。
 */

namespace Builtins {
  export namespace Front {
    export const tan: InstrDef = {
      key: 'tan',
      params: ['x'],
      compile: function (s, ctx) { return 'op tan ' + ctx.mid() + ' ' + s; }
    };
  }
}
