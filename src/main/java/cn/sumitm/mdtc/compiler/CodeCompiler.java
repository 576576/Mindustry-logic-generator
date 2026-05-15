package cn.sumitm.mdtc.compiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;

import cn.sumitm.mdtc.cli.Main;
import cn.sumitm.mdtc.core.Constants;
import cn.sumitm.mdtc.core.Utils;
import cn.sumitm.mdtc.core.stdCodeStream;
import cn.sumitm.mdtc.core.stdFuncStream;
import cn.sumitm.mdtc.formatter.CodeFormatter;

public final class CodeCompiler {
    private CodeCompiler() {}

    /**
     * 主转换函数入口
     */
    public static String compile(String codeBlock) {
        ArrayList<String> bashList = new ArrayList<>();
        Map<Integer, stdFuncStream> funcMap = new HashMap<>();

        codeBlock = insertImport(codeBlock);

        int funcStartIndex = codeBlock.indexOf("function ");
        if (funcStartIndex != -1) {
            String funcBlock = codeBlock.substring(funcStartIndex);
            funcMap = generateFuncMap(funcBlock);
            codeBlock = codeBlock.substring(0, funcStartIndex);
        }
        String codeCache = "";
        while (!codeBlock.equals(codeCache)) {
            while (!codeBlock.equals(codeCache)) {
                codeCache = codeBlock;
                codeBlock = insertFunc(codeBlock, funcMap);
            }
            codeCache = codeBlock;
            codeBlock = unfoldRepeat(codeBlock);
        }

        if (Main.primeCodeLevel == 1) {
            String filePath = Main.filePath;
            if (filePath.endsWith(".mdtc") && !filePath.endsWith("_prime.mdtc")) {
                String primeCodePath = filePath.replace(".mdtc", "_prime.mdtc");
                String writeContent = CodeFormatter.format(codeBlock);
                Utils.writeFile(primeCodePath, writeContent);

                IO.println("PrimeCode output at:\n> " + primeCodePath);
            } else IO.println("Skip writing prime code.");
        }

        codeBlock = CodeFormatter.deformat(codeBlock);

        int refNumMax = 1;
        for (String line : codeBlock.split("\n")) {
            stdCodeStream codeStream = convertCodeLine(stdCodeStream.of(line));
            refNumMax = Math.max(refNumMax, codeStream.stat());
            bashList.addAll(codeStream.toList());
        }

        stdCodeStream result_set = convertSet(stdCodeStream.of(bashList, refNumMax));
        stdCodeStream result_jump = convertJump(result_set);
        return convertLink(result_jump).toString().trim();
    }

    static String insertImport(String codeBlock) {
        ArrayList<String> bashList = new ArrayList<>(List.of(codeBlock.split("\n")));
        List<String> importLines = bashList.stream()
                .filter(line -> line.startsWith("import ")).toList();
        bashList.removeIf(line -> line.startsWith("import "));
        StringBuilder codeBlockBuilder = new StringBuilder(Utils.stringBlockOf(bashList));
        for (var line : importLines) {
            String importPath = line.substring(6).trim();
            if (!importPath.endsWith("mdtc")) importPath += ".libmdtc";
            String importBlock = Utils.readFile(importPath);
            int funcStartIndex = importBlock.indexOf("function ");
            if (funcStartIndex != -1) {
                String funcBlock = importBlock.substring(funcStartIndex);
                codeBlockBuilder.append("\n").append(funcBlock);
            }
        }
        codeBlock = codeBlockBuilder.toString();
        return codeBlock;
    }

    /**
     * <p>展开{@code repeat}块</p>
     * <p>等价的1D数组实现, 嵌套即可实现n维数组</p>
     */
    static String unfoldRepeat(String codeBlock) {
        int[] ref = {1};
        final String keyStart = "repeat(", keyEnd = "}";
        final String[] keysJump = {"do{", "for(", "if(", "else{"};

        ArrayList<String> bashList = new ArrayList<>(List.of(codeBlock.split("\n")));
        ArrayList<String> bashCache = new ArrayList<>();
        bashList.replaceAll(String::trim);

        String preserveVar = "";

        int matchIndex = 0, repeatRoutes = 0;
        int repeatStart = 0;
        boolean entryFound = false;
        for (int i = 0; i < bashList.size(); i++) {
            String bash = bashList.get(i);
            if (bash.startsWith(keyEnd)) {
                matchIndex--;
                if (!entryFound) continue;
                if (matchIndex == 0) {
                    List<String> tagsList = new ArrayList<>(bashCache.stream().
                            filter(line -> line.startsWith("::"))
                            .map(s -> s.substring(2)).toList());

                    ArrayList<String> bashTo, bashToAdd = new ArrayList<>();
                    String finalVar = preserveVar;
                    for (int j = 1; j <= repeatRoutes; j++) {
                        String prefix = "REPEAT." + ref[0] + "_";
                        String replaceToVar = preserveVar + j;

                        bashTo = new ArrayList<>(bashCache);
                        bashTo.replaceAll(s -> Utils.replaceTags(s, tagsList, prefix));
                        bashTo.replaceAll(s -> Utils.replaceVar(s, finalVar, replaceToVar));
                        bashToAdd.addAll(bashTo);
                        ref[0]++;
                    }
                    for (int j = -2; j < bashCache.size(); j++) bashList.remove(repeatStart);
                    bashList.addAll(repeatStart, bashToAdd);

                    bashCache = new ArrayList<>();
                    entryFound = false;
                    ref[0]++;
                }
            }

            if (matchIndex > 0 && entryFound) bashCache.add(bash);

            if (bash.startsWith(keyStart)) {
                int start = bash.indexOf("("), end = Utils.getEndBracket(bash, start);
                String bracketContent = bash.substring(start + 1, end);
                String[] repeatInfos = bracketContent.split(",");
                if (repeatInfos.length == 0) {
                    Utils.printError("Error: repeat() not enough infos");
                    return codeBlock;
                }
                if (repeatInfos.length < 2) {
                    repeatRoutes = Integer.parseInt(repeatInfos[0]);
                } else {
                    preserveVar = repeatInfos[0];
                    repeatRoutes = Integer.parseInt(repeatInfos[1]);
                }
                entryFound = true;
                repeatStart = i;
                matchIndex++;
            }
            for (var key : keysJump) {
                if (bash.startsWith(key)) {
                    matchIndex++;
                    break;
                }
            }
        }
        return stdCodeStream.of(bashList).toString().trim();
    }

