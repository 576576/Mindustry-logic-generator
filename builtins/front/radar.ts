/**
 * radar — 方块雷达(链式参数)。
 *
 * 语法:`radar().target(<t>).sort(<s>).main(<敌方目标>).order(<o>)`
 * 链式键:target(缺省 enemy,any,any), sort(缺省 distance), main(缺省 @this), order(缺省 1)
 * 输出:`radar <pad(any, 3, target)> <sort> <main> <order> mid.<ref>`
 * 反编译:`radar ` → `<结果>=uradar(<主体>)` + .target/.order/.sort 链(@this 主体省略)
 */

namespace Builtins {
  export namespace Front {
    import H = Builtins.Helpers;
    import D = Builtins.Domain;
    import R = Builtins.Registry;

    const CHAIN: ChainKeyDef[] = [
      { key: 'target', def: 'enemy,any,any', params: ['t'] },
      { key: 'sort', def: 'distance', params: ['s'] },
      { key: 'main', def: '@this', params: ['敌方目标'] },
      { key: 'order', def: '1', params: ['o'] }
    ];

    export const radar: InstrDef = {
      key: 'radar',
      desc: '方块雷达(链式)',
      mcode: 'radar',
      chain: CHAIN,
      compile: function (s, ctx) {
        const m = ctx.chain(s);
        const target = H.pad('any', 3, ctx.parts(H.chainGet(m, 'target', R.chainDef(CHAIN, 'target'))));
        const sort = H.chainGet(m, 'sort', R.chainDef(CHAIN, 'sort'));
        const main = H.chainGet(m, 'main', R.chainDef(CHAIN, 'main'));
        const order = H.chainGet(m, 'order', R.chainDef(CHAIN, 'order'));
        return 'radar ' + target + ' ' + sort + ' ' + main + ' ' + order + ' ' + ctx.mid();
      },
      restore: function (s) {
        const p = s.split(' ');
        const order = p[5];
        const sort = p[3];
        const block = p[4];
        let result = p[6] + '=uradar(' + (block === '@this' ? '' : block) + ')';
        const target = H.reduce('any', [p[0], p[1], p[2]].join(' '));
        if (target !== '' && target !== 'enemy') result += '.target(' + target + ')';
        if (order !== '1') result += '.order(' + order + ')';
        if (sort !== 'distance') result += '.sort(' + sort + ')';
        return result;
      }
    };
  }
}
