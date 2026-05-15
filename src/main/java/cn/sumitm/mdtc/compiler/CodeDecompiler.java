package cn.sumitm.mdtc.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import cn.sumitm.mdtc.cli.Main;
import cn.sumitm.mdtc.core.Constants;
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
                .map(line -> Integer.parseInt(line.split(" ", 3)[1]))
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

        Map<String, Function<String, String>> funcHandlers = buildDecompileHandlers();

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
                    funcHandlers.get(lineKey).apply(splitList[1].trim()) :
                    "raw(\"" + line + "\")");
        }
        return stdCodeStream.of(bashList);
    }

    private static Map<String, Function<String, String>> buildDecompileHandlers() {
        Map<String, Function<String, String>> handlers = new HashMap<>();

        handlers.put("set ", s -> s.replaceFirst(" ", "="));

        handlers.put("print ", s -> "print(" + s + ")");
        handlers.put("printchar ", s -> "printchar(" + s + ")");
        handlers.put("format ", s -> "format(" + s + ")");
        handlers.put("wait ", s -> "wait(" + s + ")");

        handlers.put("ubind ", s -> "ubind(" + s + ")");
        handlers.put("ucontrol ", s -> {
            final List<String> ctrlTypes = List.of("target", "targetp");
            String[] params = s.split(" ", 2);
            String ctrlType = params[0], target = params[1], target2 = "";

            if (!ctrlTypes.contains(ctrlType)) {
                ctrlType = "uctrl";
                target = Utils.reduceParams("0", params[0], target);
            } else {
                params = target.split(" ");
                if (ctrlType.equals("targetp")) {
                    ctrlType = "ushoot";
                    target = params[1];
                    if (target.equals("1")) target = "";
                    target2 = ".target(" + params[0] + ")";
                } else if (ctrlType.equals("target")) {
                    ctrlType = "ushoot";
                    target = params[2];
                    if (target.equals("1")) target = "";
                    target2 = ".target(" + String.join(",", params[0], params[1]) + ")";
                } else target = params[0];
            }

            return String.format("%s(%s)%s", ctrlType, target, target2);
        });

        handlers.put("draw ", s -> "draw(" + Utils.reduceParams("0", s) + ")");

        handlers.put("jump ", s -> {
            final List<String> alwaysConditions = List.of("0==0", "always");
            String[] params = s.split(" ", 2);
            String condition = Utils.reduceCondition(params[1]);
            condition = alwaysConditions.contains(condition) ? "" : ".when(" + condition + ")";
            return String.format("jump(%s)%s", params[0], condition);
        });

        handlers.put("control ", s -> {
            final List<String> ctrlTypes = List.of("enabled", "config", "color", "shoot", "shootp");
            String[] params = s.split(" ", 3);
            String block = params[1], ctrlType = params[0], target = params[2], target2 = "";
            if (!ctrlTypes.contains(ctrlType)) {
                ctrlType = "ctrl";
                target = Utils.reduceParams("0", params[0], target);
            } else {
                if (ctrlType.equals("enabled"))
                    ctrlType = "enable";
                params = target.split(" ");
                if (ctrlType.equals("shootp")) {
                    ctrlType = "shoot";
                    target = params[1];
                    if (target.equals("1")) target = "";
                    target2 = ".target(" + params[0] + ")";
                } else if (ctrlType.equals("shoot")) {
                    target = params[2];
                    if (target.equals("1")) target = "";
                    target2 = ".target(" + String.join(",", params[0], params[1]) + ")";
                } else target = params[0];
            }
            return String.format("%s.%s(%s)%s", block, ctrlType, target, target2);
        });

        handlers.put("ulocate ", s -> {
            String[] params = s.split(" ");
            String locateType = params[0], building = params[1],
                enemy = params[2], ore = params[3], block = params[7];
            String result = String.format("%s.ulocate(%s)", block,
                locateType.equals("building") ? building : locateType);
            if (locateType.equals("ore")) result += ".ore(" + ore + ")";
            if (!enemy.equals("0")) result += ".enemy(" + enemy + ")";
            return result;
        });

        handlers.put("unpackcolor ", s -> {
            String[] params = s.split(" ");
            return params[4] + ".unpack(" + Utils.reduceParams("0", 4, params) + ")";
        });
        handlers.put("printflush ", s -> s + ".pflush()");
        handlers.put("drawflush ", s -> s + ".dflush()");

        handlers.put("write ", s -> {
            String[] params = s.split(" ");
            String content = params[0], block = params[1], bit = params[2];
            content = bit.equals("0") ? content : String.join(",", content, bit);
            return block + ".write(" + content + ")";
        });

        handlers.put("sensor ", s -> {
            String[] params = s.split(" ");
            return String.format("%s=%s.sensor(%s)", params[0], params[1], params[2]);
        });

        handlers.put("read ", s -> {
            String[] params = s.split(" ");
            return String.format("%s=%s.read(%s)", params[0], params[1], params[2]);
        });

        handlers.put("select ", s -> {
            String[] params = s.split(" ");
            String condition = String.join(" ", params[1], params[2], params[3]),
                result = params[0], target = params[4], target2 = params[5];
            return String.format("%s=%s.orElse(%s)%s", result, target, target2,
                ".when(" + Utils.reduceCondition(Utils.reverseCondition(condition)) + ")");
        });

        handlers.put("op ", s -> {
            String[] params = s.split(" ");
            String operator = params[0], result = params[1], paramString;
            final Map<String, String> operatorMap = Constants.midOpValueMap;
            if (operatorMap.containsKey(operator)) {
                if (params.length < 4) {
                    return String.format("%s=%s(%s)", result, operatorMap.get(operator), params[2]);
                }
                return String.format("%s=%s %s %s", result, params[2],
                    operatorMap.get(operator), params[3]);
            } else if (operator.equals("logn") && params.length > 4 && params[4].equals("2")) {
                operator = "lb";
                paramString = params[2];
            } else {
                operator = Constants.operatorAliasMap.getOrDefault(operator, operator);
                if (params.length <= 3) {
                    paramString = params[2];
                } else if (operator.equals("log"))
                    paramString = Utils.reduceParams("0", params[3], params[2]);
                else
                    paramString = Utils.reduceParams("0", params[2], params[3]);
            }
            return String.format("%s=%s(%s)", result, operator, paramString);
        });

        handlers.put("getlink ", s -> {
            String[] params = s.split(" ");
            return String.format("%s=link(%s)", params[0], params[1]);
        });

        handlers.put("lookup ", s -> {
            final List<String> lkTypes = List.of("block", "unit", "item", "liquid", "team");
            String[] params = s.split(" ");
            String lookupType = params[0], block = params[1], index = params[2], ctrlType = "lookup", content;
            if (lkTypes.contains(lookupType)) {
                ctrlType = lookupType;
                content = index;
            } else content = String.join(",", lookupType, index);
            return String.format("%s=%s(%s)", block, ctrlType, content);
        });

        handlers.put("packcolor ", s -> {
            String[] params = s.split(" ", 2);
            return String.format("%s=pack(%s)", params[0], params[1].replace(" ", ","));
        });

        handlers.put("uradar ", s -> {
            String[] params = s.split(" ");
            String order = params[5], sort = params[3], result = params[6];
            String target = Utils.reduceParams("any", 3, params[0], params[1], params[2]);
            result += "=uradar()";
            if (!target.isEmpty() && !target.equals("enemy")) result += ".target(" + target + ")";
            if (!order.equals("1")) result += ".order(" + order + ")";
            if (!sort.equals("distance")) result += ".sort(" + sort + ")";
            return result;
        });

        handlers.put("radar ", s -> {
            String[] params = s.split(" ");
            String order = params[5], sort = params[3], result = params[6], block = params[4];
            String target = Utils.reduceParams("any", 3, params[0], params[1], params[2]);
            if (block.equals("@this")) block = "";
            result += "=uradar(" + block + ")";
            if (!target.isEmpty() && !target.equals("enemy")) result += ".target(" + target + ")";
            if (!order.equals("1")) result += ".order(" + order + ")";
            if (!sort.equals("distance")) result += ".sort(" + sort + ")";
            return result;
        });

        return handlers;
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
                            bashList.set(i, String.join(" ", parts));
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
                if (!Constants.dotCtrlCodes.contains(key)) continue;
                String block = parts.getFirst();

                String line2 = bashList.get(i - 1);
                List<String> parts2 = Utils.stringSplit(line2);
                String block2 = parts2.getFirst();
                if (!block.equals(block2)) continue;
                if (parts2.size() < 3) continue;
                String key2 = parts2.get(1) + parts2.get(2);
                if (!Constants.dotCtrlCodes.contains(key2)) continue;

                String value = line.substring(block.length());
                bashList.set(i - 1, line2 + value);
                bashList.remove(i);
            }
        }

        return stdCodeStream.of(bashList);
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
