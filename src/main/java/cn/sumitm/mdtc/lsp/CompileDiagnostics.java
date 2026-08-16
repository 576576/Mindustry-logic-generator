package cn.sumitm.mdtc.lsp;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import cn.sumitm.mdtc.cli.Main;
import cn.sumitm.mdtc.compiler.CodeCompiler;
import cn.sumitm.mdtc.core.Utils;

/**
 * 编译诊断收集:重定向 System.err 捕获编译错误,结合 CodeCompiler.lastWarnings
 * 生成 LSP 诊断(错误/警告)。
 *
 * <p>错误行号解析自编译器输出中的 "line N" / "at lineN" 模式;
 * 无法定位的错误与警告定位到文档开头。</p>
 */
final class CompileDiagnostics {

    /** 匹配 "line 12" 或 "line12"(CodeFormatter 输出 "at line3. xxx") */
    private static final Pattern LINE_PATTERN = Pattern.compile("line\\s*(\\d+)");

    private CompileDiagnostics() {}

    /** 编译一段 .mdtc 源码,返回诊断列表 */
    static List<Diagnostic> compile(String text) {
        List<Diagnostic> out = new ArrayList<>();

        // ---- 1. 捕获编译错误(stderr) ----
        PrintStream oldErr = System.err;
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream capture = new PrintStream(buf, true, StandardCharsets.UTF_8);
        System.setErr(capture);
        try {
            // LSP 模式:关闭 CLI 静态开关
            Main.isToFormat = false;
            Main.filePath = "";
            CodeCompiler.compile(text);
        } catch (Throwable t) {
            // 编译崩溃(如 RPN 栈越界)也转为错误诊断,避免客户端断连
            out.add(diagnostic(LspMessages.compileException(t), -1, DiagnosticSeverity.Error));
        } finally {
            System.setErr(oldErr);
        }

        // ---- 2. 括号错误由第 5 步精确扫描覆盖;其余编译错误(stderr) ----
        // 括号 Syntax error 与 "> expr" 续行在此跳过(消息末尾不附加行内容)
        String errText = buf.toString(StandardCharsets.UTF_8);
        String[] srcLines = text.split("\\n", -1);
        for (String rawLine : errText.split("\\n")) {
            String line = rawLine.replaceAll("\\x1B\\[[;\\d]*m", "").trim();
            if (line.isEmpty() || line.contains("Compile Warning:")
                || line.contains("Syntax error on token") || line.startsWith(">")) {
                continue;
            }
            out.add(errorFrom(line, srcLines));
        }

        // ---- 3. 负数守卫警告:按原文逐行扫描,range 仅覆盖负数 token(标黄该负数) ----
        String[] lines = text.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            for (int[] r : Utils.findInfixNegativeRanges(lines[i])) {
                String token = lines[i].substring(r[0], r[1]);
                out.add(diagnosticAt(LspMessages.negativeWarning(token),
                    i, r[0], r[1], DiagnosticSeverity.Warning));
            }
        }

        // ---- 4. 其他警告(jump 标签缺失、chain 警告等;无行号,定位整个文档) ----
        // 负数守卫消息已由第 3 步精确覆盖,这里跳过避免重复;其余按 locale 翻译
        for (String w : CodeCompiler.lastWarnings) {
            if (w.contains("is not wrapped in parentheses") || w.contains("未被 () 包裹")) continue;
            out.add(diagnostic(LspMessages.translate(w), -1, DiagnosticSeverity.Warning));
        }

        // ---- 5. 括号配对扫描:精确标红不匹配的括号 token(而非整行) ----
        out.addAll(scanBrackets(text));
        return out;
    }

    /** 括号配对扫描:返回不匹配括号的诊断(只覆盖该括号字符) */
    private static List<Diagnostic> scanBrackets(String text) {
        List<Diagnostic> out = new ArrayList<>();
        String[] lines = text.split("\\n", -1);
        Deque<int[]> stack = new ArrayDeque<>(); // {line, col, char}
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            boolean inString = false;
            for (int j = 0; j < line.length(); j++) {
                char c = line.charAt(j);
                if (c == ':' && j + 1 < line.length() && line.charAt(j + 1) == ':') break; // 注释
                if (c == '"') { inString = !inString; continue; }
                if (inString) continue;
                if (c == '(' || c == '{') {
                    stack.push(new int[]{i, j, c});
                } else if (c == ')' || c == '}') {
                    char want = c == ')' ? '(' : '{';
                    if (stack.isEmpty() || stack.peek()[2] != want) {
                        out.add(bracketError(c, i, j));
                    } else {
                        stack.pop();
                    }
                }
            }
        }
        // 未闭合的括号(按栈序报出,每个标红其所在位置)
        while (!stack.isEmpty()) {
            int[] t = stack.pop();
            out.add(bracketError((char) t[2], t[0], t[1]));
        }
        return out;
    }

    /** 括号语法错误诊断:range 仅覆盖该括号字符;消息按 locale,不附行内容 */
    private static Diagnostic bracketError(char ch, int line, int col) {
        return new Diagnostic(new Range(new Position(line, col), new Position(line, col + 1)),
            LspMessages.bracketError(ch), DiagnosticSeverity.Error, "mdtc");
    }

    /** 错误诊断:优先 "line N",其次用消息中的原文行片段定位,否则整个文档 */
    private static Diagnostic errorFrom(String msg, String[] srcLines) {
        int ln = parseLine(msg);
        if (ln < 0) {
            // 消息通常包含出错的原文行(如 "> print(hello"),逐行匹配定位
            for (int i = 0; i < srcLines.length; i++) {
                String src = srcLines[i].trim();
                if (src.length() >= 3 && !src.startsWith("::") && msg.contains(src)) {
                    ln = i;
                    break;
                }
            }
        }
        return diagnostic(LspMessages.translate(msg.trim()), ln, DiagnosticSeverity.Error);
    }

    /** 从错误文本解析行号(0-based);-1 表示无法定位 */
    private static int parseLine(String msg) {
        Matcher m = LINE_PATTERN.matcher(msg);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {
                // fall through
            }
        }
        return -1;
    }

    /** 精确 token 级诊断:range 仅覆盖 [startCol, endCol) */
    private static Diagnostic diagnosticAt(String message, int line, int startCol, int endCol,
        DiagnosticSeverity severity) {
        return new Diagnostic(new Range(new Position(line, startCol), new Position(line, endCol)),
            message, severity, "mdtc");
    }

    private static Diagnostic diagnostic(String message, int line, DiagnosticSeverity severity) {
        Range range;
        if (line >= 0) {
            range = new Range(new Position(line, 0), new Position(line, Integer.MAX_VALUE));
        } else {
            // 无法定位:覆盖整个文档(0 行到文档末尾)
            range = new Range(new Position(0, 0), new Position(Integer.MAX_VALUE, 0));
        }
        return new Diagnostic(range, message, severity, "mdtc");
    }
}
