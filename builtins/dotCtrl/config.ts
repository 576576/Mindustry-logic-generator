/**
 * .config — 控制指令;mcode 为共享指令字 `control`,按 config 分派。
 *
 * 语法:`<block>.config(<值>)`
 * 输出:`control config <block> <pad(4, s)>`
 * 反编译:`control config ` → `<block>.config(<值>)`
 */

namespace Builtins {
  export namespace DotCtrl {
    import H = Builtins.Helpers;

    export const config: InstrDef = {
      desc: '设置方块配置',
      key: 'config',
      params: ['值'],
      mcode: 'control',
      mcodeSelect: ['config'],
      compile: function (s, ctx) { return 'control config ' + ctx.block() + ' ' + H.padZero(4, ctx.parts(s)); },
      restore: function (s) {
        const parts = H.splitInto(s, 3);
        const ps = (parts[2] == null ? '' : parts[2]).split(' ');
        return parts[1] + '.config(' + (ps[0] == null ? '' : ps[0]) + ')';
      }
    };
  }
}
