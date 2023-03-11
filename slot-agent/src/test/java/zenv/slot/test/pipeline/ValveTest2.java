package zenv.slot.test.pipeline;

import lombok.Getter;
import lombok.Setter;
import zenv.slot.pipeline.AbstractValve;
import zenv.slot.pipeline.PipelineContext;

import java.util.Properties;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/28 15:57
 */
@Getter
@Setter
public class ValveTest2 extends AbstractValve<Properties, String> {
    private String name = this.getClass().getName();

    @Override
    public void init(PipelineContext context) {
        System.out.println("ValveTest2 init");
    }

    @Override
    public String process(Properties input, PipelineContext context) {
        return input.getProperty("slot.mode");
    }
}
