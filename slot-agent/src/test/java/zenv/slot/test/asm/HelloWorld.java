package zenv.slot.test.asm;

import lombok.Data;

import java.util.concurrent.TimeUnit;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/22 11:15
 */
@Data
public class HelloWorld {
    private String name;
    private static String aaa;
    private static final String bbb = "a";
    private boolean sss;
/*public int m1(int a, int b) {
        int c = a + b;
        System.out.println("aaa");
        return c;
    }*/

    /*public void m2(int a, int b) {
        try {
            int c = a + b;
            System.out.println("m2");
            TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int m4(int a, int b) {
        int c = a + b;
        System.out.println("m2");
        return c;
    }*/

    public void m3(int m3a, int m3b) throws InterruptedException {
        try {
            System.out.println("m3");
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new InterruptedException();
        } catch (ArithmeticException e2) {
            e2.printStackTrace();
        } catch (Exception e3) {

        }
    }

    public int m4(int a, int b) {
        int c = a + b;
        String s = " aaa";
        double d = (double) (a + b);
        System.out.println("aaaaa");
        return c;
    }
}
