/**
 * .ulocate — 链式定位指令(规范见 docs/instructions/dot.md)
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
