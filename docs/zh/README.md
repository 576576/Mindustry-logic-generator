<p align="center">
  <img src="../assets/sprites/icon.png" width="64" alt="MdtC">
</p>

<h1 align="center">MdtC</h1>

<p align="center">
[English](README.md) | [中文](docs/zh-Hant/README.md) | **中文**(docs/zh/README.md)
</p>

<p align="center">
  <img src="https://img.shields.io/badge/JDK-25-red" alt="JDK">
  <img alt="version" src="https://img.shields.io/badge/version-fc1d4fd-informational" />
  <img src="https://img.shields.io/badge/Mindustry-v157-orange" alt="Mindustry">
  <img src="https://img.shields.io/badge/license-GPL--3.0-green" alt="License">
</p>

<p align="center"><em>一种类 Java 的高级语言，可编译为 Mindustry 原生逻辑汇编（`.mdtc` → `.mdtcode`），支持完整的双向转换。提供 **CLI** 工具和**游戏内 Mod** 两种使用方式。</em></p>

---

## 快速开始

### Mindustry 模组

**从模组浏览器**

1. 在 Mindustry 中选择 **模组** → **模组浏览器**

2. 搜索 **MdtC** → **安装**

3. 打开任意处理器 → 点击底部工具栏的 **MdtC**

4. 在左侧编辑 `.mdtc`，右侧预览 `.mdtcode`

**手动导入**

1. [下载](https://github.com/576576/mdtC/releases) `mdtc-{{VERSION}}-Desktop.jar`

2. 在 Mindustry 中选择 **模组** → **导入模组** → 选择 JAR 文件

3. 之后同上 3–4 步

### CLI

```bash
java -jar mdtc-fc1d4fd-Cli.jar [options]
```

> 详见 [CLI 参考](docs/CLI.md)。

---

## 特性

| | |
|---|---|
| 🔄 **完整双向转换** | 编译 `.mdtc` → `.mdtcode` 并支持无损反编译 |
| 🖥️ **双栏编辑器** | 在 Mindustry 游戏内边编辑边预览 |
| 🌍 **多语言** | 自动检测游戏语言 |
| 🧩 **丰富语法** | 函数、宏、命名参数、可导入库 |

## 文档

| 文档 | 内容 |
|----------|---------|
| [语法指南](docs/SYNTAX.md) | 完整语法说明与示例 |
| [CLI 参考](docs/CLI.md) | 命令行参数与用法 |
| [模组指南](docs/MOD.md) | 游戏内模组功能与设置 |
| [构建指南](docs/BUILDING.md) | 如何从源码构建 |
| [i18n 状态](docs/i18n.md) | 翻译覆盖率与贡献指南 |

---

## 多语言

MdtC 自动检测游戏语言以适配游戏内 UI。详见 [i18n.md](docs/i18n.md)。

### 贡献翻译

1. 复制 `assets/bundles/en.properties` 为 `assets/bundles/[langCode]-[regionCode].properties`

2. 翻译所有值 — **键名不可更改**

3. （可选）复制 `docs/` 到 `docs/xx/` 并翻译文档

4. 提交 Pull Request — CI 合入后自动更新 [docs/i18n.md](docs/i18n.md)

---

## 项目结构

```
src/main/java/cn/sumitm/mdtc/
├── cli/           CLI entry (Main, CliHelper)
├── compiler/      Compiler & decompiler (CodeCompiler, CodeDecompiler, LangRegistry)
├── core/          Data structures & utilities (Constants, Utils, LangBuiltins, WrappedList)
├── formatter/     Code formatter (CodeFormatter)
├── mod/           Mindustry mod (ModInterface, LogicEditorDialog, I18n)
└── resources/     Bundled assets
```

## 许可证

本项目使用 [GNU General Public License v3.0](LICENSE) 许可。

Mindustry is © Anuken, licensed under GPLv3. This mod is not affiliated with or endorsed by Anuken.
