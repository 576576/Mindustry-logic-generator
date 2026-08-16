# DotCtrl 与 Dot 指令(编译端链式指令,共 13 个)

> [← 返回 README](../../README.md)

两类指令都以点链形式出现在表达式右侧:`<block>.<handler>(<args>)`。
`block` 为点链左侧的引用表达式(如 `reactor`、`link(1)`),由调用方
`convertDotCtrl` / `convertDot` 在调用处理器前解析并写入桥接对象。

> **链式调用检查**:链式键由各指令的 `InstrDef.chain` 声明(键名 + 缺省值,
> 单一默认值来源);链可以增长(`.ulocate(x).ore(y).enemy(z)…`),编译时对
> 未声明的链键输出警告(`chain warning: … unknown chain key "k"`),不影响输出。

## DotCtrl(控制/写入类,10 个)

### .ctrl(
- 语法:`<block>.ctrl(<类型>,<参数…>)`
- 输出:`control <w0|enabled> <block> <pad(4, w1)>`
- 说明:w0 为第一个参数(缺省 `enabled`),w1 为后续参数(缺省空串,
  仍按 4 项填充)。
- 反编译:见 `control `(未知类型还原为 `.ctrl`)

### .enable(
- 语法:`<block>.enable(<0|1>)`
- 输出:`control enabled <block> <pad(4, s)>`
- 反编译:enabled → `.enable`

### .config(
- 语法:`<block>.config(<值>)`
- 输出:`control config <block> <pad(4, s)>`

### .color(
- 语法:`<block>.color(<r>,<g>,<b>,<a>)`
- 输出:`control color <block> <pad(4, s)>`

### .shoot(
- 语法:`<block>.shoot(<shoot>).target(<设计目标>|<x>,<y>)`;shoot 缺省 `1`,
  target 缺省 `@this`
- 链式键:main(射击开关), target(设计目标或坐标)
- 说明:括号内**只有射击开关**;x/y 坐标或设计目标经 `.target(...)` 传递,
  由 target 的**参数量**区分——双参为 xy 坐标(shoot),单参为设计目标(shootp)
- 输出:`control shoot <block> <pad(4, tgt 逗号替换为空格, shoot)>`(坐标),
  或 `control shootp <block> <pad(4, tgt, shoot)>`(设计目标)
- 示例:`turret.shoot(1).target(5,6)` → `control shoot turret 5 6 1 0`
- 反编译:见 `control `(shootp/shoot 分支)

### .ulocate(
- 语法:`<block>.ulocate(<type>).ore(<ore>).building(<bld>).enemy(<enemy>)`
- 链式键:main(定位类型), ore(缺省 `0`), building(缺省 `core`),
  enemy(缺省 `0`)
- 行为:若 `type` 命中 Building 分类(`buildingContains`),则
  `building = type` 且 `type = building`。
- 输出:`ulocate <type> <building> <enemy> <ore> <block>.x <block>.y <block>.f <block>`
- 反编译:见 `ulocate `

### .unpack(
- 语法:`<block>.unpack(<r>,<g>,<b>,<a>)`
- 输出:`unpackcolor <pad(4, s)> <block>`
- 反编译:见 `unpackcolor `

### .pflush(
- 语法:`<block>.pflush()`
- 输出:`printflush <block>`

### .dflush(
- 语法:`<block>.dflush()`
- 输出:`drawflush <block>`

### .write(
- 语法:`<block>.write(<内容>,<单元号>)`
- 输出:`write <w0|null> <block> <w1|0>`

## Dot(读取/选择类,3 个)

### .sensor(
- 语法:`<结果> = <block>.sensor(<属性>)`(sensor 为表达式,结果变量在等号左侧)
- 输出:`sensor mid.<ref> <block> <s>`;调用方随后把 `mid.<ref>`
  代入表达式并递增 ref。
- 示例:`heat = reactor.sensor(@heat)`
- 反编译:见 `sensor `(还原为 `<结果>=<block>.sensor(<属性>)`)

### .read(
- 语法:`<结果> = <block>.read(<单元号>)`(read 为表达式,结果变量在等号左侧)
- 输出:`read mid.<ref> <block> <s>`

### .orElse(
- 语法:`<value>.orElse(<后备>).when(<条件>)`;后备缺省 `0`
- 链式键:main(后备), when(条件)
- 条件判定(与 jump 相同逻辑,单 token 时不特判 always/never):
  - 多 token:子编译;非空产物 → 条件取产物末行,结合 `expr` 修正;
  - 单 token:`notEqual <whenExpr> 0`;空:恒真。
- 输出:`select mid.<ref> <reverseCondition(condition)> <block> <target>`
- 反编译:见 `select `(还原为 `<结果>=<target>.orElse(<后备>).when(<条件>)`)