    /**
     * 内嵌函数到代码块
     */
    static String insertFunc(String codeBlock, Map<Integer, stdFuncStream> funcMap) {
        int[] ref = {1};
        ArrayList<String> bashList = new ArrayList<>(List.of(codeBlock.split("\n")));
        ArrayList<String> bashCache = new ArrayList<>();

        for (String bash : bashList) {
            for (var func : funcMap.entrySet()) {
                stdFuncStream funcStream = func.getValue();
                String funcName = funcStream.name();
                List<String> varsList = funcStream.varsList();
                int varsNum = funcStream.varsCount();

                int ignoreIndex = 0;
                while (bash.contains(funcName)) {
                    int start = bash.indexOf(funcName, ignoreIndex), end = Utils.getEndBracket(bash, start);
                    if (end == -1) break;
                    String funcArgs = bash.substring(start + funcName.length(), end);
                    String[] args2Array = funcArgs.split(",");
                    if (args2Array.length != varsNum - 1) {
                        ignoreIndex = end;
                        continue;
                    }

                    ArrayList<String> funcBody = new ArrayList<>(funcStream.funcBody());
                    String prefix = "FUNC." + ref[0] + "_";
                    List<String> tagsList = funcStream.tagsList();
                    funcBody.replaceAll(s -> Utils.replaceTags(s, tagsList, prefix));

                    String returnValue = varsList.getFirst(), return2Value;
                    if (returnValue.equals("void")) return2Value = "";
                    else return2Value = prefix + returnValue;
                    List<String> vars2List = new ArrayList<>();
                    vars2List.add(return2Value);
                    vars2List.addAll(Arrays.asList(args2Array));
                    funcBody.replaceAll(s -> Utils.replaceVars(s, varsList, vars2List));

                    bashCache.addAll(funcBody);
                    bash = bash.substring(0, start) + return2Value + bash.substring(end + 1);
                    ref[0]++;
                }
            }
            if (!bash.trim().isEmpty()) bashCache.add(bash);
        }
        return Utils.stringBlockOf(bashCache);
    }

    /**
     * 转换一行代码
     */
    static stdCodeStream convertCodeLine(stdCodeStream stream) {
        String codeLine = stream.expr();
        if (Utils.isSpecialControl(codeLine)) return stream;
        if (Utils.isCtrlCode(codeLine)) return convertCtrl(stream);
        if (Utils.isDotCtrlCode(codeLine)) return convertDotCtrl(stream);

        while (Utils.stringSplit(stream.expr()).size() > 1) {
            stream = convertDot(stream);
            stream = convertFront(stream);
            stream = convertMiddle(stream);
        }

        return convertSet(stream);
    }

