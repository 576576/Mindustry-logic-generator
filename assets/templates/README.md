<p align="center">
  <img src="{{icon_prefix}}assets/sprites/icon.png" width="64" alt="MdtC">
</p>

<h1 align="center">MdtC</h1>

<p align="center">
{{languages}}
</p>

<p align="center">
  <img src="https://img.shields.io/badge/JDK-25-red" alt="JDK">
  <img alt="version" src="https://img.shields.io/badge/version-{{VERSION}}-informational" />
  <img src="https://img.shields.io/badge/Mindustry-v157-orange" alt="Mindustry">
  <img src="https://img.shields.io/badge/license-GPL--3.0-green" alt="License">
</p>

<p align="center"><em>{{tagline}}</em></p>

---

## {{heading_quickstart}}

### {{heading_mod}}

**{{mod_browser_title}}**

1. {{mod_browser_step1}}

2. {{mod_browser_step2}}

3. {{mod_browser_step3}}

4. {{mod_browser_step4}}

**{{manual_import_title}}**

1. {{manual_import_step1}}

2. {{manual_import_step2}}

3. {{manual_import_step3}}

### {{heading_cli}}

```bash
java -jar mdtc-{{VERSION}}-Cli.jar [options]
```

> {{cli_ref}}

---

## {{heading_features}}

| | |
|---|---|
| 🔄 **{{feat_roundtrip}}** | {{feat_roundtrip_desc}} |
| 🖥️ **{{feat_editor}}** | {{feat_editor_desc}} |
| 🌍 **{{feat_i18n}}** | {{feat_i18n_desc}} |
| 🧩 **{{feat_syntax}}** | {{feat_syntax_desc}} |

## {{heading_docs}}

| {{doc_col_doc}} | {{doc_col_content}} |
|----------|---------|
| [{{doc_syntax}}](docs/SYNTAX.md) | {{doc_syntax_desc}} |
| [{{doc_cli}}](docs/CLI.md) | {{doc_cli_desc}} |
| [{{doc_mod}}](docs/MOD.md) | {{doc_mod_desc}} |
| [{{doc_building}}](docs/BUILDING.md) | {{doc_building_desc}} |
| [{{doc_i18n}}](docs/i18n.md) | {{doc_i18n_desc}} |

---

## {{heading_i18n}}

{{i18n_desc}}

### {{heading_contributing}}

1. {{contrib_step1}}

2. {{contrib_step2}}

3. {{contrib_step3}}

4. {{contrib_step4}}

---

## {{heading_structure}}

```
src/main/java/cn/sumitm/mdtc/
├── cli/           CLI entry (Main, CliHelper)
├── compiler/      Compiler & decompiler (CodeCompiler, CodeDecompiler, LangRegistry)
├── core/          Data structures & utilities (Constants, Utils, LangBuiltins, WrappedList)
├── formatter/     Code formatter (CodeFormatter)
├── mod/           Mindustry mod (ModInterface, LogicEditorDialog, I18n)
└── resources/     Bundled assets
```

## {{heading_license}}

{{license_text}}

Mindustry is © Anuken, licensed under GPLv3. This mod is not affiliated with or endorsed by Anuken.
