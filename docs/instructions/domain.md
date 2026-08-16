# 领域数据表(Domain)

> [← 返回 README](../../README.md)

领域数据描述各指令所引用的**游戏内容分类**与**默认值常量**。
静态目录由 `builtins/domain.ts` 提供;模组模式下 `BuiltinDomain.init()`
会把游戏实际内容名合并进 `Building` / `Lookup` 集合(仅影响 `contains` 判定)。

## 建筑分类(ulocate 可定位类型)— Building

| 枚举 | id(默认值加粗) |
|------|------|
| CORE | **core** |
| STORAGE | storage |
| GENERATOR | generator |
| TURRET | turret |
| FACTORY | factory |
| REPAIR | repair |
| BATTERY | battery |
| REACTOR | reactor |
| DRILL | drill |
| SHIELD | shield |

- 第 0 项 `core` 是 `.ulocate()` 的默认 building。
- `contains(name)`:模组模式下额外包含从游戏 `Vars.content` 推断的方块名。

## 定位模式 — Locate

| 枚举 | id(默认值加粗) |
|------|------|
| ORE | **ore** |
| BUILDING | building |

- `.ulocate()` 默认定位模式为 `ore`;当 <type> 命中 Building 分类时
  自动改写为 `building` 并把该值作为 building 参数。

## 控制类型 — Control

| 枚举 | id |
|------|------|
| ENABLED | enabled |

- `.ctrl()` 默认控制类型为 `enabled`。

## 雷达排序 — RadarSort

| 枚举 | id |
|------|------|
| DISTANCE | distance |

- `radar` / `uradar` 默认排序为 `distance`。

## lookup 内容类型 — Lookup

| 枚举 | id(默认值加粗) |
|------|------|
| BLOCK | **block** |
| UNIT | unit |
| ITEM | item |
| LIQUID | liquid |
| TEAM | team |

- `lookup()` 默认内容类型为 `block`。
- `contains(name)`:模组模式下额外包含游戏 `Vars.content` 中的方块/单位/物品/液体名。

## 链式键 — Chain

链式参数的键名(与顺序无关,均可省略):

| 键 | 用途 | 典型默认 |
|----|------|----------|
| main | 首部主参数(如目标标签/雷达主体) | 随指令 |
| target | 目标(如 `ushoot` 的目标坐标) | @this 等 |
| when | 条件表达式(如 `jump` 的跳转条件) | 空 |
| order | 排序方向(asc/desc,按 0/1 字形输出) | 1 |
| sort | 排序依据(如 distance) | distance |
| ore | 矿石类型 | 0 |
| building | 建筑类型 | core |
| enemy | 是否敌方 | 0 |

## 默认值常量

| 常量 | 值 | 说明 |
|------|-----|------|
| VAL_0 | `0` | 数值 0 |
| VAL_1 | `1` | 数值 1 |
| VAL_AT | `@this` | 自身 |
| VAL_NUL | `null` | 空值 |
| JUMP_DEFAULT | `DEFAULT` | `jump()` 缺省目标标签 |
| RADAR_TARGET | `enemy,any,any` | `radar`/`uradar` 缺省目标三元组 |
