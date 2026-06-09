package cn.sumitm.mdtc.mod;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;

import arc.Core;
import arc.util.Log;

/**
 * 多语言工具类，通过 {@code bundle.properties} 索引文件将游戏 locale
 * 映射到对应的翻译文件，无需扫描目录。加载优先级：精确区域匹配 → 语言兜底 → 英文兜底。
 * 使用 UTF-8 编码直接读取，支持任意 Unicode 字符。
 */
public final class I18n {

    private static final String BUNDLE_DIR = "assets/bundles/";
    private static final String INDEX_FILE = "bundle";
    private static final Properties bundle = new Properties();

    private I18n() {}

    /** 初始化：读取 bundle.properties 索引，按游戏 locale 精确加载 */
    public static void init() {
        Locale locale = Core.bundle.getLocale();
        String gameLang = locale.getLanguage();
        String gameRegion = locale.getCountry();

        // 1. 加载索引
        Properties index = loadProperties(INDEX_FILE);
        if (index.isEmpty()) {
            Log.err("[MdtC] bundle.properties index missing — only English available");
        }

        // 2. 始终加载英文作为兜底
        loadFile("en");

        // 3. 按索引匹配语言文件：先加载语言基座，再叠加区域差异
        if (!gameLang.isEmpty() && !"en".equals(gameLang)) {
            // 语言级基座（如 zh → zh.properties）
            String base = index.getProperty(gameLang);
            if (base != null) {
                loadFile(base);
                Log.info("[MdtC] Base bundle: @", base);
            }

            // 区域级叠加（如 zh.TW → zh-Hant.properties），仅在不同于基座时加载
            if (!gameRegion.isEmpty()) {
                String regionTarget = index.getProperty(gameLang + "." + gameRegion);
                if (regionTarget != null && !regionTarget.equals(base)) {
                    loadFile(regionTarget);
                    Log.info("[MdtC] Region overlay: @ for @", regionTarget, gameRegion);
                }
            }

            if (base == null) {
                Log.info("[MdtC] No bundle for @ — using English fallback", locale);
            }
        }

        Log.info("[MdtC] I18n ready: @ keys for locale @",
            bundle.size(), locale);
    }

    /** 从 classpath 加载单个 .properties 文件（UTF-8） */
    private static Properties loadProperties(String name) {
        Properties p = new Properties();
        String path = BUNDLE_DIR + name + ".properties";
        try (InputStream is = I18n.class.getClassLoader().getResourceAsStream(path)) {
            if (is != null) {
                p.load(new InputStreamReader(is, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            Log.err("[MdtC] Failed to load: " + path, e);
        }
        return p;
    }

    /** 加载翻译文件并合并到主 bundle */
    private static void loadFile(String name) {
        Properties p = loadProperties(name);
        if (!p.isEmpty()) {
            bundle.putAll(p);
        }
    }

    /**
     * 获取本地化字符串。
     * @param key bundle 中的键
     * @return 本地化文本，若缺失则返回 key 本身
     */
    public static String get(String key) {
        return bundle.getProperty(key, key);
    }

    /**
     * 获取本地化字符串并格式化参数。
     * @param key    bundle 中的键
     * @param args   String.format 参数
     * @return 格式化后的本地化文本
     */
    public static String format(String key, Object... args) {
        return String.format(get(key), args);
    }
}
