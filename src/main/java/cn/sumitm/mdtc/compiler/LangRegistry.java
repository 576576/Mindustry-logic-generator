package cn.sumitm.mdtc.compiler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;

import cn.sumitm.mdtc.core.Constants;
import cn.sumitm.mdtc.core.LangBuiltins.Building;
import cn.sumitm.mdtc.core.LangBuiltins.Chain;
import cn.sumitm.mdtc.core.LangBuiltins.Control;
import static cn.sumitm.mdtc.core.LangBuiltins.JUMP_DEFAULT;
import cn.sumitm.mdtc.core.LangBuiltins.Locate;
import cn.sumitm.mdtc.core.LangBuiltins.Lookup;
import static cn.sumitm.mdtc.core.LangBuiltins.RADAR_TARGET;
import cn.sumitm.mdtc.core.LangBuiltins.RadarSort;
import static cn.sumitm.mdtc.core.LangBuiltins.VAL_0;
import static cn.sumitm.mdtc.core.LangBuiltins.VAL_1;
import static cn.sumitm.mdtc.core.LangBuiltins.VAL_AT;
import static cn.sumitm.mdtc.core.LangBuiltins.VAL_NUL;
import cn.sumitm.mdtc.core.Utils;
import cn.sumitm.mdtc.core.WrappedList;
import cn.sumitm.mdtc.core.stdCodeStream;

public final class LangRegistry {

    private LangRegistry() {}

    @FunctionalInterface
    public interface SubCompiler {
        stdCodeStream compile(String expr, int[] ref);
    }

    // ==================== Ctrl ====================

    public static Map<String, Function<String, String>> ctrlHandlers(
            ArrayList<String> bashList, int[] ref, SubCompiler compile) {

        Map<String, Function<String, String>> h = new LinkedHashMap<>();

        h.put("print(",    s -> String.format("print %s", s));
        h.put("printchar(", s -> String.format("printchar %s", s));
        h.put("format(",   s -> String.format("format %s", s));
        h.put("wait(",     s -> String.format("wait %s", s));
        h.put("stop(",     _ -> "stop");
        h.put("end(",      _ -> "end");

        h.put("ubind(", s -> String.format("ubind %s", s));
        h.put("uctrl(", s -> String.format("ucontrol %s", Utils.padParams(6, s)));

        h.put("ushoot(", s -> {
            var m = Utils.getChainParams(s);
            String shoot = m.getOrDefault(Chain.MAIN.key(), VAL_1);
            String tgt   = m.getOrDefault(Chain.TARGET.key(), VAL_AT);
            return String.format("ucontrol %s %s",
                tgt.contains(",") ? "target" : "targetp",
                Utils.padParams(5, tgt.replace(',', ' '), shoot));
        });

        h.put("draw(", s -> String.format("draw %s", Utils.padParams(7, s)));

        h.put("jump(", s -> {
            var m = Utils.getChainParams(s);
            String target   = m.getOrDefault(Chain.MAIN.key(), JUMP_DEFAULT);
            String whenExpr = m.getOrDefault(Chain.WHEN.key(), "");
            var splitList   = Utils.stringSplit(whenExpr);
            String condition;

            if (splitList.size() > 1) {
                var bc = compile.compile(whenExpr, ref);
                if (!bc.bash().isEmpty()) {
                    ref[0] = bc.stat();
                    condition = Utils.getCondition(bc.bash().getLast());
                    if (!condition.equals(Constants.JumpCondition.TRUE.id()))
                        bc.bash().removeLast();
                    else if (!bc.expr().isEmpty())
                        condition = String.format("notEqual %s %s", bc.expr(), VAL_0);
                    bashList.addAll(bc.bash());
                } else {
                    condition = Constants.JumpCondition.TRUE.id();
                }
            } else if (splitList.size() == 1) {
                condition = switch (splitList.getFirst()) {
                    case "always" -> Constants.JumpCondition.TRUE.id();
                    case "never"  -> Constants.JumpCondition.FALSE.id();
                    default -> String.format("notEqual %s %s", whenExpr, VAL_0);
                };
            } else {
                condition = Constants.JumpCondition.TRUE.id();
            }
            return String.format("jump %s %s", target, condition);
        });

        h.put("jump2(", s -> {
            s = Utils.stringSplit(s).size() > 1
                ? String.format("@counter=@counter%s", s)
                : String.format("@counter=%s", s);
            bashList.addAll(compile.compile(s, ref).bash());
            return "";
        });

        h.put("printf(", s -> {
            var w = WrappedList.of(Utils.bracketPartSplit(s));
            if (w.size() < 2) return String.format("print %s", s);
            bashList.add(String.format("print %s", w.get(0)));
            IntStream.range(1, w.size())
                .mapToObj(i -> String.format("format %s", w.get(i)))
                .forEach(bashList::add);
            return "";
        });

        h.put("tag(", s -> "::" + s);
        h.put("raw(", s -> s);

        return h;
    }

