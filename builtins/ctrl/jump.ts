/**
 * jump — 链式参数指令(规范见 docs/instructions/ctrl.md)
 */
namespace Builtins {
  export namespace Ctrl {
    import H = Builtins.Helpers;
    import R = Builtins.Registry;

    const CHAIN: ChainKeyDef[] = [
      { key: 'main', def: 'DEFAULT' },
      { key: 'when', def: '' }
    ];

    export const jump: InstrDef = {
      key: 'jump',
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
          const first = splitList[0];
          if (first === 'always') condition = 'always 0 0';
          else if (first === 'never') condition = 'notEqual 0 0';
          else condition = 'notEqual ' + whenExpr + ' 0';
        } else {
          condition = 'always 0 0';
        }
        return 'jump ' + target + ' ' + condition;
      },
      restore: function (s) {
        const alwaysConditions = ['0==0', 'always'];
        const parts = H.splitInto(s, 2);
        let condition = H.reduceCondition(parts[1]);
        if (alwaysConditions.indexOf(condition) !== -1) condition = '';
        else condition = '.when(' + condition + ')';
        return 'jump(' + parts[0] + ')' + condition;
      }
    };
  }
}
