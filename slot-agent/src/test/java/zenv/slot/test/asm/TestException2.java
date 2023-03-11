package zenv.slot.test.asm;

import java.time.LocalDateTime;

/**
 * @author zhengwei AKA zenv
 * @since 2022/11/4 10:23
 */
public class TestException2 {
    public void m2() throws Exception {
        final TestException testException = new TestException();
        try {
            testException.m1();
        } catch (Exception e) {
            System.out.println("m2 catch");
            throw new Exception();
        }
    }

    public void m3() throws Exception {
        LocalDateTime var1 = LocalDateTime.now();
        Object var2 = null;

        try {
            TestException testException = new TestException();

            try {
                testException.m1();
            } catch (Exception var5) {
                System.out.println("m2 catch");
                throw new Exception();
            }
        } catch (Throwable var6) {
            System.out.println("slotslotslotslotslotslotslotslotslotslotslotslotslotslotslotslotslotslotslotslotslotslotslotslot");
            throw var6;
        }
    }
}
