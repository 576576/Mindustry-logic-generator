# MdtC Language Support (VSCode)

MdtC 语言的 VSCode 扩展与语言服务器(LSP)。
MdtC 是一门类 C 语言,可编译为 Mindustry 逻辑汇编(`.mdtc` → `.mdtcode`)。

## 功能

- **诊断** — 每次修改即时给出编译错误与警告(调用 Java CLI 编译器处理临时文件;
  括号/负数诊断精确到 token,消息按 locale 自适应中文/英文)
- **补全** — 内置指令键(ctrl / front / dot / dotCtrl);链式键协同母指令
  (如 `ushoot(...)` 后提示 `.target()`),标签带参数提示(如 `.color(hex color)`),
  光标紧贴 `.` 时自动去掉前导点避免 `..config()`
- **签名帮助** — 键入时参数提示,参数定义在各指令 `builtins/*/*.ts` 的 `params` 中(含链键)
- **格式化** — 通过 MdtC 格式化器格式化文档
- **悬停** — 指令文档(`docs/instructions/*.md`)
- **语义高亮** — 注释/字符串/数字/运算符/指令/@常量/标签/变量;
  链式指令随母指令高亮(如 `ushoot(1).target(...)` 中的 `.target`)
- **跳转到定义** — `jump` / `jump2` 标签 ↔ `::label`
- **`.mdtcode` 支持** — 编译产物(Mindustry 汇编)拥有独立语法高亮与 LSP:
  语义高亮(标签/指令/`@常量`/字符串/数字,op 运算符名与 control 分派名单独配色)、
  诊断(未知指令、`jump N` 越界、`@counter` 标签未定义、未知 `op` 运算符)、
  悬停(指令字 → 内置文档)、goto(`::label` ↔ `set @counter label`、`jump N` → 目标行)、
  指令字补全

语言服务器本身为 **TypeScript**(`server/src`,基于 `vscode-languageserver` 运行于 Node);
仅编译步骤调用 Java CLI jar。

## 环境要求

- Node.js(任意现代 LTS;运行服务器)
- Java(CLI jar 需要 Java 用于诊断/格式化)

## 构建(仓库根目录)

```
gradlew shadowJar          # 构建 CLI jar(mdtc-*-Cli.jar)
cd vscode-mdtc
npm install
npm run prepare:data       # 拷贝 builtins.js / docs / CLI jar 到 server/data
npm run compile            # tsc 编译服务器与扩展
npm run package            # 或:npx vsce package
code --install-extension mdtc-lsp-0.1.0.vsix
```

## 开发循环:服务器源码变更自动重启

编辑 `server/src/*.ts`,服务器会自动重新编译并重启:

1. 在仓库 `.vscode/settings.json`(或用户设置)中启用开发配置:`"mdtc.lsp.watch": true`
2. 运行 `npm run watch`(tsc -w 将 `server/src` 编译到 `server/out`)
3. 重载 VSCode 窗口 — 之后每次修改 `server/src/*.ts`,服务器进程都会退出,
   由客户端拉起新进程加载新代码(watch 开启时重启上限已提高)

## 配置

| Key | 说明 |
|-----|------|
| `mdtc.lsp.watch` | 开发模式:服务器源码变更时自动重启 |
| `mdtc.lsp.trace` | LSP 追踪级别:`off` / `messages` / `verbose` |