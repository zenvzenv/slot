package zenv.slot.test.pipeline;

import lombok.Getter;
import lombok.Setter;
import zenv.slot.pipeline.AbstractValve;
import zenv.slot.pipeline.PipelineContext;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/28 16:53
 */
@Setter
@Getter
public class ValveTest3 extends AbstractValve<String, Void> {
    private String name = this.getClass().getName();

    @Override
    public void init(PipelineContext context) {

    }

    @Override
    public Void process(String input, PipelineContext context) {
        System.out.println(input);
        return null;
    }
}
