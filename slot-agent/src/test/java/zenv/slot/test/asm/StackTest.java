package zenv.slot.test.asm;

import zenv.slot.trace.TraceManager;

/**
 * @author zhengwei AKA zenv
 * @since 2022/9/5 9:47
 */
public class StackTest {
    public static void main(String[] args) {
        final String s = TraceManager.entrySpan();
        TraceManager.exitSpan();
        System.out.println("aaa");
    }
}
