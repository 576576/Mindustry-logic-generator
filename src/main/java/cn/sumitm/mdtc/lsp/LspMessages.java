package cn.sumitm.mdtc.lsp;

/**
 * LSP 诊断消息本地化:按客户端 locale 选择中文/英文(不支持时回退 en)。
 * locale 取自 InitializeParams.locale(VSCode 自动传递,如 "zh-cn"/"en")。
 */
final class LspMessages {

    private static volatile boolean zh = false;

    private LspMessages() {}

    /** 设置客户端 locale(zh 开头视为中文,其余回退英文) */
    static void setLocale(String locale) {
        zh = locale != null && locale.toLowerCase().startsWith("zh");
    }

    static boolean isZh() {
        return zh;
    }

    /** 负数守卫警告(按 locale) */
    static String negativeWarning(String token) {
        String tail = token.substring(1);
        return isZh()
            ? "负数 \"" + token + "\" 未被 () 包裹;建议写成 \"(" + token
                + ")\" 或使用空格减法 \" - " + tail + "\""
            : "Negative \"" + token + "\" is not wrapped in parentheses; use \"(" + token
                + ")\" or spaced subtraction \" - " + tail + "\"";
    }

    /** 括号语法错误(按 locale) */
    static String bracketError(char ch) {
        String token = String.valueOf(ch);
        return isZh()
            ? "语法错误:标记 \"" + token + "\" 无效,请删除该标记"
            : "Syntax error on token \"" + token + "\", delete this token";
    }

    /** 编译异常兜底(按 locale) */
    static String compileException(Throwable t) {
        String detail = t.getClass().getSimpleName()
            + (t.getMessage() != null ? " - " + t.getMessage() : "");
        return isZh() ? "编译异常: " + detail : "Compile exception: " + detail;
    }

    /** 翻译编译器产生的已知消息(zh 时);未知消息原样返回 */
    static String translate(String msg) {
        if (!isZh()) return msg;
        String s = msg;
        s = s.replace("Syntax error on token \"(\", delete this token", "语法错误:标记 \"(\" 无效,请删除该标记");
        s = s.replace("Syntax error on token \"{\", delete this token", "语法错误:标记 \"{\" 无效,请删除该标记");
        s = s.replace("Syntax error on token \"}\", delete this token", "语法错误:标记 \"}\" 无效,请删除该标记");
        s = s.replace("Syntax error on token \"else\", delete this token", "语法错误:标记 \"else\" 无效,请删除该标记");
        s = s.replace("Error: for() content not match", "错误:for() 内容不匹配");
        s = s.replace("Error: repeat() not enough infos", "错误:repeat() 参数不足");
        if (s.startsWith("jump() tag not found:")) {
            String target = s.substring("jump() tag not found:".length())
                .replace("— replaced with DEFAULT", "").trim();
            s = "jump() 标签未找到: " + target + ",已替换为 DEFAULT";
        }
        if (s.startsWith("chain warning:")) {
            s = "链式警告:" + s.substring("chain warning:".length())
                .replace("unknown chain key", "未知链键")
                .replace("(ignored)", "(已忽略)");
        }
        return s;
    }
}
