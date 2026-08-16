package cn.sumitm.mdtc.core;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class ConstantsTest {

    @Test
    void operatorMapsAreBuilt() {
        assertThat(BuiltinEngine.get().midOpKeysMap()).isNotEmpty();
        assertThat(BuiltinEngine.get().midOpValueMap()).isNotEmpty();
    }

    @Test
    void midOpKeysMap_containsBasicOps() {
        var m = BuiltinEngine.get().midOpKeysMap();
        assertThat(m).containsKeys("+", ".-", "==", "!=", ">=");
        assertThat(m.get("+")).isEqualTo("add");
        assertThat(m.get(".-")).isEqualTo("sub");
    }

    @Test
    void midOpValueMap_isReverse() {
        var m = BuiltinEngine.get().midOpValueMap();
        assertThat(m.get("add")).isEqualTo("+");
        assertThat(m.get("equal")).isEqualTo("==");
    }

    @Test
    void operatorReverseMap_hasOpposites() {
        var m = BuiltinEngine.get().operatorReverseMap();
        assertThat(m.get("equal")).isEqualTo("notEqual");
        assertThat(m.get("notEqual")).isEqualTo("equal");
        assertThat(m.get("always")).isEqualTo("never");
    }

    @Test
    void midOpPriorityMap_hasAllOperators() {
        var m = BuiltinEngine.get().midOpPriorityMap();
        assertThat(m).containsKeys("+", "*", "/");
        assertThat(m.get("*")).isEqualTo(5);
        assertThat(m.get("+")).isEqualTo(4);
    }

    @Test
    void supportFormats() {
        assertThat(Constants.supportFormats).containsExactly(".mdtc", ".mdtcode", ".libmdtc");
    }

    @Test
    void dotCtrlCodes_containsExpected() {
        assertThat(BuiltinEngine.get().dotCtrlCodes()).contains(".enable(", ".shoot(", ".color(");
    }

    @Test
    void ctrlCodes_containsExpected() {
        assertThat(BuiltinEngine.get().ctrlCodes()).contains("print(", "jump(", "wait(");
    }

    @Test
    void trueAndFalseConditions() {
        assertThat(Constants.JumpCondition.TRUE.id()).isEqualTo("always 0 0");
        assertThat(Constants.JumpCondition.FALSE.id()).isEqualTo("notEqual 0 0");
    }
}
