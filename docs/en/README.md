![MdtC](../../assets/sprites/icon_head.png)

# MdtC

<a href="../zh/README.md">中文 (简体)</a> &nbsp;|&nbsp; <a href="../zh-Hant/README.md">中文 (繁體)</a> &nbsp;|&nbsp; English

![JDK](https://img.shields.io/badge/JDK-25-red) ![Version](https://img.shields.io/badge/version-{version}-informational) ![Mindustry](https://img.shields.io/badge/Mindustry-v157-orange) ![License](https://img.shields.io/badge/license-GPL--3.0-green)

## About

A C-like high-level language that compiles to Mindustry's native logic assembly, with full round-trip decompilation support. Available as a **mod** and a **CLI** tool.

## Quick Start

### Mindustry Mod

#### From Mod Browser

1. In Mindustry, go to **Mods** → **Mod Browser**

2. Search for **MdtC** → **Install**

3. Open any processor → click **MdtC** at the bottom toolbar

4. Edit `.mdtc` in the left pane, preview `.mdtcode` in the right pane

#### Manual Import

1. [Download](https://github.com/576576/mdtC/releases) `mdtc-{version}-Desktop.jar`

2. In Mindustry, go to **Mods** → **Import Mod** → select the JAR file

3. Then same as steps 3–4 above

### CLI

```bash
java -jar mdtc-{version}-Desktop.jar [options]
```

> See [CLI Reference](../docs/CLI.md) for instructions.

---

## Features

Feature | Description
--- | ---
🔄 **Full Round-Trip** | Compile `.mdtc` → `.mdtcode` and decompile back losslessly
🖥️ **Dual-Pane Editor** | Side-by-side edit & preview, directly inside Mindustry
🌍 **Multi-Language** | Auto-detects game locale
🧩 **Rich Syntax** | Functions, macros, named arguments, importable libraries

## Documentation

Document | Content
----------|---------
[Syntax Guide](../docs/SYNTAX.md) | Full language syntax with examples
[CLI Reference](../docs/CLI.md) | Command-line arguments and usage
[Mod Guide](../docs/MOD.md) | In-game mod features and settings
[Building](../docs/BUILDING.md) | How to build from source
[i18n Status](../docs/i18n.md) | Translation coverage & contributing

---

## I18n

MdtC auto-detects the game locale for its in-game UI. See [i18n.md](../docs/i18n.md) for the current translation status.

### Contributing a Translation

1. Copy `assets/bundles/en.properties` to `assets/bundles/[langCode]-[regionCode].properties`

2. Translate all values — **keys must stay unchanged**

3. (Optional) Copy `assets/docs/en.json` to `assets/docs/[langCode].json` and translate the documentation

4. Submit a Pull Request — CI will auto-update [docs/i18n.md](../docs/i18n.md) on merge

---

## Project Structure

```text
src/main/java/cn/sumitm/mdtc/
├── cli/               CLI entry (Main, CliHelper)
├── compiler/               Compiler & decompiler (CodeCompiler, CodeDecompiler, LangRegistry)
├── core/               Data structures & utilities (Constants, Utils, LangBuiltins, WrappedList)
├── formatter/               Code formatter (CodeFormatter)
├── mod/               Mindustry mod (ModInterface, LogicEditorDialog, I18n)
└── resources/               Bundled assets
```

## License

This project is licensed under [GNU General Public License v3.0](LICENSE).

Mindustry is © Anuken, licensed under GPLv3. This mod is not affiliated with or endorsed by Anuken.
