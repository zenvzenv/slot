package zenv.slot.test.asm;

/**
 * @author zhengwei AKA zenv
 * @since 2022/11/3 8:50
 */
public class TestException {
    public void m1() throws Exception {
        throw new Exception("m1 throw");
    }

    public static void main(String[] args) throws Exception {
        final TestException2 testException2 = new TestException2();
        testException2.m2();
    }
}
