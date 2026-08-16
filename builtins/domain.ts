/**
 * 领域数据(规范见 docs/instructions/domain.md)。
 * 静态目录;模组运行期由 BuiltinDomain.init() 合并 Vars.content 游戏内容名。
 */
namespace Builtins {
  export namespace Domain {
    /** ulocate 可定位建筑类型(第 0 项为默认) */
    export const BUILDING_TYPES: string[] = [
      'core', 'storage', 'generator', 'turret', 'factory',
      'repair', 'battery', 'reactor', 'drill', 'shield'
    ];
    /** 定位模式(第 0 项为默认) */
    export const LOCATE_TYPES: string[] = ['ore', 'building'];
    /** lookup 内容类型(第 0 项为默认) */
    export const LOOKUP_TYPES: string[] = ['block', 'unit', 'item', 'liquid', 'team'];
    /** 链式参数键 */
    export const CHAIN_KEYS: string[] = ['main', 'target', 'when', 'order', 'sort', 'ore', 'building', 'enemy'];
    /** .ctrl() 默认控制类型 */
    export const CONTROL_DEFAULT = 'enabled';
    /** radar/uradar 默认排序 */
    export const RADAR_SORT_DEFAULT = 'distance';
    /** 默认值常量 */
    export const VAL_0 = '0';
    export const VAL_1 = '1';
    export const VAL_AT = '@this';
    export const VAL_NUL = 'null';
    export const JUMP_DEFAULT = 'DEFAULT';
    export const RADAR_TARGET = 'enemy,any,any';
  }
}
