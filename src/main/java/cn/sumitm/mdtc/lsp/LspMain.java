package cn.sumitm.mdtc.lsp;

import java.util.concurrent.ExecutionException;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

import cn.sumitm.mdtc.cli.Main;

/**
 * MdtC LSP 服务器入口:通过 stdio 与语言客户端(VSCode 扩展)通信。
 *
 * <p>启动方式:<code>java -jar mdtc-[version]-Lsp.jar</code>(由 VSCode 扩展拉起)。</p>
 */
public final class LspMain {
    private LspMain() {}

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // LSP 模式固定静态开关
        Main.isToFormat = false;
        Main.isFormatOnly = false;
        Main.filePath = "";
        Main.outPath = "";

        MdtcLanguageServer server = new MdtcLanguageServer();
        Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, System.in, System.out);
        LanguageClient client = launcher.getRemoteProxy();
        server.connect(client);

        launcher.startListening().get();
    }
}
