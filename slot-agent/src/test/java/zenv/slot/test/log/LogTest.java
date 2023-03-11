package zenv.slot.test.log;

import zenv.slot.internal.org.slf4j.Logger;
import zenv.slot.internal.org.slf4j.LoggerFactory;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/15 8:50
 */
public class LogTest {
    private static final Logger log = LoggerFactory.getLogger(LogTest.class);

    public static void main(String[] args) {
        log.info("aaa");
    }
}
