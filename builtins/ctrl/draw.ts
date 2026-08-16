/**
 * draw — 固定填充指令。
 *
 * 语法:`draw(<类型>,<参数…>)`
 * 输出:`draw <pad(7, s)>`
 * 示例:`draw(clear)` → `draw clear 0 0 0 0 0 0`
 * 反编译:`draw ` → `draw(reduce(0, s))`(去掉尾部 0,逗号分隔)
 */

namespace Builtins {
  export namespace Ctrl {
    import H = Builtins.Helpers;

    export const draw: InstrDef = {
      key: 'draw',
      params: ['类型', '参数…'],
      compile: function (s, ctx) { return 'draw ' + H.padZero(7, ctx.parts(s)); },
      restore: function (s) { return 'draw(' + H.reduce('0', s) + ')'; }
    };
  }
}
