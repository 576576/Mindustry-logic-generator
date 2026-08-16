# 反编译端逆处理器(Decompile,共 24 个)

> [← 指令规范索引](README.md)

反编译把 mdtcode 指令行还原为 mdtc 语法。处理器以 mdtcode 指令字 + 空格
为键(如 `print `),参数 `s` 为指令字之后 trim 的内容。无法识别的
指令行一律还原为 `raw("<原行>")`。

> **实现说明(统一映射)**:本表所列还原行为**没有独立实现文件**——
> 每个指令的 `restore` 定义在其 `builtins/{大类名}/xxx.ts` 的 `InstrDef` 中,
> 由 `registry.ts` 统一派生 decompile 映射;共享指令字(`ucontrol`/`control`/
> `lookup`)按 `mcodeSelect` 分派到对应指令。`set `(赋值)与 `op `(运算符行)
> 为语言机制级通用还原。`printf`/`jump2` 等多行指令不做折叠还原
> (展开/折叠由 decompile 管道 `convertJump2`/`simplifyCode` 处理)。

## 基础还原

### set 
- 语法:`set <变量> <值>`
- 还原:`<变量>=<值>`(第一个空格替换为 `=`)

### print 
- 还原:`print(<s>)`

### printchar 
- 还原:`printchar(<s>)`

### format 
- 还原:`format(<s>)`

### wait 
- 还原:`wait(<s>)`

### ubind 
- 还原:`ubind(<s>)`

### draw 
- `draw <类型> <参数…>` → `draw(reduce(0, s))`(去掉尾部 0,逗号分隔)

### getlink 
- `getlink <变量> <索引>` → `<变量>=link(<索引>)`

### packcolor 
- `packcolor <变量> <r g b a>` → `<变量>=pack(<r,g,b,a>)`

### printflush 
- → `<s>.pflush()`

### drawflush 
- → `<s>.dflush()`

## 条件与跳转还原

### jump 
- `jump <标签> <条件>` → 条件 `reduceCondition` 后,若为 `0==0` 或
  `always` 则省略,否则 `.when(<条件>)`;输出
  `jump(<标签>)[.when(<条件>)]`

### select 
- `select <结果> <条件3元组> <块> <后备>` → 条件取反还原;
  `<结果>=<块>.orElse(<后备>).when(<条件>)`

## 单位/方块/雷达还原

### ucontrol 
- 类型 `target`/`targetp` 之外 → `uctrl(reduce(0, 类型 参数…))`
- `ucontrol targetp <x> <y> <shoot> …`:`ushoot(<y>)`(shoot 为 1 省略)
  + `.target(<x>)`
- `ucontrol target <tx> <ty> <shoot> …`:`ushoot(<ty>)` + `.target(<tx>,<ty>)`

### control 
- 类型 `enabled` → `<block>.enable(...)`;`config`/`color` 同名;
  `shootp` → `<block>.shoot(<y>)` + `.target(<x>)`;
  `shoot` → `<block>.shoot(<ty>)` + `.target(<tx>,<ty>)`;
  未知类型 → `<block>.ctrl(reduce(0, 类型 参数…))`

### ulocate 
- `ulocate <type> <building> <enemy> <ore> … <block>.x …` →
  `<block>.ulocate(<type>)`;type 为 `building` 则替换为 building 参数;
  追加 `.ore(<ore>)`(仅当 type 为 `ore` 时)、`.enemy(<enemy>)`(非 0 时)

### unpackcolor 
- `unpackcolor <r> <g> <b> <a> <block>` → `<block>.unpack(reduce(0, 前4参))`

### write 
- `write <内容> <block> <单元号>` → 单元号为 0 省略;
  `<block>.write(<内容>[,<单元号>])`

### sensor 
- `sensor <变量> <块> <属性>` → `<变量>=<块>.sensor(<属性>)`

### read 
- `read <变量> <块> <单元号>` → `<变量>=<块>.read(<单元号>)`

### op 
- 运算符名直接命中值表(如 `add`):
  - 3 参:`<结果>=<值>(<参2>)`(一元)
  - 其余:`<结果>=<参2> <值> <参3>`(中缀)
- `logn` 且末参为 `2` → `<结果>=lb(<参2>)`
- 其他:按别名表换名(log10→lg、log→ln、logn→log);3 参以内单参,
  `log` 交换两参并去尾部 0,其余两参去尾部 0;
  `<结果>=<名>(<参数>)`

### lookup 
- 类型为 block/unit/item/liquid/team → `<结果>=<类型>(<索引>)`
- 其他类型 → `<结果>=lookup(<类型>,<索引>)`

### uradar 
- `uradar <t1> <t2> <t3> <sort> 0 <order> <结果>` → `<结果>=uradar()`
  加 `.target(reduce(any,3,t1,t2,t3))`(非空且非 `enemy` 时)、
  `.order(<order>)`(非 1 时)、`.sort(<sort>)`(非 distance 时)

### radar 
- 同 uradar,主体为 `uradar(<主体>)`;`@this` 主体省略

## 附加折叠

- **jump2 折叠**:以 `@counter=` 开头的行还原为 `jump2(<表达式>)`
  (`@counter=@counter…` 增量形式保留前缀语义)。
