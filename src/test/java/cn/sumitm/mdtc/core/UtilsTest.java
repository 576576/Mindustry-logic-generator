package cn.sumitm.mdtc.core;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class UtilsTest {

    @Test
    void stringSplit_simpleAssignment() {
        List<String> result = Utils.stringSplit("x=1+2");
        assertThat(result).containsExactly("x", "=", "1", "+", "2");
    }

    @Test
    void stringSplit_complexExpression() {
        List<String> result = Utils.stringSplit("2>=3<3");
        assertThat(result).containsExactly("2", ">=", "3", "<", "3");
    }

    @Test
    void stringSplit_empty() {
        assertThat(Utils.stringSplit("")).isEmpty();
    }

    @Test
    void stringSplit_tagComment() {
        List<String> result = Utils.stringSplit("::mytag");
        assertThat(result).containsExactly("::", "mytag");
    }

    @Test
    void isNumeric_positive() {
        assertThat(Utils.isNumeric("123")).isTrue();
        assertThat(Utils.isNumeric("-456")).isTrue();
        assertThat(Utils.isNumeric("3.14")).isTrue();
    }

    @Test
    void isNumeric_negative() {
        assertThat(Utils.isNumeric("abc")).isFalse();
        assertThat(Utils.isNumeric("")).isFalse();
        assertThat(Utils.isNumeric("12a")).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
        "always,always",
        "never,never",
        "equal x 0,x==0",
        "notEqual x 0,x!=0",
        "lessThan x 0,x<0",
        "greaterThanEq x 0,x>=0"
    })
    void reduceCondition(String input, String expected) {
        assertThat(Utils.reduceCondition(input)).isEqualTo(expected);
    }

    @Test
    void reverseCondition() {
        assertThat(Utils.reverseCondition("equal x 0")).isEqualTo("notEqual x 0");
        assertThat(Utils.reverseCondition("always")).isEqualTo("never");
        assertThat(Utils.reverseCondition("lessThan a b")).isEqualTo("greaterThanEq a b");
    }

    @Test
    void isSpecialControl() {
        assertThat(Utils.isSpecialControl("::tag")).isTrue();
        assertThat(Utils.isSpecialControl("if(x==0){")).isTrue();
        assertThat(Utils.isSpecialControl("do{")).isTrue();
        assertThat(Utils.isSpecialControl("x=1")).isFalse();
    }

    @Test
    void getEndBracket() {
        assertThat(Utils.getEndBracket("func(a,b)", 4)).isEqualTo(8);
        assertThat(Utils.getEndBracket("a+(b+c)", 2)).isEqualTo(6);
        assertThat(Utils.getEndBracket("no paren", 0)).isEqualTo(-1);
    }

    @Test
    void padParams_defaultZero() {
        assertThat(Utils.padParams(3, "a,b")).isEqualTo("a b 0");
        assertThat(Utils.padParams(2, "x")).isEqualTo("x 0");
    }

    @Test
    void reduceParams_stripsTrailingDefault() {
        assertThat(Utils.reduceParams("0", "x 0 0")).isEqualTo("x");
        assertThat(Utils.reduceParams("0", "a b 0")).isEqualTo("a,b");
    }
}
