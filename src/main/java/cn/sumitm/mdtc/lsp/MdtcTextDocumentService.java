package cn.sumitm.mdtc.lsp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;

import cn.sumitm.mdtc.formatter.CodeFormatter;
import cn.sumitm.mdtc.core.BuiltinEngine;

/**
 * .mdtc 文本文档服务:诊断(编译错误/警告)、补全(指令键)、格式化、悬停(指令文档)。
 */
class MdtcTextDocumentService implements TextDocumentService {

    private LanguageClient client;
    /** uri → 最新文本(全量同步) */
    private final Map<String, String> documents = new TreeMap<>();

    MdtcTextDocumentService(LanguageClient client) {
        this.client = client;
    }

    void connect(LanguageClient client) {
        this.client = client;
    }

    // ==================== 生命周期 ====================

    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        documents.put(uri, params.getTextDocument().getText());
        publishDiagnostics(uri);
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        String text = params.getContentChanges().isEmpty()
            ? documents.getOrDefault(uri, "")
            : params.getContentChanges().get(params.getContentChanges().size() - 1).getText();
        documents.put(uri, text);
        publishDiagnostics(uri);
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        documents.remove(params.getTextDocument().getUri());
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        publishDiagnostics(params.getTextDocument().getUri());
    }

    // ==================== 诊断 ====================

    private void publishDiagnostics(String uri) {
        String text = documents.getOrDefault(uri, "");
        List<Diagnostic> diagnostics = CompileDiagnostics.compile(text);
        if (client != null) {
            client.publishDiagnostics(new PublishDiagnosticsParams(uri, diagnostics));
        }
    }

    // ==================== 补全 ====================

    @Override
    public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(CompletionParams params) {
        BuiltinEngine e = BuiltinEngine.get();
        List<CompletionItem> items = new ArrayList<>();

        for (String key : e.ctrl().keySet()) {
            items.add(item(key, CompletionItemKind.Function, "控制指令"));
        }
        for (String key : e.frontHigh().keySet()) {
            items.add(item(key, CompletionItemKind.Function, "一元/二元运算"));
        }
        for (String key : e.frontLow().keySet()) {
            items.add(item(key, CompletionItemKind.Function, "三角函数"));
        }
        for (String key : e.dotCtrl().keySet()) {
            items.add(item(key, CompletionItemKind.Method, "链式控制"));
        }
        for (String key : e.dot().keySet()) {
            items.add(item(key, CompletionItemKind.Method, "链式读取"));
        }

        String[] keywords = {"if(", "else{", "for(", "while(", "do{", "function ", "import ",
            "repeat(", "raw(", "return", "::"};
        for (String kw : keywords) {
            items.add(item(kw, CompletionItemKind.Keyword, "关键字"));
        }
        return CompletableFuture.completedFuture(Either.forRight(new CompletionList(false, items)));
    }

    private static CompletionItem item(String label, CompletionItemKind kind, String detail) {
        CompletionItem it = new CompletionItem(label);
        it.setKind(kind);
        it.setDetail(detail);
        it.setInsertText(label);
        return it;
    }

    // ==================== 格式化 ====================

    @Override
    public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams params) {
        String uri = params.getTextDocument().getUri();
        String text = documents.getOrDefault(uri, "");
        if (text.isEmpty()) return CompletableFuture.completedFuture(List.of());
        String formatted = CodeFormatter.format(text);
        if (formatted.isEmpty() || formatted.equals(text)) {
            return CompletableFuture.completedFuture(List.of());
        }
        String[] lines = text.split("\n", -1);
        int lastLine = Math.max(0, lines.length - 1);
        int lastChar = lines.length == 0 ? 0 : lines[lines.length - 1].length();
        Range full = new Range(new Position(0, 0), new Position(lastLine, lastChar));
        return CompletableFuture.completedFuture(List.of(new TextEdit(full, formatted)));
    }

    // ==================== 悬停 ====================

    @Override
    public CompletableFuture<Hover> hover(HoverParams params) {
        String uri = params.getTextDocument().getUri();
        String text = documents.getOrDefault(uri, "");
        if (text.isEmpty()) return CompletableFuture.completedFuture(null);

        Position pos = params.getPosition();
        String[] lines = text.split("\n", -1);
        if (pos.getLine() < 0 || pos.getLine() >= lines.length) {
            return CompletableFuture.completedFuture(null);
        }
        String line = lines[pos.getLine()];
        String word = wordAt(line, pos.getCharacter());

        String doc = InstructionDocs.lookup(word);
        if (doc != null && !doc.isEmpty()) {
            Hover hover = new Hover(List.of(Either.<String, org.eclipse.lsp4j.MarkedString>forLeft(doc)),
                new Range(new Position(pos.getLine(), 0), new Position(pos.getLine(), line.length())));
            return CompletableFuture.completedFuture(hover);
        }
        return CompletableFuture.completedFuture(null);
    }

    /** 取光标处的标识符(字母/数字/._/@/-) */
    private static String wordAt(String line, int column) {
        if (column < 0) column = 0;
        if (column > line.length()) column = line.length();
        int start = column, end = column;
        while (start > 0 && isWordChar(line.charAt(start - 1))) start--;
        while (end < line.length() && isWordChar(line.charAt(end))) end++;
        return line.substring(start, end);
    }

    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '.' || c == '@' || c == '-';
    }

    // ==================== 语义高亮 ====================

    @Override
    public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params) {
        String text = documents.getOrDefault(params.getTextDocument().getUri(), "");
        if (text.isEmpty()) return CompletableFuture.completedFuture(null);
        return CompletableFuture.completedFuture(new SemanticTokens(MdtcSemanticTokens.encode(text)));
    }

    // ==================== jump/jump2 标签 goto ====================

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>>
        definition(DefinitionParams params) {
        String uri = params.getTextDocument().getUri();
        String text = documents.getOrDefault(uri, "");
        if (text.isEmpty()) return CompletableFuture.completedFuture(null);

        String[] lines = text.split("\n", -1);
        Position pos = params.getPosition();
        if (pos.getLine() < 0 || pos.getLine() >= lines.length) {
            return CompletableFuture.completedFuture(null);
        }
        String cursorLine = lines[pos.getLine()];

        // ---- 1. 光标在标签定义行(::NAME)上 → 返回所有引用行 ----
        String labelDef = labelDefAt(cursorLine);
        if (labelDef != null && pos.getCharacter() <= cursorLine.length()) {
            List<Location> refs = new ArrayList<>();
            for (int i = 0; i < lines.length; i++) {
                String l = lines[i];
                int idx = l.indexOf("jump");
                while (idx != -1) {
                    int open = l.indexOf('(', idx);
                    if (open != -1) {
                        int close = l.indexOf(')', open);
                        if (close != -1) {
                            String arg = l.substring(open + 1, close).split(",", 2)[0].trim();
                            if (arg.equals(labelDef)) {
                                refs.add(location(uri, i));
                            }
                            idx = close;
                        } else break;
                    } else break;
                    idx = l.indexOf("jump", idx + 4);
                }
            }
            return CompletableFuture.completedFuture(
                refs.isEmpty() ? null : Either.forLeft(refs));
        }

        // ---- 2. 光标在 jump/jump2 引用上 → 返回标签定义行 ----
        String target = jumpTargetAt(cursorLine, pos.getCharacter());
        if (target != null) {
            for (int i = 0; i < lines.length; i++) {
                String l = lines[i].trim();
                if (l.startsWith("::" + target)) {
                    return CompletableFuture.completedFuture(
                        Either.forLeft(List.of(location(uri, i))));
                }
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    /** 行首标签定义:返回标签名(不含 ::);非标签行返回 null */
    private static String labelDefAt(String line) {
        String t = line.trim();
        if (!t.startsWith("::")) return null;
        String name = t.substring(2).trim();
        return name.isEmpty() ? null : name.split("\s+")[0];
    }

    /** 光标处 jump/jump2 的目标标签;非引用位置返回 null */
    private static String jumpTargetAt(String line, int column) {
        int from = 0;
        while (true) {
            int idx = line.indexOf("jump", from);
            if (idx == -1) return null;
            int open = line.indexOf('(', idx);
            if (open != -1 && open - idx <= 6) { // jump( jump2(
                int close = line.indexOf(')', open);
                if (close != -1) {
                    String arg = line.substring(open + 1, close).split(",", 2)[0].trim();
                    if (!arg.isEmpty() && column >= open + 1 && column <= close) {
                        return arg;
                    }
                }
            }
            from = idx + 4;
        }
    }

    private static Location location(String uri, int line) {
        return new Location(uri, new Range(new Position(line, 0), new Position(line, Integer.MAX_VALUE)));
    }
}
