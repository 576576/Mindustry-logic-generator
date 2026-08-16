import * as vscode from "vscode";
import {
  LanguageClient,
  LanguageClientOptions,
  ServerOptions,
  TransportKind,
  Trace,
} from "vscode-languageclient/node";

let client: LanguageClient | undefined;

export function activate(context: vscode.ExtensionContext) {
  const config = vscode.workspace.getConfiguration("mdtc");
  const jarPath: string =
    config.get<string>("lsp.jarPath") ||
    context.asAbsolutePath("server/mdtc-3.0.1-Lsp.jar");
  const extraArgs: string[] = config.get<string[]>("lsp.javaArgs") || [];
  const traceLevel: string = config.get<string>("lsp.trace") || "off";

  const traceCh = vscode.window.createOutputChannel("MdtC LSP Trace");
  traceCh.appendLine(
    "[MdtC] activating: jar=" + jarPath + " trace=" + traceLevel
  );

  const serverOptions: ServerOptions = {
    command: "java",
    args: [...extraArgs, "-jar", jarPath],
    transport: TransportKind.stdio,
    options: {
      cwd: vscode.workspace.rootPath || undefined,
    },
  };

  const clientOptions: LanguageClientOptions = {
    documentSelector: [{ scheme: "file", language: "mdtc" }],
    outputChannelName: "MdtC Language Server",
    diagnosticCollectionName: "mdtc",
    traceOutputChannel: traceCh,
  };

  client = new LanguageClient(
    "mdtcLsp",
    "MdtC Language Server",
    serverOptions,
    clientOptions
  );

  void client.start().then(() => {
    if (client) {
      client.setTrace(
        traceLevel === "verbose"
          ? Trace.Verbose
          : traceLevel === "messages"
            ? Trace.Messages
            : Trace.Off
      );
    }
  });
  context.subscriptions.push(new vscode.Disposable(() => client?.stop()));
}

export function deactivate(): Thenable<void> | undefined {
  return client?.stop();
}
