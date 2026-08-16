# Front 指令(编译端前缀函数,共 34 个)

> [← 返回 README](../../README.md)

Front 指令以函数形式嵌套在表达式中:`<handler>(<args>)`;调用方
`convertFront` 在高优组与低优组中扫描,命中后用 `mid.<ref>` 替换
整个调用表达式并递增 ref。参数 `s` 为括号内 trim 内容;`w` 为
按顶层逗号切分的参数数组。

> **链式调用检查**:`radar`/`uradar` 的链式键由 `InstrDef.chain` 声明
> (target/sort/main/order),未知链键输出警告,不影响输出。

> **具体指令规范(语法/参数/默认值/链式键/输出/反编译)已移入各指令
> 对应的 `.ts` 文件 JSDoc**,本页仅保留指令清单索引。

## FrontHigh(高优组,30 个)

#### 一元运算(输出 `op <op> mid.<ref> <s> 0`)

### not(
### abs(
### sign(
### floor(
### ceil(
### round(
### sqrt(
### rand(
### asin(
### acos(
### atan(
→ [builtins/front/not.ts](../../builtins/front/not.ts) 等 11 个:`not(x)` → `op not mid.<ref> <s> 0`

### ln(
### lg(
### lb(
→ [builtins/front/ln.ts](../../builtins/front/ln.ts) / [lg.ts](../../builtins/front/lg.ts) / [lb.ts](../../builtins/front/lb.ts):op 名 log / log10 / logn

#### 二元运算(输出 `op <op> mid.<ref> <w0> <w1>`)

### max(
### min(
### len(
### angle(
### angleDiff(
### noise(
→ [builtins/front/max.ts](../../builtins/front/max.ts) 等 6 个:`max(a,b)` → `op max mid.<ref> <w0> <w1>`

### log(
→ [builtins/front/log.ts](../../builtins/front/log.ts):`log(<底数>,<真数>)` → `op logn mid.<ref> <w1> <w0>`(两参交换)

#### 查表/取色(输出到 `mid.<ref>`)

### link(
→ [builtins/front/link.ts](../../builtins/front/link.ts):`link(<索引>)` → `getlink mid.<ref> <s>`

### lookup(
→ [builtins/front/lookup.ts](../../builtins/front/lookup.ts):`lookup(<类型>,<索引>)` → `lookup <w0|block> mid.<ref> <wLast|0>`

### block(
### unit(
### item(
### liquid(
### team(
→ [builtins/front/block.ts](../../builtins/front/block.ts) 等 5 个:`block(@copper-wall)` → `lookup block mid.<ref> <s>`

### pack(
→ [builtins/front/pack.ts](../../builtins/front/pack.ts):`pack(<r>,<g>,<b>,<a>)` → `packcolor mid.<ref> <pad(4, s)>`

### uradar(
→ [builtins/front/uradar.ts](../../builtins/front/uradar.ts):`uradar().target(<t>).sort(<s>).order(<o>)` → `uradar …`

## FrontLow(低优组,4 个)

### sin(
### cos(
### tan(
→ [builtins/front/sin.ts](../../builtins/front/sin.ts) 等 3 个:`sin(x)` → `op sin mid.<ref> <s>`(无尾随 0)

### radar(
→ [builtins/front/radar.ts](../../builtins/front/radar.ts):`radar().target(<t>).sort(<s>).main(<敌方目标>).order(<o>)` → `radar …`
