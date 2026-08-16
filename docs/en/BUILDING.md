# Building from Source

> [← Back to README](README.md) · [Instruction Spec →](../instructions/README.md)

## Prerequisites

- **JDK 25+** — [Adoptium](https://adoptium.net/) recommended
- **Gradle** — wrapper included (`gradlew`)
- **Node.js + npm** *optional, only needed for built-in instruction development* — run `npm install` once; otherwise the committed `builtins/gen/builtins.js` is used

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
3. README badges (filled from `gradle.properties` at render time)

The build system derives artifact names from `gradle.properties`. CI writes a real version for commits marked `r-x.y.z`/`beta-x.y.z`, and the commit short SHA otherwise (nightly).

## CI Build

The CI pipeline (`.github/workflows/ci.yml`) runs on every push/PR to `main`/`master`; markers in the commit message control build targets and version:

| Commit marker | Effect |
|------|-------------|
| `b-cli` | Build the CLI jar only |
| `b-mod` | Build the Desktop mod jar only |
| `b-all` | Build all artifacts (default) |
| `b-none` | Skip builds (docs/i18n only) |
| `r-x.y.z` | Set version X.Y.Z and create a GitHub Release |
| `beta-x.y.z` | Set version X.Y.Z (beta, no stable Release) |

Pipeline stages:

1. **prebuild** — Detects build targets & version from the commit message, updates version files and i18n hashes
2. **i18n** — Regenerates translation status and localized READMEs when bundles/docs change
3. **build** — JDK 25 + `./gradlew build`, then `shadowJar` / `jarMod` per markers
4. **release / nightly** — Creates a GitHub Release for stable versions; publishes nightly otherwise

## Project Structure

```text
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
