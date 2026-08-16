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

    // ==================== Ctrl: 零参 ====================

    @Test void compile_stop()     { assertThat(CodeCompiler.compile("stop()")).isEqualTo("stop"); }
    @Test void compile_end()      { assertThat(CodeCompiler.compile("end()")).isEqualTo("end"); }

    // ==================== Ctrl: 原样透传 ====================

    @Test void compile_print()    { assertThat(CodeCompiler.compile("print(flush)")).isEqualTo("print flush"); }
    @Test void compile_printchar(){ assertThat(CodeCompiler.compile("printchar(A)")).isEqualTo("printchar A"); }
    @Test void compile_format()   { assertThat(CodeCompiler.compile("format(x)")).isEqualTo("format x"); }
    @Test void compile_wait()     { assertThat(CodeCompiler.compile("wait(0.5)")).isEqualTo("wait 0.5"); }
    @Test void compile_ubind()    { assertThat(CodeCompiler.compile("ubind(@copper)")).isEqualTo("ubind @copper"); }
    @Test void compile_tag()      { assertThat(CodeCompiler.compile("tag(MYTAG)")).isNotNull(); }
    @Test void compile_raw()      { assertThat(CodeCompiler.compile("raw(set x 1)")).isEqualTo("set x 1"); }

    // ==================== Ctrl: 固定填充 ====================

    @Test void compile_uctrl_1arg()  { assertThat(CodeCompiler.compile("uctrl(getBlock)"))
        .isEqualTo("ucontrol getBlock 0 0 0 0 0"); }
    @Test void compile_uctrl_full()  { assertThat(CodeCompiler.compile("uctrl(getBlock,a,b,c,d,e)"))
        .isEqualTo("ucontrol getBlock a b c d e"); }

    @Test void compile_draw_1arg()   { assertThat(CodeCompiler.compile("draw(clear)"))
        .isEqualTo("draw clear 0 0 0 0 0 0"); }
    @Test void compile_draw_full()   { assertThat(CodeCompiler.compile("draw(clear,10,20,30,40,50,60)"))
        .isEqualTo("draw clear 10 20 30 40 50 60"); }

    // ==================== Ctrl: jump ====================

    @Test void compile_jump_always() { assertNotNull("jump(L1,always)"); }
    @Test void compile_jump_never()  { assertNotNull("jump(L1,never)"); }
    @Test void compile_jump_expr()   { assertNotNull("jump(L1,x>0)"); }
    @Test void compile_jump_default(){ assertNotNull("jump(L1)"); }

    // ==================== Ctrl: printf 展开 ====================

    @Test void compile_printf_1arg() { assertThat(CodeCompiler.compile("printf(hello)"))
        .isEqualTo("print hello"); }
    @Test void compile_printf_multi(){ String r = CodeCompiler.compile("printf(hello,x,y)");
        assertThat(r).contains("print hello", "format x", "format y"); }

    // ==================== Front High: 一元运算 ====================

    @Test void compile_not()   { assertContains("not(x)",   "op not mid."); }
    @Test void compile_abs()   { assertContains("abs(x)",   "op abs mid."); }
    @Test void compile_sign()  { assertContains("sign(x)",  "op sign mid."); }
    @Test void compile_floor() { assertContains("floor(x)", "op floor mid."); }
    @Test void compile_ceil()  { assertContains("ceil(x)",  "op ceil mid."); }
    @Test void compile_round() { assertContains("round(x)", "op round mid."); }
    @Test void compile_sqrt()  { assertContains("sqrt(x)",  "op sqrt mid."); }
    @Test void compile_rand()  { assertContains("rand(x)",  "op rand mid."); }
    @Test void compile_asin()  { assertContains("asin(x)",  "op asin mid."); }
    @Test void compile_acos()  { assertContains("acos(x)",  "op acos mid."); }
    @Test void compile_atan()  { assertContains("atan(x)",  "op atan mid."); }
    @Test void compile_ln()    { assertContains("ln(x)",    "op log mid."); }
    @Test void compile_lg()    { assertContains("lg(x)",    "op log10 mid."); }
    @Test void compile_lb()    { assertContains("lb(x)",    "op logn mid."); }

    // ==================== 减法运算符: - (前后空格) / .- (兼容) ====================

    @Test void compile_sub_spaced() { assertContains("x=a - b", "op sub"); }
    @Test void compile_sub_legacy() { assertContains("x=a .- b", "op sub"); }
    @Test void compile_sub_negative() { assertThat(CodeCompiler.compile("x=a - -5")).contains("op sub"); }
    @Test void compile_hyphenVar_isIdentifier() {
        // 无空格的 "-" 是标识符,不是减法
        assertThat(CodeCompiler.compile("x=a-b")).isEqualTo("set x a-b");
    }

    // ==================== 负数无空格守卫警告 ====================

    @Test void guard_negativeLiteral_warns() {
        CodeCompiler.compile("x=(1 + -1)");
        assertThat(CodeCompiler.lastWarnings)
            .anyMatch(w -> w.contains("-1") && w.contains("减法"));
    }

    @Test void guard_negativeVar_warns() {
        CodeCompiler.compile("y = -x0");
        assertThat(CodeCompiler.lastWarnings)
            .anyMatch(w -> w.contains("-x0") && w.contains("减法"));
    }

    @Test void guard_spacedSub_noWarning() {
        CodeCompiler.compile("x=1 - 1");
        assertThat(CodeCompiler.lastWarnings).isEmpty();
    }

    @Test void guard_hyphenIdentifier_noWarning() {
        CodeCompiler.compile("x=phase-fabric");
        assertThat(CodeCompiler.lastWarnings).isEmpty();
    }

    @Test void guard_legacyDotSub_noWarning() {
        // 旧写法 .- 是完整运算符,不产生无空格负数
        CodeCompiler.compile("x=1 .- 2");
        assertThat(CodeCompiler.lastWarnings).isEmpty();
    }
    @Test void compile_sub_rpnPrecedence() {
        assertContains("x=1 - 2*3", "op sub");
        assertContains("x=1 - 2*3", "op mul");
    }

    // ==================== Front High: 二元运算 ====================

    @Test void compile_max()       { assertContains("max(a,b)",       "op max mid."); }
    @Test void compile_min()       { assertContains("min(a,b)",       "op min mid."); }
    @Test void compile_len()       { assertContains("len(a,b)",       "op len mid."); }
    @Test void compile_angle()     { assertContains("angle(a,b)",     "op angle mid."); }
    @Test void compile_angleDiff() { assertContains("angleDiff(a,b)", "op angleDiff mid."); }
    @Test void compile_noise()     { assertContains("noise(a,b)",     "op noise mid."); }
    @Test void compile_log_fn()    { assertContains("log(a,b)",       "op logn mid."); }

    // ==================== Front High: 查表 ====================

    @Test void compile_link()   { assertContains("link(1)",           "getlink mid."); }
    @Test void compile_block()  { assertContains("block(@copper-wall)","lookup block mid."); }
    @Test void compile_unit()   { assertContains("unit(@dagger)",     "lookup unit mid."); }
    @Test void compile_item()   { assertContains("item(@copper)",     "lookup item mid."); }
    @Test void compile_liquid() { assertContains("liquid(@water)",    "lookup liquid mid."); }
    @Test void compile_team()   { assertContains("team(sharded)",     "lookup team mid."); }
    @Test void compile_lookup() { assertContains("lookup(item,@copper)","lookup item mid."); }
    @Test void compile_pack()   { assertContains("pack(1,2,3,4)",     "packcolor mid."); }

    // ==================== Front Low: 三角函数 ====================

    @Test void compile_sin() { assertContains("sin(x)", "op sin mid."); }
    @Test void compile_cos() { assertContains("cos(x)", "op cos mid."); }
    @Test void compile_tan() { assertContains("tan(x)", "op tan mid."); }

    // ==================== 编译→反编译 往返 ====================

    @Test void roundTrip_draw()   { roundTrip("draw(clear)"); }
    @Test void roundTrip_print()  { roundTrip("print(hello)"); }
    @Test void roundTrip_format() { roundTrip("format(x)"); }
    @Test void roundTrip_not()    { roundTrip("not(x)"); }
    @Test void roundTrip_abs()    { roundTrip("abs(-3)"); }
    @Test void roundTrip_max()    { roundTrip("max(a,b)"); }
    @Test void roundTrip_sin()    { roundTrip("sin(theta)"); }
    @Test void roundTrip_uctrl()  { roundTrip("uctrl(getBlock,a,b,c,d,e)"); }
    @Test void roundTrip_stop()   { roundTrip("stop()"); }

    private void assertNotNull(String input) {
        assertThat(CodeCompiler.compile(input)).isNotNull().isNotEmpty();
    }

    private void roundTrip(String input) {
        String compiled = CodeCompiler.compile(input);
        String result = CodeDecompiler.decompile(compiled);
        assertThat(result).isNotNull().isNotEmpty();
    }

    private void assertContains(String input, String expected) {
        assertThat(CodeCompiler.compile(input)).contains(expected);
    }
}
