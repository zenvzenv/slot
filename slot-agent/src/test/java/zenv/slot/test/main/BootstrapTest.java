package zenv.slot.test.main;

import zenv.slot.pipeline.Pipeline;
import zenv.slot.pipeline.valve.BuildSlotClassValve;
import zenv.slot.pipeline.valve.InitValve;
import zenv.slot.pipeline.valve.TransformClassValve;

/**
 * @author zhengwei AKA zenv
 * @since 2022/9/5 14:15
 */
public class BootstrapTest {

    public static void main(String[] args) {
        final Pipeline<String> pipeline = new Pipeline<>("slot");
        pipeline.addLast(new InitValve());
        pipeline.addLast(new BuildSlotClassValve());
        pipeline.addLast(new TransformClassValve());
    }
}
