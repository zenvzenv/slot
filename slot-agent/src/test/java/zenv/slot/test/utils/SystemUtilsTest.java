package zenv.slot.test.utils;

import zenv.slot.utils.SystemUtils;
import org.junit.Test;

/**
 * @author zhengwei AKA zenv
 * @since 2022/9/26 10:44
 */
public class SystemUtilsTest {
    @Test
    public void test1(){
        System.out.println(SystemUtils.getSlotHome());
    }
}
