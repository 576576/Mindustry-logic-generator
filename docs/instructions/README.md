# 内置指令规范(Specification of Built-in Instructions)

> [← 返回 README](../../README.md)

本目录是 MdtC 语言**内置指令的唯一规范(Spec)**,也是"文档驱动开发"的契约:

```text
docs/instructions/*.md      ←  规范(人类可读,唯一事实源)
        │ 逐条对应(标题为 `### name(`)
        ▼
builtins/{大类}/xxx.ts      ←  每个指令一个 InstrDef 双向映射文件(compile 发射 + restore 还原)
        │ registry.ts 统一派生(compile/decompile/chain 三表)+ tools/sync-js.mjs 编译
        ▼
builtins/gen/builtins.js    ←  随包资源(CLI 与模组运行期加载,Rhino 执行)
        │
        ▼
Java 主程序                 ←  BuiltinEngine 加载注册表,不再硬编码任何指令
```

**一致性保障**:`BuiltinEngineTest` 会读取本目录各分类文档中 `### name(` 标题,
断言其与加载后的指令注册表完全一致(文档里出现的指令必须存在,注册表里
存在的指令必须被文档收录),防止文档与实现漂移。

## 文档索引

| 文档 | 内容 |
|------|------|
| [domain.md](domain.md) | 领域数据:ulocate 建筑/locate/lookup 内容类型/链式键/默认值常量 |
| [operators.md](operators.md) | 运算符表(含优先级与匹配顺序)、别名/反转/偏移表、指令码表 |
| [ctrl.md](ctrl.md) | 编译端 Ctrl 指令(print…raw,共 15 个) |
| [dot.md](dot.md) | 编译端 DotCtrl + Dot 指令(enabled…orElse,共 13 个) |
| [front.md](front.md) | 编译端 FrontHigh + FrontLow 指令(not…radar,共 34 个) |
| [decompile.md](decompile.md) | 反编译端逆处理器(set…radar,共 24 个,由指令定义派生) |

## 指令定义结构(InstrDef 双向映射)

每个指令一个文件(`builtins/{大类名}/xxx.ts`),导出 `InstrDef` 对象:

| 字段 | 说明 |
|------|------|
| `key` | **裸指令名**(如 `print` / `ulocate` / `not`);完整扫描键由大类模板生成 |
| `mcode` | mdtcode 指令字(缺省 = 裸 key;共享指令字如 `ucontrol`/`control`/`select` 需显式) |
| `mcodeSelect` | 共享指令字的分派 token(如 `control` 下的 `enabled`/`shoot`) |
| `chain` | 链式键声明(键名 + 缺省值;**compile 与 restore 的单一默认值来源**) |
| `compile` | 发射:mdtc 参数 → mdtcode 行(可含 \n 多行) |
| `restore` | 还原:mdtcode 参数 → mdtc(缺省 = 无反编译条目) |

**匹配符号由大类定义**:各大类的 `index.ts` 导出 `CategorySpec`(`keyOf` 模板 +
`defs`),注册表据此生成完整扫描键:

| 大类 | 模板 | 示例 |
|------|------|------|
| `ctrl` | 裸名 + `(` | `print` → `print(` |
| `dotCtrl` / `dot` | `.` + 裸名 + `(` | `ulocate` → `.ulocate(` |
| `front`(high/low) | 裸名 + `(` | `not` → `not(` |
| `mcodeSelect` | 共享指令字的分派 token(如 `control` 下的 `enabled`/`shoot`) |
| `chain` | 链式键声明(键名 + 缺省值;**compile 与 restore 的单一默认值来源**) |
| `compile` | 发射:mdtc 参数 → mdtcode 行(可含 \n 多行) |
| `restore` | 还原:mdtcode 参数 → mdtc(缺省 = 无反编译条目) |

**统一 compile/decompile**:注册表由 `registry.ts` 从所有指令定义统一派生
compile 五类映射 + decompile 映射 + chain 表;不再有独立的反编译处理器文件。
`set `(赋值)与 `op `(运算符行)为语言机制级通用还原,由运算符表数据驱动。

**多行指令**:`printf`/`jump2` 只有 compile(展开为多行),无 restore——
多行折叠回一行(jump2 的 `@counter` 折叠、中间变量合并)仍由 decompile
管道(`convertJump2`/`simplifyCode`)处理。

## 指令的通用约定

- **指令名(tag)**:编译端用 `print(` 这种带左括号的键;反编译端用 `print ` 带空格键。
- **链式链参数(chain)**:形如 `.target(x).ore(y)` 的链,经 `getChainParams`
  解析为 `main`(逗号前首部)+ 各显式键;文档中以"链式键"列明可用的键与默认值。
- **链式调用检查**:指令的 `chain` 声明列出合法链键(含 `main`);
  编译时对未知链键输出警告(`chain warning: … unknown chain key "x"`),
  不影响编译输出。
- **参数填充**:`pad(n)` 表示将参数按顶层逗号切分后补齐到 n 项(缺省填 `0`,
  空格连接);`pad(def, n)` 以 `def` 为缺省填充值。
- **子编译(sub)**:`jump(...).when(expr)` 等会把内嵌表达式交给主编译管道
  (`convertCodeLine`)先行编译,再根据产物拼接条件。
- **`mid.N`**:编译管道内部的中间变量槽,随 `ref` 计数器递增。
- **空返回**:某些指令(如 `jump2`)不直接产出行,而是通过桥接对象把编译行
  写入 bash 列表后返回空串。

## 如何新增/修改一条指令(流程)

1. 在本目录对应的分类文档中,按 `### name(` 标题补写/修改规范(语法、参数、
   默认值、链式键、输出、反编译形态)。
2. 在 `builtins/{大类名}/` 下新建 `xxx.ts`(或修改现有文件),导出 `InstrDef`:
   `key` + `compile` 必填;`restore`(需要反编译)、`chain`(链式指令)、
   `mcode`/`mcodeSelect`(共享指令字分派)按需填写。
3. 若属新指令,在 `builtins/{大类名}/index.ts` 的 `defs` 数组中登记
   (**顺序即匹配顺序**;`ctrl` 中 `raw` 必须最后)。
4. 运行 `node tools/sync-js.mjs` 重新生成 `builtins/gen/builtins.js` 并提交。
5. 运行 `./gradlew build`:既有行为测试、往返测试、文档↔注册表一致性测试、
   链检查测试全部通过;必要时在 `sample_cases/` 增补用例。
