/**
 * uradar — 单位雷达(链式参数)。
 *
 * 语法:`uradar().target(<t>).sort(<s>).order(<o>)`
 * 链式键:target(缺省 enemy,any,any), sort(缺省 distance), order(缺省 1)
 * 输出:`uradar <pad(any, 3, target)> <sort> 0 <order> mid.<ref>`
 *   (target 按逗号切分填充到 3 项,缺省 any)
 * 反编译:`uradar ` → `<结果>=uradar()` + .target/.order/.sort 链(非缺省时)
 */

namespace Builtins {
  export namespace Front {
    import H = Builtins.Helpers;
    import D = Builtins.Domain;
    import R = Builtins.Registry;

    const CHAIN: ChainKeyDef[] = [
      { key: 'target', def: 'enemy,any,any' },
      { key: 'sort', def: 'distance' },
      { key: 'order', def: '1' }
    ];

    export const uradar: InstrDef = {
      key: 'uradar',
      mcode: 'uradar',
      chain: CHAIN,
      compile: function (s, ctx) {
        const m = ctx.chain(s);
        const target = H.pad('any', 3, ctx.parts(H.chainGet(m, 'target', R.chainDef(CHAIN, 'target'))));
        const sort = H.chainGet(m, 'sort', R.chainDef(CHAIN, 'sort'));
        const order = H.chainGet(m, 'order', R.chainDef(CHAIN, 'order'));
        return 'uradar ' + target + ' ' + sort + ' 0 ' + order + ' ' + ctx.mid();
      },
      restore: function (s) {
        const p = s.split(' ');
        const order = p[5];
        const sort = p[3];
        let result = p[6] + '=uradar()';
        const target = H.reduce('any', [p[0], p[1], p[2]].join(' '));
        if (target !== '' && target !== 'enemy') result += '.target(' + target + ')';
        if (order !== '1') result += '.order(' + order + ')';
        if (sort !== 'distance') result += '.sort(' + sort + ')';
        return result;
      }
    };
  }
}
