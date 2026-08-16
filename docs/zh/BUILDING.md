# 从源码构建

> [← 返回 README](README.md) · [内置指令规范 →](../instructions/README.md)

## 前置要求

- **JDK 25+** — [Adoptium](https://adoptium.net/) 推荐使用
- **Gradle** — 仓库自带 wrapper(`gradlew`)
- **Node.js + npm** *可选,仅内置指令开发时需要* — 首次运行前执行 `npm install`;缺省时构建使用已提交的 `builtins/gen/builtins.js`

## 快速构建

```bash
# 完整构建 + 测试
./gradlew build

# CLI fat JAR(独立命令行工具)
./gradlew shadowJar
# → build/libs/mdtc-[version]-Cli.jar

# Desktop mod JAR(放入 Mindustry mods/ 目录)
./gradlew jarMod
# → build/libs/mdtc-[version]-Desktop.jar
```

## 所有构建目标

| 命令 | 产物 | 说明 |
|------|--------|-------------|
| `build` | classes + 测试结果 | 编译 + 运行测试 |
| `shadowJar` | `mdtc-[version]-Cli.jar` | 包含所有依赖的 CLI fat JAR |
| `jarMod` | `mdtc-[version]-Desktop.jar` | Desktop mod JAR |
| `jarAndroidMod` | `mdtc-[version]-Android.jar` | Android mod JAR(需要 SDK) |
| `deployMod` | `mdtc-[version].jar` | Desktop + Android 合并 |
| `syncBuiltinJs` | `builtins/gen/builtins.js` | 重新编译内置指令(`.ts` → `.js`) |
| `extractRhino` | `build/generated/rhino/` | 抽取 rhino 类(CLI/run/test 使用) |

## 构建 Android Mod

构建 Android Mod 需要:

1. **Android SDK** — 设置 `ANDROID_HOME` 环境变量
2. **Build tools** 将含 `d8` 的 build tools 添加到 PATH(如 `$ANDROID_HOME/build-tools/34.0.0/`)
3. 安装 API level 30 平台

```bash
./gradlew jarAndroidMod
```

## 版本号修改

在三处文件中更新版本号:

1. `gradle.properties` — `version=X.Y.Z`
2. `mod.hjson` — `version: X.Y.Z`
3. README 徽章链接(渲染时从 `gradle.properties` 自动填充)

构建系统从 `gradle.properties` 读取版本号自动生成产物文件名。CI 对含 `r-x.y.z`/`beta-x.y.z` 标记的提交写入正式版本,普通提交写入提交短 SHA(nightly)。

## CI 构建

CI 流水线(`.github/workflows/ci.yml`)在每次 push / PR 到 `main`/`master` 时运行,通过提交信息中的标记控制构建目标与版本:

| 提交信息标记 | 效果 |
|------|-------------|
| `b-cli` | 仅构建 CLI jar |
| `b-mod` | 仅构建 Desktop 模组 jar |
| `b-all` | 构建全部产物(默认) |
| `b-none` | 跳过构建(仅文档/翻译) |
| `r-x.y.z` | 版本号设为 X.Y.Z,创建 GitHub Release |
| `beta-x.y.z` | 版本号设为 X.Y.Z(beta,不创建稳定 Release) |

流水线阶段:

1. **prebuild** — 检测构建目标与版本号,更新版本文件与 i18n 哈希
2. **i18n** — bundle/文档变化时重新生成翻译状态与多语言 README
3. **build** — JDK 25 + `./gradlew build`,随后按标记执行 `shadowJar` / `jarMod`
4. **release / nightly** — 稳定版创建 GitHub Release;普通提交发布 nightly

## 项目结构

```text
build.gradle.kts     — Gradle 构建配置(Kotlin DSL)
gradle.properties    — 版本号
mod.hjson            — Mindustry mod 清单
builtins/            — 内置指令唯一事实源(.ts → gen/builtins.js)
docs/instructions/   — 内置指令规范(文档驱动契约)
tools/sync-js.mjs    — .ts → .js 生成工具
src/
  main/java/         — 主要源码
  main/resources/    — version.properties 模板
  test/java/         — 单元测试
assets/
  bundles/           — 多语言 .properties 文件
  sprites/           — mod 贴图(可选)
docs/                — 文档
sample_cases/        — 语法样例(.mdtc/.mdtcode)
modules/             — 可复用 .libmdtc 函数库
```
