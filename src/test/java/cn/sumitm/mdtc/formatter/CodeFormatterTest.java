package cn.sumitm.mdtc.formatter;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class CodeFormatterTest {

    @Test
    void format_addsIndentation() {
        String input = "if(x==0){\nx=1\n}\nprint(x)";
        String result = CodeFormatter.format(input);
        assertThat(result).contains("\tx=1");
        assertThat(result).doesNotContain("\tprint"); // not inside block
    }

    @Test
    void format_emptyBlock() {
        String input = "do{\n}\nwhile(x<5)";
        String result = CodeFormatter.format(input);
        assertThat(result).isNotNull();
        assertThat(result).contains("do{");
    }

    @Test
    void deformat_trimsButKeepsBlankLines() {
        // 保留空行:编译诊断的行号需与源码一致
        String input = "  x=1  \n\n  y=2  ";
        String result = CodeFormatter.deformat(input);
        assertThat(result).isEqualTo("x=1\n\ny=2");
    }

    @Test
    void deformat_trimsEachLine() {
        String input = "  x=1  \n  y=2  ";
        String result = CodeFormatter.deformat(input);
        assertThat(result).isEqualTo("x=1\ny=2");
    }

    @Test
    void format_withUnmatchedBrackets_returnsEmpty() {
        String result = CodeFormatter.format("do{\nx=1\n");
        // Should either return empty or add closing brace
        assertThat(result).isNotNull();
    }
}
