# {{building.title}}

> [{{building.back_readme}}](README.md) · [{{building.back_spec}}](../instructions/README.md)

## {{building.prereq}}

- **JDK 25+** — [Adoptium](https://adoptium.net/) {{building.prereq_jdk}}
- **Gradle** — {{building.prereq_gradle}}
- **Node.js + npm** *{{building.prereq_node_opt}}* — {{building.prereq_node}}

## {{building.quick}}

```bash
# {{building.quick_build}}
./gradlew build

# {{building.quick_cli}}
./gradlew shadowJar
# → build/libs/mdtc-[version]-Cli.jar

# {{building.quick_mod}}
./gradlew jarMod
# → build/libs/mdtc-[version]-Desktop.jar
```

## {{building.targets}}

| {{building.t_col1}} | {{building.t_col2}} | {{building.t_col3}} |
|------|--------|-------------|
| `build` | {{building.t_build_out}} | {{building.t_build}} |
| `shadowJar` | `mdtc-[version]-Cli.jar` | {{building.t_shadow}} |
| `jarMod` | `mdtc-[version]-Desktop.jar` | {{building.t_jarMod}} |
| `jarAndroidMod` | `mdtc-[version]-Android.jar` | {{building.t_android}} |
| `deployMod` | `mdtc-[version].jar` | {{building.t_deploy}} |
| `syncBuiltinJs` | `builtins/gen/builtins.js` | {{building.t_sync}} |
| `extractRhino` | `build/generated/rhino/` | {{building.t_rhino}} |

## {{building.android}}

{{building.android_intro}}

1. **Android SDK** — {{building.android_1}}
2. **Build tools** {{building.android_2}}
3. {{building.android_3}}

```bash
./gradlew jarAndroidMod
```

## {{building.version}}

{{building.version_intro}}

1. `gradle.properties` — `version=X.Y.Z`
2. `mod.hjson` — `version: X.Y.Z`
3. {{building.version_3}}

{{building.version_note}}

## {{building.ci}}

{{building.ci_intro}}

| {{building.ci_marker}} | {{building.ci_effect}} |
|------|-------------|
| `b-cli` | {{building.ci_bcli}} |
| `b-mod` | {{building.ci_bmod}} |
| `b-all` | {{building.ci_ball}} |
| `b-none` | {{building.ci_bnone}} |
| `r-x.y.z` | {{building.ci_r}} |
| `beta-x.y.z` | {{building.ci_beta}} |

{{building.ci_steps}}:

1. **prebuild** — {{building.ci_prebuild}}
2. **i18n** — {{building.ci_i18n}}
3. **build** — {{building.ci_build}}
4. **release / nightly** — {{building.ci_release}}

## {{building.structure}}

```text
{{building.structure_body}}
```
