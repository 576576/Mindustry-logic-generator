# Building from Source

> [← Back to README](../../README.md)

## Prerequisites

- **JDK 25+** — [Adoptium](https://adoptium.net/) recommended
- **Gradle** — wrapper included (`gradlew`)
- **Node.js + npm** *(可选,仅内置指令开发时需要)* — 首次运行前执行 `npm install`;
  缺省时构建使用已提交的 `builtins/gen/builtins.js`

## Quick Build

```bash
# Full build + tests
./gradlew build

# CLI fat JAR (standalone command-line tool)
./gradlew shadowJar
# → build/libs/mdtc-[version]-Cli.jar

# Desktop mod JAR (for Mindustry mods/ folder)
./gradlew jarMod
# → build/libs/mdtc-[version]-Desktop.jar
```

## All Build Targets

| Task | Output | Description |
|------|--------|-------------|
| `build` | classes + test results | Compile + run tests |
| `shadowJar` | `mdtc-[version]-Cli.jar` | CLI fat JAR with dependencies |
| `jarMod` | `mdtc-[version]-Desktop.jar` | Desktop mod JAR |
| `jarAndroidMod` | `mdtc-[version]-Android.jar` | Android mod JAR (needs SDK) |
| `deployMod` | `mdtc-[version].jar` | Combined Desktop + Android |
| `syncBuiltinJs` | `builtins/gen/builtins.js` | Recompile built-in instructions (`.ts` → `.js`) |
| `extractRhino` | `build/generated/rhino/` | Extract rhino classes for CLI/run/test |

## Android Mod

Building the Android mod requires:

1. **Android SDK** — set `ANDROID_HOME` environment variable
2. **Build tools** with `d8` in PATH (e.g., `$ANDROID_HOME/build-tools/34.0.0/`)
3. API level 30 platform installed

```bash
./gradlew jarAndroidMod
```

## Version Bump

Update version in three files:

1. `gradle.properties` — `version=X.Y.Z`
2. `mod.hjson` — `version: X.Y.Z`
3. README badges

The build system derives artifact names from `gradle.properties`.

## Project Structure

```
build.gradle.kts     — Gradle build config (Kotlin DSL)
gradle.properties    — version number
mod.hjson            — Mindustry mod manifest
builtins/            — built-in instructions source of truth (.ts → gen/builtins.js)
docs/instructions/   — built-in instruction spec (doc-driven contract)
tools/sync-js.mjs    — .ts → .js generator
src/
  main/java/         — main source
  main/resources/    — version.properties template
  test/java/         — unit tests
assets/
  bundles/           — i18n .properties files
  sprites/           — mod textures (optional)
docs/                — documentation
sample_cases/        — syntax examples (.mdtc/.mdtcode)
modules/             — reusable .libmdtc libraries
```
