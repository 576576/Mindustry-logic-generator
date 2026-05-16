package cn.sumitm.mdtc.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static final List<String> dotCtrlCodes = List.of(
        ".ctrl(", ".enable(", ".config(", ".color(", ".shoot(",
        ".ulocate(", ".unpack(", ".pflush(", ".dflush(", ".write("
    );

    public static final List<String> dotCodes = List.of(".sensor(", ".read(", ".orElse(");

    public static final List<String> dotCodesAll = Stream.concat(dotCtrlCodes.stream(), dotCodes.stream())
        .collect(Collectors.toList());

    public static final List<String> dotOpReduced = dotCodesAll.stream()
        .map(s -> s.substring(0, s.length() - 1))
        .toList();

    public static final List<String> ctrlCodes = List.of(
        "print(", "printchar(", "format(", "wait(", "stop(",
        "end(", "ubind(", "uctrl(", "ushoot(", "jump(", "jump2(", "printf(", "tag(", "raw("
    );

    public static final Map<String, Integer> operatorOffsetMap = Map.ofEntries(
        Map.entry("op", 2),
        Map.entry("sensor", 1),
        Map.entry("getlink", 1),
        Map.entry("radar", 7),
        Map.entry("uradar", 7),
        Map.entry("lookup", 2),
        Map.entry("packcolor", 1),
        Map.entry("read", 1),
        Map.entry("set", 1),
        Map.entry("select", 1)
    );

    public static final Map<String, String> operatorAliasMap = Map.of(
        "log10", "lg",
        "log", "ln",
        "logn", "log"
    );

    public static final Map<String, String> operatorReverseMap = Map.ofEntries(
        Map.entry("notEqual", "equal"),
        Map.entry("equal", "notEqual"),
        Map.entry("strictEqual", "notEqual"),
        Map.entry("lessThan", "greaterThanEq"),
        Map.entry("lessThanEq", "greaterThan"),
        Map.entry("greaterThan", "lessThanEq"),
        Map.entry("greaterThanEq", "lessThan"),
        Map.entry("always", "never"),
        Map.entry("never", "always")
    );

    public static final Map<String, String> midOpKeysMap;
    public static final Map<String, String> midOpValueMap;
    public static final Map<String, Integer> midOpPriorityMap;

    static {
        midOpKeysMap = new HashMap<>();
        midOpValueMap = new HashMap<>();
        midOpPriorityMap = new HashMap<>();

        for (Operator o : Operator.values()) {
            midOpKeysMap.put(o.value, o.name());
            midOpValueMap.put(o.name(), o.value);
            midOpPriorityMap.put(o.value, o.priority);
        }
    }

    public static final List<String> supportFormats = List.of(".mdtc", ".mdtcode", ".libmdtc");

    public enum Operator {
        add("+", 4), sub(".-", 4),
        mul("*", 5), idiv("//", 5),
        div("/", 5), emod("%%", 5),
        mod(".%", 5), pow(".^", 7),
        strictEqual("===", 3), equal("==", 3),
        notEqual("!=", 3), land("&&", 2),
        greaterThanEq(">=", 3), lessThanEq("<=", 3),
        ushr(">>>", 5), shr(">>", 5),
        shl("<<", 5), xor("^", 2),
        greaterThan(">", 3), lessThan("<", 3),
        and("&", 2), or("|", 2),
        lbracket("(", 10), rbracket(")", 10),
        set("=", 1), always("always", 1), never("never", 1);

        public final String value;
        public final int priority;

        Operator(String value, int priority) {
            this.value = value;
            this.priority = priority;
        }
    }
}
