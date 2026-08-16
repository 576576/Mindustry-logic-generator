# 反编译端逆处理器(Decompile)

> [← 返回 README](../../README.md)

反编译把 mdtcode 指令行还原为 mdtc 语法。处理器以 mdtcode 指令字 + 空格
为键(如 `print `),参数 `s` 为指令字之后 trim 的内容。无法识别的
指令行一律还原为 `raw("<原行>")`。

> **实现说明(统一映射)**:还原行为**没有独立实现文件**——每个指令的
> `restore` 定义在其 `builtins/{大类名}/xxx.ts` 的 `InstrDef` 中,由
> `registry.ts` 统一派生 decompile 映射;共享指令字(`ucontrol`/`control`/
> `lookup`)按 `mcodeSelect` 分派到对应指令。`set `(赋值)与 `op `(运算符行)
> 为语言机制级通用还原。`printf`/`jump2` 等多行指令不做折叠还原
> (展开/折叠由 decompile 管道 `convertJump2`/`simplifyCode` 处理)。

## 基础还原

### set 
语言机制级通用还原(`registry.setRestore`):`set <变量> <值>` → `<变量>=<值>`

### print 
→ [builtins/ctrl/print.ts](../../builtins/ctrl/print.ts):`print <s>` → `print(<s>)`

### printchar 
→ [builtins/ctrl/printchar.ts](../../builtins/ctrl/printchar.ts):`printchar(<s>)`

### format 
→ [builtins/ctrl/format.ts](../../builtins/ctrl/format.ts):`format(<s>)`

### wait 
→ [builtins/ctrl/wait.ts](../../builtins/ctrl/wait.ts):`wait(<s>)`

### ubind 
→ [builtins/ctrl/ubind.ts](../../builtins/ctrl/ubind.ts):`ubind(<s>)`

### draw 
→ [builtins/ctrl/draw.ts](../../builtins/ctrl/draw.ts):`draw(reduce(0, s))`

### getlink 
→ [builtins/front/link.ts](../../builtins/front/link.ts):`<变量>=link(<索引>)`

### packcolor 
→ [builtins/front/pack.ts](../../builtins/front/pack.ts):`<变量>=pack(<r,g,b,a>)`

### printflush 
→ [builtins/dotCtrl/pflush.ts](../../builtins/dotCtrl/pflush.ts):`<s>.pflush()`

### drawflush 
→ [builtins/dotCtrl/dflush.ts](../../builtins/dotCtrl/dflush.ts):`<s>.dflush()`

## 条件与跳转还原

### jump 
→ [builtins/ctrl/jump.ts](../../builtins/ctrl/jump.ts):`jump(<标签>)[.when(<条件>)]`(恒真省略)

### select 
→ [builtins/dot/orElse.ts](../../builtins/dot/orElse.ts):`<结果>=<块>.orElse(<后备>).when(<条件>)`

## 单位/方块/雷达还原

### ucontrol 
→ [builtins/ctrl/uctrl.ts](../../builtins/ctrl/uctrl.ts)(兜底)+ [ushoot.ts](../../builtins/ctrl/ushoot.ts)(target/targetp 分派)

### control 
→ [builtins/dotCtrl/ctrl.ts](../../builtins/dotCtrl/ctrl.ts)(兜底)+ [enable/config/color/shoot](../../builtins/dotCtrl/enable.ts)(类型分派)

### ulocate 
→ [builtins/dotCtrl/ulocate.ts](../../builtins/dotCtrl/ulocate.ts):`<block>.ulocate(<type>)[.ore(...)][.enemy(...)]`

### unpackcolor 
→ [builtins/dotCtrl/unpack.ts](../../builtins/dotCtrl/unpack.ts):`<block>.unpack(reduce(0, 前4参))`

### write 
→ [builtins/dotCtrl/write.ts](../../builtins/dotCtrl/write.ts):`<block>.write(<内容>[,<单元号>])`

### sensor 
→ [builtins/dot/sensor.ts](../../builtins/dot/sensor.ts):`<变量>=<块>.sensor(<属性>)`

### read 
→ [builtins/dot/read.ts](../../builtins/dot/read.ts):`<变量>=<块>.read(<单元号>)`

### op 
语言机制级通用还原(`registry.opRestore`):运算符表/别名表数据驱动;
`logn`+末参 `2` → `lb`;`log` 两参交换去尾部 0

### lookup 
→ [builtins/front/lookup.ts](../../builtins/front/lookup.ts)(兜底)+ [block/unit/item/liquid/team](../../builtins/front/block.ts)(类型分派)

### uradar 
→ [builtins/front/uradar.ts](../../builtins/front/uradar.ts):`<结果>=uradar()` + `.target/.order/.sort` 链

### radar 
→ [builtins/front/radar.ts](../../builtins/front/radar.ts):`<结果>=uradar(<主体>)` + `.target/.order/.sort` 链

## 附加折叠

- **jump2 折叠**:以 `@counter=` 开头的行还原为 `jump2(<表达式>)`
  (`@counter=@counter…` 增量形式保留前缀语义),由 decompile 管道 `convertJump2` 处理。
