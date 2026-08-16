package cn.sumitm.mdtc.mod.ui;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.CheckBox;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.Slider;
import arc.scene.ui.TextArea;
import arc.util.Align;
import arc.util.Log;
import cn.sumitm.mdtc.compiler.CodeCompiler;
import cn.sumitm.mdtc.compiler.CodeDecompiler;
import cn.sumitm.mdtc.formatter.CodeFormatter;
import cn.sumitm.mdtc.mod.I18n;
import mindustry.Vars;
import mindustry.ui.dialogs.BaseDialog;

public class LogicEditorDialog extends BaseDialog {

    private TextArea sourceArea;
    private TextArea outputArea;
    private Label statusLabel;
    private Label warnLabel;
    private ScrollPane warnScroll;
    private boolean autoFormat;
    private boolean autoLoad;
    private boolean autoSave;
    private int indentWidth = 2;
    private ScrollPane sourceScroll;
    private ScrollPane outputScroll;

    private static final float BTN_WIDTH = 130f;
    private static final float BTN_HEIGHT = 40f;
    private static final float IMP_BTN_WIDTH = 70f;
    private static final float SET_BTN_WIDTH = 100f;
    private static final float CP_BTN_WIDTH = 60f;
    private static final float PAD = 6f;

    public LogicEditorDialog() { this(""); }

    public LogicEditorDialog(String initialCode) {
        super(I18n.get("mdtc.title"));
        loadSettings();
        setupUI(initialCode);
        addCloseButton();

        hidden(() -> {
            if (autoSave && outputArea != null) {
                String code = outputArea.getText();
                if (code != null && !code.isBlank()) {
                    Vars.ui.logic.canvas.load(code.replace("\r\n", "\n"));
                    statusLabel.setText("[green]" + I18n.get("mdtc.exported"));
                }
            }
        });
    }

    private void loadSettings() {
        autoFormat = Core.settings.getBool("mdtc.autoformat", true);
        autoLoad = Core.settings.getBool("mdtc.autoload", true);
        autoSave = Core.settings.getBool("mdtc.autosave", true);
        indentWidth = Core.settings.getInt("mdtc.indent", 2);
    }

    private void setupUI(String initialCode) {
        cont.table(toolbar -> {
            toolbar.defaults().size(BTN_WIDTH, BTN_HEIGHT).pad(PAD);

            toolbar.button(I18n.get("mdtc.compile") + " >", () -> compile());
            toolbar.button("< " + I18n.get("mdtc.decompile"), this::doDecompile);
            toolbar.button(I18n.get("mdtc.format"), () -> doFormat());

            toolbar.add().growX();
            toolbar.button(I18n.get("mdtc.settings"), this::showSettings)
                .size(SET_BTN_WIDTH, BTN_HEIGHT).pad(PAD);
        }).growX().pad(PAD).row();

        cont.table(panes -> {
            panes.defaults().growY().padTop(PAD).padBottom(PAD);

            panes.table(left -> {
                left.table(header -> {
                    header.add(I18n.get("mdtc.source")).padBottom(4f).left().growX();
                    header.button(I18n.get("mdtc.pane.import.src"), () -> pickFile("mdtc", this::importToLeft)).size(IMP_BTN_WIDTH, BTN_HEIGHT);
                    header.button(I18n.get("mdtc.pane.import.lib"), () -> pickFile("libmdtc", this::appendLib)).size(IMP_BTN_WIDTH, BTN_HEIGHT);
                    header.button(I18n.get("mdtc.copy"), () -> {
                        Core.app.setClipboardText(sourceArea.getText());
                        statusLabel.setText(I18n.get("mdtc.source.copied"));
                    }).size(CP_BTN_WIDTH, BTN_HEIGHT);
                    header.button(I18n.get("mdtc.pane.clear"), () -> {
                        sourceArea.setText("");
                        relayoutScrolls();
                        statusLabel.setText(I18n.get("mdtc.cleared"));
                    }).size(CP_BTN_WIDTH, BTN_HEIGHT);
                }).growX().row();
                sourceArea = new TextArea("");
                sourceArea.setMaxLength(500000);
                sourceScroll = new ScrollPane(sourceArea);
                sourceScroll.setFadeScrollBars(false);
                sourceScroll.setScrollingDisabledX(true);
                left.add(sourceScroll).grow().fill();
            }).grow().fill().padLeft(PAD * 3).padRight(PAD * 2);

            panes.table(right -> {
                right.table(header -> {
                    header.add(I18n.get("mdtc.output")).padBottom(4f).left().growX();
                    header.button(I18n.get("mdtc.pane.import.raw"), () -> pickFile("mdtcode", this::importToRight)).size(IMP_BTN_WIDTH, BTN_HEIGHT);
                    header.button(I18n.get("mdtc.copy"), () -> {
                        Core.app.setClipboardText(outputArea.getText());
                        statusLabel.setText(I18n.get("mdtc.output.copied"));
                    }).size(CP_BTN_WIDTH, BTN_HEIGHT);
                    header.button(I18n.get("mdtc.pane.clear"), () -> {
                        outputArea.setText("");
                        relayoutScrolls();
                        statusLabel.setText(I18n.get("mdtc.cleared"));
                    }).size(CP_BTN_WIDTH, BTN_HEIGHT);
                }).growX().row();
                outputArea = new TextArea("");
                outputArea.setMaxLength(500000);
                outputScroll = new ScrollPane(outputArea);
                outputScroll.setFadeScrollBars(false);
                outputScroll.setScrollingDisabledX(true);
                right.add(outputScroll).grow().fill();
            }).grow().fill().padLeft(PAD * 2).padRight(PAD * 3);
        }).grow().fill().minHeight(400f).row();

        cont.table(warnBar -> {
            warnBar.add(I18n.get("mdtc.warnings")).color(Color.orange).padRight(PAD * 2).left();
            warnLabel = new Label("");
            warnLabel.setWrap(true);
            warnLabel.setAlignment(Align.topLeft);
            warnScroll = new ScrollPane(warnLabel);
            warnScroll.setFadeScrollBars(false);
            warnScroll.setScrollingDisabledX(true);
            warnBar.add(warnScroll).growX().height(2 * 20f); // 2 行高,可上下滚动
        }).growX().pad(PAD).row();

        cont.table(statusBar -> {
            statusLabel = statusBar.add(I18n.get("mdtc.ready")).growX().left().get();
        }).growX().pad(PAD);

        if (autoLoad && initialCode != null && !initialCode.isBlank()) {
            outputArea.setText(initialCode);
            doDecompile();
        }
    }