    // --- Helper: build funcHandlers map for convertCtrl ---
    private static Map<String, Function<String, String>> buildCtrlHandlers(
            ArrayList<String> bashList, int[] ref) {
        Map<String, Function<String, String>> handlers = new HashMap<>();

        handlers.put("print(", s -> "print " + s);
        handlers.put("printchar(", s -> "printchar " + s);
        handlers.put("format(", s -> "format " + s);
        handlers.put("wait(", s -> "wait " + s);
        handlers.put("stop(", _ -> "stop");
        handlers.put("end(", _ -> "end");

        handlers.put("ubind(", s -> "ubind " + s);
        handlers.put("uctrl(", s -> "ucontrol " + Utils.padParams(6, s));

        handlers.put("ushoot(", s -> {
            final String defaultTarget = "@this", defaultShooting = "1";
            Map<String, String> paramsMap = Utils.getChainParams(s);
            String shooting = paramsMap.getOrDefault("main", defaultShooting);
            String target = paramsMap.getOrDefault("target", defaultTarget);
            String ctrlType = target.contains(",") ? "target" : "targetp";
            target = target.replace(',', ' ');
            String shootArgs = Utils.padParams(5, target, shooting);
            return "ucontrol " + ctrlType + " " + shootArgs;
        });

        handlers.put("draw(", s -> "draw " + Utils.padParams(7, s));

        handlers.put("jump(", s -> {
            final String defaultTarget = "DEFAULT",
                trueCondition = Constants.trueCondition,
                falseCondition = Constants.falseCondition;
            Map<String, String> paramsMap = Utils.getChainParams(s);
            String target = paramsMap.getOrDefault("main", defaultTarget);
            String condition = trueCondition;

            String whenExpr = paramsMap.getOrDefault("when", "");
            List<String> splitList = Utils.stringSplit(whenExpr);
            if (splitList.size() > 1) {
                stdCodeStream bashCache = convertCodeLine(stdCodeStream.of(whenExpr, ref[0]));
                if (!bashCache.bash().isEmpty()) {
                    ref[0] = bashCache.stat();
                    String bashLast = bashCache.bash().getLast();
                    condition = Utils.getCondition(bashLast);
                    if (!condition.equals(trueCondition)) bashCache.bash().removeLast();
                    else if (!bashCache.expr().isEmpty())
                        condition = String.join(" ", "notEqual", bashCache.expr(), "0");
                    bashList.addAll(bashCache.bash());
                }
            } else if (splitList.size() == 1) {
                if (splitList.getFirst().equals("always")) condition = trueCondition;
                else if (splitList.getFirst().equals("never")) condition = falseCondition;
                else condition = String.join(" ", "notEqual", whenExpr, "0");
            }
            return String.join(" ", "jump", target, condition);
        });

        handlers.put("jump2(", s -> {
            List<String> strSplit = Utils.stringSplit(s);
            if (strSplit.size() > 1) s = "@counter=@counter" + s;
            else s = "@counter=" + s;
            stdCodeStream jump2stream = convertCodeLine(stdCodeStream.of(s));
            bashList.addAll(jump2stream.bash());
            return "";
        });

        handlers.put("printf(", s -> {
            String[] parts = s.split(",");
            if (parts.length < 2) return "print " + s;
            bashList.add("print " + parts[0]);
            IntStream.range(1, parts.length).mapToObj(i ->
                    "format " + parts[i]).forEach(bashList::add);
            return "";
        });

        handlers.put("tag(", s -> "::" + s);
        handlers.put("raw(", s -> s.substring(1, s.length() - 1));

        return handlers;
    }

    /**
     * 转换{@code CtrlCode}类型函数
     */
    static stdCodeStream convertCtrl(stdCodeStream stream) {
        ArrayList<String> bashList = stream.bash();
        String expr = stream.expr();
        int[] ref = {stream.stat()};

        Map<String, Function<String, String>> funcHandlers = buildCtrlHandlers(bashList, ref);

        final List<String> ignoreKeys = List.of("jump(", "jump2(", "draw(", "ushoot(", "tag(", "raw(", "print(", "printf(");
        for (Map.Entry<String, Function<String, String>> entry : funcHandlers.entrySet()) {
            while (expr.contains(entry.getKey())) {
                int start = expr.indexOf(entry.getKey()), end = Utils.getEndDotChain(expr, start);
                if (end == -1) {
                    Utils.printError("Bracket unmatched of frontCode:\n> " + expr);
                    return stream;
                }
                String s = expr.substring(start + entry.getKey().length(), end).trim();
                List<String> splitList = Utils.stringSplit(s);
                if (splitList.size() > 1 && !ignoreKeys.contains(entry.getKey())) {
                    List<String> splitParts = Utils.bracketPartSplit(s);
                    for (int i = 0; i < splitParts.size(); i++) {
                        String part = splitParts.get(i);
                        stdCodeStream bashCache = convertCodeLine(stdCodeStream.of(part, ref[0]));
                        bashList.addAll(bashCache.bash());
                        splitParts.set(i, bashCache.expr());
                        ref[0] = bashCache.stat();
                    }
                    s = String.join(",", splitParts);
                }
                String result = entry.getValue().apply(s);
                bashList.add(result);
                expr = "";
            }
        }
        return stdCodeStream.of(bashList);
    }

    // --- Helper: build dotCtrl handlers ---
    private static Map<String, Function<String, String>> buildDotCtrlHandlers(
            ArrayList<String> bashList, String[] blockRef, int[] ref) {
        Map<String, Function<String, String>> handlers = new HashMap<>();

        handlers.put(".ctrl(", s -> {
            String ctrlType = "enabled", target = "";
            String[] params = s.split(",", 2);
            if (params.length > 1) {
                ctrlType = params[0];
                target = params[1];
            }
            return String.join(" ", "control" + ctrlType + blockRef[0]
                + Utils.padParams(4, target));
        });
        handlers.put(".enable(", s -> "control enabled " + blockRef[0] + " " + Utils.padParams(4, s));
        handlers.put(".config(", s -> "control config " + blockRef[0] + " " + Utils.padParams(4, s));
        handlers.put(".color(", s -> "control color " + blockRef[0] + " " + Utils.padParams(4, s));

        handlers.put(".shoot(", s -> {
            final String defaultTarget = "@this", defaultShooting = "1";
            Map<String, String> paramsMap = Utils.getChainParams(s);
            String shooting = paramsMap.getOrDefault("main", defaultShooting);
            String target = paramsMap.getOrDefault("target", defaultTarget);
            String ctrlType = target.contains(",") ? "shoot" : "shootp";
            target = target.replace(',', ' ');
            String shootArgs = Utils.padParams(4, target, shooting);
            return "control " + ctrlType + " " + blockRef[0] + " " + shootArgs;
        });

        handlers.put(".ulocate(", s -> {
            final String defaultType = "ore", defaultOre = "0",
                defaultBuilding = "core", defaultEnemy = "0";
            Map<String, String> paramsMap = Utils.getChainParams(s);
            String locateType = paramsMap.getOrDefault("main", defaultType);
            String ore = paramsMap.getOrDefault("ore", defaultOre);
            String building = paramsMap.getOrDefault("building", defaultBuilding);
            String enemy = paramsMap.getOrDefault("enemy", defaultEnemy);

            final List<String> buildings = List.of("core", "storage", "generator",
                "turret", "factory", "repair", "battery", "reactor", "drill", "shield");
            if (buildings.contains(locateType)) {
                building = locateType;
                locateType = "building";
            }
            return String.join(" ", "ulocate", locateType, building,
                enemy, ore, blockRef[0] + ".x", blockRef[0] + ".y",
                blockRef[0] + ".f", blockRef[0]);
        });

        handlers.put(".unpack(", s -> "unpackcolor " + Utils.padParams(4, s) + " " + blockRef[0]);
        handlers.put(".pflush(", _ -> "printflush " + blockRef[0]);
        handlers.put(".dflush(", _ -> "drawflush " + blockRef[0]);
        handlers.put(".write(", s -> {
            String[] parts = s.split(",");
            String content = "null", bit = "0";
            if (parts.length > 0) content = parts[0];
            if (parts.length > 1) bit = parts[1];
            return "write " + content + " " + blockRef[0] + " " + bit;
        });

        return handlers;
    }

