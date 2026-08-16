/**
 * .color — 控制指令;mcode 为共享指令字 `control`,按 color 分派。
 *
 * 语法:`<block>.color(<r>,<g>,<b>,<a>)`
 * 输出:`control color <block> <pad(4, s)>`
 * 反编译:`control color ` → `<block>.color(<r,g,b,a>)`
 */

namespace Builtins {
  export namespace DotCtrl {
    import H = Builtins.Helpers;

    export const color: InstrDef = {
      key: 'color',
      params: ['r', 'g', 'b', 'a'],
      mcode: 'control',
      mcodeSelect: ['color'],
      compile: function (s, ctx) { return 'control color ' + ctx.block() + ' ' + H.padZero(4, ctx.parts(s)); },
      restore: function (s) {
        const parts = H.splitInto(s, 3);
        const ps = (parts[2] == null ? '' : parts[2]).split(' ');
        return parts[1] + '.color(' + (ps[0] == null ? '' : ps[0]) + ')';
      }
    };
  }
}
