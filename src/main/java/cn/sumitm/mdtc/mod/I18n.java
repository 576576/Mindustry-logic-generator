package cn.sumitm.mdtc.mod;

import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

import arc.Core;
import arc.util.Log;

/**
 * 多语言工具类，检测游戏语言并加载对应翻译。
 * 通过 mod ClassLoader 读取 {@code assets/bundles/} 中的 .properties 文件，
 * 按 Java 标准 locale 回退规则叠加：默认 → 语言 → 语言_国家。
 */
public final class I18n {

    private static final String BUNDLE_BASE = "assets/bundles/bundle";
    private static final Properties bundle = new Properties();

    private I18n() {}

    /** 初始化：根据游戏当前语言加载对应的 bundle */
    public static void init() {
        Locale locale = Core.bundle.getLocale();
        String lang = locale.getLanguage();
        String country = locale.getCountry();

        // 按优先级从低到高加载，后加载的覆盖先加载的
        load(BUNDLE_BASE + ".properties");
        if (!lang.isEmpty()) {
            if (!country.isEmpty()) {
                load(BUNDLE_BASE + "_" + lang + "_" + country + ".properties");
            }
            load(BUNDLE_BASE + "_" + lang + ".properties");
        }

        Log.info("[MdtC] I18n loaded for locale: @ (@ keys)",
            locale, bundle.size());
    }

    /** 从 classpath 加载单个 .properties 文件 */
    private static void load(String path) {
        try (InputStream is = I18n.class.getClassLoader().getResourceAsStream(path)) {
            if (is != null) {
                Properties p = new Properties();
                p.load(is);
                bundle.putAll(p);
            }
        } catch (Exception e) {
            Log.err("[MdtC] Failed to load bundle: " + path, e);
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
