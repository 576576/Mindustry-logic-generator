package cn.sumitm.mdtc.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import cn.sumitm.mdtc.cli.Main;
import cn.sumitm.mdtc.core.BuiltinEngine;
import cn.sumitm.mdtc.core.BuiltinHandler;
import cn.sumitm.mdtc.core.Utils;
import cn.sumitm.mdtc.core.stdCodeStream;
import cn.sumitm.mdtc.formatter.CodeFormatter;

public final class CodeDecompiler {
    private CodeDecompiler() {}

    /**
     * 主转换函数入口
     */
    public static String decompile(String codeBlock) {
        ArrayList<String> bashList = new ArrayList<>(List.of(codeBlock.split("\n")));

        stdCodeStream result_link = convertLink(stdCodeStream.of(bashList));
        stdCodeStream result_jump = convertJump(result_link);

        if (Main.primeCodeLevel >= 2) {
            String filePath = Main.filePath;
            if (filePath.endsWith(".mdtcode")) {
                String primeCodePath = filePath.replace(".mdtcode", "_prime.mdtc");
                String writeContent = CodeFormatter.format(result_jump.toString());
                Utils.writeFile(primeCodePath, writeContent);

                IO.println("PrimeCode output at:\n> " + primeCodePath);
            } else IO.println("Skip writing prime code.");
        }

        stdCodeStream result_code = convertCode(result_jump);
        stdCodeStream result_fold = simplifyCode(result_code);
        stdCodeStream result_jump2 = convertJump2(result_fold);

        String result_format = !Main.isToFormat ? result_jump2.toString()
            : CodeFormatter.format(result_jump2.toString());
        return result_format.trim();
    }

    /**
     * 将原始jump绝对跳转转换为标签相对跳转
     */
    static stdCodeStream convertLink(stdCodeStream stream) {
        ArrayList<String> bashList = stream.bash();
        List<Integer> tags = bashList.stream()
                .filter(line -> line.startsWith("jump "))
                .map(line -> Integer.valueOf(line.split(" ", 3)[1]))
                .sorted().toList();

        Map<Integer, String> tagMap = new HashMap<>();
        for (int i = 0; i < tags.size(); i++) {
            tagMap.put(tags.get(i), "TAG." + (i + 1));
        }

        int offset = 0;
        for (int i = 0; i < bashList.size(); i++) {
            String line = bashList.get(i);
            if (tagMap.containsKey(i - offset)) {
                bashList.add(i, "::" + tagMap.get(i - offset));
                offset++;
                i++;
            }
            if (line.startsWith("jump ")) {
                String jumpLine = line.split(" ", 3)[1];
                int target = Integer.parseInt(jumpLine);
                bashList.set(i, line.replaceFirst(jumpLine, tagMap.get(target)));
            }
        }

        return stdCodeStream.of(bashList);
    }

    /**
     * 逆转换原生的jump为if/while
     */
    static stdCodeStream convertJump(stdCodeStream stream) {
        ArrayList<String> bashList = stream.bash();
        HashSet<String> ignoreTags = new HashSet<>(), ignoreLines = new HashSet<>();

        while (true) {
            String tagTo = "", line2 = "";
            int lineIndex = -1, line2Index = -1;
            for (int i = 0; i < bashList.size(); i++) {
                String line = bashList.get(i);
                if (line.startsWith("::")) {
                    tagTo = line.substring(2);
                    if (ignoreTags.contains(tagTo)) continue;
                    lineIndex = i;
                    break;
                }
            }
            for (int i = 0; i < bashList.size(); i++) {
                String line = bashList.get(i);
                if (line.startsWith("jump " + tagTo + " ")) {
                    if (ignoreLines.contains(line)) continue;
                    line2Index = i;
                    line2 = line;
                    break;
                }
            }
            if (line2Index == -1) {
                if (lineIndex == -1) break;
                ignoreTags.add(tagTo);
                continue;
            }

            final List<String> keysStart = List.of("if(", "do{");
            int matchIndex = 0;
            if (lineIndex < line2Index) { //do-while
                o:
                for (int i = lineIndex; i < line2Index; i++) {
                    String line = bashList.get(i);
                    for (String key : keysStart) {
                        if (line.startsWith(key)) {
                            matchIndex++;
                            continue o;
                        }
                    }
                    if (line.startsWith("}")) matchIndex--;
                }
                if (matchIndex != 0) {
                    ignoreLines.add(line2);
                    continue;
                }

                String condition = Utils.reduceCondition(line2.split(" ", 3)[2]);
                bashList.add(lineIndex, "do{");
                bashList.set(line2Index + 1, "}while(" + condition + ")");
            } else { //if
                o:
                for (int i = line2Index; i < lineIndex; i++) {
                    String line = bashList.get(i);
                    for (String key : keysStart) {
                        if (line.startsWith(key)) {
                            matchIndex++;
                            continue o;
                        }
                    }
                    if (line.startsWith("}")) matchIndex--;
                }
                if (matchIndex != 0) {
                    ignoreLines.add(line2);
                    continue;
                }

                String condition = Utils.reduceCondition(
                    Utils.reverseCondition(line2.split(" ", 3)[2]));
                if (condition.equals("never")) {
                    ignoreLines.add(line2);
                    continue;
                }
                bashList.add(lineIndex, "}");
                bashList.set(line2Index, "if(" + condition + "){");
            }
        }

        Utils.removeSpareTags(bashList);

        return stdCodeStream.of(bashList);
    }

