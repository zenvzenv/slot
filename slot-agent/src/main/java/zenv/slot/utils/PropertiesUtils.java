package zenv.slot.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/4 9:13
 */
public final class PropertiesUtils {
    private PropertiesUtils() {
    }

    /**
     * 加载 jar 包内 properties 配置文件
     *
     * @param filePath 文件全路径
     * @return 配置信息
     */
    public static Properties loadInternalProp(String filePath) {
        Properties properties = new Properties();
        final InputStream fileStream = PropertiesUtils.class.getClassLoader().getResourceAsStream(filePath);
        try {
            properties.load(fileStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    /**
     * 加载 jar 包外配置文件
     *
     * @param filePath 文件全路径
     * @return 配置信息
     */
    public static Properties loadExternalProp(String filePath) throws IOException {
        Properties properties = new Properties();
        try (final InputStream fileStream = new FileInputStream(filePath)) {
            properties.load(fileStream);
        }
        return properties;
    }

    public static String loadExternalPropValue(String filePath, String key) {
        Properties properties = new Properties();
        try (final InputStream fileStream = new FileInputStream(filePath)) {
            properties.load(fileStream);
            return properties.getProperty(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
