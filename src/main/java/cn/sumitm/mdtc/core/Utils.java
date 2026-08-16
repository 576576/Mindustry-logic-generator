package cn.sumitm.mdtc.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import cn.sumitm.mdtc.compiler.CodeCompiler;

public final class Utils {
    private Utils() {}

    /** 内置指令引擎(指令/运算符/领域数据的运行期提供者) */
    private static BuiltinEngine eng() {
        return BuiltinEngine.get();
    }

    /** 编译期收集开关:true 时 stringSplit 的负数守卫等警告写入 CodeCompiler.lastWarnings;反编译路径关闭 */
    private static boolean collectWarnings = false;

    /** 编译中当前处理的行号(0-based,守卫警告定位用) */
    private static int currentLine = 0;

    public static void setCollectWarnings(boolean on) {
        collectWarnings = on;
    }

    public static boolean isCollectWarnings() {
        return collectWarnings;
    }

    public static void setCurrentLine(int line) {
        currentLine = line;
    }

    public static int currentLine() {
        return currentLine;
    }

    public static String readFile(String filePath) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return "";
        }
        try {
            return Files.readString(path);
        } catch (Exception e) {
            printError("Unable to read file: " + e.getMessage());
            return "";
        }
    }

    public static void writeFile(String filePath, String content) {
        try {
            Files.writeString(Paths.get(filePath), content);
        } catch (IOException e) {
            printError("Unable to write file." + e.getMessage());
        }
    }

    public static void openWithExplorer(String filePath) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            if (os.contains("win")) {
                pb = new ProcessBuilder("explorer", "/select,", filePath);
            } else if (os.contains("mac")) {
                pb = new ProcessBuilder("open", "-R", filePath);
            } else {
                // Linux and others: open the parent directory
                Path parent = Paths.get(filePath).getParent();
                pb = new ProcessBuilder("xdg-open", parent != null ? parent.toString() : filePath);
            }
            pb.start();
        } catch (IOException e) {
            printError("Unable to open directory: " + e.getMessage());
        }
    }

    public static String formatParams(int paramNum, String[] params, String defaultParam, String delimiter) {
        params = Arrays.copyOf(params, paramNum);
        for (int i = 0; i < params.length; i++)
            if (params[i] == null || params[i].isEmpty()) params[i] = defaultParam;
        return String.join(delimiter, params);
    }

    public static String padParams(String defaultParam, int paramNum, String paramString) {
        return formatParams(paramNum, bracketPartSplit(paramString).toArray(new String[0]), defaultParam, " ");
    }

    public static String padParams(int paramNum, String... params) {
        return formatParams(paramNum, params, "0", " ");
    }

    public static String padParams(int paramNum, String paramString) {
        return formatParams(paramNum, bracketPartSplit(paramString).toArray(new String[0]), "0", " ");
    }

    public static String reduceParams(String defaultParam, String paramString) {
        int dpLength = defaultParam.length();
        while (paramString.endsWith(defaultParam)) {
            paramString = paramString.substring(0, paramString.length() - dpLength).trim();
        }
        return paramString.replace(" ", ",");
    }

    public static String reduceParams(String defaultParam, String... params) {
        String paramString = String.join(" ", params);
        return reduceParams(defaultParam, paramString);
    }

    public static String reduceParams(String defaultParam, int paramNum, String... params) {
        params = Arrays.copyOf(params, paramNum);
        for (int i = 0; i < params.length; i++)
            if (params[i] == null) params[i] = defaultParam;
        return reduceParams(defaultParam, params);
    }

    public static String reduceCondition(String condition) {
        String[] params = condition.split(" ", 3);
        return switch (params[0]) {
            case "always" -> "always";
            case "never"  -> "never";
            default       -> params[1] + eng().midOpValueMap().get(params[0]) + params[2].trim();
        };
    }

    public static boolean isDotCtrlCode(String codeLine) {
        return eng().dotCtrlCodes().stream().anyMatch(codeLine::contains);
    }

    public static boolean isCtrlCode(String codeLine) {
        return eng().ctrlCodes().stream().anyMatch(codeLine::startsWith);
    }

    public static void printError(String message) {
        System.err.println("\u001B[31m" + message + "\u001B[0m");
    }

    public static int getEndDotChain(String expr, int start) {
        int end = getEndBracket(expr, start);
        while (end < expr.length() - 1 && expr.charAt(end + 1) == '.') {
            int pos = end + 1;
            if (expr.startsWith(".^", pos)) return end;
            if (eng().dotCtrlCodes().stream().anyMatch(k -> expr.startsWith(k, pos))) return end;
            if (eng().dotCodes().stream().anyMatch(k -> expr.startsWith(k, pos))) return end;
            int endNext = getEndBracket(expr, pos);
            if (endNext == -1) return end;
            end = endNext;
        }
        return end;
    }

    public static String getDotBlock(String expr) {
        int index = expr.length() - 1;
        for (String key : eng().dotCtrlCodes()) {
            int keyIndex = expr.indexOf(key);
            if (keyIndex != -1 && keyIndex < index) index = keyIndex;
        }
        return expr.substring(0, index);
    }

    /**
     * 预处理字符串,分离变量和运算符
     *
     * @return 变量分离的字符串数组
     */
    public static List<String> stringSplit(String str) {
        if (str.isEmpty()) return List.of();
        if (str.startsWith("::")) return List.of("::", str.substring(2));
        if (str.contains("::")) str = str.substring(0, str.indexOf("::"));

        final List<Character> keysSplit = List.of(',', ';');
        List<String> tokens = new ArrayList<>();
        StringBuilder tokenBuilder = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (keysSplit.contains(c)) {
                tokens.add(tokenBuilder.toString().trim());
                tokenBuilder = new StringBuilder();
                tokens.add(c + "");
                continue;
            }
            boolean isOperator = false;
            for (String opValue : eng().operatorValues()) {
                if (str.startsWith(opValue, i)) {
                    // spaced 运算符(如 "-" 减法)要求前后空白或行边界,
                    // 前边界含右括号(反编译折叠会产生 ")- x" 粘连),否则视为标识符一部分
                    if (eng().spacedOperatorValues().contains(opValue)
                            && !(isLeftBoundary(str, i - 1) && isSpaceBoundary(str, i + opValue.length()))) {
                        continue;
                    }
                    if (!tokenBuilder.toString().trim().isEmpty()) {
                        tokens.add(tokenBuilder.toString().trim());
                        tokenBuilder = new StringBuilder();
                    }
                    tokens.add(opValue);
                    i += opValue.length() - 1;
                    isOperator = true;
                    break;
                }
            }
            if (!isOperator) tokenBuilder.append(c);
        }
        if (!tokenBuilder.toString().trim().isEmpty())
            tokens.add(tokenBuilder.toString().trim());

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (token.startsWith("-") && token.length() > 1 && !Character.isWhitespace(token.charAt(1))) {
                // 负数守卫:仅当负数紧跟在中置运算符之后(如 "1 + -1")才提示,
                // 避免 "x = -5"、"(-114514)" 等常规负数写法产生噪音
                if (collectWarnings && isAfterInfixOperator(tokens, i)) {
                    CodeCompiler.addWarning("line " + (currentLine + 1)
                        + ": 负数 \"" + token + "\" 未被 () 包裹;建议写成 \"(" + token
                        + ")\" 或使用空格减法 \" - " + token.substring(1)
                        + "\" | Negative \"" + token + "\" is not wrapped in parentheses; use \"(" + token
                        + ")\" or spaced subtraction \" - " + token.substring(1) + "\"");
                }
                if (!isNumeric(token)) {
                    List<String> tokenTo = List.of("(", "0", eng().subOperatorValue(), token.substring(1), ")");
                    tokens.remove(i);
                    tokens.addAll(i, tokenTo);
                    i += tokenTo.size() - 1;
                }
            }
        }

        for (int i = 1; i < tokens.size() - 1; i++) {
            String token = tokens.get(i);
            tokenBuilder = new StringBuilder();

            if (token.equals("(")) {
                String tokenNow = tokens.get(i - 1);
                if (eng().dotOpReduced().contains(tokenNow)) {
                    tokenBuilder.append(tokenNow).append(token);
                    int matchIndex = 0;
                    for (int j = i + 1; j < tokens.size(); j++) {
                        tokenNow = tokens.get(j);
                        if (tokenNow.equals("(")) matchIndex++;
                        if (tokenNow.equals(")")) matchIndex--;
                        if (matchIndex != 0) continue;
                        if (eng().dotOpReduced().contains(tokenNow) || j == tokens.size() - 1) {
                            String tokenTo = stringOf(tokens.subList(i - 1, j));
                            tokens.subList(i - 1, j).clear();
                            tokens.add(i - 1, tokenTo);
                            break;
                        }
                    }
                }
            }
        }
        return tokens;
    }

    /**
     * 检测一行中"中置运算符后无空格负数"的 token 区间(供 LSP 精确标黄该负数)。
     *
     * @return {起始列, 结束列} 列表(可能多个);无命中返回空列表
     */
    public static List<int[]> findInfixNegativeRanges(String line) {
        List<int[]> out = new ArrayList<>();
        List<String> tokens = stringSplit(line);
        int searchFrom = 0;
        for (int i = 1; i < tokens.size(); i++) {
            String t = tokens.get(i);
            if (t.startsWith("-") && t.length() > 1 && !Character.isWhitespace(t.charAt(1))
                    && isAfterInfixOperator(tokens, i)) {
                int idx = line.indexOf(t, searchFrom);
                if (idx >= 0) {
                    out.add(new int[]{idx, idx + t.length()});
                    searchFrom = idx + t.length();
                }
            }
        }
        return out;
    }

    /** 是否位于中置运算符之后(排除赋值 =、括号与 always/never 伪运算符) */
    private static boolean isAfterInfixOperator(List<String> tokens, int idx) {
        if (idx <= 0) return false;
        String prev = tokens.get(idx - 1);
        if (!eng().midOpPriorityMap().containsKey(prev)) return false;
        return !prev.equals("=") && !prev.equals("(") && !prev.equals(")")
            && !prev.equals("always") && !prev.equals("never");
    }

    /** 空格边界判断:字符串边界或空白字符(供 spaced 运算符匹配) */
    private static boolean isSpaceBoundary(String str, int idx) {
        return idx < 0 || idx >= str.length() || Character.isWhitespace(str.charAt(idx));
    }

    /** 左边界判断:字符串边界、空白或右括号(右括号后紧跟减号视为减法) */
    private static boolean isLeftBoundary(String str, int idx) {
        if (idx < 0) return true;
        char c = str.charAt(idx);
        return Character.isWhitespace(c) || c == ')';
    }

    public static List<String> bracketPartSplit(String str) {
        if (str.isEmpty()) return List.of();

        List<String> tokens = new ArrayList<>();
        List<String> splitList = stringSplit(str);
        int matchIndex = 0;
        StringBuilder token = new StringBuilder();
        for (String part : splitList) {
            if (matchIndex == 0 && ",;".contains(part)) {
                tokens.add(token.toString());
                token = new StringBuilder();
                continue;
            } else if (part.equals("(")) matchIndex++;
            else if (part.equals(")")) matchIndex--;
            token.append(part);
        }
        if (!token.isEmpty()) tokens.add(token.toString());
        tokens.replaceAll(String::trim);
        return tokens;
    }

    public static String stringOf(List<String> list) {
        return list.stream().reduce("", (a, b) -> a + b);
    }

    public static String stringBlockOf(List<String> bashList) {
        return bashList.stream().reduce("", (a, b) -> a + "\n" + b).trim();
    }

    /**
     * 转换所有已声明变量到保留变量名
     */
    public static String replaceVars(String s, List<String> varsList, List<String> replaceToList) {
        if (varsList.size() != replaceToList.size()) {
            printError("Unable to deal with " + varsList + replaceToList);
        }
        List<String> splitList = new ArrayList<>(stringSplit(s));
        for (int i = 0; i < splitList.size(); i++) {
            for (int j = 0; j < varsList.size(); j++) {
                if (splitList.get(i).equals(varsList.get(j)))
                    splitList.set(i, replaceToList.get(j));
            }
        }
        return stringOf(splitList);
    }

    public static String replaceVar(String s, String var, String replaceToVar) {
        List<String> splitList = new ArrayList<>(stringSplit(s));
        for (int i = 0; i < splitList.size(); i++) {
            if (splitList.get(i).equals(var))
                splitList.set(i, replaceToVar);
        }
        return stringOf(splitList);
    }

    /**
     * 转换所有已声明标签到保留变量名和标签
     */
    public static String replaceTags(String s, List<String> tagsList, String prefix) {
        final List<String> keyList = List.of("::", "jump");
        List<String> splitList = new ArrayList<>(stringSplit(s));
        if (splitList.size() < 2 || !keyList.contains(splitList.getFirst())) return s;
        for (int i = 0; i < keyList.size(); i++) {
            if (splitList.getFirst().equals(keyList.get(i))) {
                String tag = splitList.get(i + 1);
                if (tagsList.contains(tag)) splitList.set(i + 1, prefix + tag);
            }
        }
        return stringOf(splitList);
    }

    public static String reverseCondition(String codeLine) {
        final Map<String, String> reMap = eng().operatorReverseMap();
        String[] splitList = codeLine.split(" ");
        for (int i = 0; i < splitList.length; i++) {
            String part = splitList[i];
            if (reMap.containsKey(part)) splitList[i] = reMap.get(part);
        }
        return String.join(" ", splitList);
    }

    public static boolean isSpecialControl(String codeLine) {
        String[] keys = new String[]{"::", "}", "do{", "for(", "if(", "else{"};
        for (String key : keys) {
            if (codeLine.startsWith(key)) return true;
        }
        return false;
    }

    public static int getEndBracket(String expr, int start) {
        if (start < 0) return -1;
        Stack<Integer> stack = new Stack<>();
        for (int i = start; i < expr.length(); i++) {
            if (expr.charAt(i) == '(') {
                stack.push(i);
            } else if (expr.charAt(i) == ')') {
                if (stack.isEmpty()) return -1;
                stack.pop();
                if (stack.isEmpty()) return i;
            }
        }
        return -1;
    }

    public static String getCondition(String codeLine) {
        final String defaultCondition = "always 0 0";
        String[] params = codeLine.split(" ");
        if (params.length == 0) return defaultCondition;
        String key = params[0];
        if (key.equals("op")) {
            if (!eng().operatorReverseMap().containsKey((params[1]))) {
                String target = params[eng().operatorOffsetMap().get(key)];
                return String.join(" ", "notEqual", target, "0");
            }
            return String.join(" ", params[1], params[3], params[4]);
        } else if (eng().operatorOffsetMap().containsKey(key)) {
            String target = params[eng().operatorOffsetMap().get(key)];
            return String.join(" ", "notEqual", target, "0");
        }
        return defaultCondition;
    }

    public static Map<String, String> getChainParams(String s) {
        String expr = ".main(" + s + ")";
        Map<String, String> paramsMap = new HashMap<>();
        for (int i = 0; i < expr.length(); i++) {
            if (expr.charAt(i) == '.') {
                int start = expr.indexOf("(", i), end = getEndBracket(expr, start);
                String key = expr.substring(i + 1, start);
                String value = expr.substring(start + 1, end).trim();
                if (!value.isEmpty()) paramsMap.put(key, value);
                i = end;
            }
        }
        return paramsMap;
    }

    public static boolean isNumeric(String str) {
        return str != null && Constants.NUMBER_PATTERN.matcher(str).matches();
    }

    /**
     * 将中置表达式转为逆波兰表达式
     *
     * @return 逆波兰表达式数组
     */
    public static List<String> generateRpn(String str) {
        return rpn(stringSplit(str));
    }

    public static List<String> rpn(List<String> tokens) {
        List<String> output = new ArrayList<>();
        Stack<String> operators = new Stack<>();
        for (String token : tokens) {
            if (isOperator(token)) {
                if (token.equals("(")) {
                    operators.push(token);
                } else if (token.equals(")")) {
                    while (!operators.empty() && !operators.peek().equals("(")) {
                        output.add(operators.pop());
                    }
                    if (!operators.empty() && operators.peek().equals("(")) {
                        operators.pop();
                    }
                } else {
                    while (!operators.empty() && !operators.peek().equals("(") && cmp(token, operators.peek()) <= 0) {
                        output.add(operators.pop());
                    }
                    operators.push(token);
                }
            } else {
                output.add(token);
            }
        }

        // 遍历结束，将运算符栈全部压入output
        while (!operators.empty()) {
            if (!operators.peek().equals("(")) {
                output.add(operators.pop());
            } else {
                operators.pop(); // Remove any remaining left parentheses
            }
        }
        return output;
    }

    public static void removeSpareTags(ArrayList<String> bashList) {
        for (int i = 0; i < bashList.size(); i++) {
            String line = bashList.get(i);
            if (line.startsWith("::")) {
                String tagTo = line.substring(2);
                if (bashList.stream().noneMatch(l -> l.startsWith("jump " + tagTo + " "))) {
                    bashList.remove(i);
                    i--;
                }
            }
        }
    }

    public static boolean isLowPriority(String op0, String... ops) {
        int p0 = getPriority(op0);
        for (String op : ops) {
            int p = getPriority(op);
            if (p >= p0) return true;
        }
        return false;
    }

    /**
     * 比较两个符号的优先级
     *
     * @return c1的优先级是否比c2的高，高则返回正数，等于返回0，小于返回负数
     */
    public static int cmp(String c1, String c2) {
        int p1 = eng().midOpPriorityMap().getOrDefault(c1, 0);
        int p2 = eng().midOpPriorityMap().getOrDefault(c2, 0);
        return p1 - p2;
    }

    /**
     * 枚举出来的才视为运算符，用于扩展
     *
     * @return 运算符合法性
     */
    public static boolean isOperator(String c) {
        return eng().midOpPriorityMap().containsKey(c);
    }

    public static int getPriority(String c) {
        int priority = eng().midOpPriorityMap().getOrDefault(c, 11);
        if (priority == 10) priority = 0;
        return priority;
    }
}
