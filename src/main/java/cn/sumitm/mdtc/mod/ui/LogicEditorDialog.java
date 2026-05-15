package cn.sumitm.mdtc.mod.ui;

import arc.Core;
import arc.scene.ui.CheckBox;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextArea;
import arc.util.Log;
import cn.sumitm.mdtc.compiler.CodeCompiler;
import cn.sumitm.mdtc.compiler.CodeDecompiler;
import cn.sumitm.mdtc.formatter.CodeFormatter;
import cn.sumitm.mdtc.mod.I18n;
import mindustry.ui.dialogs.BaseDialog;

public class LogicEditorDialog extends BaseDialog {

    private TextArea sourceArea;
    private TextArea outputArea;
    private Label statusLabel;
    private boolean autoFormat;
    private boolean autoLoad;

    private static final float BTN_WIDTH = 130f;
    private static final float BTN_HEIGHT = 40f;
    private static final float SET_BTN_WIDTH = 100f;
    private static final float CP_BTN_WIDTH = 80f;
    private static final float PAD = 6f;

    public LogicEditorDialog() {
        this("");
    }

    public LogicEditorDialog(String initialCode) {
        super(I18n.get("mdtc.title"));
        loadSettings();
        setupUI(initialCode);
        addCloseButton();
    }

    private void loadSettings() {
        autoFormat = Core.settings.getBool("mdtc.autoformat", true);
        autoLoad = Core.settings.getBool("mdtc.autoload", true);
    }

    private void setupUI(String initialCode) {
        cont.table(toolbar -> {
            toolbar.defaults().size(BTN_WIDTH, BTN_HEIGHT).pad(PAD);

            toolbar.button(I18n.get("mdtc.compile"), () -> compile());
            toolbar.button(I18n.get("mdtc.decompile"), this::doDecompile);
            toolbar.button(I18n.get("mdtc.format"), () -> doFormat());
            toolbar.button(I18n.get("mdtc.clear"), () -> clearEditor());

            toolbar.add().growX();
            toolbar.button(I18n.get("mdtc.settings"), this::showSettings)
                .size(SET_BTN_WIDTH, BTN_HEIGHT).pad(PAD);
        }).growX().pad(PAD).row();

        cont.table(panes -> {
            panes.defaults().growY().padTop(PAD).padBottom(PAD);

            panes.table(left -> {
                left.table(header -> {
                    header.add(I18n.get("mdtc.source")).padBottom(4f).left().growX();
                    header.button(I18n.get("mdtc.copy"), () -> {
                        Core.app.setClipboardText(sourceArea.getText());
                        statusLabel.setText(I18n.get("mdtc.source.copied"));
                    }).size(CP_BTN_WIDTH, BTN_HEIGHT);
                }).growX().row();
                sourceArea = new TextArea("");
                sourceArea.setMaxLength(100000);
                ScrollPane sp = new ScrollPane(sourceArea);
                sp.setScrollingDisabled(false, false);
                sp.setFadeScrollBars(false);
                sp.setCancelTouchFocus(true);
                left.add(sp).grow().fill();
            }).grow().fill().padLeft(PAD * 3).padRight(PAD * 2);

            panes.table(right -> {
                right.table(header -> {
                    header.add(I18n.get("mdtc.output")).padBottom(4f).left().growX();
                    header.button(I18n.get("mdtc.copy"), () -> {
                        Core.app.setClipboardText(outputArea.getText());
                        statusLabel.setText(I18n.get("mdtc.output.copied"));
                    }).size(CP_BTN_WIDTH, BTN_HEIGHT);
                }).growX().row();
                outputArea = new TextArea("");
                outputArea.setMaxLength(100000);
                ScrollPane sp = new ScrollPane(outputArea);
                sp.setScrollingDisabled(false, false);
                sp.setFadeScrollBars(false);
                sp.setCancelTouchFocus(true);
                right.add(sp).grow().fill();
            }).grow().fill().padLeft(PAD * 2).padRight(PAD * 3);
        }).grow().fill().minHeight(400f).row();

        cont.table(statusBar -> {
            statusLabel = statusBar.add(I18n.get("mdtc.ready")).growX().left().get();
        }).growX().pad(PAD);

        if (autoLoad && initialCode != null && !initialCode.isBlank()) {
            outputArea.setText(initialCode);
            doDecompile();
        }
    }

    private void showSettings() {
        BaseDialog dlg = new BaseDialog(I18n.get("mdtc.settings"));
        dlg.addCloseButton();

        dlg.cont.pane(p -> {
            p.margin(14f);

            CheckBox autoLoadCb = new CheckBox(I18n.get("mdtc.settings.autoload"));
            autoLoadCb.setChecked(autoLoad);
            p.add(autoLoadCb).left().padBottom(10f).row();

            CheckBox autoFmtCb = new CheckBox(I18n.get("mdtc.settings.autoformat"));
            autoFmtCb.setChecked(autoFormat);
            p.add(autoFmtCb).left().padBottom(6f).row();

            autoFmtCb.changed(() -> autoFormat = autoFmtCb.isChecked());
            autoLoadCb.changed(() -> autoLoad = autoLoadCb.isChecked());

            dlg.hidden(() -> persistSettings());
        });

        dlg.show();
    }

    private void persistSettings() {
        Core.settings.put("mdtc.autoformat", autoFormat);
        Core.settings.put("mdtc.autoload", autoLoad);
        Core.settings.forceSave();
    }

    private void compile() {
        String source = sourceArea.getText();
        if (source == null || source.isBlank()) {
            statusLabel.setText("[red]" + I18n.get("mdtc.error.source.empty"));
            return;
        }
        try {
            String result = CodeCompiler.compile(source);
            outputArea.setText(result);
            statusLabel.setText("[green]" + I18n.format("mdtc.compiled",
                result.split("\n").length));
        } catch (Exception ex) {
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
            if (autoFormat) {
                result = CodeFormatter.format(result);
            }
            result = result.replace("\t", "  ");
            sourceArea.setText(result);
            statusLabel.setText("[green]" + I18n.format(
                autoFormat ? "mdtc.decompiled.fmt" : "mdtc.decompiled",
                result.split("\n").length));
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
                result = result.replace("\t", "  ");
                sourceArea.setText(result);
                statusLabel.setText("[green]" + I18n.format("mdtc.formatted",
                    result.split("\n").length));
            }
        } catch (Exception ex) {
            statusLabel.setText("[red]Format error: " + ex.getMessage());
            Log.err(ex);
        }
    }

    private void clearEditor() {
        sourceArea.setText("");
        outputArea.setText("");
        statusLabel.setText(I18n.get("mdtc.cleared"));
    }
}
