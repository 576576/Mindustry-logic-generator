package cn.sumitm.mdtc.lsp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.FormattingOptions;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * LSP 服务器单元测试:验证诊断(错误/警告)、补全、格式化、悬停。
 */
class LspServerTest {

    /** 记录 publishDiagnostics 的假客户端 */
    static class FakeClient implements LanguageClient {
        final List<PublishDiagnosticsParams> published = new ArrayList<>();

        @Override
        public void telemetryEvent(Object o) { }

        @Override
        public void publishDiagnostics(PublishDiagnosticsParams params) {
            published.add(params);
        }

        @Override
        public void showMessage(MessageParams params) { }

        @Override
        public CompletableFuture<MessageActionItem> showMessageRequest(org.eclipse.lsp4j.ShowMessageRequestParams params) {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void logMessage(MessageParams params) { }
    }

    private MdtcTextDocumentService service;
    private FakeClient client;

    @BeforeEach
    void setUp() {
        client = new FakeClient();
        service = new MdtcTextDocumentService(client);
    }

    private void open(String uri, String text) {
        TextDocumentItem doc = new TextDocumentItem(uri, "mdtc", 0, text);
        service.didOpen(new DidOpenTextDocumentParams(doc));
    }

    private List<Diagnostic> lastDiagnostics() {
        assertThat(client.published).isNotEmpty();
        return client.published.get(client.published.size() - 1).getDiagnostics();
    }

    @Test
    void diagnostics_reportsCompileError() {
        open("file:///test.mdtc", "if(x==0){\nprint(flush)");
        List<Diagnostic> ds = lastDiagnostics();
        assertThat(ds).anyMatch(d -> d.getSeverity() == DiagnosticSeverity.Error);
    }

    @Test
    void diagnostics_reportsNegativeLiteralWarning() {
        open("file:///test.mdtc", "x=(1 + -1)\nprint(flush)");
        List<Diagnostic> ds = lastDiagnostics();
        assertThat(ds).anyMatch(d -> d.getSeverity() == DiagnosticSeverity.Warning
            && d.getMessage().contains("-1"));
    }

    @Test
    void diagnostics_warningRange_isOnSourceLine() {
        // 第 3 行的中置运算符后负数 → 警告 range 应定位到第 3 行,而非整个文档
        open("file:///test.mdtc", "print(flush)\nx=1\ny=2 + -3");
        Diagnostic warn = lastDiagnostics().stream()
            .filter(d -> d.getSeverity() == DiagnosticSeverity.Warning)
            .findFirst().orElse(null);
        assertThat(warn).isNotNull();
        assertThat(warn.getRange().getStart().getLine()).isEqualTo(2);
        assertThat(warn.getRange().getEnd().getLine()).isEqualTo(2);
    }

    @Test
    void diagnostics_warningBelowCommentAndBlankLines() {
        // 注释与空行之后的负数警告 → 仍精确标在源码行(回归:行号偏移)
        open("file:///test.mdtc", "::注释\n\n\n::计算\nx=1 + -1");
        Diagnostic warn = lastDiagnostics().stream()
            .filter(d -> d.getSeverity() == DiagnosticSeverity.Warning)
            .findFirst().orElse(null);
        assertThat(warn).isNotNull();
        assertThat(warn.getRange().getStart().getLine()).isEqualTo(4);
        assertThat(warn.getRange().getEnd().getLine()).isEqualTo(4);
    }

    @Test
    void diagnostics_warningWithFunctionAbove() {
        // 函数定义在上方(编译时函数块被提取,行号会变)→ 警告仍标在原文行
        String code = "function void f(x){\n\tprint(x)\n}\n\n::主\nx=1 + -1";
        open("file:///test.mdtc", code);
        Diagnostic warn = lastDiagnostics().stream()
            .filter(d -> d.getSeverity() == DiagnosticSeverity.Warning)
            .findFirst().orElse(null);
        assertThat(warn).isNotNull();
        assertThat(warn.getRange().getStart().getLine()).isEqualTo(5);
        assertThat(warn.getRange().getEnd().getLine()).isEqualTo(5);
    }

    @Test
    void diagnostics_cleanCode_noDiagnostics() {
        open("file:///test.mdtc", "x=1 + 2\nprint(flush)");
        assertThat(lastDiagnostics()).isEmpty();
    }

    @Test
    void diagnostics_onDidChange() {
        open("file:///test.mdtc", "print(flush)");
        assertThat(lastDiagnostics()).isEmpty();

        VersionedTextDocumentIdentifier id = new VersionedTextDocumentIdentifier();
        id.setUri("file:///test.mdtc");
        org.eclipse.lsp4j.DidChangeTextDocumentParams change =
            new org.eclipse.lsp4j.DidChangeTextDocumentParams();
        change.setTextDocument(id);
        change.setContentChanges(List.of(new TextDocumentContentChangeEvent("x=(1 + -1)")));
        service.didChange(change);

        List<Diagnostic> ds = lastDiagnostics();
        assertThat(ds).anyMatch(d -> d.getSeverity() == DiagnosticSeverity.Warning);
    }

    @Test
    void completion_listsInstructions() {
        open("file:///test.mdtc", "print(flush)");
        Either<List<CompletionItem>, CompletionList> res =
            service.completion(new org.eclipse.lsp4j.CompletionParams()).join();
        CompletionList list = res.isRight() ? res.getRight() : null;
        assertThat(list).isNotNull();
        assertThat(list.getItems()).isNotEmpty();
        assertThat(list.getItems()).anyMatch(i -> i.getLabel().equals("print("));
        assertThat(list.getItems()).anyMatch(i -> i.getLabel().equals(".sensor("));
    }

