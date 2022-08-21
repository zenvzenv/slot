package zhengwei.slot.test.modify;

import zhengwei.slot.entity.SlotOutput;

import java.util.Random;
import java.util.UUID;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/20 13:24
 */
public class HelloWorld {
    public int add(int a, int b) throws InterruptedException {
        SlotOutput slotOutput = new SlotOutput();
        slotOutput.setSpanId(UUID.randomUUID().toString());
        long timer = System.currentTimeMillis();
        int c = a + b;
        Random rand = new Random(System.currentTimeMillis());
        int num = rand.nextInt(300);
        Thread.sleep(100 + num);
        slotOutput.setDuration(System.currentTimeMillis() - timer);
        slotOutput.setSuccess(true);
        System.out.println(slotOutput);
        return c;
    }

    public void add2(int a, int b) throws InterruptedException {
        SlotOutput slotOutput = new SlotOutput();
        slotOutput.setSpanId(UUID.randomUUID().toString());
        long timer = System.currentTimeMillis();
        int c = a + b;
        Random rand = new Random(System.currentTimeMillis());
        int num = rand.nextInt(300);
        Thread.sleep(100 + num);
        slotOutput.setDuration(System.currentTimeMillis() - timer);
        slotOutput.setSuccess(true);
        System.out.println(slotOutput);
    }

    public void add3(int a, int b) throws InterruptedException {
        SlotOutput slotOutput = new SlotOutput();
        slotOutput.setSpanId(UUID.randomUUID().toString());
        long timer = System.currentTimeMillis();
        try {
            int c = a + b;
            Random rand = new Random(System.currentTimeMillis());
            int num = rand.nextInt(300);
            Thread.sleep(100 + num);
            slotOutput.setDuration(System.currentTimeMillis() - timer);
            slotOutput.setSuccess(true);
            System.out.println(slotOutput);
        } catch (InterruptedException  e){
            e.printStackTrace();
            slotOutput.setDuration(System.currentTimeMillis() - timer);
            slotOutput.setSuccess(false);
            System.out.println(slotOutput);
        }
    }

    /*public int sub(int a, int b) throws InterruptedException {
        int c = a - b;
        Random rand = new Random(System.currentTimeMillis());
        int num = rand.nextInt(300);
        Thread.sleep(100 + num);
        return c;
    }*/

    public SlotOutput sub(int a, int b) throws InterruptedException {
        SlotOutput slotOutput = new SlotOutput();
        slotOutput.setSpanId(UUID.randomUUID().toString());
        long timer = System.currentTimeMillis();
        int c = a + b;
        Random rand = new Random(System.currentTimeMillis());
        int num = rand.nextInt(300);
        Thread.sleep(100 + num);
        slotOutput.setDuration(System.currentTimeMillis() - timer);
        System.out.println(slotOutput);
        return slotOutput;
    }
}
