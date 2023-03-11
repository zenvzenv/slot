package zenv.slot.utils;

import zenv.slot.log.AnsiLog;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.Properties;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/23 17:19
 */
public final class SystemUtils {
    public static String SLOT_HOME;

    private SystemUtils() {
    }

    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (IOException e) {
            return "null";
        }
    }

    /**
     * 获取 slot-agent 的工作目录
     *
     * @return slot-agent 工作目录
     */
    public static String getSlotHome() {
        if (null != SLOT_HOME) {
            return SLOT_HOME;
        }
        final CodeSource codeSource = SystemUtils.class.getProtectionDomain().getCodeSource();
        if (null != codeSource) {
            try {
                SLOT_HOME = new File(codeSource.getLocation().toURI().getSchemeSpecificPart()).getParentFile().getAbsolutePath();
            } catch (URISyntaxException e) {
                AnsiLog.error("try to find slot home from CodeSource error", e);
            }
        }
        if (null == SLOT_HOME) {
            SLOT_HOME = new File("").getAbsolutePath();
        }
        return SLOT_HOME;
    }
}
