# MdtC — Mindustry 逻辑代码编译器

> v2.0 · JDK 25 · Mindustry v157

一种类 Java 的高级语言，可编译为 Mindustry 原生逻辑汇编（`.mdtc` → `.mdtcode`），也可反向反编译。

提供 **CLI 命令行工具** 和 **游戏内模组** 两种使用方式。

---

## 快速开始

### CLI 工具

```bash
# 编译 .mdtc → .mdtcode
java -jar build/libs/mdtc-2.0-all.jar -i "sample_cases/case1.mdtc"

# 反编译 .mdtcode → .mdtc
java -jar build/libs/mdtc-2.0-all.jar -i "sample_cases/case1.mdtcode"

# 格式化 .libmdtc
java -jar build/libs/mdtc-2.0-all.jar -i "modules/example.libmdtc" -fo
```

### 游戏内模组

1. 将 `build/libs/mdtcDesktop.jar` 放入 Mindustry `mods/` 目录
2. 打开任意处理器 → 底部出现 **MdtC** 按钮
3. 点击打开左右双栏编辑器，处理器代码自动带入右栏并反编译到左栏

---

## 命令行参数

| 参数 | 说明 |
|------|------|
| `-i, --file <path>` | 输入文件 (`.mdtc` / `.mdtcode` / `.libmdtc`) |
| `-o, --output <path>` | 输出路径（默认与输入同目录、不同后缀） |
| `-f, --format` | 编译后同时格式化源文件 |
| `-fo, --format-only` | 仅格式化 |
| `-oo, --open-out` | 编译后在资源管理器中定位输出文件 |
| `-gpc, --generate-prime-code <0-2>` | 生成中间代码（调试用） |
| `-v, --version` | 显示版本 |

---

## 语法速览

| 特性 | 示例 |
|------|------|
| 注释/标签 | `::这是注释` `tag(标签名)` |
| 赋值 | `x = 1 + 2` |
| 函数调用 | `max(a, b)` `sin(x)` `len(s)` |
| 控制语句 | `if(cond){ }` `for(var, list){ }` `do{ }` |
| 等待/停止 | `wait(6)` `stop()` `end()` |
| 单位控制 | `ubind(@unit)` `ushoot(x, y, shoot)` |
| 传感器 | `block.sensor(result, @type)` `.read(result, cell)` |
| 跳转 | `jump(tag, when(cond))` `jump2(expr)` |
| 函数定义 | `function name(arg1, arg2){ ... }` |
| 导入 | `import path/to/lib` |
| 重复展开 | `repeat(var, 3){ ... }` |
| 打印 | `print(x)` `printf(x)` `printchar(x)` |

> 详细语法见 [`sample_cases/`](sample_cases/) 下的样例文件。

---

## 构建

```bash
# 要求 JDK 25+
./gradlew build          # 完整构建 + 测试
./gradlew jarMod          # 构建 Mindustry mod JAR
./gradlew shadowJar       # 构建 CLI fat JAR
```

---

## 项目结构

```
src/main/java/cn/sumitm/mdtc/
├── cli/          CLI 入口 (Main, CliHelper)
├── compiler/     编译器 + 反编译器 (CodeCompiler, CodeDecompiler)
├── core/         核心类型与工具 (Constants, Utils, stdCodeStream, stdFuncStream)
├── formatter/    代码格式化 (CodeFormatter)
└── mod/          Mindustry 模组 (ModInterface, I18n, ui/)
assets/
├── bundles/      多语言资源 (en, zh_CN)
└── sprites/      模组贴图
sample_cases/     语法样例
modules/          可复用函数库 (.libmdtc)
```
