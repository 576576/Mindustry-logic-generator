package cn.sumitm.mdtc.core;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 运行期领域数据:在模组模式下把游戏实际内容名合并进内置分类集合。
 * 静态分类目录的单一事实源在 builtins/domain.ts(经 BuiltinEngine 加载);
 * 本类只负责运行期合并与查询。
 */
public final class BuiltinDomain {

    private BuiltinDomain() {}

    private static volatile boolean seeded = false;
    private static final Set<String> buildingNames = new LinkedHashSet<>();
    private static final Set<String> lookupNames = new LinkedHashSet<>();

    /** 由 ModInterface 在 ClientLoadEvent 中调用(模组模式合并游戏内容) */
    public static void init() {
        ensureSeeded();
        try {
            mindustry.Vars.content.blocks().each(b -> {
                if (b.buildVisibility.visible() && !b.isHidden()) {
                    String cat = b.category.name();
                    if ("turret".equals(cat) || "crafting".equals(cat) || "production".equals(cat)
                            || "power".equals(cat) || "defense".equals(cat)) {
                        buildingNames.add(b.name);
                    }
                }
            });
            mindustry.Vars.content.blocks().each(b -> lookupNames.add(b.name));
            mindustry.Vars.content.units().each(u -> lookupNames.add(u.name));
            mindustry.Vars.content.items().each(i -> lookupNames.add(i.name));
            mindustry.Vars.content.liquids().each(l -> lookupNames.add(l.name));
        } catch (Throwable ignored) {
            // CLI 模式:Vars.content == null(mod 依赖为 compileOnly),保持静态目录
        }
    }

    /** Building 分类 contains(含模组运行期合并的游戏内容名) */
    public static boolean buildingContains(String name) {
        ensureSeeded();
        return buildingNames.contains(name);
    }

    /** Lookup 分类 contains(含模组运行期合并的游戏内容名) */
    public static boolean lookupContains(String name) {
        ensureSeeded();
        return lookupNames.contains(name);
    }

    private static void ensureSeeded() {
        if (seeded) return;
        synchronized (BuiltinDomain.class) {
            if (seeded) return;
            buildingNames.addAll(BuiltinEngine.get().buildingTypes());
            lookupNames.addAll(BuiltinEngine.get().lookupTypes());
            seeded = true;
        }
    }
}
