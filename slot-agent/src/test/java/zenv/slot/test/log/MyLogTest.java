package zenv.slot.test.log;

import zenv.slot.internal.org.slf4j.Logger;
import zenv.slot.log.SlotLogUtils;
import org.junit.Test;

import java.util.Properties;

/**
 * @author zhengwei AKA zenv
 * @since 2022/9/26 10:28
 */
public class MyLogTest {
    @Test
    public void test1() {
        SlotLogUtils.initLogger("monitor");
        final Logger logger = SlotLogUtils.getLogger(MyLogTest.class);
        logger.info("aaa");
    }
}
