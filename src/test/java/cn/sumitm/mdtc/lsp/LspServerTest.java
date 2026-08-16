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
}
