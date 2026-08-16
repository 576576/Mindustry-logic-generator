/**
 * jump — 链式参数指令。
 *
 * 语法:`jump(<目标标签>).when(<条件>)`;when 缺省恒真
 * 链式键:main(目标标签,DEFAULT), when(条件,空)
 * 条件判定:
 * - when 多 token:子编译 whenExpr,产物非空时 condition 取末行(getCondition);
 *   非 `always 0 0` 弹出该行;`always 0 0` 且子表达式非空则 `notEqual <expr> 0`;产物行并入输出
 * - when 单 token:`always` → `always 0 0`;`never` → `notEqual 0 0`;其他 → `notEqual <whenExpr> 0`
 * - when 空:恒真
 * 输出:`jump <target> <condition>`
 * 反编译:`jump ` → `jump(<tag>)[.when(<cond>)]`(恒真省略 when)
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
