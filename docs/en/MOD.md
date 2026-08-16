# Mod Guide

> [← Back to README](../../README.md)

## Installation

1. Download `mdtc-[version]-Desktop.jar` from [Releases](https://github.com/576576/mdtC/releases)
2. In Mindustry, go to **Mods** → **Import Mod** → select the JAR file
3. Launch Mindustry — the mod loads automatically

## Usage

1. Open any **Micro Processor** or **Logic Processor** block
2. In the processor code dialog, a new **MdtC** button appears in the bottom toolbar
3. Click to open the MdtC dual-pane editor

### Editor Layout

```
┌──────────────────────────────────────────┐
│  [Compile] [Decompile] [Format]   [Settings] │
├───────────────────┬──────────────────────┤
│  Source .mdtc     │  Output .mdtcode     │
│  [Import/Copy/Clear] │  [Import/Copy/Clear] │
│  (editable)       │  (editable)          │
├───────────────────┴──────────────────────┤
│  Status: Ready                           │
└──────────────────────────────────────────┘
```

### Workflow

- **From Processor** (default): Current processor code loads into right pane → auto-decompiles to left pane
- **Compile**: Edit left `.mdtc` → click Compile → result appears in right `.mdtcode`
- **Decompile**: Paste `.mdtcode` into right → click Decompile → result in left
- **Copy to Processor**: Copy right pane → paste back into Mindustry's editor via Edit > Import
- **Import**: Copy file content to clipboard → click an Import button on the pane header:
  - **Import Src** (left header) → paste `.mdtc` source into left pane
  - **Import Lib** (left header) → append `.libmdtc` library to left pane
  - **Import Raw** (right header) → paste `.mdtcode` native code into right pane

## Settings

Click **Settings** to configure:

| Setting | Default | Description |
|---------|---------|-------------|
| Auto-load on open | ON | Auto-import processor code when opening |
| Auto Format | ON | Auto-format code after decompile |
| Indent Width | 2 | Spaces per indentation level (1–8) |

Settings persist across game sessions via Mindustry's settings system.

## Language Support

Detects game language automatically. See [i18n.md](../i18n.md) for supported languages and contribution guide.
