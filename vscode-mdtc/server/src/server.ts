// MdtC 语言服务器(TypeScript):stdio 通信,由 VSCode 扩展启动。
// 诊断调用 Java CLI 编译;补全/悬停/语义高亮/goto 由本服务器实现。
import {
  createConnection,
  TextDocuments,
  TextDocumentSyncKind,
  ProposedFeatures,
  InitializeParams,
  CompletionItem,
  CompletionList,
  DocumentFormattingParams,
  Hover,
  HoverParams,
  SemanticTokens,
  SemanticTokensParams,
  SignatureHelp,
  SignatureHelpParams,
  DefinitionParams,
  Location,
  TextEdit,
} from "vscode-languageserver/node";
import { TextDocument } from "vscode-languageserver-textdocument";
import * as path from "node:path";
import { validateDocument } from "./diagnostics";
import { loadData, InstructionData } from "./data";
import { loadDocs, lookupDoc } from "./hover";
import { buildCompletion } from "./completion";
import { signatureAt } from "./signature";
import { formatDocument } from "./formatting";
import { encodeSemanticTokens, TOKEN_TYPES } from "./semantic";
import { definitionAt } from "./goto";
import { setLocale } from "./messages";

const connection = createConnection(ProposedFeatures.all);
const documents = new TextDocuments(TextDocument);

let cliJar = "";
let instructionData: InstructionData = { items: [], operators: [], chainByParent: new Map() };

// 开发模式(MDTC_LSP_WATCH=1):源码修改自动重启(由客户端 restart 拉起)
import * as fs from "node:fs";

connection.onInitialize((params: InitializeParams) => {
  setLocale(params.locale);
  const init = params.initializationOptions as { cliJar?: string; dataDir?: string } | undefined;
  cliJar = init?.cliJar || path.join(__dirname, "..", "data", "mdtc-Cli.jar");
  const dataDir = init?.dataDir || path.join(__dirname, "..", "data");

  instructionData = loadData(path.join(dataDir, "builtins.js"));
  loadDocs(path.join(dataDir, "docs"));

  if (process.env.MDTC_LSP_WATCH === "1") {
    try {
      const srcDir = path.join(__dirname, "..", "src");
      fs.watch(srcDir, { recursive: true }, () => {
        connection.console.log("[mdtc-lsp] source changed — restarting");
        setTimeout(() => process.exit(0), 200);
      });
    } catch { /* ignore */ }
  }

  return {
    capabilities: {
      textDocumentSync: TextDocumentSyncKind.Full,
      completionProvider: { triggerCharacters: ["(", "."] },
      signatureHelpProvider: { triggerCharacters: ["(", ","] },
      definitionProvider: true,
      documentFormattingProvider: true,
      hoverProvider: true,
      semanticTokensProvider: {
        legend: { tokenTypes: TOKEN_TYPES, tokenModifiers: [] },
        full: true,
      },
    },
  };
});

// ==================== 诊断 ====================

async function validate(doc: TextDocument): Promise<void> {
  const { diagnostics } = validateDocument(doc, cliJar);
  connection.sendDiagnostics({ uri: doc.uri, diagnostics });
}

documents.onDidChangeContent((e) => {
  void validate(e.document);
});
documents.onDidOpen((e) => {
  void validate(e.document);
});

documents.listen(connection);

// ==================== 补全 ====================

connection.onCompletion((params): CompletionList => {
  const doc = documents.get(params.textDocument.uri);
  return CompletionList.create(
    buildCompletion(instructionData, doc ?? undefined, params.position)
  );
});

// ==================== 签名帮助 ====================

connection.onSignatureHelp((params: SignatureHelpParams): SignatureHelp | null => {
  const doc = documents.get(params.textDocument.uri);
  if (!doc) return null;
  return signatureAt(doc, params.position, instructionData);
});

// ==================== 格式化 ====================

connection.onDocumentFormatting((params: DocumentFormattingParams): TextEdit[] => {
  const doc = documents.get(params.textDocument.uri);
  if (!doc) return [];
  return formatDocument(doc, cliJar);
});

// ==================== 悬停 ====================

connection.onHover((params: HoverParams): Hover | null => {
  const doc = documents.get(params.textDocument.uri);
  if (!doc) return null;
  const lines = doc.getText().split("\n");
  const pos = params.position;
  if (pos.line < 0 || pos.line >= lines.length) return null;
  const line = lines[pos.line];
  const word = wordAt(line, pos.character);
  const d = lookupDoc(word);
  if (!d) return null;
  return {
    contents: { kind: "markdown", value: d },
    range: { start: { line: pos.line, character: 0 }, end: { line: pos.line, character: line.length } },
  };
});

function wordAt(line: string, column: number): string {
  if (column < 0) column = 0;
  if (column > line.length) column = line.length;
  let s = column, e = column;
  while (s > 0 && /[A-Za-z0-9_.@-]/.test(line[s - 1])) s--;
  while (e < line.length && /[A-Za-z0-9_.@-]/.test(line[e])) e++;
  return line.substring(s, e);
}

// ==================== 语义高亮 ====================

connection.languages.semanticTokens.on((params: SemanticTokensParams): SemanticTokens => {
  const doc = documents.get(params.textDocument.uri);
  if (!doc) return { data: [] };
  return { data: encodeSemanticTokens(doc, instructionData) };
});

// ==================== goto(jump/jump2 标签) ====================

connection.onDefinition((params: DefinitionParams): Location[] => {
  const doc = documents.get(params.textDocument.uri);
  if (!doc) return [];
  const locs = definitionAt(doc, params.position.line, params.position.character);
  return locs.map((l) => ({ ...l, uri: doc.uri }));
});

connection.listen();
