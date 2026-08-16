package cn.sumitm.mdtc.core;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 语言级常量。
 * <p>运算符表、指令码表与领域分类目录已外移到 builtins/*.ts
 * (单一事实源),运行期经 {@link BuiltinEngine} 加载。</p>
 */
public final class Constants {
    private Constants() {}

    public enum JumpCondition {
        TRUE ("always 0 0"),
        FALSE("notEqual 0 0");

        private final String id;
        JumpCondition(String id) { this.id = id; }
        public String id() { return id; }
    }

    public static final Pattern NUMBER_PATTERN = Pattern.compile("^([-+])?\\d+(\\.\\d+)?$");

    public static final List<String> supportFormats = List.of(".mdtc", ".mdtcode", ".libmdtc");
}
