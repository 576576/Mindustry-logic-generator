# AGENT.md — MdtC 项目结构·接口·构建·模组

> 供 AI 编码助手理解本项目的完整上下文。人工可读文档见 `README.md`。

---

## 1. 项目概览

本项目是一个 Mindustry 逻辑代码编译器，包含两大产物：

| 产物 | 构建任务 | 输出 |
|------|----------|------|
| CLI 工具 | `shadowJar` | `mdtc-2.0-all.jar` — 可独立运行的 fat JAR |
| Mindustry 模组 | `jarMod` | `mdtcDesktop.jar` — 放入 `mods/` 目录的游戏模组 |

---

## 2. 构建系统

- **工具**: Gradle 9.5.0 + Kotlin DSL (`build.gradle.kts`)
- **JDK**: 25（`toolchain { languageVersion = JavaLanguageVersion.of(25) }`）
- **版本号**: `gradle.properties` → `version=2.0`

### 关键 Gradle 任务

| 任务 | 类型 | 说明 |
|------|------|------|
| `build` | 标准 | 编译 + 测试 |
| `shadowJar` | Shadow | CLI fat JAR，入口 `cn.sumitm.mdtc.cli.Main` |
| `jarMod` | 自定义 Jar | Mod JAR，含 `mod.hjson` + `assets/` + 运行时依赖 |
| `deployMod` | 自定义 Jar | 合并 Desktop + Android JAR（需 Android SDK） |
| `jarAndroidMod` | 自定义 | d8 dex 处理 |

### Mindustry 依赖

```kotlin
compileOnly("Anuken:Mindustry:v157")
```
从 GitHub Releases Ivy 仓库获取，**compileOnly** — 不打包进 JAR（由游戏提供）。

### 模组 JAR 内容

```
mdtcDesktop.jar
├── cn/sumitm/mdtc/...      编译产物
├── picocli/...              运行时依赖
├── mod.hjson                模组清单
├── assets/
│   ├── bundles/             多语言 (.properties)
│   └── sprites/             贴图
└── META-INF/
```

---

## 3. 源码结构

### 3.1 CLI 入口 — `cn.sumitm.mdtc.cli`

| 类 | 职责 |
|----|------|
| `Main` | `main()` 入口，加载版本号，解析参数，路由到编译/反编译/格式化 |
| `CliHelper` | Picocli 注解定义（`-i` `-o` `-f` `-gpc` 等） |

**关键静态字段**（编译器在内部引用）：
- `Main.primeCodeLevel` (int, 默认 0) — 控制中间代码生成级别
- `Main.filePath` (String, 默认 "") — 当前文件路径

> ⚠️ 编译器 `CodeCompiler.compile()` 会在 `Main.primeCodeLevel == 1` 时尝试写文件。从模组调用时该值为 0，不会触发。

### 3.2 编译器核心 — `cn.sumitm.mdtc.compiler`

#### `CodeCompiler`
- `public static String compile(String codeBlock)` — 主入口，`.mdtc` → `.mdtcode`
- 多趟管道：`insertImport` → `generateFuncMap` → `insertFunc` → `unfoldRepeat` → `convertCodeLine` → `convertSet` → `convertJump` → `convertLink`
- 所有中间结果存为 `mid.X` 变量，通过 `stdCodeStream.stat` 计数

#### `CodeDecompiler`
- `public static String decompile(String codeBlock)` — `.mdtcode` → `.mdtc`
- 管道：`convertLink` → `convertJump` → `convertCode` → `simplifyCode` → `convertJump2`

### 3.3 核心类型 — `cn.sumitm.mdtc.core`

| 类 | 说明 |
|----|------|
| `Constants` | 操作符映射、op 码偏移、语言关键字、`Operator` 枚举（含 `add/sub/mul/div/mod/pow/equal/notEqual/land/...` ） |
| `Utils` | `stringSplit`, `generateRpn`(调度场算法), `readFile/writeFile`, 括号匹配, 参数填充 |
| `stdCodeStream` | `record(ArrayList<String> bash, String expr, int stat)` — 编译管道数据载体 |
| `stdFuncStream` | `record(String funcName, List<String> funcBody, List<String> varsList, List<String> tagsList)` — 函数元数据 |

