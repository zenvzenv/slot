package zenv.slot.test.asm.exception;

public class HelloWorldRun {
    public static void main(String[] args) throws Exception {
        HelloWorld instance = new HelloWorld();
        instance.test(null, 0);
    }
}