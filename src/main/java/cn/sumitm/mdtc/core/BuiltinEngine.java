package cn.sumitm.mdtc.core;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cn.sumitm.mdtc.compiler.EmitCtx;
import rhino.Context;
import rhino.Function;
import rhino.Scriptable;
import rhino.ScriptableObject;

/**
 * 内置指令引擎。
 *
 * <p>单一事实源为 <code>builtins/*.ts</code>(经 <code>tools/sync-js.mjs</code>
 * 编译为 <code>builtins/gen/builtins.js</code> 随包资源)。本类在运行期用 Rhino
 * 执行该 bundle,读取 <code>Builtins.registry</code>,并把各指令处理器包装为
 * {@link BuiltinHandler};主程序不再硬编码任何指令表。</p>
 *
 * <p>模组模式下游戏自带 Rhino(<code>rhino.*</code>);CLI 模式下由构建任务
 * <code>extractRhino</code> 从 Mindustry JAR 抽取同款类打进 fat JAR。</p>
 */
public final class BuiltinEngine {

    private static final String BUNDLE_RESOURCE = "builtins/gen/builtins.js";

    private static volatile BuiltinEngine instance;

    private final Scriptable scope;
    private final Scriptable registry;

    // ---- 指令处理器注册表 ----
    private final Map<String, BuiltinHandler> ctrl = new LinkedHashMap<>();
    private final Map<String, BuiltinHandler> dotCtrl = new LinkedHashMap<>();
    private final Map<String, BuiltinHandler> dot = new LinkedHashMap<>();
    private final Map<String, BuiltinHandler> frontHigh = new LinkedHashMap<>();
    private final Map<String, BuiltinHandler> frontLow = new LinkedHashMap<>();
    private final Map<String, BuiltinHandler> decompile = new LinkedHashMap<>();

    // ---- 运算符表 ----
    private final List<String> operatorValues = new ArrayList<>();
    private final Map<String, String> midOpKeysMap = new LinkedHashMap<>();
    private final Map<String, String> midOpValueMap = new LinkedHashMap<>();
    private final Map<String, Integer> midOpPriorityMap = new LinkedHashMap<>();
    private final Map<String, String> operatorAliasMap = new LinkedHashMap<>();
    private final Map<String, String> operatorReverseMap = new LinkedHashMap<>();
    private final Map<String, Integer> operatorOffsetMap = new LinkedHashMap<>();
    private String subOperatorValue = ".-";

    // ---- 指令码表 ----
    private final List<String> ctrlCodes = new ArrayList<>();
    private final List<String> dotCtrlCodes = new ArrayList<>();
    private final List<String> dotCodes = new ArrayList<>();
    private final List<String> dotCodesAll = new ArrayList<>();
    private final List<String> dotOpReduced = new ArrayList<>();

    // ---- 链式键声明表(供检查与测试) ----
    private final Map<String, List<String>> chainTable = new LinkedHashMap<>();

    // ---- 领域 ----
    private final List<String> buildingTypes = new ArrayList<>();
    private final List<String> locateTypes = new ArrayList<>();
    private final List<String> lookupTypes = new ArrayList<>();
    private final List<String> chainKeys = new ArrayList<>();
    private final Map<String, String> domainConstants = new LinkedHashMap<>();

    private BuiltinEngine() {
        Context cx = Context.enter();
        try {
            scope = cx.initStandardObjects();
            String js = readResource(BUNDLE_RESOURCE);
            cx.evaluateString(scope, js, BUNDLE_RESOURCE, 1);
            Object builtins = scope.get("Builtins", scope);
            if (!(builtins instanceof Scriptable bs)) {
                throw new IllegalStateException("builtins bundle broken: no Builtins namespace in " + BUNDLE_RESOURCE);
            }
            Object reg = ScriptableObject.getProperty(bs, "registry");
            if (!(reg instanceof Scriptable rs)) {
                throw new IllegalStateException("builtins bundle broken: no Builtins.registry in " + BUNDLE_RESOURCE);
            }
            registry = rs;
            loadHandlers(registry, "ctrl", ctrl);
            loadHandlers(registry, "dotCtrl", dotCtrl);
            loadHandlers(registry, "dot", dot);
            loadHandlers(registry, "frontHigh", frontHigh);
            loadHandlers(registry, "frontLow", frontLow);
            loadHandlers(registry, "decompile", decompile);

            Scriptable ops = objectProp(registry, "operators");
            loadOperators(ops);
            Scriptable codes = objectProp(registry, "codes");
            loadCodes(codes);
            Scriptable domain = objectProp(registry, "domain");
            loadDomain(domain);
            Scriptable chain = objectProp(registry, "chain");
            loadChainTable(chain);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to initialize BuiltinEngine", e);
        } finally {
            Context.exit();
        }
    }

