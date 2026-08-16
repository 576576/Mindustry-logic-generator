/**
 * Front 大类注册(high 组先于 low 组处理)
 * 匹配符号由大类定义:裸指令名 + "("。
 */
namespace Builtins {
  export namespace Front {
    export const keyOf: Registry.CategorySpec['keyOf'] = function (name) { return name + '('; };
    export const highDefs: InstrDef[] = [
      not, abs, sign, floor, ceil, round, sqrt, rand, asin, acos, atan,
      ln, lg, lb,
      max, min, len, angle, angleDiff, noise, log,
      link, lookup, block, unit, item, liquid, team, pack, uradar
    ];
    export const lowDefs: InstrDef[] = [sin, cos, tan, radar];
    export const defs: InstrDef[] = highDefs.concat(lowDefs);
    export const high: Registry.CategorySpec = { keyOf: keyOf, defs: highDefs };
    export const low: Registry.CategorySpec = { keyOf: keyOf, defs: lowDefs };
    export const category: Registry.CategorySpec = { keyOf: keyOf, defs: defs };
  }
}
