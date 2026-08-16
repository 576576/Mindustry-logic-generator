/**
 * .shoot — 链式参数指令(规范见 docs/instructions/dot.md)
 * mcode 为共享指令字 "control",按 shoot/shootp 分派。
 */
namespace Builtins {
  export namespace DotCtrl {
    import H = Builtins.Helpers;
    import R = Builtins.Registry;

    const CHAIN: ChainKeyDef[] = [
      { key: 'main', def: '1' },
      { key: 'target', def: '@this' }
    ];

    export const shoot: InstrDef = {
      key: 'shoot',
      mcode: 'control',
      mcodeSelect: ['shoot', 'shootp'],
      chain: CHAIN,
      compile: function (s, ctx) {
        const m = ctx.chain(s);
        const shoot = H.chainGet(m, 'main', R.chainDef(CHAIN, 'main'));
        const tgt = H.chainGet(m, 'target', R.chainDef(CHAIN, 'target'));
        const kind = tgt.indexOf(',') !== -1 ? 'shoot' : 'shootp';
        return 'control ' + kind + ' ' + ctx.block() + ' ' + H.padZero(4, [tgt.replace(/,/g, ' '), shoot]);
      },
      restore: function (s) {
        const parts = H.splitInto(s, 3);
        const ps = (parts[2] == null ? '' : parts[2]).split(' ');
        let target: string;
        let target2 = '';
        if (parts[0] === 'shoot') {
          target = ps[2] == null ? '' : ps[2];
          if (target === '1') target = '';
          target2 = '.target(' + [ps[0] == null ? '' : ps[0], ps[1] == null ? '' : ps[1]].join(',') + ')';
        } else {
          target = ps[1] == null ? '' : ps[1];
          if (target === '1') target = '';
          target2 = '.target(' + (ps[0] == null ? '' : ps[0]) + ')';
        }
        return parts[1] + '.shoot(' + target + ')' + target2;
      }
    };
  }
}
