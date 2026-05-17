# MdtC — Mindustry Logic Coder

[![CI](https://github.com/576576/mdtC/actions/workflows/ci.yml/badge.svg)](https://github.com/576576/mdtC/actions/workflows/ci.yml)
[![Version](https://img.shields.io/github/v/release/576576/mdtC?label=version&color=blue)](https://github.com/576576/mdtC/releases)
[![JDK](https://img.shields.io/badge/JDK-25-red)](https://adoptium.net/)
[![Mindustry](https://img.shields.io/badge/Mindustry-v157-orange)](https://github.com/Anuken/Mindustry)
[![License](https://img.shields.io/badge/license-GPL--3.0-green)](LICENSE)

> **Language**: **English** | [简体中文](docs/zh_CN/README.md)

A Java-like high-level language that compiles to Mindustry's native logic assembly (`.mdtc` → `.mdtcode`), with full round-trip decompilation support. Available as a **CLI** tool and an **in-game mod**.

---

## Quick Start

### CLI

```bash
java -jar mdtc-[version]-Cli.jar [options]
```

| Flag | Long | Argument | Description |
|------|------|----------|-------------|
| `-i` | `--file` | `<path>` | Input file (`.mdtc` / `.mdtcode` / `.libmdtc`) |
| `-o` | `--output` | `<path>` | Output path (default: auto-derived) |
| `-f` | `--format` | — | Format source after compilation |
| `-fo` | `--format-only` | — | Only format, skip compile |
| `-oo` | `--open-out` | — | Reveal output in file explorer |
| `-gpc` | `--generate-prime-code` | `<0\|1\|2>` | Generate intermediate code (debug) |
| `-v` | `--version` | — | Show version info |

```bash
# Compile
java -jar mdtc-[version]-Cli.jar -i sample_cases/case1.mdtc

# Decompile
java -jar mdtc-[version]-Cli.jar -i sample_cases/case1.mdtcode -f

# Format only
java -jar mdtc-[version]-Cli.jar -i modules/example.libmdtc -fo

# Custom output + open after
java -jar mdtc-[version]-Cli.jar -i sample.mdtc -o out.mdtcode -oo
```

> See [CLI Reference](docs/CLI.md) for more examples.

### Desktop Mod

1. Download `mdtc-[version]-Desktop.jar` from [Releases](https://github.com/576576/mdtC/releases)
2. In Mindustry, go to **Mods** → **Import Mod** → select the JAR file
3. Open any processor → click **MdtC** at the bottom toolbar
4. Edit `.mdtc` in the left pane, preview `.mdtcode` in the right pane

### Android Mod

1. Download `mdtc-[version]-Android.jar` from [Releases](https://github.com/576576/mdtC/releases)
2. In Mindustry, go to **Mods** → **Import Mod** → select the JAR file
3. Same workflow as Desktop Mod

> If the Android JAR is not available, use the Desktop JAR — it works on Android for code-only mods.

---

## Documentation

| Document | Content |
|----------|---------|
| [Syntax Guide](docs/SYNTAX.md) | Full language syntax with examples |
| [CLI Reference](docs/CLI.md) | Command-line arguments and usage |
| [Mod Guide](docs/MOD.md) | In-game mod features and settings |
| [Building](docs/BUILDING.md) | How to build from source |
| [AGENT.md](AGENT.md) | Project architecture (for AI assistants) |
| [i18n Status](i18n.md) | Translation coverage & contributing |

---

## Features

- **Full Round-Trip**: Compile `.mdtc` → `.mdtcode` and decompile back
- **Dual-Pane Editor**: Side-by-side edit/preview in Mindustry
- **Auto-Format**: Configurable indentation, bracket-aware
- **i18n**: English & 简体中文 — auto-detects game locale
- **Persistent Settings**: Indent width, auto-format, auto-import
- **Modular Libraries**: Reusable `.libmdtc` function imports
- **Repeat Macro**: `repeat(var, n){ ... }` compile-time unrolling
- **Named Arguments**: `.main(...).target(...).when(...)` chain syntax

---

## Build Artifacts

| File | Platform | Build Task |
|------|----------|------------|
| `mdtc-[version]-Cli.jar` | CLI (fat JAR) | `./gradlew shadowJar` |
| `mdtc-[version]-Desktop.jar` | Desktop Mod | `./gradlew jarMod` |
| `mdtc-[version]-Android.jar` | Android Mod | `./gradlew jarAndroidMod` |

---

## Internationalization

MdtC auto-detects the game locale for its in-game UI. See [i18n.md](i18n.md) for the current translation status.

### Contributing a Translation

1. Copy `assets/bundles/bundle.properties` to `assets/bundles/bundle_xx_XX.properties`  
   (use a [valid locale code](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Locale.html))
2. Translate all values — **keys must stay unchanged**
3. (Optional) Copy `docs/` to `docs/xx/` and translate the documentation
4. Submit a Pull Request — CI will auto-update [i18n.md](i18n.md) on merge

---

## Project Structure

```
src/main/java/cn/sumitm/mdtc/
├── cli/           CLI entry (Main, CliHelper)
├── compiler/      Compiler & decompiler (CodeCompiler, CodeDecompiler, LangRegistry)
├── core/          Types & utilities (Constants, Utils, LangBuiltins, WrappedList)
├── formatter/     Code formatter
└── mod/           Mindustry mod (ModInterface, I18n, ui/LogicEditorDialog)
assets/bundles/    i18n resources (en, zh_CN)
docs/              Documentation
sample_cases/      Syntax examples
modules/           Reusable .libmdtc libraries
```

## License

GPL-3.0 — see [LICENSE](LICENSE).
