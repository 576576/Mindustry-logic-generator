# Ctrl 指令(编译端控制指令,共 15 个)

> [← 指令规范索引](README.md)

所有 ctrl 指令以 `xxx(` 形式出现在一行中(整行即该指令),输出替换整行。
除特别说明外,参数 `s` 为括号内 trim 后的内容。

## 透传指令

### print(
- 语法:`print(<内容>)`
- 输出:`print <s>`
- 示例:`print(flush)` → `print flush`
- 反编译:见 [decompile.md](decompile.md) 的 `print `

### printchar(
- 语法:`printchar(<码点>)`
- 输出:`printchar <s>`

### format(
- 语法:`format(<值>)`
- 输出:`format <s>`

### wait(
- 语法:`wait(<秒数>)`
- 输出:`wait <s>`

### stop(
- 语法:`stop()`
- 输出:`stop`(忽略参数)
- 反编译:裸 `stop` 反向还原。

### end(
- 语法:`end()`
- 输出:`end`(忽略参数)

### ubind(
- 语法:`ubind(<单位类型>)`
- 输出:`ubind <s>`

### raw(
- 语法:`raw("<原生 mdtcode 指令>")`
- 输出:`<s>`(原样透传,不做任何编译)

### tag(
- 语法:`tag(<标签名>)`
- 输出:`::<s>`(标签行;由后续 convertLink 解析)

## 固定填充指令

### uctrl(
- 语法:`uctrl(<类型>,<参数…>)`
- 输出:`ucontrol <pad(6, s)>`
- 说明:参数按顶层逗号切分,填充到 6 项(缺省 `0`,空格连接)。
- 示例:`uctrl(getBlock)` → `ucontrol getBlock 0 0 0 0 0`
- 反编译:见 [decompile.md](decompile.md) 的 `ucontrol `

### draw(
- 语法:`draw(<类型>,<参数…>)`
- 输出:`draw <pad(7, s)>`
- 示例:`draw(clear)` → `draw clear 0 0 0 0 0 0`

## 链式参数指令

> 链式键由 `InstrDef.chain` 声明(键名 + 缺省值);编译时未知链键输出警告。

### ushoot(
- 语法:`ushoot(<x>,<y>,<shoot>).target(<tx>,<ty>)`;shoot 缺省 `1`,
  target 缺省 `@this`
- 链式键:main(开火), target(目标)
- 输出:目标含逗号时 `ucontrol target <pad(5, tgt 逗号替换为空格, shoot)>`,
  否则 `ucontrol targetp <pad(5, tgt, shoot)>`
- 示例:`ushoot(10,20,1).target(5,6)` → `ucontrol target 5 6 1 0 0`
- 反编译:见 `ucontrol `(target/targetp 分支)

### jump(
- 语法:`jump(<目标标签>).when(<条件>)`;when 缺省恒真
- 链式键:main(目标标签,DEFAULT), when(条件,空)
- 条件判定:
  - when 表达式含多 token:子编译 `whenExpr`,若产物行非空:
    - `condition = getCondition(最后一行)`;若非 `always 0 0`,弹出该行;
    - 若为 `always 0 0` 且子表达式非空,则 `condition = notEqual <expr> 0`;
    - 产物行整体并入输出。
  - when 为单 token:`always` → `always 0 0`;`never` → `notEqual 0 0`;
    其他 → `notEqual <whenExpr> 0`。
  - when 为空:恒真。
- 输出:`jump <target> <condition>`
- 反编译:见 `jump `(还原为 `jump(<tag>).when(<cond>)`,恒真省略 when)

### jump2(
- 语法:`jump2(<表达式或增量表达式>)`
- 输出:不直接产出行;将 `s` 转为 `@counter=<s>`(单 token)或
  `@counter=@counter<s>`(多 token),交给子编译后把产物行写入 bash 列表,
  返回空串。
- 反编译:见 [decompile.md](decompile.md) 的 `@counter=` 折叠(jump2 还原)。

### printf(
- 语法:`printf(<格式串>,<参数…>)`
- 输出:参数不足 2 个时退化为 `print <s>`;否则产生多行:
  `print <p0>` + 每个后续参数一行 `format <pN>`。
- 示例:`printf(hello,x,y)` → 两行 `print hello`、`format x`、`format y`
  (由调用方按行并入 bash 列表)。
