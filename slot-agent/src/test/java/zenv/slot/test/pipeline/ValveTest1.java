package zenv.slot.test.pipeline;

import lombok.Getter;
import lombok.Setter;
import zenv.slot.pipeline.AbstractValve;
import zenv.slot.pipeline.PipelineContext;
import zenv.slot.utils.PropertiesUtils;

import java.util.Properties;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/28 15:53
 */
@Setter
@Getter
public class ValveTest1 extends AbstractValve<String, Properties> {
    private String name = this.getClass().getName();

    @Override
    public void init(PipelineContext context) {
        System.out.println("ValveTest1 init");
    }

    @Override
    public Properties process(String input, PipelineContext context) {
        return PropertiesUtils.loadInternalProp(input);
    }
}
