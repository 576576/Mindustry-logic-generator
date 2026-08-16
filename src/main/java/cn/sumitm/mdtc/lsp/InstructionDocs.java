package cn.sumitm.mdtc.lsp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 指令文档查询:预解析 resources/docs/instructions/*.md 中的 "### 键" 段落,
 * 供悬停(hover)显示。
 */
final class InstructionDocs {

    /** 指令键(完整扫描键,如 "print("、"ulocate("、".sensor(")→ 文档段落 */
    private static final Map<String, String> DOCS = new LinkedHashMap<>();

    static {
        load("ctrl.md");
        load("front.md");
        load("dot.md");
        load("dotCtrl.md");
        load("domain.md");
        load("operators.md");
    }

    private static void load(String doc) {
        try (InputStream in = InstructionDocs.class.getClassLoader()
                .getResourceAsStream("docs/instructions/" + doc)) {
            if (in == null) return;
            BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            StringBuilder section = new StringBuilder();
            String currentKey = null;
            String line;
            while ((line = r.readLine()) != null) {
                if (line.startsWith("### ")) {
                    if (currentKey != null && section.length() > 0) {
                        DOCS.putIfAbsent(currentKey, section.toString().trim());
                    }
                    currentKey = line.substring(4).trim();
                    section = new StringBuilder();
                } else if (currentKey != null) {
                    section.append(line).append('\n');
                }
            }
            if (currentKey != null && section.length() > 0) {
                DOCS.putIfAbsent(currentKey, section.toString().trim());
            }
        } catch (Exception ignored) {
            // 文档缺失时悬停返回空
        }
    }

    /** 查询指令键文档(键前缀匹配,如 "print" → "print(") */
    static String lookup(String word) {
        if (word == null || word.isEmpty()) return null;
        String direct = DOCS.get(word);
        if (direct != null) return direct;
        // 前缀匹配:print → print(
        for (Map.Entry<String, String> e : DOCS.entrySet()) {
            if (e.getKey().startsWith(word) || word.startsWith(e.getKey())) {
                return e.getValue();
            }
        }
        return null;
    }
}
