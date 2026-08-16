/**
 * .shoot — 链式参数指令;mcode 为共享指令字 `control`,按 shoot/shootp 分派。
 *
 * 语法:`<block>.shoot(<shoot>).target(<设计目标>|<x>,<y>)`;shoot 缺省 `1`,target 缺省 `@this`
 * 链式键:main(射击开关), target(设计目标或坐标)
 * 说明:括号内只有射击开关;x/y 坐标或设计目标经 .target(...) 传递,
 *   由 target 参数量区分——双参为 xy 坐标(shoot),单参为设计目标(shootp)
 * 输出:`control shoot <block> <pad(4, tgt 逗号替换为空格, shoot)>`(坐标)
 *   或 `control shootp <block> <pad(4, tgt, shoot)>`(设计目标)
 * 示例:`turret.shoot(1).target(5,6)` → `control shoot turret 5 6 1 0`
 * 反编译:`control ` shoot/shootp → `<block>.shoot(<shoot>)` + `.target(...)`
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
