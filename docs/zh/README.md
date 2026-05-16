# MdtC — Mindustry 逻辑代码编写器

[![CI](https://github.com/576576/mdtC/actions/workflows/ci.yml/badge.svg)](https://github.com/576576/mdtC/actions/workflows/ci.yml)
[![Version](https://img.shields.io/badge/dynamic/yaml?url=https%3A%2F%2Fraw.githubusercontent.com%2F576576%2FmdtC%2Fmain%2Fmod.hjson&query=%24.version&label=version&color=blue)](https://github.com/576576/mdtC/releases)
[![JDK](https://img.shields.io/badge/JDK-25-red)](https://adoptium.net/)
[![Mindustry](https://img.shields.io/badge/Mindustry-v157-orange)](https://github.com/Anuken/Mindustry)
[![License](https://img.shields.io/badge/license-GPL--3.0-green)](../../LICENSE)

> **语言**: [English](../../README.md) | **简体中文**

一种类 Java 的高级语言，可编译为 Mindustry 原生逻辑汇编（`.mdtc` → `.mdtcode`），并支持完整反向反编译。提供 **CLI 命令行工具** 和 **游戏内模组** 两种形态。

---

## 快速开始

### CLI

```bash
java -jar mdtc-[version]-Cli.jar [选项]
```

| 参数 | 完整写法 | 值 | 说明 |
|------|----------|-----|------|
| `-i` | `--file` | `<路径>` | 输入文件（`.mdtc` / `.mdtcode` / `.libmdtc`） |
| `-o` | `--output` | `<路径>` | 输出路径（默认自动推导） |
| `-f` | `--format` | — | 编译后格式化源码 |
| `-fo` | `--format-only` | — | 仅格式化，跳过编译 |
| `-oo` | `--open-out` | — | 在资源管理器中打开输出文件 |
| `-gpc` | `--generate-prime-code` | `<0\|1\|2>` | 生成中间代码（调试用） |
| `-v` | `--version` | — | 显示版本信息 |

```bash
# 编译
java -jar mdtc-[version]-Cli.jar -i sample_cases/case1.mdtc

# 反编译
java -jar mdtc-[version]-Cli.jar -i sample_cases/case1.mdtcode -f

# 仅格式化
java -jar mdtc-[version]-Cli.jar -i modules/example.libmdtc -fo

# 指定输出 + 编译后打开
java -jar mdtc-[version]-Cli.jar -i sample.mdtc -o out.mdtcode -oo
```

> 更多示例见 [CLI 参考](CLI.md)。

### Desktop Mod

1. 从 [Releases](https://github.com/576576/mdtC/releases) 下载 `mdtc-[version]-Desktop.jar`
2. 在 Mindustry 中进入 **Mods** → **导入 Mod** → 选择 JAR 文件
3. 打开任意处理器 → 底部工具栏出现 **MdtC** 按钮
4. 左侧编辑 `.mdtc`，右侧预览 `.mdtcode`

### Android Mod

1. 从 [Releases](https://github.com/576576/mdtC/releases) 下载 `mdtc-[version]-Android.jar`
2. 在 Mindustry 中进入 **Mods** → **导入 Mod** → 选择 JAR 文件
3. 与 Desktop Mod 完全相同的使用方式

> 若 Android JAR 不可用，Desktop JAR 对纯代码模组也可在 Android 上使用。

---

## 文档

| 文档 | 内容 |
|------|------|
| [语法指南](SYNTAX.md) | 完整语言语法及示例 |
| [CLI 参考](CLI.md) | 命令行参数与用法 |
| [Mod 指南](MOD.md) | 游戏内模组功能与设置 |
| [构建指南](BUILDING.md) | 从源码构建 |
| [AGENT.md](../../AGENT.md) | 项目架构（供 AI 辅助阅读） |

---

## 特性

- **完整往返**：编译 `.mdtc` → `.mdtcode`，并可反向反编译
- **双栏编辑器**：游戏内左右对照编辑/预览
- **自动格式化**：可配置缩进，括号感知
- **多语言**：English & 简体中文 — 自动检测游戏语言
- **持久化设置**：缩进宽度、自动格式化、自动导入
- **模块化库**：可复用的 `.libmdtc` 函数导入
- **Repeat 宏**：`repeat(var, n){ ... }` 编译时展开
- **链式命名参数**：`.main(...).target(...).when(...)` 语法

---

## 构建产物

| 文件 | 平台 | 构建命令 |
|------|------|----------|
| `mdtc-[version]-Cli.jar` | CLI (fat JAR) | `./gradlew shadowJar` |
| `mdtc-[version]-Desktop.jar` | Desktop Mod | `./gradlew jarMod` |
| `mdtc-[version]-Android.jar` | Android Mod | `./gradlew jarAndroidMod` |

---

## 项目结构

```
src/main/java/cn/sumitm/mdtc/
├── cli/           命令行入口（Main, CliHelper）
├── compiler/      编译器与反编译器（CodeCompiler, CodeDecompiler, LangRegistry）
├── core/          类型与工具类（Constants, Utils, LangBuiltins, WrappedList）
├── formatter/     代码格式化
└── mod/           Mindustry 模组（ModInterface, I18n, ui/LogicEditorDialog）
assets/bundles/    多语言资源（en, zh_CN）
docs/              文档
sample_cases/      语法样例
modules/           可复用 .libmdtc 函数库
```

## 许可证

GPL-3.0 — 详见 [LICENSE](../../LICENSE)。
