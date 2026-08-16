# Front 指令(编译端前缀函数,共 32 个)

> [← 指令规范索引](README.md)

Front 指令以函数形式嵌套在表达式中:`<handler>(<args>)`;调用方
`convertFront` 在高优组与低优组中扫描,命中后用 `mid.<ref>` 替换
整个调用表达式并递增 ref。参数 `s` 为括号内 trim 内容;`w` 为
按顶层逗号切分的参数数组。

> **链式调用检查**:`radar`/`uradar` 的链式键由 `InstrDef.chain` 声明
> (target/sort/main/order),未知链键输出警告,不影响输出。

## FrontHigh(高优组,28 个)

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
- 语法:`not(x)` 等 11 个,使用同名 op 名。
- 示例:`not(x)` → `op not mid.1 x 0`
- 反编译:见 [decompile.md](decompile.md) 的 `op `

### ln(
- 输出:`op log mid.<ref> <s> 0`

### lg(
- 输出:`op log10 mid.<ref> <s> 0`

### lb(
- 输出:`op logn mid.<ref> <s> 2`

#### 二元运算(输出 `op <op> mid.<ref> <w0> <w1>`)

### max(
### min(
### len(
### angle(
### angleDiff(
### noise(
- 语法:`max(a,b)` 等 6 个,使用同名 op 名,w0/w1 为前两个参数。
- 示例:`max(a,b)` → `op max mid.1 a b`

### log(
- 语法:`log(<底数>,<真数>)`
- 输出:`op logn mid.<ref> <w1> <w0>`(**两参交换**)
- 示例:`log(2,8)` → `op logn mid.1 8 2`

#### 查表/取色(输出到 `mid.<ref>`)

### link(
- 语法:`link(<索引>)`
- 输出:`getlink mid.<ref> <s>`
- 反编译:见 `getlink `(还原为 `<结果>=link(<索引>)`)

### lookup(
- 语法:`lookup(<类型>,<索引>)`
- 输出:`lookup <w0|block> mid.<ref> <wLast|0>`
- 说明:类型缺省 `block`;索引取最后一个参数,缺省 `0`。
- 反编译:见 `lookup `

### block(
### unit(
### item(
### liquid(
### team(
- 语法:`block(@copper-wall)` 等 5 个,分别固定 lookup 类型。
- 输出:`lookup <type> mid.<ref> <s>`

### pack(
- 语法:`pack(<r>,<g>,<b>,<a>)`
- 输出:`packcolor mid.<ref> <pad(4, s)>`
- 反编译:见 `packcolor `(还原为 `<结果>=pack(<r,g,b,a>)`)

### uradar(
- 语法:`uradar().target(<t>).sort(<s>).order(<o>)`
- 链式键:target(缺省 `enemy,any,any`), sort(缺省 `distance`),
  order(缺省 `1`)
- 输出:`uradar <pad(any, 3, target)> <sort> 0 <order> mid.<ref>`
  (target 按逗号切分填充到 3 项,缺省 `any`)
- 反编译:见 `uradar `

## FrontLow(低优组,4 个)

### sin(
### cos(
### tan(
- 语法:`sin(x)` 等 3 个,输出 `op <op> mid.<ref> <s>`(注意:无尾随 0)

### radar(
- 语法:`radar().target(<t>).sort(<s>).main(<敌方目标>).order(<o>)`
- 链式键:target(缺省 `enemy,any,any`), sort(缺省 `distance`),
  main(缺省 `@this`), order(缺省 `1`)
- 输出:`radar <pad(any, 3, target)> <sort> <main> <order> mid.<ref>`
- 反编译:见 `radar `
