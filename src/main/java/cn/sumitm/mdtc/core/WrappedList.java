package cn.sumitm.mdtc.core;

import java.util.List;

/**
 * 对 {@link List}{@code <String>} 的轻量包装，内化边界处理和 trim。
 * 所有取值方法自动 {@link String#trim()}。
 */
public final class WrappedList {

    private final List<String> inner;

    private WrappedList(List<String> inner) {
        this.inner = inner;
    }

    /** 包装一个已分割的列表 */
    public static WrappedList of(List<String> list) {
        return new WrappedList(list);
    }

    // ---- 查询 ----

    public int size() {
        return inner.size();
    }

    public boolean isEmpty() {
        return inner.isEmpty();
    }

    // ---- 取值 ----

    /** 安全取第 index 项（trim 后），越界返回 def */
    public String getOrDefault(int index, String def) {
        if (index < 0 || index >= inner.size()) return def;
        String v = inner.get(index).trim();
        return v.isEmpty() ? def : v;
    }

    /** 安全取第 index 项（trim 后），越界返回 null */
    public String getOrNull(int index) {
        return getOrDefault(index, null);
    }

    /** 安全取第 index 项（trim 后），越界返回 "0" */
    public String getOrZero(int index) {
        return getOrDefault(index, "0");
    }

    /** 等于 {@link #getOrNull(int)} */
    public String get(int index) {
        return getOrNull(index);
    }

    // ---- 原始委托 ----

    public List<String> raw() {
        return inner;
    }
}
