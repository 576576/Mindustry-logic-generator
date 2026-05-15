package cn.sumitm.mdtc.core;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class ConstantsTest {

    @Test
    void operatorMapsAreBuilt() {
        assertThat(Constants.midOpKeysMap).isNotEmpty();
        assertThat(Constants.midOpValueMap).isNotEmpty();
    }

    @Test
    void midOpKeysMap_containsBasicOps() {
        assertThat(Constants.midOpKeysMap).containsKeys("+", ".-", "==", "!=", ">=");
        assertThat(Constants.midOpKeysMap.get("+")).isEqualTo("add");
        assertThat(Constants.midOpKeysMap.get(".-")).isEqualTo("sub");
    }

    @Test
    void midOpValueMap_isReverse() {
        assertThat(Constants.midOpValueMap.get("add")).isEqualTo("+");
        assertThat(Constants.midOpValueMap.get("equal")).isEqualTo("==");
    }

    @Test
    void operatorReverseMap_hasOpposites() {
        assertThat(Constants.operatorReverseMap.get("equal")).isEqualTo("notEqual");
        assertThat(Constants.operatorReverseMap.get("notEqual")).isEqualTo("equal");
        assertThat(Constants.operatorReverseMap.get("always")).isEqualTo("never");
    }

    @Test
    void midOpPriorityMap_hasAllOperators() {
        assertThat(Constants.midOpPriorityMap).containsKeys("+", "*", "/");
        assertThat(Constants.midOpPriorityMap.get("*")).isEqualTo(5);
        assertThat(Constants.midOpPriorityMap.get("+")).isEqualTo(4);
    }

    @Test
    void supportFormats() {
        assertThat(Constants.supportFormats).containsExactly(".mdtc", ".mdtcode", ".libmdtc");
    }

    @Test
    void dotCtrlCodes_containsExpected() {
        assertThat(Constants.dotCtrlCodes).contains(".enable(", ".shoot(", ".color(");
    }

    @Test
    void ctrlCodes_containsExpected() {
        assertThat(Constants.ctrlCodes).contains("print(", "jump(", "wait(");
    }

    @Test
    void trueAndFalseConditions() {
        assertThat(Constants.trueCondition).isEqualTo("always 0 0");
        assertThat(Constants.falseCondition).isEqualTo("notEqual 0 0");
    }
}
