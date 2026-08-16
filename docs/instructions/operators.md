# 运算符表与指令码表(Operators & Codes)

> [← 返回 README](../../README.md)

## 运算符表(Operator)

**数组顺序即词法匹配顺序**(`stringSplit` 在字符串各位置
按此顺序尝试匹配),不可随意调整;例如 `===` 必须排在 `==` 前,
`>>>` 排 `>>` 前、`>>` 排 `>` 前,`&&` 排 `&` 前。

| 运算符名 | 词法值 | 优先级 | 备注 |
|----------|--------|--------|------|
| add | `+` | 4 | |
| sub | `.-` | 4 | 减法(连字符被占用) |
| mul | `*` | 5 | |
| idiv | `//` | 5 | 整数除 |
| div | `/` | 5 | 浮点除 |
| emod | `%%` | 5 | 欧几里得模 |
| mod | `.%` | 5 | |
| pow | `.^` | 7 | |
| strictEqual | `===` | 3 | |
| equal | `==` | 3 | |
| notEqual | `!=` | 3 | |
| land | `&&` | 2 | 逻辑与 |
| greaterThanEq | `>=` | 3 | |
| lessThanEq | `<=` | 3 | |
| ushr | `>>>` | 5 | 无符号右移 |
| shr | `>>` | 5 | |
| shl | `<<` | 5 | |
| xor | `^` | 2 | |
| greaterThan | `>` | 3 | |
| lessThan | `<` | 3 | |
| and | `&` | 2 | 按位与 |
| or | `|` | 2 | |
| lbracket | `(` | 10 | 左括号 |
| rbracket | `)` | 10 | 右括号 |
| set | `=` | 1 | 赋值 |
| always | `always` | 1 | 恒真 |
| never | `never` | 1 | 恒假 |

派生物:
- `midOpKeysMap`:词法值 → 运算符名。
- `midOpValueMap`:运算符名 → 词法值。
- `midOpPriorityMap`:词法值 → 优先级。

## 指令输出偏移表(operatorOffsetMap)

`set` 合并优化(`convertSet`)与条件提取(`getCondition`)需要知道
某条 mdtcode 指令的操作数起始位置(按空格分隔后的下标):

| mdtcode 指令字 | 偏移(操作数下标) |
|----------------|------------------|
| op | 2 |
| sensor | 1 |
| getlink | 1 |
| radar | 7 |
| uradar | 7 |
| lookup | 2 |
| packcolor | 1 |
| read | 1 |
| set | 1 |
| select | 1 |

## 运算符别名表(operatorAliasMap)

反编译时,无法直接映射回词法值的运算符名改用别名:

| 原生 op 名 | 别名 |
|------------|------|
| log10 | lg |
| log | ln |
| logn | log |

## 条件反转表(operatorReverseMap)

`if/else`、`.orElse`、反编译 `if` 重构时对条件取反使用:

| 条件 | 反转 |
|------|------|
| notEqual | equal |
| equal | notEqual |
| strictEqual | notEqual |
| lessThan | greaterThanEq |
| lessThanEq | greaterThan |
| greaterThan | lessThanEq |
| greaterThanEq | lessThan |
| always | never |
| never | always |

## 指令码表(Codes)

| 列表 | 内容 |
|------|------|
| ctrlCodes | `print( printchar( format( wait( stop( end( ubind( uctrl( ushoot( draw( jump( jump2( printf( tag( raw(` |
| dotCtrlCodes | `.ctrl( .enable( .config( .color( .shoot( .ulocate( .unpack( .pflush( .dflush( .write(` |
| dotCodes | `.sensor( .read( .orElse(` |
| dotCodesAll | dotCtrlCodes ∪ dotCodes |
| dotOpReduced | dotCodesAll 去掉各键末尾的 `(` |
