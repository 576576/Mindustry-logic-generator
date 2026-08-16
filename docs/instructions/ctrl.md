# Ctrl 指令(编译端控制指令,共 15 个)

> [← 返回 README](../../README.md)

所有 ctrl 指令以 `xxx(` 形式出现在一行中(整行即该指令),输出替换整行。
除特别说明外,参数 `s` 为括号内 trim 后的内容。

> **具体指令规范(语法/参数/默认值/链式键/输出/反编译)已移入各指令
> 对应的 `.ts` 文件 JSDoc**,本页仅保留指令清单索引。

## 透传指令

### print(
→ [builtins/ctrl/print.ts](../../builtins/ctrl/print.ts):`print(<内容>)` → `print <s>`

### printchar(
→ [builtins/ctrl/printchar.ts](../../builtins/ctrl/printchar.ts):`printchar(<码点>)` → `printchar <s>`

### format(
→ [builtins/ctrl/format.ts](../../builtins/ctrl/format.ts):`format(<值>)` → `format <s>`

### wait(
→ [builtins/ctrl/wait.ts](../../builtins/ctrl/wait.ts):`wait(<秒数>)` → `wait <s>`

### stop(
→ [builtins/ctrl/stop.ts](../../builtins/ctrl/stop.ts):`stop()` → `stop`(忽略参数)

### end(
→ [builtins/ctrl/end.ts](../../builtins/ctrl/end.ts):`end()` → `end`(忽略参数)

### ubind(
→ [builtins/ctrl/ubind.ts](../../builtins/ctrl/ubind.ts):`ubind(<单位类型>)` → `ubind <s>`

### raw(
→ [builtins/ctrl/raw.ts](../../builtins/ctrl/raw.ts):`raw("<原生 mdtcode 指令>")` → 原样透传

### tag(
→ [builtins/ctrl/tag.ts](../../builtins/ctrl/tag.ts):`tag(<标签名>)` → `::<s>`

## 固定填充指令

### uctrl(
→ [builtins/ctrl/uctrl.ts](../../builtins/ctrl/uctrl.ts):`uctrl(<类型>,<参数…>)` → `ucontrol <pad(6, s)>`

### draw(
→ [builtins/ctrl/draw.ts](../../builtins/ctrl/draw.ts):`draw(<类型>,<参数…>)` → `draw <pad(7, s)>`

## 链式参数指令

> 链式键由 `InstrDef.chain` 声明(键名 + 缺省值);编译时未知链键输出警告。

### ushoot(
→ [builtins/ctrl/ushoot.ts](../../builtins/ctrl/ushoot.ts):`ushoot(<shoot>).target(<目标>|<x>,<y>)` → `ucontrol target|targetp …`

### jump(
→ [builtins/ctrl/jump.ts](../../builtins/ctrl/jump.ts):`jump(<目标标签>).when(<条件>)` → `jump <target> <condition>`

### jump2(
→ [builtins/ctrl/jump2.ts](../../builtins/ctrl/jump2.ts):`jump2(<表达式或增量>)` → `@counter=` 子编译,无直接输出行

### printf(
→ [builtins/ctrl/printf.ts](../../builtins/ctrl/printf.ts):`printf(<格式串>,<参数…>)` → 多行 `print` + `format` 展开