    /** 获取引擎单例(懒加载,线程安全) */
    public static BuiltinEngine get() {
        if (instance == null) {
            synchronized (BuiltinEngine.class) {
                if (instance == null) instance = new BuiltinEngine();
            }
        }
        return instance;
    }

    /** 仅供测试重置引擎(如替换 bundle 后重新加载) */
    public static void reset() {
        synchronized (BuiltinEngine.class) {
            instance = null;
        }
    }

    /** 顶层 scope(供 EmitCtx 构造 JS 对象) */
    public Scriptable scope() {
        return scope;
    }

    // ==================== 注册表访问 ====================

    public Map<String, BuiltinHandler> ctrl()       { return ctrl; }
    public Map<String, BuiltinHandler> dotCtrl()    { return dotCtrl; }
    public Map<String, BuiltinHandler> dot()        { return dot; }
    public Map<String, BuiltinHandler> frontHigh()  { return frontHigh; }
    public Map<String, BuiltinHandler> frontLow()   { return frontLow; }
    public Map<String, BuiltinHandler> decompile()  { return decompile; }

    // ==================== 运算符表访问 ====================

    /** 运算符词法值,顺序 = 词法匹配顺序(等同旧 Constants.Operator.values() 的 value) */
    public List<String> operatorValues() { return operatorValues; }
    /** 词法值 → 运算符名(midOpKeysMap) */
    public Map<String, String> midOpKeysMap() { return midOpKeysMap; }
    /** 运算符名 → 词法值(midOpValueMap) */
    public Map<String, String> midOpValueMap() { return midOpValueMap; }
    /** 词法值 → 优先级(midOpPriorityMap) */
    public Map<String, Integer> midOpPriorityMap() { return midOpPriorityMap; }
    public Map<String, String> operatorAliasMap() { return operatorAliasMap; }
    public Map<String, String> operatorReverseMap() { return operatorReverseMap; }
    public Map<String, Integer> operatorOffsetMap() { return operatorOffsetMap; }
    public String subOperatorValue() { return subOperatorValue; }

    // ==================== 指令码表 ====================

    public List<String> ctrlCodes()    { return ctrlCodes; }
    public List<String> dotCtrlCodes() { return dotCtrlCodes; }
    public List<String> dotCodes()     { return dotCodes; }
    public List<String> dotCodesAll()  { return dotCodesAll; }
    public List<String> dotOpReduced() { return dotOpReduced; }

    // ==================== 领域数据 ====================

    /** 链式键声明表:指令键 → 合法链键列表(含 main) */
    public Map<String, List<String>> chainTable() { return chainTable; }

    public List<String> buildingTypes() { return buildingTypes; }
    public List<String> locateTypes()   { return locateTypes; }
    public List<String> lookupTypes()   { return lookupTypes; }
    public List<String> chainKeys()     { return chainKeys; }
    /** 领域默认值常量(VAL_0/VAL_1/@this/null/DEFAULT/enemy,any,any …) */
    public Map<String, String> domainConstants() { return domainConstants; }

    // ==================== 加载逻辑 ====================

    private void loadHandlers(Scriptable registry, String key, Map<String, BuiltinHandler> target) {
        Object raw = ScriptableObject.getProperty(registry, key);
        if (!(raw instanceof Scriptable map)) {
            throw new IllegalStateException("registry missing category: " + key);
        }
        Object[] ids = map.getIds();
        for (Object id : ids) {
            String name = Context.toString(id);
            Object val = ScriptableObject.getProperty(map, name);
            if (val instanceof Function fn) {
                target.put(name, (s, ctx) -> callHandler(fn, s, ctx));
            }
        }
    }

    private void loadOperators(Scriptable ops) {
        Object raw = ScriptableObject.getProperty(ops, "list");
        if (raw instanceof Scriptable list) {
            Object lenObj = ScriptableObject.getProperty(list, "length");
            int len = lenObj instanceof Number n ? n.intValue() : 0;
            for (int i = 0; i < len; i++) {
                Scriptable item = (Scriptable) ScriptableObject.getProperty(list, i);
                String name = strProp(item, "name");
                String value = strProp(item, "value");
                int priority = intProp(item, "priority");
                operatorValues.add(value);
                midOpKeysMap.put(value, name);
                midOpValueMap.put(name, value);
                midOpPriorityMap.put(value, priority);
            }
        }
        operatorAliasMap.putAll(strMap(objectProp(ops, "alias")));
        operatorReverseMap.putAll(strMap(objectProp(ops, "reverse")));
        for (var e : numMap(objectProp(ops, "offset")).entrySet()) {
            operatorOffsetMap.put(e.getKey(), e.getValue());
        }
        Object sub = ScriptableObject.getProperty(ops, "SUB_VALUE");
        if (sub != null) subOperatorValue = Context.toString(sub);
    }