    private void pickFile(String extension, java.util.function.Consumer<String> onContent) {
        // Mindustry v159+:Platform.showFileChooser 接收 FileChooserParams
        var params = mindustry.ui.FileChooser.open(extension);
        params.submit(file -> {
            try {
                String content = file.readString();
                if (content != null && !content.isBlank()) {
                    onContent.accept(content);
                } else {
                    statusLabel.setText("[red]" + I18n.get("mdtc.import.empty"));
                }
            } catch (Exception e) {
                statusLabel.setText("[red]Read error: " + e.getMessage());
                Log.err(e);
            }
        });
        Vars.platform.showFileChooser(params);
    }

    private void importToLeft(String content) {
        content = content.replace("\r\n", "\n");
        sourceArea.setText(content);
        relayoutScrolls();
        statusLabel.setText("[green]" + I18n.format("mdtc.import.success",
            content.split("\n").length));
    }

    private void importToRight(String content) {
        content = content.replace("\r\n", "\n");
        outputArea.setText(content);
        relayoutScrolls();
        statusLabel.setText("[green]" + I18n.format("mdtc.import.success",
            content.split("\n").length));
    }

    private void appendLib(String content) {
        content = content.replace("\r\n", "\n");
        String existing = sourceArea.getText();
        sourceArea.setText(
            (existing != null && !existing.isBlank()) ? existing + "\n" + content : content);
        relayoutScrolls();
        statusLabel.setText("[green]" + I18n.format("mdtc.import.success",
            content.split("\n").length));
    }

    // ==================== Settings ====================

