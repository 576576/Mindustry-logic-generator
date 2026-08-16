![MdtC](../../assets/sprites/icon_head.png)

# MdtC

<a href="../zh/README.md">中文 (简体)</a> &nbsp;|&nbsp; 中文 (繁體) &nbsp;|&nbsp; <a href="../en/README.md">English</a>

![JDK](https://img.shields.io/badge/JDK-25-red) ![Version](https://img.shields.io/badge/version-9374705-informational) ![Mindustry](https://img.shields.io/badge/Mindustry-v159-orange) ![License](https://img.shields.io/badge/license-GPL--3.0-green)

## 簡介

一種類似 C 的高階語言，可編譯為 Mindustry 原生邏輯組合語言，支援完整的雙向轉換。提供**模組**和 **CLI** 工具兩種使用方式。

## 快速開始

### Mindustry 模組

#### 從模組瀏覽器

1. 在 Mindustry 中選擇 **模組** → **模組瀏覽器**

2. 搜尋 **MdtC** → **安裝**

3. 開啟任意處理器 → 點擊底部工具列的 **MdtC**

4. 在左側編輯 `.mdtc`，右側預覽 `.mdtcode`

#### 手動匯入

1. [下載](https://github.com/576576/mdtC/releases) `mdtc-{version}-Desktop.jar`

2. 在 Mindustry 中選擇 **模組** → **匯入模組** → 選擇 JAR 檔案

3. 之後步驟與上方 3–4 相同

### 命令列使用

```bash
java -jar mdtc-{version}-Desktop.jar [options]
```

> 詳見 [CLI 參考](CLI.md)。

---

## 特性

特性 | 描述
--- | ---
🔄 **完整雙向轉換** | 編譯 `.mdtc` → `.mdtcode` 並支援無損反編譯
🖥️ **雙欄編輯器** | 在 Mindustry 遊戲內邊編輯邊預覽
🌍 **多語言** | 自動偵測遊戲語言
🧩 **豐富語法** | 函數、巨集、命名參數、可匯入的函式庫

## 文件

文件 | 內容
----------|---------
[語法指南](SYNTAX.md) | 完整語法說明與範例
[CLI 參考](CLI.md) | 命令列參數與用法
[模組指南](MOD.md) | 遊戲內模組功能與設定
[建置指南](BUILDING.md) | 如何從原始碼建置
[指令規範](../instructions/README.md) | 內建指令唯一規範(文件驅動開發契約)
[i18n 狀態](../i18n.md) | 翻譯覆蓋率與貢獻指南

---

## 多語言

MdtC 自動偵測遊戲語言以適配遊戲內 UI。詳見 [i18n.md](../i18n.md)。

### 貢獻翻譯

1. 複製 `assets/bundles/en.properties` 為 `assets/bundles/[langCode]-[regionCode].properties`

2. 翻譯所有值 — **鍵名不可更改**

3. （可選）複製 `assets/docs/en.json` 為 `assets/docs/[langCode].json` 並翻譯文件

4. 提交 Pull Request — CI 合併後自動更新 [docs/i18n.md](../i18n.md)

---

## 專案結構

```text
src/main/java/cn/sumitm/mdtc/
├── cli/               CLI entry (Main, CliHelper)
├── compiler/               Compiler & decompiler (CodeCompiler, CodeDecompiler, EmitCtx)
├── core/               BuiltinEngine, BuiltinDomain, Constants, Utils, WrappedList
├── formatter/               Code formatter (CodeFormatter)
├── mod/               Mindustry mod (ModInterface, LogicEditorDialog, I18n)
├── resources/               Bundled assets(含 builtins/gen)
├── builtins/               內建指令唯一事實來源(每個指令一個 .ts 檔)
└── tools/               sync-js.mjs(.ts → gen/builtins.js 建置工具)
```

## 授權條款

本專案使用 [GNU General Public License v3.0](LICENSE) 授權。

Mindustry is © Anuken, licensed under GPLv3. This mod is not affiliated with or endorsed by Anuken.
