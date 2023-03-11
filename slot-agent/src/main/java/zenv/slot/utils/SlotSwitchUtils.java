package zenv.slot.utils;

import zenv.slot.internal.org.slf4j.Logger;
import zenv.slot.log.SlotLogUtils;

import java.io.IOException;
import java.util.Properties;

import static zenv.slot.conf.Constant.*;

/**
 * @author zhengwei AKA zenv
 * @since 2022/11/10 16:46
 */
public final class SlotSwitchUtils {
    private static final Logger log = SlotLogUtils.getLogger(SlotLogUtils.class);

    /**
     * 初始化或更新埋点开关字段
     */
    public static void initAndUpdateSwitch() {
        log.debug("检查埋点开关...");
        final Properties prop;
        try {
            prop = PropertiesUtils.loadExternalProp(SLOT_OUTPUT_SWITCH_PROP_FILE_PATH);
        } catch (IOException e) {
            log.info("暂无埋点开关配置文件，默认关闭埋点");
            SLOT_OUTPUT_SWITCH.set(Boolean.FALSE);
            return;
        }
        if (0 == prop.size()) {
            log.info("暂无埋点开关配置文件，默认关闭埋点");
            SLOT_OUTPUT_SWITCH.set(Boolean.FALSE);
            return;
        }
        final String serviceSwitch = prop.getProperty(SERVICE.get());
        if (null == serviceSwitch) {
            SLOT_OUTPUT_SWITCH.set(Boolean.FALSE);
            log.info("埋点开关文件暂无服务信息，默认关闭埋点");
            return;
        }
        final boolean open = Boolean.parseBoolean(serviceSwitch);
        SLOT_OUTPUT_SWITCH.set(open);
        log.info("检测到开关文件，埋点开关: {}", SLOT_OUTPUT_SWITCH.get());
    }
}
