/**
 * .ulocate — 链式定位指令。
 *
 * 语法:`<block>.ulocate(<type>).ore(<ore>).building(<bld>).enemy(<enemy>)`
 * 链式键:main(定位类型,缺省 ore), ore(缺省 0), building(缺省 core), enemy(缺省 0)
 * 行为:type 命中 Building 分类(buildingContains)时,building = type 且 type = building
 * 输出:`ulocate <type> <building> <enemy> <ore> <block>.x <block>.y <block>.f <block>`
 * 反编译:`ulocate ` → `<block>.ulocate(<type>)`(type 为 building 时用 building 参数;
 *   追加 .ore(<ore>)(仅当 type 为 ore)、.enemy(<enemy>)(非 0 时)
 */

namespace Builtins {
  export namespace DotCtrl {
    import H = Builtins.Helpers;
    import D = Builtins.Domain;
    import R = Builtins.Registry;

    const CHAIN: ChainKeyDef[] = [
      { key: 'main', def: 'ore' },
      { key: 'ore', def: '0' },
      { key: 'building', def: 'core' },
      { key: 'enemy', def: '0' }
    ];

    export const ulocate: InstrDef = {
      key: 'ulocate',
      chain: CHAIN,
      compile: function (s, ctx) {
        const m = ctx.chain(s);
        let type = H.chainGet(m, 'main', R.chainDef(CHAIN, 'main'));
        let bld = H.chainGet(m, 'building', R.chainDef(CHAIN, 'building'));
        const ore = H.chainGet(m, 'ore', R.chainDef(CHAIN, 'ore'));
        const en = H.chainGet(m, 'enemy', R.chainDef(CHAIN, 'enemy'));
        if (ctx.buildingContains(type)) { bld = type; type = D.LOCATE_TYPES[1]; }
        const b = ctx.block();
        return 'ulocate ' + type + ' ' + bld + ' ' + en + ' ' + ore + ' ' +
          b + '.x ' + b + '.y ' + b + '.f ' + b;
      },
      restore: function (s) {
        const p = s.split(' ');
        const locateType = p[0];
        const building = p[1];
        const enemy = p[2];
        const ore = p[3];
        const block = p[7];
        let result = block + '.ulocate(' + (locateType === 'building' ? building : locateType) + ')';
        if (locateType === 'ore') result += '.ore(' + ore + ')';
        if (enemy !== '0') result += '.enemy(' + enemy + ')';
        return result;
      }
    };
  }
}
