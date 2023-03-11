package zenv.slot.pipeline.valve;

import lombok.Getter;
import lombok.Setter;
import zenv.slot.internal.org.slf4j.Logger;
import zenv.slot.log.SlotLogUtils;
import zenv.slot.pipeline.AbstractValve;
import zenv.slot.pipeline.PipelineContext;
import zenv.slot.transform.SlotTransformer;

import java.lang.instrument.Instrumentation;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/30 16:18
 */
@Getter
@Setter
public class InstrumentationValve extends AbstractValve<SlotTransformer, Void> {
    private static final Logger log = SlotLogUtils.getLogger(InstrumentationValve.class);
    private String name;
    private final Instrumentation instrumentation;

    public InstrumentationValve(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    @Override
    public void init(PipelineContext context) {

    }

    @Override
    public Void process(SlotTransformer input, PipelineContext context) {
        log.info("添加在 {} 到 Transformer", input.getClass().getName());
        instrumentation.addTransformer(input);
        return null;
    }
}