    /**
     * 转换{@code DotCtrlCode}类型函数
     */
    static stdCodeStream convertDotCtrl(stdCodeStream stream) {
        ArrayList<String> bashList = stream.bash();
        String expr = stream.expr();
        String finalExpr = expr;
        String[] blockRef = {Utils.getDotBlock(finalExpr)};
        int[] ref = {stream.stat()};

        Map<String, Function<String, String>> funcHandlers = buildDotCtrlHandlers(bashList, blockRef, ref);

        final List<String> ignoreKeys = List.of(".shoot(", ".ulocate(");
        for (Map.Entry<String, Function<String, String>> entry : funcHandlers.entrySet()) {
            while (expr.contains(entry.getKey())) {
                int start = expr.indexOf(entry.getKey()), end = Utils.getEndDotChain(expr, start);
                if (end == -1) {
                    Utils.printError("Bracket unmatched of frontCode:\n> " + expr);
                    return stream;
                }
                String s = expr.substring(start + entry.getKey().length(), end).trim();
                List<String> splitList = Utils.stringSplit(s);
                if (splitList.size() > 1 && !ignoreKeys.contains(entry.getKey())) {
                    List<String> splitParts = Utils.bracketPartSplit(s);
                    for (int i = 0; i < splitParts.size(); i++) {
                        String part = splitParts.get(i);
                        stdCodeStream bashCache = convertCodeLine(stdCodeStream.of(part, ref[0]));
                        bashList.addAll(bashCache.bash());
                        splitParts.set(i, bashCache.expr());
                        ref[0] = bashCache.stat();
                    }
                    String reduceContent = String.join(",", splitParts);
                    expr = expr.replace(s, reduceContent);
                    end = Utils.getEndDotChain(expr, start);
                    s = reduceContent;
                }
                String result = entry.getValue().apply(s);
                bashList.add(result);
                expr = expr.substring(0, start) + expr.substring(end + 1);
            }
        }
        if (expr.equals(blockRef[0])) expr = "";
        return stdCodeStream.of(bashList, expr);
    }

    // --- Helper: build dot handlers ---
    private static Map<String, Function<String, String>> buildDotHandlers(
            ArrayList<String> bashList, String[] blockRef, int[] ref) {
        Map<String, Function<String, String>> handlers = new HashMap<>();

        handlers.put(".sensor(", s -> "sensor mid." + ref[0] + " " + blockRef[0] + " " + s);
        handlers.put(".read(", s -> "read mid." + ref[0] + " " + blockRef[0] + " " + s);

        handlers.put(".orElse(", s -> {
            final String defaultTarget = "0",
                defaultCondition = Constants.trueCondition;
            Map<String, String> paramsMap = Utils.getChainParams(s);
            String target = paramsMap.getOrDefault("main", defaultTarget);
            String condition = defaultCondition;

            String whenExpr = paramsMap.getOrDefault("when", "");
            List<String> splitList = Utils.stringSplit(whenExpr);
            if (splitList.size() > 1) {
                stdCodeStream bashCache = convertCodeLine(stdCodeStream.of(whenExpr, ref[0]));
                if (!bashCache.bash().isEmpty()) {
                    ref[0] = bashCache.stat();
                    String bashLast = bashCache.bash().getLast();
                    condition = Utils.getCondition(bashLast);
                    if (!condition.equals(defaultCondition)) bashCache.bash().removeLast();
                    else if (!bashCache.expr().isEmpty())
                        condition = String.join(" ", "notEqual", bashCache.expr(), "0");
                    bashList.addAll(bashCache.bash());
                }
            } else if (splitList.size() == 1)
                condition = String.join(" ", "notEqual", whenExpr, "0");

            condition = Utils.reverseCondition(condition);
            return String.join(" ", "select", "mid." + ref[0], condition,
                blockRef[0], target);
        });

        return handlers;
    }