    // ==================== Dot Ctrl ====================

    public static Map<String, Function<String, String>> dotCtrlHandlers(
            ArrayList<String> bashList, String[] blockRef, int[] ref) {

        Map<String, Function<String, String>> h = new LinkedHashMap<>();

        h.put(".ctrl(", s -> {
            var w = WrappedList.of(Utils.bracketPartSplit(s));
            String fmt = String.format("control%s%s", w.getOrDefault(0, Control.values()[0].id()), blockRef[0])
                + Utils.padParams(4, w.getOrDefault(1, ""));
            return fmt;
        });

        h.put(".enable(", s -> String.format("control enabled %s %s", blockRef[0], Utils.padParams(4, s)));
        h.put(".config(", s -> String.format("control config %s %s", blockRef[0], Utils.padParams(4, s)));
        h.put(".color(",  s -> String.format("control color %s %s",  blockRef[0], Utils.padParams(4, s)));

        h.put(".shoot(", s -> {
            var m = Utils.getChainParams(s);
            String shoot = m.getOrDefault(Chain.MAIN.key(), VAL_1);
            String tgt   = m.getOrDefault(Chain.TARGET.key(), VAL_AT);
            return String.format("control %s %s %s",
                tgt.contains(",") ? "shoot" : "shootp", blockRef[0],
                Utils.padParams(4, tgt.replace(',', ' '), shoot));
        });

        h.put(".ulocate(", s -> {
            var m = Utils.getChainParams(s);
            String type = m.getOrDefault(Chain.MAIN.key(),     Locate.values()[0].id());
            String ore  = m.getOrDefault(Chain.ORE.key(),      VAL_0);
            String bld  = m.getOrDefault(Chain.BUILDING.key(), Building.values()[0].id());
            String en   = m.getOrDefault(Chain.ENEMY.key(),    VAL_0);
            if (Building.contains(type)) { bld = type; type = Locate.BUILDING.id(); }
            return String.format("ulocate %s %s %s %s %s.x %s.y %s.f %s",
                type, bld, en, ore, blockRef[0], blockRef[0], blockRef[0], blockRef[0]);
        });

        h.put(".unpack(", s -> String.format("unpackcolor %s %s", Utils.padParams(4, s), blockRef[0]));
        h.put(".pflush(", _ -> String.format("printflush %s", blockRef[0]));
        h.put(".dflush(", _ -> String.format("drawflush %s", blockRef[0]));
        h.put(".write(", s -> {
            var w = WrappedList.of(Utils.bracketPartSplit(s));
            return String.format("write %s %s %s",
                w.getOrDefault(0, VAL_NUL), blockRef[0], w.getOrDefault(1, VAL_0));
        });

        return h;
    }

    // ==================== Dot ====================

    public static Map<String, Function<String, String>> dotHandlers(
            ArrayList<String> bashList, String[] blockRef, int[] ref, SubCompiler compile) {

        Map<String, Function<String, String>> h = new LinkedHashMap<>();

        h.put(".sensor(", s -> String.format("sensor mid.%d %s %s", ref[0], blockRef[0], s));
        h.put(".read(",   s -> String.format("read mid.%d %s %s",   ref[0], blockRef[0], s));

        h.put(".orElse(", s -> {
            var m = Utils.getChainParams(s);
            String target   = m.getOrDefault(Chain.MAIN.key(), VAL_0);
            String whenExpr = m.getOrDefault(Chain.WHEN.key(), "");
            var splitList   = Utils.stringSplit(whenExpr);
            String condition;

            if (splitList.size() > 1) {
                var bc = compile.compile(whenExpr, ref);
                if (!bc.bash().isEmpty()) {
                    ref[0] = bc.stat();
                    condition = Utils.getCondition(bc.bash().getLast());
                    if (!condition.equals(Constants.JumpCondition.TRUE.id()))
                        bc.bash().removeLast();
                    else if (!bc.expr().isEmpty())
                        condition = String.format("notEqual %s %s", bc.expr(), VAL_0);
                    bashList.addAll(bc.bash());
                } else {
                    condition = Constants.JumpCondition.TRUE.id();
                }
            } else if (splitList.size() == 1) {
                condition = String.format("notEqual %s %s", whenExpr, VAL_0);
            } else {
                condition = Constants.JumpCondition.TRUE.id();
            }
            return String.format("select mid.%d %s %s %s",
                ref[0], Utils.reverseCondition(condition), blockRef[0], target);
        });

        return h;
    }

    // ==================== Front High ====================

