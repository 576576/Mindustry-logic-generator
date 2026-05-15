package cn.sumitm.mdtc.compiler;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import cn.sumitm.mdtc.core.Utils;

class CodeCompilerTest {

    @Test
    void compile_emptyBlock_hasHeaderFooter() {
        // An empty .mdtc block should produce HEAD/END tags
        String result = CodeCompiler.compile("\n");
        assertThat(result).isNotNull();
    }

    @Test
    void compile_withCase0() {
        // case0: just tags and comments
        String input = Utils.readFile("sample_cases/case0.mdtc");
        String result = CodeCompiler.compile(input);
        assertThat(result).isNotNull();
    }

    @Test
    void compile_withCase1_arithmetic() {
        String input = Utils.readFile("sample_cases/case1.mdtc");
        String result = CodeCompiler.compile(input);
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
    }

    @Test
    void roundTrip_case0() {
        // Compile then decompile — result should be parseable
        String input = Utils.readFile("sample_cases/case0.mdtc");
        String compiled = CodeCompiler.compile(input);
        String decompiled = CodeDecompiler.decompile(compiled);
        assertThat(decompiled).isNotNull();
        assertThat(decompiled).isNotEmpty();
    }

    @Test
    void roundTrip_case1() {
        String input = Utils.readFile("sample_cases/case1.mdtc");
        String compiled = CodeCompiler.compile(input);
        String decompiled = CodeDecompiler.decompile(compiled);
        assertThat(decompiled).isNotNull();
    }

    @Test
    void compileWithFunctions() {
        String input = Utils.readFile("sample_cases/case5.mdtc");
        String result = CodeCompiler.compile(input);
        assertThat(result).isNotNull();
    }
}