    /**
     * 转换{@code DotCode}类型函数
     */
    static stdCodeStream convertDot(stdCodeStream stream) {
        ArrayList<String> bashList = stream.bash();
        String expr = stream.expr();
        int[] ref = {stream.stat()};
        String[] blockRef = {""};

        Map<String, Function<String, String>> funcHandlers = buildDotHandlers(bashList, blockRef, ref);

        final List<String> ignoreKeys = List.of(".orElse(");
        for (Map.Entry<String, Function<String, String>> entry : funcHandlers.entrySet()) {
            while (expr.contains(entry.getKey())) {
                int start = expr.indexOf(entry.getKey()), end = Utils.getEndDotChain(expr, start);
                List<String> splitList = Utils.stringSplit(expr.substring(0, start));
                blockRef[0] = splitList.getLast();

                String s = expr.substring(start + entry.getKey().length(), end).trim();
                splitList = Utils.stringSplit(s);
                if (splitList.size() > 1 && !ignoreKeys.contains(entry.getKey())) {
                    stdCodeStream bashCache = convertCodeLine(stdCodeStream.of(s, ref[0]));
                    if (!bashCache.bash().isEmpty()) {
                        ref[0] = bashCache.stat();
                        bashList.addAll(bashCache.bash());
                        String midVariable = "mid." + ref[0];
                        expr = expr.replace(s, midVariable);
                        s = midVariable;
                    }
                }
                String result = entry.getValue().apply(s.trim());
                bashList.add(result);
                expr = expr.substring(0, start - blockRef[0].length())
                    + "mid." + ref[0] + expr.substring(end + 1);
                ref[0]++;
            }
        }
        return new stdCodeStream(bashList, expr, ref[0]);
    }

    // --- Helpers: build front handlers ---
    private static Map<String, Function<String, String>> buildFrontHandlersHigh(int[] ref) {
        Map<String, Function<String, String>> handlers = new HashMap<>();

        handlers.put("not(", s -> "op not mid." + ref[0] + " " + s + " 0");
        handlers.put("abs(", s -> "op abs mid." + ref[0] + " " + s + " 0");
        handlers.put("sign(", s -> "op sign mid." + ref[0] + " " + s + " 0");
        handlers.put("floor(", s -> "op floor mid." + ref[0] + " " + s + " 0");
        handlers.put("ceil(", s -> "op ceil mid." + ref[0] + " " + s + " 0");
        handlers.put("round(", s -> "op round mid." + ref[0] + " " + s + " 0");
        handlers.put("sqrt(", s -> "op sqrt mid." + ref[0] + " " + s + " 0");
        handlers.put("rand(", s -> "op rand mid." + ref[0] + " " + s + " 0");
        handlers.put("asin(", s -> "op asin mid." + ref[0] + " " + s + " 0");
        handlers.put("acos(", s -> "op acos mid." + ref[0] + " " + s + " 0");
        handlers.put("atan(", s -> "op atan mid." + ref[0] + " " + s + " 0");
        handlers.put("ln(", s -> "op log mid." + ref[0] + " " + s + " 0");
        handlers.put("lg(", s -> "op log10 mid." + ref[0] + " " + s + " 0");
        handlers.put("lb(", s -> "op logn mid." + ref[0] + " " + s + " 2");

        handlers.put("max(", s -> {
            String[] paramParts = s.split(",");
            return "op max mid." + ref[0] + " " + paramParts[0].trim() + " " + paramParts[1].trim();
        });
        handlers.put("min(", s -> {
            String[] paramParts = s.split(",");
            return "op min mid." + ref[0] + " " + paramParts[0].trim() + " " + paramParts[1].trim();
        });
        handlers.put("len(", s -> {
            String[] paramParts = s.split(",");
            return "op len mid." + ref[0] + " " + paramParts[0].trim() + " " + paramParts[1].trim();
        });
        handlers.put("angle(", s -> {
            String[] paramParts = s.split(",");
            return "op angle mid." + ref[0] + " " + paramParts[0].trim() + " " + paramParts[1].trim();
        });
        handlers.put("angleDiff(", s -> {
            String[] paramParts = s.split(",");
            return "op angleDiff mid." + ref[0] + " " + paramParts[0].trim() + " " + paramParts[1].trim();
        });
        handlers.put("noise(", s -> {
            String[] paramParts = s.split(",");
            return "op noise mid." + ref[0] + " " + paramParts[0].trim() + " " + paramParts[1].trim();
        });
        handlers.put("log(", s -> {
            String[] paramParts = s.split(",");
            return "op logn mid." + ref[0] + " " + paramParts[1].trim() + " " + paramParts[0].trim();
        });

        handlers.put("link(", s -> "getlink mid." + ref[0] + " " + s);
        handlers.put("lookup(", s -> {
            String lookupType = "block", index = "0";
            String[] paramParts = s.split(",");
            if (paramParts.length > 0) index = paramParts[paramParts.length - 1];
            if (paramParts.length > 1) lookupType = paramParts[0];
            return String.join(" ", "lookup", lookupType, "mid." + ref[0], index);
        });
        handlers.put("block(", s -> "lookup block mid." + ref[0] + " " + s);
        handlers.put("unit(", s -> "lookup unit mid." + ref[0] + " " + s);
        handlers.put("item(", s -> "lookup item mid." + ref[0] + " " + s);
        handlers.put("liquid(", s -> "lookup liquid mid." + ref[0] + " " + s);
        handlers.put("team(", s -> "lookup team mid." + ref[0] + " " + s);
        handlers.put("pack(", s -> "packcolor mid." + ref[0] + " " + Utils.padParams(4, s));

        handlers.put("uradar(", s -> {
            final String block = "0", defaultTarget = "enemy,any,any",
                defaultOrder = "1", defaultSort = "distance";
            Map<String, String> paramsMap = Utils.getChainParams(s);
            String target = paramsMap.getOrDefault("target", defaultTarget);
            String order = paramsMap.getOrDefault("order", defaultOrder);
            String sort = paramsMap.getOrDefault("sort", defaultSort);
            target = Utils.padParams("any", 3, target);
            return "uradar " + target + " " + sort + " " + block + " "
                + order + " mid." + ref[0];
        });

        return handlers;
    }

