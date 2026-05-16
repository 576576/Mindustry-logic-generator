# Mod Guide

> [← Back to README](../README.md)

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
┌──────────────────────────────────────┐
│ [Compile] [Decompile] [Format] [Clear]  [Settings] │
├──────────────┬───────────────────────┤
│ Source .mdtc │  Output .mdtcode      │
│ (editable)   │  (editable)           │
│              │                       │
│    [Copy]    │     [Copy]            │
├──────────────┴───────────────────────┤
│ Status: Ready                        │
└──────────────────────────────────────┘
```

### Workflow

- **From Processor** (default): Current processor code loads into right pane → auto-decompiles to left pane
- **Compile**: Edit left `.mdtc` → click Compile → result appears in right `.mdtcode`
- **Decompile**: Paste `.mdtcode` into right → click Decompile → result in left
- **Copy to Processor**: Copy right pane → paste back into Mindustry's editor via Edit > Import

## Settings

Click **Settings** to configure:

| Setting | Default | Description |
|---------|---------|-------------|
| Auto-load on open | ON | Auto-import processor code when opening |
| Auto Format | ON | Auto-format code after decompile |
| Indent Width | 2 | Spaces per indentation level (1–8) |

Settings persist across game sessions via Mindustry's settings system.

## Language Support

Detects game language automatically:

- **English** — default
- **简体中文** — when game language is set to Chinese

To add a new language, create `assets/bundles/bundle_xx_XX.properties` and translate the keys.
