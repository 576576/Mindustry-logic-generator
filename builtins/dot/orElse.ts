/**
 * .orElse — 条件选择(规范见 docs/instructions/dot.md)
 */
namespace Builtins {
  export namespace Dot {
    import H = Builtins.Helpers;
    import R = Builtins.Registry;

    const CHAIN: ChainKeyDef[] = [
      { key: 'main', def: '0' },
      { key: 'when', def: '' }
    ];

    export const orElse: InstrDef = {
      key: 'orElse',
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
