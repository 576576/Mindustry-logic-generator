/**
 * ushoot — 链式参数指令(规范见 docs/instructions/ctrl.md)
 * mcode 为共享指令字 "ucontrol",按 target/targetp 分派。
 */
namespace Builtins {
  export namespace Ctrl {
    import H = Builtins.Helpers;
    import R = Builtins.Registry;

    const CHAIN: ChainKeyDef[] = [
      { key: 'main', def: '1' },
      { key: 'target', def: '@this' }
    ];

    export const ushoot: InstrDef = {
      key: 'ushoot',
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