    public static Map<String, Function<String, String>> frontHandlersHigh(int[] ref) {
        Map<String, Function<String, String>> h = new LinkedHashMap<>();

        h.put("not(",   s -> String.format("op not mid.%d %s 0",   ref[0], s));
        h.put("abs(",   s -> String.format("op abs mid.%d %s 0",   ref[0], s));
        h.put("sign(",  s -> String.format("op sign mid.%d %s 0",  ref[0], s));
        h.put("floor(", s -> String.format("op floor mid.%d %s 0", ref[0], s));
        h.put("ceil(",  s -> String.format("op ceil mid.%d %s 0",  ref[0], s));
        h.put("round(", s -> String.format("op round mid.%d %s 0", ref[0], s));
        h.put("sqrt(",  s -> String.format("op sqrt mid.%d %s 0",  ref[0], s));
        h.put("rand(",  s -> String.format("op rand mid.%d %s 0",  ref[0], s));
        h.put("asin(",  s -> String.format("op asin mid.%d %s 0",  ref[0], s));
        h.put("acos(",  s -> String.format("op acos mid.%d %s 0",  ref[0], s));
        h.put("atan(",  s -> String.format("op atan mid.%d %s 0",  ref[0], s));
        h.put("ln(",    s -> String.format("op log mid.%d %s 0",   ref[0], s));
        h.put("lg(",    s -> String.format("op log10 mid.%d %s 0", ref[0], s));
        h.put("lb(",    s -> String.format("op logn mid.%d %s 2",  ref[0], s));

        h.put("max(",      s -> { var w = WrappedList.of(Utils.bracketPartSplit(s)); return String.format("op max mid.%d %s %s",       ref[0], w.get(0), w.get(1)); });
        h.put("min(",      s -> { var w = WrappedList.of(Utils.bracketPartSplit(s)); return String.format("op min mid.%d %s %s",       ref[0], w.get(0), w.get(1)); });
        h.put("len(",      s -> { var w = WrappedList.of(Utils.bracketPartSplit(s)); return String.format("op len mid.%d %s %s",       ref[0], w.get(0), w.get(1)); });
        h.put("angle(",    s -> { var w = WrappedList.of(Utils.bracketPartSplit(s)); return String.format("op angle mid.%d %s %s",     ref[0], w.get(0), w.get(1)); });
        h.put("angleDiff(",s -> { var w = WrappedList.of(Utils.bracketPartSplit(s)); return String.format("op angleDiff mid.%d %s %s", ref[0], w.get(0), w.get(1)); });
        h.put("noise(",    s -> { var w = WrappedList.of(Utils.bracketPartSplit(s)); return String.format("op noise mid.%d %s %s",     ref[0], w.get(0), w.get(1)); });
        h.put("log(",      s -> { var w = WrappedList.of(Utils.bracketPartSplit(s)); return String.format("op logn mid.%d %s %s",      ref[0], w.get(1), w.get(0)); });

        h.put("link(", s -> String.format("getlink mid.%d %s", ref[0], s));
        h.put("lookup(", s -> {
            var w = WrappedList.of(Utils.bracketPartSplit(s));
            return String.format("lookup %s mid.%d %s",
                w.getOrDefault(0, Lookup.values()[0].id()), ref[0],
                w.getOrDefault(w.size() - 1, VAL_0));
        });
        h.put("block(", s  -> String.format("lookup block mid.%d %s",  ref[0], s));
        h.put("unit(", s   -> String.format("lookup unit mid.%d %s",   ref[0], s));
        h.put("item(", s   -> String.format("lookup item mid.%d %s",   ref[0], s));
        h.put("liquid(", s -> String.format("lookup liquid mid.%d %s", ref[0], s));
        h.put("team(", s   -> String.format("lookup team mid.%d %s",   ref[0], s));
        h.put("pack(", s   -> String.format("packcolor mid.%d %s",     ref[0], Utils.padParams(4, s)));

        h.put("uradar(", s -> {
            var m = Utils.getChainParams(s);
            return String.format("uradar %s %s %s %s mid.%d",
                Utils.padParams("any", 3, m.getOrDefault(Chain.TARGET.key(), RADAR_TARGET)),
                m.getOrDefault(Chain.SORT.key(), RadarSort.values()[0].id()),
                VAL_0, m.getOrDefault(Chain.ORDER.key(), VAL_1), ref[0]);
        });

        return h;
    }

    // ==================== Front Low ====================

    public static Map<String, Function<String, String>> frontHandlersLow(int[] ref) {
        Map<String, Function<String, String>> h = new LinkedHashMap<>();

        h.put("sin(", s -> String.format("op sin mid.%d %s", ref[0], s));
        h.put("cos(", s -> String.format("op cos mid.%d %s", ref[0], s));
        h.put("tan(", s -> String.format("op tan mid.%d %s", ref[0], s));

        h.put("radar(", s -> {
            var m = Utils.getChainParams(s);
            return String.format("radar %s %s %s %s mid.%d",
                Utils.padParams("any", 3, m.getOrDefault(Chain.TARGET.key(), RADAR_TARGET)),
                m.getOrDefault(Chain.SORT.key(), RadarSort.values()[0].id()),
                m.getOrDefault(Chain.MAIN.key(), VAL_AT),
                m.getOrDefault(Chain.ORDER.key(), VAL_1), ref[0]);
        });

        return h;
    }
}
