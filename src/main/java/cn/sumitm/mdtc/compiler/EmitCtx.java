package cn.sumitm.mdtc.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.sumitm.mdtc.core.BuiltinDomain;
import cn.sumitm.mdtc.core.Utils;
import cn.sumitm.mdtc.core.stdCodeStream;
import rhino.Context;
import rhino.Scriptable;
import rhino.ScriptableObject;

/**
 * 指令执行桥接对象:运行期注入给 builtins/*.ts 处理器
 * (.ts 侧类型契约见 builtins/ctx.d.ts 的 EmitCtx)。
 */
public final class EmitCtx {

    private final ArrayList<String> bash;
    private final int[] ref;
    private final String[] blockRef;
    private final Scriptable scope;

    public EmitCtx(ArrayList<String> bash, int[] ref, String[] blockRef, Scriptable scope) {
        this.bash = bash;
        this.ref = ref;
        this.blockRef = blockRef;
        this.scope = scope;
    }

    /** 当前 ref 计数 */
    public int ref() { return ref[0]; }

    /** 覆写 ref(子编译后同步) */
    public void setRef(int n) { ref[0] = n; }

    /** 当前中间变量名 "mid.<ref>" */
    public String mid() { return "mid." + ref[0]; }

    /** 追加一行输出 */
    public void bash(String line) { bash.add(line); }

    /** 追加多行输出(JS Array) */
    public void bashAll(Object lines) { bash.addAll(jsArrayToList(lines)); }

    /** 点链左侧 block 引用 */
    public String block() { return blockRef == null ? "" : blockRef[0]; }

    /** 子编译:调用主编译管道 convertCodeLine,返回 {bash, expr, stat} */
    public Object compileSub(String expr) {
        stdCodeStream bc = CodeCompiler.convertCodeLine(stdCodeStream.of(expr, ref[0]));
        Context cx = Context.getCurrentContext();
        Scriptable obj = cx.newObject(scope);
        ScriptableObject.putProperty(obj, "bash", cx.newArray(scope, bc.bash().toArray()));
        ScriptableObject.putProperty(obj, "expr", bc.expr());
        ScriptableObject.putProperty(obj, "stat", bc.stat());
        return obj;
    }

    /** 顶层逗号切分(等同 Utils.bracketPartSplit) */
    public String[] parts(String s) { return Utils.bracketPartSplit(s).toArray(new String[0]); }

    /** 词法切分(等同 Utils.stringSplit) */
    public String[] split(String s) { return Utils.stringSplit(s).toArray(new String[0]); }

    /** 链式参数解析(等同 Utils.getChainParams),返回 JS 对象 */
    public Scriptable chain(String s) {
        Map<String, String> m = Utils.getChainParams(s);
        Context cx = Context.getCurrentContext();
        Scriptable obj = cx.newObject(scope);
        for (var e : m.entrySet()) ScriptableObject.putProperty(obj, e.getKey(), e.getValue());
        return obj;
    }

    /** 输出一条编译警告(打印到 stderr,不中断编译) */
    public void warn(String msg) {
        Utils.printError(msg);
    }

    /** Building 分类 contains(含模组运行期合并的游戏内容名) */
    public boolean buildingContains(String name) { return BuiltinDomain.buildingContains(name); }

    /** Lookup 分类 contains(含模组运行期合并的游戏内容名) */
    public boolean lookupContains(String name) { return BuiltinDomain.lookupContains(name); }

    private static List<String> jsArrayToList(Object arr) {
        List<String> out = new ArrayList<>();
        if (arr instanceof Scriptable s) {
            Object lenObj = ScriptableObject.getProperty(s, "length");
            int len = lenObj instanceof Number n ? n.intValue() : 0;
            for (int i = 0; i < len; i++) {
                Object v = ScriptableObject.getProperty(s, i);
                out.add(v == null ? "" : Context.toString(v));
            }
        }
        return out;
    }
}
