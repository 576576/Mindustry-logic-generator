package cn.sumitm.mdtc.formatter;

import cn.sumitm.mdtc.core.Utils;

public final class CodeFormatter {
    private CodeFormatter() {}

    /**
     * 主代码格式化函数入口
     */
    public static String format(String codeBlock) {
        final String[] keysStart = {"do{", "for(", "if(", "else{", "repeat(", "function "};
        final String[] keysEnd = {"}"};
        final String[] lines = codeBlock.split("\n");

        int matchIndex = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                sb.append("\n");
                continue;
            }

            for (var key : keysEnd)
                if (line.startsWith(key)) {
                    matchIndex--;
                    break;
                }
            if (matchIndex < 0) {
                Utils.printError("Syntax error on token \"}\", delete this token\n> " + line);
                return "";
            }

            sb.append("\t".repeat(matchIndex)).append(line).append("\n");

            for (var key : keysStart)
                if (line.startsWith(key)) {
                    matchIndex++;
                    break;
                }
        }
        while (matchIndex > 0) {
            matchIndex--;
            sb.append("\t".repeat(matchIndex)).append("}\n");
        }

        return sb.toString().trim();
    }

    /**
     * 代码去格式化(trim 各行;保留空行,保证编译诊断的行号与源码一致)
     */
    public static String deformat(String codeBlock) {
        StringBuilder sb = new StringBuilder();
        for (String line : codeBlock.split("\n")) {
            sb.append(line.trim()).append("\n");
        }
        // 仅去掉末尾多余换行(不 trim 开头,保留行号对齐)
        String out = sb.toString();
        while (!out.isEmpty() && out.endsWith("\n")) {
            out = out.substring(0, out.length() - 1);
        }
        return out;
    }
}
