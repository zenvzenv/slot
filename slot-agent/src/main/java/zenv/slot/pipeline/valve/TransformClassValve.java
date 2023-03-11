package zenv.slot.pipeline.valve;

import lombok.Getter;
import lombok.Setter;
import zenv.slot.entity.SlotInfo;
import zenv.slot.internal.org.slf4j.Logger;
import zenv.slot.log.SlotLogUtils;
import zenv.slot.pipeline.AbstractValve;
import zenv.slot.pipeline.PipelineContext;
import zenv.slot.transform.SlotTransformer;

import java.lang.instrument.Instrumentation;
import java.util.Set;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/2 9:01
 */
@Setter
@Getter
public class TransformClassValve extends AbstractValve<Set<SlotInfo>, SlotTransformer> {
    private static final Logger log = SlotLogUtils.getLogger(TransformClassValve.class);
    private String name = "转换类";
    private Instrumentation inst;

    @Override
    public void init(PipelineContext context) {

    }

    @Override
    public SlotTransformer process(Set<SlotInfo> input, PipelineContext context) {
        log.info("组装 SlotTransformer");
        return new SlotTransformer(input);
    }
}