    private static Map<String, Function<String, String>> buildFrontHandlersLow(int[] ref) {
        Map<String, Function<String, String>> handlers = new HashMap<>();

        handlers.put("sin(", s -> "op sin mid." + ref[0] + " " + s);
        handlers.put("cos(", s -> "op cos mid." + ref[0] + " " + s);
        handlers.put("tan(", s -> "op tan mid." + ref[0] + " " + s);

        handlers.put("radar(", s -> {
            final String defaultBlock = "@this", defaultTarget = "enemy,any,any",
                defaultOrder = "1", defaultSort = "distance";
            Map<String, String> paramsMap = Utils.getChainParams(s);
            String block = paramsMap.getOrDefault("main", defaultBlock);
            String target = paramsMap.getOrDefault("target", defaultTarget);
            String order = paramsMap.getOrDefault("order", defaultOrder);
            String sort = paramsMap.getOrDefault("sort", defaultSort);
            target = Utils.padParams("any", 3, target);
            return "radar " + target + " " + sort + " " + block + " "
                + order + " mid." + ref[0];
        });

        return handlers;
    }

    /**
     * 转换{@code FrontCode}类型函数
     */
    static stdCodeStream convertFront(stdCodeStream stream) {
        ArrayList<String> bashList = new ArrayList<>(stream.bash());
        int[] ref = {stream.stat()};

        Map<String, Function<String, String>> funcHandlers_high = buildFrontHandlersHigh(ref);
        Map<String, Function<String, String>> funcHandlers_low = buildFrontHandlersLow(ref);

        String expr = stream.expr();

        final List<String> ignoreKeys = List.of("radar(", "uradar(");
        for (Map<String, Function<String, String>> handlers : List.of(funcHandlers_high, funcHandlers_low)) {
            for (Map.Entry<String, Function<String, String>> entry : handlers.entrySet()) {
                while (expr.contains(entry.getKey())) {
                    int start = expr.indexOf(entry.getKey()), end = Utils.getEndDotChain(expr, start);
                    if (end == -1) {
                        Utils.printError("Bracket unmatched of frontCode:\n> " + expr);
                        return stream;
                    }
                    String s = expr.substring(start + entry.getKey().length(), end).trim();
                    List<String> splitList = Utils.stringSplit(s);
                    if (splitList.size() > 1 && !ignoreKeys.contains(entry.getKey())) {
                        List<String> splitParts = Utils.bracketPartSplit(s);
                        for (int i = 0; i < splitParts.size(); i++) {
                            String part = splitParts.get(i);
                            stdCodeStream bashCache = convertCodeLine(stdCodeStream.of(part, ref[0]));
                            bashList.addAll(bashCache.bash());
                            splitParts.set(i, bashCache.expr());
                            ref[0] = bashCache.stat();
                        }
                        String reduceContent = String.join(",", splitParts);
                        expr = expr.replace(s, reduceContent);
                        end = Utils.getEndDotChain(expr, start);
                        s = reduceContent;
                    }
                    String result = entry.getValue().apply(s);
                    bashList.add(result);

                    String regex = expr.substring(start, end + 1);
                    expr = expr.replace(regex, "mid." + ref[0]);
                    ref[0]++;
                }
            }
        }

        return new stdCodeStream(bashList, expr, ref[0]);
    }

    /**
     * 转换{@code MidCode}类型函数
     */
    static stdCodeStream convertMiddle(stdCodeStream stream) {
        List<String> rpnArray = Utils.generateRpn(stream.expr());
        ArrayList<String> stack = new ArrayList<>();
        ArrayList<String> bashList = stream.bash();
        int[] ref = {stream.stat()};
        final Map<String, String> operatorMap = Constants.midOpKeysMap;
        final Map<String, Integer> offsetMap = Constants.operatorOffsetMap;

        for (String token : rpnArray) {
            if (operatorMap.containsKey(token)) {
                String op = operatorMap.get(token);
                String midVar = "mid." + ref[0];
                if (!op.equals("set")) {
                    String arg1 = stack.get(stack.size() - 2), arg2 = stack.getLast();
                    String result = String.join(" ", "op", op, midVar, arg1, arg2);
                    if (op.equals("sub")) {
                        if (arg1.equals("0") && Utils.isNumeric(arg2))
                            result = "set " + midVar + " -" + arg2;
                    }
                    bashList.add(result);
                    stack.removeLast();
                    stack.removeLast();
                    stack.add(midVar);
                    ref[0]++;
                } else {
                    String result = "set " + stack.getFirst() + " " + stack.getLast();
                    if (!bashList.isEmpty()) {
                        String bashLast = bashList.getLast();
                        int ctrlOffset = offsetMap.getOrDefault(bashLast.split(" ")[0], -1);
                        if (ctrlOffset != -1 && bashLast.split(" ")[ctrlOffset].equals(stack.getLast())) {
                            result = bashLast.replaceFirst(stack.getLast(), stack.getFirst());
                            bashList.removeLast();
                        }
                    }
                    bashList.add(result);
                    stack.clear();
                }
            } else {
                stack.add(token);
            }
        }

        StringBuilder expr = new StringBuilder();
        for (String item : stack) {
            expr.append(item);
        }
        return new stdCodeStream(bashList, expr.toString(), ref[0]);
    }

