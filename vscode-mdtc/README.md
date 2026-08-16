# MdtC Language Support (VSCode)

Language server for [MdtC](https://github.com/576576/mdtC) — a C-like language
that compiles to Mindustry logic assembly (`.mdtc` → `.mdtcode`).

## Features

- **Diagnostics** — compile errors & warnings on every change
- **Completion** — built-in instruction keys (ctrl / front / dot / dotCtrl)
- **Formatting** — document formatting via the MdtC formatter
- **Hover** — instruction docs from `docs/instructions/*.md`

## Requirements

- Java 21+ on PATH (bundled LSP server jar needs Java to launch)

## Build (from repo root)

```
gradlew lspJar
copy build\libs\mdtc-*-Lsp.jar vscode-mdtc\server\
cd vscode-mdtc
npm install
npm run package     # or: npx vsce package
code --install-extension mdtc-lsp-0.1.0.vsix
```

## Configuration

| Key | Description |
|-----|-------------|
| `mdtc.lsp.jarPath` | Path to LSP jar (empty = bundled) |
| `mdtc.lsp.javaArgs` | Extra JVM args for the server process |