    /**
     * 逆转换输入代码中的单行代码到mdtc形式
     */
    static stdCodeStream convertCode(stdCodeStream stream) {
        ArrayList<String> bashList = stream.bash();

        Map<String, BuiltinHandler> funcHandlers = BuiltinEngine.get().decompile();
        EmitCtx ctx = new EmitCtx(new ArrayList<>(), new int[]{1}, null, BuiltinEngine.get().scope());

        List<String> ignoreKeys = List.of("::", "do{", "for(", "if(", "else{", "}");
        o:
        for (int i = 0; i < bashList.size(); i++) {
            String line = bashList.get(i);
            for (String key : ignoreKeys)
                if (line.startsWith(key)) continue o;
            String[] splitList = line.split(" ", 2);
            if (splitList.length < 2) {
                bashList.set(i, line + "()");
                continue;
            }

            String lineKey = splitList[0] + " ";
            bashList.set(i, funcHandlers.containsKey(lineKey) ?
                    funcHandlers.get(lineKey).apply(splitList[1].trim(), ctx) :
                    "raw(\"" + line + "\")");
        }
        return stdCodeStream.of(bashList);
    }


    /**
     * 折叠多重语句
     */
    static stdCodeStream simplifyCode(stdCodeStream stream) {
        ArrayList<String> bashList = stream.bash();

        for (int i = bashList.size() - 1; i > 0; i--) {
            String line = bashList.get(i);
            List<String> parts = new ArrayList<>(Utils.stringSplit(line));

            String op0, op1, op2;
            for (String midVar : parts) {
                if (midVar.matches("mid\\.\\d+")) {
                    for (int j = i - 1; j >= 0; j--) {
                        String assignLine = bashList.get(j);
                        if (assignLine.startsWith(midVar + "=")) {
                            String value = assignLine.substring(midVar.length() + 1).trim();
                            List<String> parts2 = new ArrayList<>(Utils.stringSplit(value));
                            int replaceIndex = parts.indexOf(midVar);
                            if (replaceIndex == -1) continue;

                            boolean bracketTo = false;
                            if (parts2.size() > 2) {
                                op0 = parts2.get(1);
                                if (replaceIndex < 1) continue;
                                op1 = parts.get(replaceIndex - 1);
                                if (replaceIndex < parts.size() - 1) {
                                    op2 = parts.get(replaceIndex + 1);
                                    bracketTo = Utils.isLowPriority(op0, op1, op2);
                                } else if (replaceIndex == parts.size() - 1) {
                                    bracketTo = Utils.isLowPriority(op0, op1);
                                }
                            }

                            boolean finalBracketTo = bracketTo;
                            parts.replaceAll(part -> part.equals(midVar)
                                ? (finalBracketTo ? ("(" + value + ")") : value) : part);
                            bashList.set(i, joinTokens(parts));
                            bashList.remove(j);
                            break;
                        }
                    }
                    break;
                }
            }
        }

        for (int i = bashList.size() - 1; i > 0; i--) {
            String line = bashList.get(i);
            List<String> parts = Utils.stringSplit(line);
            if (parts.size() > 2) {
                String key = parts.get(1) + parts.get(2);
                if (!BuiltinEngine.get().dotCtrlCodes().contains(key)) continue;
                String block = parts.getFirst();

                String line2 = bashList.get(i - 1);
                List<String> parts2 = Utils.stringSplit(line2);
                String block2 = parts2.getFirst();
                if (!block.equals(block2)) continue;
                if (parts2.size() < 3) continue;
                String key2 = parts2.get(1) + parts2.get(2);
                if (!BuiltinEngine.get().dotCtrlCodes().contains(key2)) continue;

                String value = line.substring(block.length());
                bashList.set(i - 1, line2 + value);
                bashList.remove(i);
            }
        }

        return stdCodeStream.of(bashList);
    }

    /** 将 token 列表重新拼接为字符串，括号和点号两侧不留空格 */
    private static String joinTokens(List<String> tokens) {
        if (tokens.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(tokens.get(0));
        for (int i = 1; i < tokens.size(); i++) {
            String prev = tokens.get(i - 1);
            String curr = tokens.get(i);
            boolean noSpace = "(".equals(curr) || ")".equals(curr) || ",".equals(curr)
                || "(".equals(prev) || ")".equals(prev) || ".".equals(prev)
                || prev.endsWith("(") || prev.startsWith(".");
            if (noSpace) sb.append(curr);
            else sb.append(' ').append(curr);
        }
        return sb.toString();
    }

    /**
     * 重整以@counter=@counter开头的语句为jump2()
     */
    static stdCodeStream convertJump2(stdCodeStream stream) {
        ArrayList<String> bashList = stream.bash();
        bashList.replaceAll(line -> line.startsWith("@counter=")
            ? "jump2(" + line.substring(
                line.startsWith("@counter=@counter") ? 17 : 9).trim() + ")"
            : line);
        return stdCodeStream.of(bashList);
    }
}