    /**
     * 转换{@code set}类型函数的非孤立行
     */
    static stdCodeStream convertSet(stdCodeStream stream) {
        final Map<String, Integer> offsetMap = Constants.operatorOffsetMap;
        ArrayList<String> bashList = stream.bash();
        String expr = stream.expr();

        if (bashList.isEmpty()) return stream;
        for (int i = 1; i < bashList.size(); i++) {
            String bashLast = bashList.get(i);
            if (bashLast.startsWith("set ")) {
                String[] setInfos = bashLast.split(" ");
                String var0 = setInfos[1], midVar = setInfos[2];
                String bashFormer = bashList.get(i - 1);
                int ctrlOffset = offsetMap.getOrDefault(bashFormer.split(" ")[0], -1);
                if (ctrlOffset != -1 && bashFormer.split(" ")[ctrlOffset].equals(midVar)) {
                    bashList.set(i - 1, bashFormer.replaceFirst(midVar, var0));
                    bashList.remove(i);
                    i--;
                    if (midVar.equals(expr)) expr = "";
                }
            }
        }
        return stdCodeStream.of(bashList, expr, stream.stat());
    }

    /**
     * 转换{@code if/for/while}为原生{@code jump}
     */
    static stdCodeStream convertJump(stdCodeStream stream) {
        ArrayList<String> bashList = stream.bash();
        bashList.removeIf(String::isEmpty);
        int[] ref = {stream.stat()};

        final String keyStart = "{", keyEnd = "}";
        while (true) {
            int matchIndex = 0, lineIndex = -1, line2Index = -1;
            for (int i = 0; i < bashList.size(); i++) {
                String line = bashList.get(i);
                if (line.endsWith(keyStart)) {
                    if (lineIndex == -1) lineIndex = i;
                    matchIndex++;
                }
                if (line.startsWith(keyEnd)) {
                    if (matchIndex == 1) line2Index = i;
                    matchIndex--;
                }
                if (line2Index != -1) break;
            }
            if (line2Index == -1) {
                if (lineIndex != -1) {
                    Utils.printError("Error: {} not match at line " + lineIndex);
                    Utils.printError(lineIndex + " " + bashList.get(lineIndex));
                    return stdCodeStream.of();
                } else break;
            }

            String line = bashList.get(lineIndex), line2 = bashList.get(line2Index);
            ArrayList<String> bashCache;
            String tagTo = "TAG." + ref[0];

            if (line.startsWith("if(")) {
                tagTo += "_endIf";
                int start = line.indexOf("("), end = Utils.getEndBracket(line, start);
                String bracketContent = line.substring(start + 1, end);

                String jumpString = "jump(" + tagTo + ").when(" + bracketContent + ")";
                ArrayList<String> initStream = convertCtrl(stdCodeStream.of(jumpString)).bash();
                jumpString = Utils.reverseCondition(initStream.getLast());
                initStream.set(initStream.size() - 1, jumpString);

                bashList.set(line2Index, "::" + tagTo);
                bashList.remove(lineIndex);
                bashList.addAll(lineIndex, initStream);
                ref[0]++;
            } else if (line.startsWith("else{")) {
                tagTo += "_endElse";

                String endIfLine = bashList.get(lineIndex - 1);
                if (!endIfLine.startsWith("::") || !endIfLine.endsWith("_endIf")) {
                    Utils.printError("Error: else not match at line " + lineIndex);
                    return stdCodeStream.of();
                }
                String jumpString = "jump " + tagTo + " always 0 0";

                bashList.set(line2Index, "::" + tagTo);
                bashList.remove(lineIndex);
                bashList.add(lineIndex - 1, jumpString);
                ref[0]++;
            } else if (line.startsWith("do{")) {
                tagTo += "_do";
                int start = line2.indexOf("("), end = Utils.getEndBracket(line2, start);
                String bracketContent = line2.substring(start + 1, end);

                String jumpString = "jump(" + tagTo + ").when(" + bracketContent + ")";
                bashCache = convertCtrl(stdCodeStream.of(jumpString)).bash();

                bashList.remove(line2Index);
                bashList.addAll(line2Index, bashCache);
                bashList.set(lineIndex, "::" + tagTo);
                ref[0]++;
            } else if (line.startsWith("for(")) {
                String tagEnd = tagTo + "_endFor";
                tagTo += "_for";
                int start = line.indexOf("("), end = Utils.getEndBracket(line, start);
                String bracketContent = line.substring(start + 1, end);
                bashList.set(lineIndex, "::TAG." + ref[0]);

                String[] forParts = bracketContent.split(";");
                if (forParts.length != 3) {
                    Utils.printError("Error: for() content not match");
                    return stdCodeStream.of();
                }
                ArrayList<String> initStream = convertCodeLine(stdCodeStream.of(forParts[0])).bash();

                String jumpString = "jump(" + tagEnd + ").when(" + forParts[1] + ")";

                ArrayList<String> conditionStream = convertCtrl(stdCodeStream.of(jumpString)).bash();
                jumpString = Utils.reverseCondition(conditionStream.getLast());
                conditionStream.set(conditionStream.size() - 1, jumpString);
                conditionStream.addFirst("::" + tagTo);

                ArrayList<String> operateStream = convertCodeLine(stdCodeStream.of(forParts[2])).bash();
                operateStream.add("jump " + tagTo + " always 0 0");
                operateStream.add("::" + tagEnd);

                bashList.remove(line2Index);
                bashList.addAll(line2Index, operateStream);
                bashList.remove(lineIndex);
                bashList.addAll(lineIndex, conditionStream);
                bashList.addAll(lineIndex, initStream);
                ref[0]++;
            } else {
                Utils.printError("Undefined loop type of " + line);
            }
        }
        return stdCodeStream.of(bashList);
    }

