package cn.sumitm.mdtc.lsp;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.SemanticTokensLegend;
import org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

/**
 * MdtC 语言服务器:为 .mdtc/.libmdtc 提供诊断、补全、格式化、悬停。
 * 通过 stdio 与客户端(VSCode 扩展)通信,由 LspMain 启动。
 */
class MdtcLanguageServer implements LanguageServer, LanguageClientAware {

    private final MdtcTextDocumentService textDocumentService;
    private final WorkspaceService workspaceService = new WorkspaceService() {
        @Override
        public void didChangeConfiguration(org.eclipse.lsp4j.DidChangeConfigurationParams params) {
            // 忽略
        }

        @Override
        public void didChangeWatchedFiles(org.eclipse.lsp4j.DidChangeWatchedFilesParams params) {
            // 忽略
        }
    };

    MdtcLanguageServer() {
        this.textDocumentService = new MdtcTextDocumentService(null);
    }

    @Override
    public void connect(LanguageClient client) {
        this.textDocumentService.connect(client);
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
        // 诊断消息按客户端 locale 本地化(zh → 中文,其余回退 en)
        LspMessages.setLocale(params.getLocale());
        ServerCapabilities caps = new ServerCapabilities();
        caps.setTextDocumentSync(TextDocumentSyncKind.Full);
        caps.setCompletionProvider(new CompletionOptions(false, List.of("(", ".")));
        caps.setDocumentFormattingProvider(true);
        caps.setHoverProvider(true);
        caps.setDefinitionProvider(true); // jump/jump2 标签 goto
        SemanticTokensWithRegistrationOptions st = new SemanticTokensWithRegistrationOptions(
            new SemanticTokensLegend(MdtcSemanticTokens.TOKEN_TYPES, List.of()));
        st.setFull(true);
        caps.setSemanticTokensProvider(st);
        return CompletableFuture.completedFuture(new InitializeResult(caps));
    }

    @Override
    public void setTrace(org.eclipse.lsp4j.SetTraceParams params) {
        // 忽略客户端 trace 设置
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void exit() {
        System.exit(0);
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return textDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return workspaceService;
    }
}
