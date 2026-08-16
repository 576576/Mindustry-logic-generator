/**
 * .unpack — 取色指令。
 *
 * 语法:`<block>.unpack(<r>,<g>,<b>,<a>)`
 * 输出:`unpackcolor <pad(4, s)> <block>`
 * 反编译:`unpackcolor ` → `<block>.unpack(reduce(0, 前4参))`
 */

namespace Builtins {
  export namespace DotCtrl {
    import H = Builtins.Helpers;

    export const unpack: InstrDef = {
      key: 'unpack',
      params: ['r', 'g', 'b', 'a'],
      mcode: 'unpackcolor',
      compile: function (s, ctx) { return 'unpackcolor ' + H.padZero(4, ctx.parts(s)) + ' ' + ctx.block(); },
      restore: function (s) {
        const p = s.split(' ');
        return p[4] + '.unpack(' + H.reduceArr('0', 4, p.slice(0, 4)) + ')';
      }
    };
  }
}