    /**
     * 将{@code jump}中的动态链接转为静态
     */
    static stdCodeStream convertLink(stdCodeStream stream) {
        ArrayList<String> bashList = stream.bash();
        String expr = stream.expr();

        if (Main.primeCodeLevel == 2) {
            String filePath = Main.filePath;
            if (filePath.endsWith(".mdtc") && !filePath.endsWith("_prime.mdtc")) {
                String primeCodePath = filePath.replace(".mdtc", "_prime.mdtc");
                String writeContent = Utils.stringBlockOf(bashList);
                Utils.writeFile(primeCodePath, writeContent);

                IO.println("PrimeCode output at:\n> " + primeCodePath);
            } else IO.println("Skip writing prime code.");
        }

        Utils.removeSpareTags(bashList);
        if (!bashList.isEmpty()) {
            bashList.add(bashList.size() - 1, "::END");
            if (bashList.getLast().startsWith("::"))
                bashList.add("end");
            if (!bashList.contains("::DEFAULT"))
                bashList.addFirst("::DEFAULT");
            bashList.addFirst("::HEAD");
        }

        int tagNum;
        for (int i = 0; i < bashList.size(); i++) {
            String line = bashList.get(i);
            tagNum = 0;
            if (line.startsWith("jump ")) {
                String[] parts = line.split(" ", 3);
                if (parts.length > 1) {
                    String target = parts[1].trim();
                    int index = -1;
                    for (int j = 0; j < bashList.size(); j++) {
                        String codeLine = bashList.get(j);
                        if (bashList.get(j).startsWith("::")) {
                            String tag = codeLine.substring(2).trim();
                            if (tag.equals(target)) {
                                index = j - tagNum;
                                break;
                            }
                            tagNum++;
                        }
                    }
                    if (index >= 0) {
                        String jumpString = String.join(" ", "jump", index + "", parts[2]);
                        bashList.set(i, jumpString);
                    } else {
                        Utils.printError("Compile Error: jump() tag not found of [" + target + "]");
                        Utils.printError(i + 1 + " " + line);
                        return stream;
                    }
                }
            }
        }

        bashList.removeIf(line -> line.startsWith("::"));
        return stdCodeStream.of(bashList, expr);
    }

    /**
     * 将函数块分离到函数,暂存于funcMap
     */
    static HashMap<Integer, stdFuncStream> generateFuncMap(String funcBlock) {
        final String keyStart = "function", keyEnd = "}";
        final String[] keysJump = {"do{", "for(", "if(", "else{"};
        HashMap<Integer, stdFuncStream> funcMap = new HashMap<>();

        ArrayList<String> bashList = new ArrayList<>(List.of(funcBlock.split("\n")));
        ArrayList<String> bashCache = new ArrayList<>();

        int matchIndex = 0;
        String funcName = "";
        List<String> varsList = List.of(), tagsList;

        for (String bash : bashList) {
            if (bash.startsWith(keyEnd)) {
                matchIndex--;
                if (matchIndex == 0) {
                    tagsList = bashCache.stream()
                            .filter(s -> s.startsWith("::"))
                            .map(s -> s.substring(2))
                            .toList();

                    stdFuncStream funcStream = stdFuncStream.of(funcName, bashCache, varsList, tagsList);
                    int funcHash = Objects.hash(funcName, funcStream.varsCount());
                    funcMap.put(funcHash, funcStream);
                    bashCache = new ArrayList<>();
                }
            }

            if (matchIndex > 0) bashCache.add(bash.trim());

            if (bash.startsWith(keyStart)) {
                String[] funcSplit = bash.split(" ");
                if (funcSplit.length < 3) {
                    Utils.printError("Bad definition of function <anonymous>");
                    return funcMap;
                }
                String returnValue = funcSplit[1], funcHead = funcSplit[2];
                int argsStart = funcHead.indexOf("("), argsEnd = Utils.getEndBracket(funcHead, argsStart);
                funcName = funcHead.substring(0, argsStart + 1);
                if (argsEnd == -1) {
                    Utils.printError("Bad definition of function " + funcHead);
                }
                String[] funcArgs = funcHead.substring(argsStart + 1, argsEnd).split(",");

                varsList = new ArrayList<>();
                varsList.add(returnValue);
                varsList.addAll(Arrays.asList(funcArgs));

                matchIndex++;
            }
            for (var key : keysJump) {
                if (bash.startsWith(key)) {
                    matchIndex++;
                    break;
                }
            }
        }
        return funcMap;
    }
}
