/**
 * ushoot — 链式参数指令;mcode 为共享指令字 `ucontrol`,按 target/targetp 分派。
 *
 * 语法:`ushoot(<shoot>).target(<目标>|<x>,<y>)`;shoot 缺省 `1`,target 缺省 `@this`
 * 链式键:main(射击开关), target(目标;双参为坐标,单参为目标单位)
 * 输出:目标含逗号时 `ucontrol target <pad(5, tgt 逗号替换为空格, shoot)>`,
 *   否则 `ucontrol targetp <pad(5, tgt, shoot)>`
 * 示例:`ushoot(1).target(114,514)` → `ucontrol target 114 514 1 0 0`
 * 反编译:`ucontrol ` target/targetp → `ushoot(<shoot>)` + `.target(...)`
 */

namespace Builtins {
  export namespace Ctrl {
    import H = Builtins.Helpers;
    import R = Builtins.Registry;

    const CHAIN: ChainKeyDef[] = [
      { key: 'main', def: '1', params: ['shoot'] },
      { key: 'target', def: '@this', params: ['目标'] }
    ];

    export const ushoot: InstrDef = {
      desc: '射击控制(开火/停火)',
      key: 'ushoot',
      params: ['shoot'],
      mcode: 'ucontrol',
      mcodeSelect: ['target', 'targetp'],
      chain: CHAIN,
      compile: function (s, ctx) {
        const m = ctx.chain(s);
        const shoot = H.chainGet(m, 'main', R.chainDef(CHAIN, 'main'));
        const tgt = H.chainGet(m, 'target', R.chainDef(CHAIN, 'target'));
        const kind = tgt.indexOf(',') !== -1 ? 'target' : 'targetp';
        return 'ucontrol ' + kind + ' ' + H.padZero(5, [tgt.replace(/,/g, ' '), shoot]);
      },
      restore: function (s) {
        const parts = H.splitInto(s, 2);
        const ps = parts[1].split(' ');
        let target: string;
        let target2 = '';
        if (parts[0] === 'targetp') {
          target = ps[1] == null ? '' : ps[1];
          if (target === '1') target = '';
          target2 = '.target(' + (ps[0] == null ? '' : ps[0]) + ')';
        } else {
          target = ps[2] == null ? '' : ps[2];
          if (target === '1') target = '';
          target2 = '.target(' + [ps[0] == null ? '' : ps[0], ps[1] == null ? '' : ps[1]].join(',') + ')';
        }
        return 'ushoot(' + target + ')' + target2;
      }
    };
  }
}
