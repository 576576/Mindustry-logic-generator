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
  const traceLevel: string = config.get<string>("lsp.trace") || "off";
  const watch: boolean = config.get<boolean>("lsp.watch") || false;

  const serverModule = context.asAbsolutePath("server/out/server.js");
  const serverOptions: ServerOptions = {
    command: "node",
    args: [serverModule, "--stdio"],
    transport: TransportKind.stdio,
    options: {
      cwd: vscode.workspace.rootPath || undefined,
      env: {
        ...process.env,
        ...(watch ? { MDTC_LSP_WATCH: "1" } : {}),
      },
    },
  };

  const clientOptions: LanguageClientOptions = {
    documentSelector: [{ scheme: "file", language: "mdtc" }, { scheme: "file", language: "mdtcode" }],
    outputChannelName: "MdtC Language Server",
    diagnosticCollectionName: "mdtc",
    traceOutputChannel: vscode.window.createOutputChannel("MdtC LSP Trace"),
    initializationOptions: {
      cliJar: context.asAbsolutePath("server/data/mdtc-Cli.jar"),
      dataDir: context.asAbsolutePath("server/data"),
    },
    // watch 模式下 server 每次源码变更都会自行退出(process.exit(0)),
    // 客户端依赖内置 errorHandler 的 CloseAction.Restart 自动拉起新进程;
    // 提高上限避免 3 分钟内连续编辑超过 4 次后不再重启。
    connectionOptions: { maxRestartCount: watch ? 1000 : 4 },
  };

  client = new LanguageClient(
    "mdtcLsp",
    "MdtC Language Server",
    serverOptions,
    clientOptions
  );
  client.setTrace(
    traceLevel === "verbose"
      ? Trace.Verbose
      : traceLevel === "messages"
        ? Trace.Messages
        : Trace.Off
  );

  client.start();
  context.subscriptions.push(new vscode.Disposable(() => client?.stop()));
}

export function deactivate(): Thenable<void> | undefined {
  return client?.stop();
}
