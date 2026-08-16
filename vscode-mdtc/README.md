# MdtC Language Support (VSCode)

Language server for [MdtC](https://github.com/576576/mdtC) — a C-like language
that compiles to Mindustry logic assembly (`.mdtc` → `.mdtcode`).

## Features

- **Diagnostics** — compile errors & warnings on every change
  (uses the Java CLI compiler on a temp file; bracket/negative-number
  diagnostics are token-exact, locale-adaptive zh/en)
- **Completion** — built-in instruction keys (ctrl / front / dot / dotCtrl);
  chain keys coordinated with their parent (e.g. `ushoot(...)` → `.target(`)
- **Signature help** — parameter hints while typing, defined per instruction
  in `builtins/*/*.ts` (`params`), incl. chain keys
- **Formatting** — document formatting via the MdtC formatter
- **Hover** — instruction docs from `docs/instructions/*.md`
- **Semantic highlighting** — comments / strings / numbers / operators /
  instructions / @constants / labels / variables
- **Go to definition** — `jump` / `jump2` label ↔ `::label`

The language server itself is **TypeScript** (`server/src`, runs on Node via
`vscode-languageserver`); only the compile step shells out to the Java CLI jar.

## Requirements

- Node.js (any modern LTS; dev loop uses it to run the server)
- Java (the bundled CLI jar needs Java on PATH for diagnostics/formatting)

## Build (from repo root)

```
gradlew shadowJar          # build the CLI jar (mdtc-*-Cli.jar)
cd vscode-mdtc
npm install
npm run prepare:data       # copies builtins.js / docs / CLI jar into server/data
npm run compile            # tsc for server + extension
npm run package            # or: npx vsce package
code --install-extension mdtc-lsp-0.1.0.vsix
```

## Dev loop: auto-restart on server source change

Edit `server/src/*.ts`, the server recompiles and restarts automatically:

1. enable the dev watch config in the repo's `.vscode/settings.json`
   (or user settings): `"mdtc.lsp.watch": true`
2. run `npm run watch` (tsc -w for `server/src` → `server/out`)
3. reload the VSCode window — now every edit to `server/src/*.ts`
   makes the server process exit; the client spawns a fresh process with
   the new code (restart limit raised while watch is on).

## Configuration

| Key | Description |
|-----|-------------|
| `mdtc.lsp.watch` | Dev mode: auto-restart the TS server when its source changes |
| `mdtc.lsp.trace` | LSP trace level: `off` / `messages` / `verbose` |