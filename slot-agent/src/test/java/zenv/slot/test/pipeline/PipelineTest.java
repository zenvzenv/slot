package zenv.slot.test.pipeline;

import zenv.slot.pipeline.Pipeline;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/28 15:52
 */
public class PipelineTest {
    public static void main(String[] args) {
        final Pipeline<String> pipeline=new Pipeline<>("test");
        pipeline.addLast(new ValveTest1());
        pipeline.addLast(new ValveTest2());
        pipeline.addLast(new ValveTest3());

        pipeline.start("slot.properties");
    }
}
