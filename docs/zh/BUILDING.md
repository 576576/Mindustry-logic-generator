# 从源码构建

## 前置要求

- **JDK 25+** — 推荐使用 [Adoptium](https://adoptium.net/)
- **Gradle** — 仓库自带 wrapper（`gradlew`）

## 快速构建

```bash
# 完整构建 + 测试
./gradlew build

# CLI fat JAR（独立命令行工具）
./gradlew shadowJar
# → build/libs/mdtc-[version]-Cli.jar

# Desktop mod JAR（放入 Mindustry mods/ 目录）
./gradlew jarMod
# → build/libs/mdtc-[version]-Desktop.jar
```

## 所有构建目标

| 命令 | 产物 | 说明 |
|------|------|------|
| `build` | classes + 测试结果 | 编译 + 运行测试 |
| `shadowJar` | `mdtc-[version]-Cli.jar` | 包含所有依赖的 CLI fat JAR |
| `jarMod` | `mdtc-[version]-Desktop.jar` | Desktop mod JAR |
| `jarAndroidMod` | `mdtc-[version]-Android.jar` | Android mod JAR（需要 SDK） |
| `deployMod` | `mdtc-[version].jar` | Desktop + Android 合并 |

## 构建 Android Mod

构建 Android Mod 需要：

1. **Android SDK** — 设置 `ANDROID_HOME` 环境变量
2. 将 **build tools**（含 `d8`）添加到 PATH（如 `$ANDROID_HOME/build-tools/34.0.0/`）
3. 安装 API level 30 平台

```bash
./gradlew jarAndroidMod
```

## 版本号修改

在三处文件中更新版本号：

1. `gradle.properties` — `version=X.Y.Z`
2. `mod.hjson` — `version: X.Y.Z`
3. README 徽章链接

构建系统从 `gradle.properties` 读取版本号自动生成产物文件名。

## 项目结构

```
build.gradle.kts     — Gradle 构建配置（Kotlin DSL）
gradle.properties    — 版本号
mod.hjson            — Mindustry mod 清单
src/
  main/java/         — 主要源码
  main/resources/    — version.properties 模板
  test/java/         — 单元测试
assets/
  bundles/           — 多语言 .properties 文件
  sprites/           — mod 贴图（可选）
docs/                — 文档
sample_cases/        — 语法样例（.mdtc/.mdtcode）
modules/             — 可复用 .libmdtc 函数库
```
