<p align="center">
  <img src="{{icon_prefix}}assets/sprites/icon.png" width="64" alt="MdtC">
</p>

<h1 align="center">MdtC</h1>

<p align="center">
{{languages}}
</p>

<p align="center">
  <img src="https://img.shields.io/github/actions/workflow/status/576576/mdtC/ci.yml?branch=main&label=CI" alt="CI">
  <img src="https://img.shields.io/badge/JDK-25-red" alt="JDK">
  <img src="https://img.shields.io/badge/Mindustry-v157-orange" alt="Mindustry">
  <img alt="version" src="https://img.shields.io/badge/version-ebd18cf-informational" />
  <img src="https://img.shields.io/badge/license-GPL--3.0-green" alt="License">
</p>

<p align="center"><em>{{tagline}}</em></p>

---

## Quick Start

### Mindustry Mod

**From Mod Browser**

1. In Mindustry, go to **Mods** → **Mod Browser**
2. Search for **MdtC** → **Install**
3. Open any processor → click **MdtC** at the bottom toolbar
4. Edit `.mdtc` in the left pane, preview `.mdtcode` in the right pane

**Manual Import**

1. [Download](https://github.com/576576/mdtC/releases) `mdtc-ebd18cf-Desktop.jar`
2. In Mindustry, go to **Mods** → **Import Mod** → select the JAR file
3. Then same as steps 3–4 above

### Cli

```bash
java -jar mdtc-ebd18cf-Cli.jar [options]
```

> See [CLI Reference](docs/CLI.md) for instructions.
---


## Features

- **Full Round-Trip**: Compile `.mdtc` → `.mdtcode` and decompile back
- **Dual-Pane Editor**: Side-by-side edit/preview in Mindustry
- **Auto-Format**: Configurable indentation, bracket-aware
- **i18n**: English, 简体中文, 繁體中文, 日本語, Français — auto-detects game locale
- **Persistent Settings**: Indent width, auto-format, auto-import
- **Modular Libraries**: Reusable `.libmdtc` function imports
- **Repeat Macro**: `repeat(var, n){ ... }` compile-time unrolling
- **Named Arguments**: `.main(...).target(...).when(...)` chain syntax


## Documentation

| Document | Content |
|----------|---------|
| [Syntax Guide](docs/SYNTAX.md) | Full language syntax with examples |
| [CLI Reference](docs/CLI.md) | Command-line arguments and usage |
| [Mod Guide](docs/MOD.md) | In-game mod features and settings |
| [Building](docs/BUILDING.md) | How to build from source |
| [i18n Status](docs/i18n.md) | Translation coverage & contributing |

---

## I18n

MdtC auto-detects the game locale for its in-game UI. See [i18n.md](docs/i18n.md) for the current translation status.

### Contributing a Translation

1. Copy `assets/bundles/en.properties` to `assets/bundles/[langCode]-[regionCode].properties`  
   (use a [valid locale code](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Locale.html))
2. Translate all values — **keys must stay unchanged**
3. (Optional) Copy `docs/` to `docs/xx/` and translate the documentation
4. Submit a Pull Request — CI will auto-update [docs/i18n.md](docs/i18n.md) on merge

---

## Project Structure

```
src/main/java/cn/sumitm/mdtc/
├── cli/           CLI entry (Main, CliHelper)
├── compiler/      Compiler & decompiler (CodeCompiler, CodeDecompiler, LangRegistry)
├── core/          Data structures & utilities (Constants, Utils, LangBuiltins, WrappedList)
├── formatter/     Code formatter (CodeFormatter)
├── mod/           Mindustry mod (ModInterface, LogicEditorDialog, I18n)
└── resources/     Bundled assets
```


## License

This project is licensed under [GNU General Public License v3.0](LICENSE).

Mindustry is © Anuken, licensed under GPLv3. This mod is not affiliated with or endorsed by Anuken.
