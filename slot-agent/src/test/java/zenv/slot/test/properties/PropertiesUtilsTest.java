package zenv.slot.test.properties;

import zenv.slot.utils.PropertiesUtils;

import java.io.IOException;
import java.util.Properties;

import static zenv.slot.conf.Constant.SLOT_OUTPUT_SWITCH_PROP_FILE_PATH;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/30 16:43
 */
public class PropertiesUtilsTest {
    public static void main(String[] args) throws IOException {
        /*final Properties properties = PropertiesUtils.loadExternalProp("F:/slot/slot.properties ");
        System.out.println(properties);*/

        String s = "com.xx.monitor";
        System.out.println(s.replace(".", "/"));

        final Properties prop = PropertiesUtils.loadExternalProp(SLOT_OUTPUT_SWITCH_PROP_FILE_PATH);
        final boolean open = Boolean.parseBoolean(prop.getProperty("monitor"));
        System.out.println(open);
    }
}
