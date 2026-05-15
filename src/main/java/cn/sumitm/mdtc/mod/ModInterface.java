package cn.sumitm.mdtc.mod;

import arc.Events;
import arc.util.Log;
import arc.util.Time;
import cn.sumitm.mdtc.mod.ui.LogicEditorDialog;
import mindustry.Vars;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.mod.Mod;

public class ModInterface extends Mod {

    public ModInterface() {
        Log.info("MdtC Mod loaded.");

        Events.on(ClientLoadEvent.class, e -> {
            I18n.init();
            Time.runTask(10f, () -> {
                // 在处理器代码编辑菜单中添加 MdtC 按钮
                Vars.ui.logic.shown(() -> {
                    String currentCode = Vars.ui.logic.canvas.save();
                    Vars.ui.logic.buttons.button(I18n.get("mdtc.button"), () -> {
                        new LogicEditorDialog(currentCode).show();
                    }).size(160f, 64f);
                });
            });
        });
    }

    @Override
    public void loadContent() {
        Log.info("MdtC mod content initialized.");
    }
}