    private void showSettings() {
        BaseDialog dlg = new BaseDialog(I18n.get("mdtc.settings"));
        dlg.addCloseButton();

        final int[] indentVal = { indentWidth };

        dlg.cont.pane(p -> {
            p.margin(14f);

            CheckBox autoLoadCb = new CheckBox(I18n.get("mdtc.settings.autoload"));
            autoLoadCb.setChecked(autoLoad);
            p.add(autoLoadCb).left().padBottom(10f).row();

            CheckBox autoSaveCb = new CheckBox(I18n.get("mdtc.settings.autosave"));
            autoSaveCb.setChecked(autoSave);
            p.add(autoSaveCb).left().padBottom(10f).row();

            CheckBox autoFmtCb = new CheckBox(I18n.get("mdtc.settings.autoformat"));
            autoFmtCb.setChecked(autoFormat);
            p.add(autoFmtCb).left().padBottom(10f).row();

            Label indentLabel = new Label(I18n.format("mdtc.settings.indent", indentWidth));
            p.add(indentLabel).left().padTop(6f).row();
            Slider indentSlider = new Slider(1, 8, 1, false);
            indentSlider.setValue(indentWidth);
            p.table(t -> {
                t.add(indentSlider).growX().padRight(10f);
                t.add().width(40f);
            }).growX().row();

            indentSlider.changed(() -> {
                indentVal[0] = (int) indentSlider.getValue();
                indentLabel.setText(I18n.format("mdtc.settings.indent", indentVal[0]));
            });

            autoFmtCb.changed(() -> autoFormat = autoFmtCb.isChecked());
            autoLoadCb.changed(() -> autoLoad = autoLoadCb.isChecked());
            autoSaveCb.changed(() -> autoSave = autoSaveCb.isChecked());

            dlg.hidden(() -> {
                indentWidth = (int) indentVal[0];
                persistSettings();
            });
        });

        dlg.show();
    }

    private void persistSettings() {
        Core.settings.put("mdtc.autoformat", autoFormat);
        Core.settings.put("mdtc.autoload", autoLoad);
        Core.settings.put("mdtc.autosave", autoSave);
        Core.settings.put("mdtc.indent", indentWidth);
        Core.settings.forceSave();
    }

    // ==================== Compile / Decompile / Format ====================

    private void compile() {
        String source = sourceArea.getText();
        if (source == null || source.isBlank()) {
            statusLabel.setText("[red]" + I18n.get("mdtc.error.source.empty"));
            return;
        }
        try {
            String result = CodeCompiler.compile(source);
            outputArea.setText(result);
            relayoutScrolls();
            var warnings = CodeCompiler.lastWarnings;
            if (warnings.isEmpty()) {
                warnLabel.setText("");
                statusLabel.setText("[green]" + I18n.format("mdtc.compiled", result.split("\n").length));
            } else {
                warnLabel.setText("[orange]" + String.join("\n", warnings));
                statusLabel.setText("[orange]" + I18n.format("mdtc.warn.count", warnings.size()));
                for (String w : warnings) Log.warn("[MdtC] " + w);
            }
        } catch (Throwable ex) {
            statusLabel.setText("[red]" + I18n.get("mdtc.error.compile") + ex.getMessage());
            Log.err(ex);
        }
    }

    private void doDecompile() {
        String source = outputArea.getText();
        if (source == null || source.isBlank()) {
            statusLabel.setText("[red]" + I18n.get("mdtc.error.output.empty"));
            return;
        }
        try {
            String result = CodeDecompiler.decompile(source);
            if (autoFormat) result = CodeFormatter.format(result);
            result = fixIndent(result);
            sourceArea.setText(result);
            relayoutScrolls();
            statusLabel.setText("[green]" + I18n.format(
                autoFormat ? "mdtc.decompiled.fmt" : "mdtc.decompiled", result.split("\n").length));
        } catch (Exception ex) {
            statusLabel.setText("[red]" + I18n.get("mdtc.error.decompile") + ex.getMessage());
            Log.err(ex);
        }
    }

    private void doFormat() {
        String source = sourceArea.getText();
        if (source == null || source.isBlank()) {
            statusLabel.setText("[red]" + I18n.get("mdtc.error.source.empty"));
            return;
        }
        try {
            String result = CodeFormatter.format(source);
            if (result.isEmpty() || result.equals(source)) {
                statusLabel.setText("Nothing to format");
            } else {
                result = fixIndent(result);
                sourceArea.setText(result);
                relayoutScrolls();
                statusLabel.setText("[green]" + I18n.format("mdtc.formatted", result.split("\n").length));
            }
        } catch (Exception ex) {
            statusLabel.setText("[red]Format error: " + ex.getMessage());
            Log.err(ex);
        }
    }

    private void relayoutScrolls() {
        if (sourceScroll != null) {
            int lines = sourceArea.getText().split("\n", -1).length;
            sourceArea.setPrefRows(Math.max(lines + 2, 20));
            sourceScroll.layout();
        }
        if (outputScroll != null) {
            int lines = outputArea.getText().split("\n", -1).length;
            outputArea.setPrefRows(Math.max(lines + 2, 20));
            outputScroll.layout();
        }
    }

    private String fixIndent(String text) {
        return text.replace("\t", " ".repeat(indentWidth));
    }
}
