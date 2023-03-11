package zenv.slot.test.asm.exception;

public class HelloWorld {
    public void test(String name, int age) {
        try {
            int length = name.length();
            System.out.println("length = " + length);
        }
        catch (NullPointerException ex) {
            System.out.println("name is null");
        }

        int val = div(10, age);
        System.out.println("val = " + val);
    }

    public int div(int a, int b) {
        return a / b;
    }
}