package zhengwei.slot.test.modify;

import java.util.Random;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/20 13:24
 */
public class HelloWorld2 {
    public void add(int a, int b) {
        try {
            int c = a + b;
            Random rand = new Random(System.currentTimeMillis());
            int num = rand.nextInt(300);
            Thread.sleep(100 + num);
        } catch (InterruptedException  e) {
            e.printStackTrace();
        }
//        return c;
    }
}