### 3.4 格式化 — `cn.sumitm.mdtc.formatter`

| 类 | 说明 |
|----|------|
| `CodeFormatter` | `format(String)` — 基于 `do{ for( if(` 等关键字自动缩进；`deformat(String)` — 去空行 |

### 3.5 Mindustry 模组 — `cn.sumitm.mdtc.mod`

#### `ModInterface`
- 继承 `mindustry.mod.Mod`
- `ClientLoadEvent` 时调用 `I18n.init()`，然后注入按钮到处理器编辑 UI：
  ```java
  Vars.ui.logic.shown(() -> {
      Vars.ui.logic.buttons.button(I18n.get("mdtc.button"), () -> {
          new LogicEditorDialog(currentCode).show();
      }).size(160f, 64f);
  });
  ```

#### `I18n`
- 多语言工具类，通过 `I18n.class.getClassLoader().getResourceAsStream()` 从 `assets/bundles/` 加载 `.properties`
- 按 locale 回退：默认(en) → 语言(zh) → 语言_国家(zh_CN)
- API: `I18n.get(key)` → 本地化文本；`I18n.format(key, args...)` → 格式化

#### `ui/LogicEditorDialog`
- 继承 `mindustry.ui.dialogs.BaseDialog`
- **布局**: 工具栏 + 左右双栏 + 状态栏
- **左栏**: `TextArea` 包裹在 `ScrollPane` 中 — 编辑 `.mdtc`
- **右栏**: `TextArea` 包裹在 `ScrollPane` 中 — 预览/输入 `.mdtcode`
- **工具栏**: `[Compile] [Decompile] [Format] [Clear]` ··· `[Settings]`
- **设置**: `BaseDialog` 弹出，含 `CheckBox` ×2（自动带入、自动格式化），关闭时 `Core.settings.forceSave()` 持久化

**设置持久化 key**:
| Key | 类型 | 默认 |
|-----|------|------|
| `mdtc.autoformat` | bool | true |
| `mdtc.autoload` | bool | true |

### 3.6 多语言资源

```
assets/bundles/
├── bundle.properties          英文（默认）
└── bundle_zh_CN.properties    简体中文（Unicode 转义 \uXXXX）
```

Bundle key 前缀 `mdtc.`，共 ~25 个 key。

---

## 4. 模组依赖 Mindustry API

本模组 `compileOnly` 依赖 Mindustry v157，以下为用到的关键类：

| 类 | 用途 |
|----|------|
| `mindustry.mod.Mod` | 模组基类 |
| `mindustry.Vars` | `ui.logic`（处理器对话框）、`ui.menufrag` |
| `mindustry.logic.LogicDialog` | 处理器代码编辑 UI |
| `mindustry.logic.LCanvas` | 处理器代码画布，`save()` 获取当前代码 |
| `mindustry.ui.dialogs.BaseDialog` | 对话框基类 |
| `arc.Core` | `app.setClipboardText()`, `bundle.getLocale()`, `settings`, `files` |
| `arc.scene.ui.*` | `TextArea`, `ScrollPane`, `CheckBox`, `Label`, `TextButton` |
| `arc.util.Log` | 日志 |
| `arc.Events` / `arc.util.Time` | 事件/定时 |

---

## 5. 开发注意事项

1. **不要修改 `CodeCompiler` / `CodeDecompiler` 的公共 API** — 它们是纯函数，CLI 和模组共用
2. **Bundle 文件** — 中文必须用 `\uXXXX` 转义（`.properties` 规范），新增 key 时同步更新两份文件
3. **编译验证** — 每次修改后运行 `./gradlew build` 确认测试通过
4. **模组 JAR** — 最终构建用 `./gradlew jarMod`，产物 `build/libs/mdtcDesktop.jar`
5. **Mindustry API** — 所有 Mindustry 类都是 `compileOnly`，不可用 `implementation`
6. **模组入口** — `mod.hjson` 中 `main` 指向 `cn.sumitm.mdtc.mod.ModInterface`，`minGameVersion: 157`
