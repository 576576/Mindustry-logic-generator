/**
 * .orElse — 条件选择。
 *
 * 语法:`<value>.orElse(<后备>).when(<条件>)`;后备缺省 `0`
 * 链式键:main(后备), when(条件)
 * 条件判定(与 jump 相同,单 token 时不特判 always/never):
 * - 多 token:子编译;非空产物 → 条件取产物末行,结合 expr 修正
 * - 单 token:`notEqual <whenExpr> 0`;空:恒真
 * 输出:`select mid.<ref> <reverseCondition(condition)> <block> <target>`
 * 反编译:`select ` → `<结果>=<target>.orElse(<后备>).when(<条件>)`
 */

namespace Builtins {
  export namespace Dot {
    import H = Builtins.Helpers;
    import R = Builtins.Registry;

    const CHAIN: ChainKeyDef[] = [
      { key: 'main', def: '0', params: ['后备'] },
      { key: 'when', def: '', params: ['条件'] }
    ];

    export const orElse: InstrDef = {
      desc: '条件缺省(满足条件取后备值)',
      key: 'orElse',
      params: ['后备'],
      mcode: 'select',
      chain: CHAIN,
      compile: function (s, ctx) {
        const m = ctx.chain(s);
        const target = H.chainGet(m, 'main', R.chainDef(CHAIN, 'main'));
        const whenExpr = H.chainGet(m, 'when', R.chainDef(CHAIN, 'when'));
        const splitList = ctx.split(whenExpr);
        let condition: string;
        if (splitList.length > 1) {
          const bc = ctx.compileSub(whenExpr);
          if (bc.bash.length > 0) {
            ctx.setRef(bc.stat);
            condition = H.getCondition(bc.bash[bc.bash.length - 1]);
            if (condition !== 'always 0 0') {
              bc.bash = bc.bash.slice(0, bc.bash.length - 1);
            } else if (bc.expr !== '') {
              condition = 'notEqual ' + bc.expr + ' 0';
            }
            ctx.bashAll(bc.bash);
          } else {
            condition = 'always 0 0';
          }
        } else if (splitList.length === 1) {
          condition = 'notEqual ' + whenExpr + ' 0';
        } else {
          condition = 'always 0 0';
        }
        return 'select ' + ctx.mid() + ' ' + H.reverseCondition(condition) +
          ' ' + ctx.block() + ' ' + target;
      },
      restore: function (s) {
        const p = s.split(' ');
        const condition = [p[1], p[2], p[3]].join(' ');
        return p[0] + '=' + p[4] + '.orElse(' + p[5] + ')' +
          '.when(' + H.reduceCondition(H.reverseCondition(condition)) + ')';
      }
    };
  }
}
