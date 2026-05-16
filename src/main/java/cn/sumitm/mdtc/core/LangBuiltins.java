package cn.sumitm.mdtc.core;

import java.util.*;

import mindustry.Vars;

/**
 * Mindustry 领域知识 — 以枚举集中管理游戏数据。
 * <ul>
 *   <li>每个枚举 <b>第 0 项为默认值</b></li>
 *   <li>{@link #init()} 在模组加载时调用，从 {@code Vars.content} 接入游戏实际内容</li>
 *   <li>CLI 模式下 {@code Vars.content} 为 null，自动回退到硬编码默认值</li>
 * </ul>
 */
public final class LangBuiltins {

    private LangBuiltins() {}

    /** 由 ModInterface 在 ClientLoadEvent 中调用 */
    public static void init() {
        Building.loadFromGame();
        Lookup.loadFromGame();
    }

    // ==================== 建筑分类 (ulocate) ====================

    /** ulocate 支持的可定位建筑类型。第 0 项 {@link #CORE} 为默认。 */
    public enum Building {
        CORE, STORAGE, GENERATOR, TURRET, FACTORY,
        REPAIR, BATTERY, REACTOR, DRILL, SHIELD;

        private static Set<String> allTypeNames = new LinkedHashSet<>();
        static { for (var v : values()) allTypeNames.add(v.id()); }

        public String id() {
            return switch (this) {
                case CORE      -> "core";
                case STORAGE   -> "storage";
                case GENERATOR -> "generator";
                case TURRET    -> "turret";
                case FACTORY   -> "factory";
                case REPAIR    -> "repair";
                case BATTERY   -> "battery";
                case REACTOR   -> "reactor";
                case DRILL     -> "drill";
                case SHIELD    -> "shield";
            };
        }

        public static Building of(String name) {
            for (var v : values()) if (v.id().equals(name)) return v;
            return null;
        }

        public static boolean contains(String name) {
            return allTypeNames.contains(name);
        }

        static void loadFromGame() {
            try {
                // 尝试从游戏方块的 category 属性推断 ulocate 类型
                var map = new LinkedHashMap<String, String>();
                for (var b : Vars.content.blocks()) {
                    if (b.buildVisibility.visible() && !b.isHidden()) {
                        String cat = b.category.name();
                        map.put(b.name,
                            "turret".equals(cat)     ? "turret"    :
                            "crafting".equals(cat)   ? "factory"   :
                            "production".equals(cat) ? "drill"     : // best effort
                            "power".equals(cat)      ? "generator" :
                            "defense".equals(cat)    ? "turret"    :
                            null);
                    }
                }
                // 按 id() 去重 → allTypeNames
                allTypeNames = new LinkedHashSet<>();
                for (var v : values()) allTypeNames.add(v.id());
                for (var name : map.keySet()) {
                    String type = map.get(name);
                    if (type != null) allTypeNames.add(name);
                }
            } catch (Exception ignored) {
                // CLI 模式: Vars.content == null，保持 static initializer 的硬编码值
            }
        }
    }

    // ==================== 定位模式 ====================

    public enum Locate {
        ORE, BUILDING;
        public String id() { return name().toLowerCase(); }
    }

    // ==================== 控制 ====================

    public enum Control {
        ENABLED;
        public String id() { return name().toLowerCase(); }
    }

    // ==================== 雷达 ====================

    public enum RadarSort {
        DISTANCE;
        public String id() { return name().toLowerCase(); }
    }

    // ==================== lookup 内容分类 ====================

    /** lookup 查询的内容类型。第 0 项 {@link #BLOCK} 为默认。 */
    public enum Lookup {
        BLOCK, UNIT, ITEM, LIQUID, TEAM;

        private static Set<String> names = new LinkedHashSet<>();
        static { for (var v : values()) names.add(v.id()); }

        public String id() { return name().toLowerCase(); }

        public static boolean contains(String name) { return names.contains(name); }

        static void loadFromGame() {
            names = new LinkedHashSet<>();
            for (var v : values()) names.add(v.id());
            try {
                Vars.content.blocks().each(b -> names.add(b.name));
                Vars.content.units().each(u -> names.add(u.name));
                Vars.content.items().each(i -> names.add(i.name));
                Vars.content.liquids().each(l -> names.add(l.name));
            } catch (Exception ignored) {}
        }
    }

    // ==================== 链式参数键 ====================

    public enum Chain {
        MAIN, TARGET, WHEN, ORDER, SORT, ORE, BUILDING, ENEMY;
        public String key() { return name().toLowerCase(); }
    }

    // ==================== 零星默认值 ====================

    public static final String VAL_0   = "0";
    public static final String VAL_1   = "1";
    public static final String VAL_AT  = "@this";
    public static final String VAL_NUL = "null";
    public static final String JUMP_DEFAULT = "DEFAULT";
    public static final String RADAR_TARGET = "enemy,any,any";
}
