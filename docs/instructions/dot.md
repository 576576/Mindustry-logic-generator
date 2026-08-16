# DotCtrl 与 Dot 指令(编译端链式指令)

> [← 返回 README](../../README.md)

两类指令都以点链形式出现在表达式右侧:`<block>.<handler>(<args>)`。
`block` 为点链左侧的引用表达式(如 `reactor`、`link(1)`),由调用方
`convertDotCtrl` / `convertDot` 在调用处理器前解析并写入桥接对象。

> **链式调用检查**:链式键由各指令的 `InstrDef.chain` 声明(键名 + 缺省值,
> 单一默认值来源);链可以增长(`.ulocate(x).ore(y).enemy(z)…`),编译时对
> 未声明的链键输出警告(`chain warning: … unknown chain key "k"`),不影响输出。

> **具体指令规范(语法/参数/默认值/链式键/输出/反编译)已移入各指令
> 对应的 `.ts` 文件 JSDoc**,本页仅保留指令清单索引。

## DotCtrl(控制/写入类,10 个)

### .ctrl(
→ [builtins/dotCtrl/ctrl.ts](../../builtins/dotCtrl/ctrl.ts):`<block>.ctrl(<类型>,<参数…>)` → `control <w0|enabled> <block> <pad(4, w1)>`

### .enable(
→ [builtins/dotCtrl/enable.ts](../../builtins/dotCtrl/enable.ts):`<block>.enable(<0|1>)` → `control enabled <block> <pad(4, s)>`

### .config(
→ [builtins/dotCtrl/config.ts](../../builtins/dotCtrl/config.ts):`<block>.config(<值>)` → `control config <block> <pad(4, s)>`

### .color(
→ [builtins/dotCtrl/color.ts](../../builtins/dotCtrl/color.ts):`<block>.color(<r>,<g>,<b>,<a>)` → `control color <block> <pad(4, s)>`

### .shoot(
→ [builtins/dotCtrl/shoot.ts](../../builtins/dotCtrl/shoot.ts):`<block>.shoot(<shoot>).target(<设计目标>|<x>,<y>)` → `control shoot|shootp …`

### .ulocate(
→ [builtins/dotCtrl/ulocate.ts](../../builtins/dotCtrl/ulocate.ts):`<block>.ulocate(<type>).ore(<ore>).building(<bld>).enemy(<enemy>)` → `ulocate …`

### .unpack(
→ [builtins/dotCtrl/unpack.ts](../../builtins/dotCtrl/unpack.ts):`<block>.unpack(<r>,<g>,<b>,<a>)` → `unpackcolor <pad(4, s)> <block>`

### .pflush(
→ [builtins/dotCtrl/pflush.ts](../../builtins/dotCtrl/pflush.ts):`<block>.pflush()` → `printflush <block>`

### .dflush(
→ [builtins/dotCtrl/dflush.ts](../../builtins/dotCtrl/dflush.ts):`<block>.dflush()` → `drawflush <block>`

### .write(
→ [builtins/dotCtrl/write.ts](../../builtins/dotCtrl/write.ts):`<block>.write(<内容>,<单元号>)` → `write <w0|null> <block> <w1|0>`

## Dot(读取/选择类,3 个)

### .sensor(
→ [builtins/dot/sensor.ts](../../builtins/dot/sensor.ts):`<结果> = <block>.sensor(<属性>)` → `sensor mid.<ref> <block> <s>`

### .read(
→ [builtins/dot/read.ts](../../builtins/dot/read.ts):`<结果> = <block>.read(<单元号>)` → `read mid.<ref> <block> <s>`

### .orElse(
→ [builtins/dot/orElse.ts](../../builtins/dot/orElse.ts):`<value>.orElse(<后备>).when(<条件>)` → `select mid.<ref> …`