    private void loadCodes(Scriptable codes) {
        ctrlCodes.addAll(jsArrayToStrings(ScriptableObject.getProperty(codes, "CTRL")));
        dotCtrlCodes.addAll(jsArrayToStrings(ScriptableObject.getProperty(codes, "DOT_CTRL")));
        dotCodes.addAll(jsArrayToStrings(ScriptableObject.getProperty(codes, "DOT")));
        dotCodesAll.addAll(Stream.concat(dotCtrlCodes.stream(), dotCodes.stream()).collect(Collectors.toList()));
        dotOpReduced.addAll(dotCodesAll.stream().map(s -> s.substring(0, s.length() - 1)).toList());
    }

    private void loadDomain(Scriptable domain) {
        buildingTypes.addAll(jsArrayToStrings(ScriptableObject.getProperty(domain, "BUILDING_TYPES")));
        locateTypes.addAll(jsArrayToStrings(ScriptableObject.getProperty(domain, "LOCATE_TYPES")));
        lookupTypes.addAll(jsArrayToStrings(ScriptableObject.getProperty(domain, "LOOKUP_TYPES")));
        chainKeys.addAll(jsArrayToStrings(ScriptableObject.getProperty(domain, "CHAIN_KEYS")));
        for (String k : List.of(
                "CONTROL_DEFAULT", "RADAR_SORT_DEFAULT", "VAL_0", "VAL_1",
                "VAL_AT", "VAL_NUL", "JUMP_DEFAULT", "RADAR_TARGET")) {
            domainConstants.put(k, strProp(domain, k));
        }
    }

    private void loadChainTable(Scriptable chain) {
        for (Object id : chain.getIds()) {
            String key = Context.toString(id);
            chainTable.put(key, jsArrayToStrings(ScriptableObject.getProperty(chain, key)));
        }
    }

    // ==================== 调用 ====================

    /** 调用 .ts 处理器;ctx 为 EmitCtx(compiler 包) */
    private static String callHandler(Function fn, String s, Object ctx) {
        Context cx = Context.enter();
        try {
            Object res = fn.call(cx, fn, fn, new Object[]{s, ctx});
            return res == null || res == Context.getUndefinedValue() ? "" : Context.toString(res);
        } finally {
            Context.exit();
        }
    }

    // ==================== Rhino 工具 ====================

    private static String readResource(String path) {
        try (InputStream in = BuiltinEngine.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Missing builtin bundle resource: " + path
                    + " — run 'node tools/sync-js.mjs' (or npm install && npm run sync) first.");
            }
            try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return r.lines().collect(Collectors.joining("\n"));
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to read builtin bundle resource: " + path, e);
        }
    }

    private static Scriptable objectProp(Scriptable o, String key) {
        Object v = ScriptableObject.getProperty(o, key);
        if (!(v instanceof Scriptable s)) {
            throw new IllegalStateException("builtins registry missing object: " + key);
        }
        return s;
    }

    private static String strProp(Scriptable o, String key) {
        Object v = ScriptableObject.getProperty(o, key);
        return v == null ? "" : Context.toString(v);
    }

    private static int intProp(Scriptable o, String key) {
        Object v = ScriptableObject.getProperty(o, key);
        return v instanceof Number n ? n.intValue() : 0;
    }

    private static List<String> jsArrayToStrings(Object raw) {
        List<String> out = new ArrayList<>();
        if (raw instanceof Scriptable arr) {
            Object lenObj = ScriptableObject.getProperty(arr, "length");
            int len = lenObj instanceof Number n ? n.intValue() : 0;
            for (int i = 0; i < len; i++) {
                Object v = ScriptableObject.getProperty(arr, i);
                out.add(v == null ? "" : Context.toString(v));
            }
        }
        return out;
    }

    private static Map<String, String> strMap(Scriptable o) {
        Map<String, String> m = new LinkedHashMap<>();
        for (Object id : o.getIds()) {
            String key = Context.toString(id);
            m.put(key, strProp(o, key));
        }
        return m;
    }

    private static Map<String, Integer> numMap(Scriptable o) {
        Map<String, Integer> m = new LinkedHashMap<>();
        for (Object id : o.getIds()) {
            String key = Context.toString(id);
            m.put(key, intProp(o, key));
        }
        return m;
    }
}
