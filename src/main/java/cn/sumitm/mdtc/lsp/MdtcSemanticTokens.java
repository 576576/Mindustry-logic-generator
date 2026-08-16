package cn.sumitm.mdtc.lsp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cn.sumitm.mdtc.core.BuiltinEngine;

/**
 * .mdtc 语义词法:为全文生成 LSP semantic tokens(相对编码)。
 *
 * <p>token 类别:注释、字符串、数字、运算符、关键字、指令(function/method)、
 * @ 常量(enumMember)、标签(label,自定义)、变量。</p>
 */
final class MdtcSemanticTokens {

    /** legend token 类型(索引即语义 token 编码中的 typeIdx) */
    static final List<String> TOKEN_TYPES = List.of(
        "comment",    // 0
        "string",     // 1
        "number",     // 2
        "operator",   // 3
        "keyword",    // 4
        "function",   // 5 指令(ctrl/front)
        "method",     // 6 链式指令(dot/dotCtrl)
        "variable",   // 7
        "enumMember", // 8 @ 常量
        "label"       // 9 :: 标签(自定义类型)
    );

    private static final int COMMENT = 0, STRING = 1, NUMBER = 2, OPERATOR = 3,
        KEYWORD = 4, FUNCTION = 5, METHOD = 6, VARIABLE = 7, ENUM_MEMBER = 8, LABEL = 9;

    private static final List<String> KEYWORDS = List.of(
        "if(", "else{", "for(", "while(", "do{", "function ", "import ", "repeat(", "raw(", "return");

    /** 指令完整扫描键,按长度降序(避免 print( 吞掉 printchar() */
    private static List<String> allKeys() {
        BuiltinEngine e = BuiltinEngine.get();
        List<String> keys = new ArrayList<>();
        keys.addAll(e.ctrl().keySet());
        keys.addAll(e.frontHigh().keySet());
        keys.addAll(e.frontLow().keySet());
        keys.addAll(e.dotCtrl().keySet());
        keys.addAll(e.dot().keySet());
        keys.sort(Comparator.comparingInt(String::length).reversed());
        return keys;
    }

    private static final List<String> OPERATORS = BuiltinEngine.get().operatorValues();

    private MdtcSemanticTokens() {}

    /** 生成相对编码的语义 token 数据 */
    static List<Integer> encode(String text) {
        List<int[]> tokens = new ArrayList<>(); // {line, start, len, type}
        String[] lines = text.split("\n", -1);
        List<String> keys = allKeys();
        for (int lineIdx = 0; lineIdx < lines.length; lineIdx++) {
            scanLine(lines[lineIdx], lineIdx, keys, tokens);
        }

        List<Integer> data = new ArrayList<>();
        int prevLine = 0, prevChar = 0;
        for (int[] t : tokens) {
            int deltaLine = t[0] - prevLine;
            int deltaStart = deltaLine == 0 ? t[1] - prevChar : t[1];
            data.add(deltaLine);
            data.add(deltaStart);
            data.add(t[2]);
            data.add(t[3]);
            data.add(0); // modifiers
            prevLine = t[0];
            prevChar = t[1];
        }
        return data;
    }

    private static void scanLine(String line, int lineIdx, List<String> keys, List<int[]> out) {
        int pos = 0;
        int len = line.length();
        while (pos < len) {
            char c = line.charAt(pos);
            if (Character.isWhitespace(c)) {
                pos++;
                continue;
            }
            // 注释 ::... (行首或任意位置后的 ::)
            if (c == ':' && pos + 1 < len && line.charAt(pos + 1) == ':') {
                add(out, lineIdx, pos, len - pos, COMMENT);
                return;
            }
            // 字符串
            if (c == '"') {
                int end = pos + 1;
                while (end < len && line.charAt(end) != '"') end++;
                add(out, lineIdx, pos, end - pos + 1, STRING);
                pos = end + 1;
                continue;
            }
            // @ 常量
            if (c == '@') {
                int end = pos + 1;
                while (end < len && isWordChar(line.charAt(end))) end++;
                add(out, lineIdx, pos, end - pos, ENUM_MEMBER);
                pos = end;
                continue;
            }
            // 数字
            if (Character.isDigit(c) || (c == '.' && pos + 1 < len && Character.isDigit(line.charAt(pos + 1)))) {
                int end = pos + 1;
                while (end < len && (Character.isDigit(line.charAt(end)) || line.charAt(end) == '.')) end++;
                add(out, lineIdx, pos, end - pos, NUMBER);
                pos = end;
                continue;
            }
            // 指令键(长优先)
            String matchedKey = null;
            for (String key : keys) {
                if (line.startsWith(key, pos)) {
                    matchedKey = key;
                    break;
                }
            }
            if (matchedKey != null) {
                int type = matchedKey.startsWith(".") ? METHOD : FUNCTION;
                add(out, lineIdx, pos, matchedKey.length(), type);
                pos += matchedKey.length();
                continue;
            }
            // 关键字
            String kw = null;
            for (String k : KEYWORDS) {
                if (line.startsWith(k, pos)) {
                    kw = k;
                    break;
                }
            }
            if (kw != null) {
                add(out, lineIdx, pos, kw.length(), KEYWORD);
                pos += kw.length();
                continue;
            }
            // 运算符
            String op = null;
            for (String o : OPERATORS) {
                if (line.startsWith(o, pos)) {
                    op = o;
                    break;
                }
            }
            if (op != null) {
                add(out, lineIdx, pos, op.length(), OPERATOR);
                pos += op.length();
                continue;
            }
            // 标识符(变量)
            int end = pos;
            while (end < len && isWordChar(line.charAt(end))) end++;
            if (end > pos) {
                add(out, lineIdx, pos, end - pos, VARIABLE);
                pos = end;
                continue;
            }
            pos++;
        }
    }

    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '.' || c == '-';
    }

    private static void add(List<int[]> out, int line, int start, int len, int type) {
        if (len <= 0) return;
        out.add(new int[]{line, start, len, type});
    }
}
