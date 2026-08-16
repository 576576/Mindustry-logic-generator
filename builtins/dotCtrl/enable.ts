/**
 * .enable — 控制指令;mcode 为共享指令字 `control`,按 enabled 分派。
 *
 * 语法:`<block>.enable(<0|1>)`
 * 输出:`control enabled <block> <pad(4, s)>`
 * 反编译:`control enabled ` → `<block>.enable(<0|1>)`
 */

namespace Builtins {
  export namespace DotCtrl {
    import H = Builtins.Helpers;

    export const enable: InstrDef = {
      key: 'enable',
      params: ['0|1'],
      mcode: 'control',
      mcodeSelect: ['enabled'],
      compile: function (s, ctx) { return 'control enabled ' + ctx.block() + ' ' + H.padZero(4, ctx.parts(s)); },
      restore: function (s) {
        const parts = H.splitInto(s, 3);
        const ps = (parts[2] == null ? '' : parts[2]).split(' ');
        return parts[1] + '.enable(' + (ps[0] == null ? '' : ps[0]) + ')';
      }
    };
  }
}