    @Test
    void formatting_returnsFullTextEdit() {
        open("file:///test.mdtc", "if(x==0){\nprint(flush)\n}");
        DocumentFormattingParams params = new DocumentFormattingParams();
        params.setTextDocument(new TextDocumentIdentifier("file:///test.mdtc"));
        params.setOptions(new FormattingOptions());
        List<? extends TextEdit> edits = service.formatting(params).join();
        assertThat(edits).isNotEmpty();
    }

    @Test
    void hover_returnsInstructionDoc() {
        open("file:///test.mdtc", "print(flush)");
        HoverParams params = new HoverParams();
        params.setTextDocument(new TextDocumentIdentifier("file:///test.mdtc"));
        params.setPosition(new Position(0, 2));
        Hover hover = service.hover(params).join();
        assertThat(hover).isNotNull();
        assertThat(hover.getContents().getLeft()).asString().isNotBlank();
    }

    @Test
    void hover_unknownWord_returnsNull() {
        open("file:///test.mdtc", "zzznotaninstruction");
        HoverParams params = new HoverParams();
        params.setTextDocument(new TextDocumentIdentifier("file:///test.mdtc"));
        params.setPosition(new Position(0, 1));
        assertThat(service.hover(params).join()).isNull();
    }

    // ==================== 语义高亮 ====================

    @Test
    void semanticTokens_coversAllCategories() {
        String code = "::循环\nfor(i=0;i<10;i=i+1){\n\tprint(\"hi\")\n\t@copper=1\n}\n";
        open("file:///test.mdtc", code);
        org.eclipse.lsp4j.SemanticTokensParams stp = new org.eclipse.lsp4j.SemanticTokensParams();
        stp.setTextDocument(new TextDocumentIdentifier("file:///test.mdtc"));
        org.eclipse.lsp4j.SemanticTokens st = service.semanticTokensFull(stp).join();
        assertThat(st).isNotNull();
        assertThat(st.getData()).isNotEmpty();

        // 解码 token 类型集合:data 每 5 个一组 [deltaLine, deltaStart, len, typeIdx, mod]
        List<Integer> types = new ArrayList<>();
        int line = 0, col = 0;
        var data = st.getData();
        for (int i = 0; i + 4 < data.size(); i += 5) {
            line += data.get(i);
            col = data.get(i + 1) == 0 && i > 0 ? col : (data.get(i) == 0 ? col + data.get(i + 1) : data.get(i + 1));
            // 简化:直接收集 typeIdx
            types.add(data.get(i + 3));
        }
        // 注释(label 0)/字符串(1)/数字(2)/关键字(4)/指令(5)/@常量(8)都出现过
        assertThat(types).contains(0); // :: 注释
        assertThat(types).contains(1); // 字符串
        assertThat(types).contains(2); // 数字
        assertThat(types).contains(4); // for( 关键字
        assertThat(types).contains(5); // print( 指令
        assertThat(types).contains(8); // @copper
    }

    // ==================== jump/jump2 标签 goto ====================

    private static final String JUMP_CODE =
        "::TAG.5\nset e 3\njump(TAG.5)\njump2(TAG.5)\n::bind.end\nend()\njump2(bind.end)\n";

    @Test
    void definition_jumpTarget_findsLabel() {
        open("file:///test.mdtc", JUMP_CODE);
        org.eclipse.lsp4j.DefinitionParams p = new org.eclipse.lsp4j.DefinitionParams();
        p.setTextDocument(new TextDocumentIdentifier("file:///test.mdtc"));
        p.setPosition(new Position(2, 7)); // jump(TAG.5) 内
        var res = service.definition(p).join();
        assertThat(res).isNotNull();
        assertThat(res.getLeft()).hasSize(1);
        assertThat(res.getLeft().get(0).getRange().getStart().getLine()).isEqualTo(0);
    }

    @Test
    void definition_jump2Target_findsLabel() {
        open("file:///test.mdtc", JUMP_CODE);
        org.eclipse.lsp4j.DefinitionParams p = new org.eclipse.lsp4j.DefinitionParams();
        p.setTextDocument(new TextDocumentIdentifier("file:///test.mdtc"));
        p.setPosition(new Position(3, 7)); // jump2(TAG.5) 内
        var res = service.definition(p).join();
        assertThat(res).isNotNull();
        assertThat(res.getLeft()).hasSize(1);
        assertThat(res.getLeft().get(0).getRange().getStart().getLine()).isEqualTo(0);
    }

    @Test
    void definition_onLabel_returnsReferences() {
        open("file:///test.mdtc", JUMP_CODE);
        org.eclipse.lsp4j.DefinitionParams p = new org.eclipse.lsp4j.DefinitionParams();
        p.setTextDocument(new TextDocumentIdentifier("file:///test.mdtc"));
        p.setPosition(new Position(0, 4)); // ::TAG.5 定义行
        var res = service.definition(p).join();
        assertThat(res).isNotNull();
        assertThat(res.getLeft()).hasSize(2); // jump(TAG.5) + jump2(TAG.5)
        assertThat(res.getLeft()).allMatch(l -> l.getRange().getStart().getLine() >= 2);
    }

    @Test
    void definition_unknownTarget_returnsNull() {
        open("file:///test.mdtc", JUMP_CODE);
        org.eclipse.lsp4j.DefinitionParams p = new org.eclipse.lsp4j.DefinitionParams();
        p.setTextDocument(new TextDocumentIdentifier("file:///test.mdtc"));
        p.setPosition(new Position(6, 3)); // 非 jump 行
        assertThat(service.definition(p).join()).isNull();
    }
}
