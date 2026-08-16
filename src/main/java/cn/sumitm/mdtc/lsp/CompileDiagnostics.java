package cn.sumitm.mdtc.lsp;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
            out.add(diagnostic("编译异常: " + t.getClass().getSimpleName()
                + (t.getMessage() != null ? " - " + t.getMessage() : "")
                + " | Compile exception: " + t.getClass().getSimpleName()
                + (t.getMessage() != null ? " - " + t.getMessage() : ""), -1, DiagnosticSeverity.Error));
        } finally {
            System.setErr(oldErr);
        }

        // ---- 2. 错误(stderr 中非 "Compile Warning" 的行;剥离 ANSI 颜色码) ----
        // "> expr" 续行与上一行合并;errorFrom 用消息中的原文行片段定位
        String errText = buf.toString(StandardCharsets.UTF_8);
        String[] srcLines = text.split("\\n", -1);
        StringBuilder pending = new StringBuilder();
        for (String rawLine : errText.split("\\n")) {
            String line = rawLine.replaceAll("\\x1B\\[[;\\d]*m", "").trim();
            if (line.isEmpty() || line.contains("Compile Warning:")) {
                if (!pending.isEmpty()) {
                    out.add(errorFrom(pending.toString(), srcLines));
                    pending.setLength(0);
                }
                continue;
            }
            if (line.startsWith(">")) {
                pending.append(' ').append(line.substring(1).trim());
            } else {
                if (!pending.isEmpty()) {
                    out.add(errorFrom(pending.toString(), srcLines));
                    pending.setLength(0);
                }
                pending.append(line);
            }
        }
        if (!pending.isEmpty()) {
            out.add(errorFrom(pending.toString(), srcLines));
        }

        // ---- 3. 负数守卫警告:按原文逐行扫描精确定位(不依赖编译行号,
        //      函数/import/repeat 展开或空行都不会造成偏移) ----
        String[] lines = text.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            String token = Utils.findInfixNegative(lines[i]);
            if (token != null) {
                out.add(diagnostic("负数 \"" + token + "\" 未被 () 包裹;建议写成 \"(" + token
                    + ")\" 或使用空格减法 \" - " + token.substring(1)
                    + "\" | Negative \"" + token + "\" is not wrapped in parentheses; use \"(" + token
                    + ")\" or spaced subtraction \" - " + token.substring(1) + "\"", i, DiagnosticSeverity.Warning));
            }
        }

        // ---- 4. 其他警告(jump 标签缺失、chain 警告等;无行号,定位整个文档) ----
        // 负数守卫消息已由第 3 步精确覆盖,这里跳过避免重复
        for (String w : CodeCompiler.lastWarnings) {
            if (w.contains("未被 () 包裹")) continue;
            out.add(diagnostic(w, -1, DiagnosticSeverity.Warning));
        }
        return out;
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
        return diagnostic(msg.trim(), ln, DiagnosticSeverity.Error);
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
