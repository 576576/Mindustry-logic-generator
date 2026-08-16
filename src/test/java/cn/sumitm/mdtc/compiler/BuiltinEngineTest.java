package cn.sumitm.mdtc.compiler;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import cn.sumitm.mdtc.core.BuiltinEngine;

/**
 * 内置指令引擎测试:验证 .ts 注册表加载与 文档↔注册表 一致性(文档驱动契约)。
 */
class BuiltinEngineTest {

    private static final String DOCS = "docs/instructions";

    @Test
    void engineLoadsAllCategories() {
        BuiltinEngine e = BuiltinEngine.get();
        assertThat(e.ctrl()).isNotEmpty();
        assertThat(e.dotCtrl()).isNotEmpty();
        assertThat(e.dot()).isNotEmpty();
        assertThat(e.frontHigh()).isNotEmpty();
        assertThat(e.frontLow()).isNotEmpty();
        assertThat(e.decompile()).isNotEmpty();
    }

    @Test
    void domainData() {
        BuiltinEngine e = BuiltinEngine.get();
        assertThat(e.buildingTypes()).containsExactly(
            "core", "storage", "generator", "turret", "factory",
            "repair", "battery", "reactor", "drill", "shield");
        assertThat(e.locateTypes()).containsExactly("ore", "building");
        assertThat(e.lookupTypes()).containsExactly("block", "unit", "item", "liquid", "team");
        assertThat(e.chainKeys()).contains("main", "target", "when", "order", "sort", "ore", "building", "enemy");
        assertThat(e.domainConstants().get("VAL_AT")).isEqualTo("@this");
        assertThat(e.domainConstants().get("VAL_NUL")).isEqualTo("null");
        assertThat(e.domainConstants().get("JUMP_DEFAULT")).isEqualTo("DEFAULT");
        assertThat(e.domainConstants().get("RADAR_TARGET")).isEqualTo("enemy,any,any");
        assertThat(e.domainConstants().get("CONTROL_DEFAULT")).isEqualTo("enabled");
        assertThat(e.domainConstants().get("RADAR_SORT_DEFAULT")).isEqualTo("distance");
    }

    @Test
    void operatorTable() {
        BuiltinEngine e = BuiltinEngine.get();
        assertThat(e.operatorValues()).hasSize(27);
        assertThat(e.operatorValues().getFirst()).isEqualTo("+");
        assertThat(e.operatorValues().getLast()).isEqualTo("never");
        assertThat(e.midOpValueMap().get("sub")).isEqualTo(".-");
        assertThat(e.midOpPriorityMap().get("*")).isEqualTo(5);
        assertThat(e.midOpPriorityMap().get("+")).isEqualTo(4);
        assertThat(e.operatorOffsetMap().get("radar")).isEqualTo(7);
        assertThat(e.operatorReverseMap().get("always")).isEqualTo("never");
        assertThat(e.operatorAliasMap().get("log10")).isEqualTo("lg");
        assertThat(e.subOperatorValue()).isEqualTo(".-");
    }

    @Test
    void codeTables() {
        BuiltinEngine e = BuiltinEngine.get();
        assertThat(e.ctrlCodes()).hasSize(15);
        assertThat(e.dotCtrlCodes()).hasSize(10);
        assertThat(e.dotCodes()).hasSize(3);
        assertThat(e.dotCodesAll()).hasSize(13);
        assertThat(e.dotOpReduced()).contains(".enable", ".sensor");
        assertThat(e.dotOpReduced()).doesNotContain(".enable(");
    }

    @Test
    void chainTable_containsDeclaredKeys() {
        BuiltinEngine e = BuiltinEngine.get();
        assertThat(e.chainTable().get(".ulocate(")).containsExactly("main", "ore", "building", "enemy");
        assertThat(e.chainTable().get("ushoot(")).containsExactly("main", "target");
        assertThat(e.chainTable().get(".shoot(")).containsExactly("main", "target");
        assertThat(e.chainTable().get(".orElse(")).containsExactly("main", "when");
        assertThat(e.chainTable().get("jump(")).containsExactly("main", "when");
        assertThat(e.chainTable().get("uradar(")).containsExactly("target", "sort", "order");
        assertThat(e.chainTable().get("radar(")).containsExactly("target", "sort", "main", "order");
    }

    /**
     * 链式调用检查:未知链键输出警告,但不影响编译输出。
     */
    @Test
    void unknownChainKey_emitsWarning() throws Exception {
        var err = new ByteArrayOutputStream();
        var old = System.err;
        try {
            System.setErr(new PrintStream(err, true, StandardCharsets.UTF_8));
            String out = CodeCompiler.compile("core=link(0)\ncore.ulocate(ore).badkey(1)");
            assertThat(out).contains("ulocate ore core 0 0 core.x core.y core.f core");
            assertThat(err.toString(StandardCharsets.UTF_8)).contains("unknown chain key \"badkey\"");
        } finally {
            System.setErr(old);
        }
    }

    /**
     * 文档驱动一致性:各分类文档中 `### <key>` 标题必须与注册表键完全一致。
     */
    @Test
    void docRegistryConsistency() throws IOException {
        BuiltinEngine e = BuiltinEngine.get();
        assertDocMatches("ctrl.md", e.ctrl());
        assertDocMatches("dot.md", union(e.dotCtrl(), e.dot()));
        assertDocMatches("front.md", union(e.frontHigh(), e.frontLow()));
        assertDocMatches("decompile.md", e.decompile());
    }

    private static void assertDocMatches(String doc, Map<String, ?> registry) throws IOException {
        Set<String> docKeys = docKeys(doc);
        assertThat(registry.keySet())
            .as("注册表与 %s 文档必须一致(缺少的指令={%s},多余={%s})",
                doc, minus(docKeys, registry.keySet()), minus(registry.keySet(), docKeys))
            .isEqualTo(docKeys);
    }

    private static Set<String> docKeys(String doc) throws IOException {
        Set<String> keys = new HashSet<>();
        for (String line : Files.readAllLines(Path.of(DOCS, doc))) {
            if (line.startsWith("### ")) {
                // 保留尾部形态(反编译键以空格结尾,如 "print ")
                keys.add(line.substring(4));
            }
        }
        return keys;
    }

    private static Set<String> minus(Set<String> a, Set<String> b) {
        Set<String> r = new HashSet<>(a);
        r.removeAll(b);
        return r;
    }

    private static <K, V> Map<K, V> union(Map<K, V> a, Map<K, V> b) {
        Map<K, V> u = new LinkedHashMap<>(a);
        u.putAll(b);
        return u;
    }
}
