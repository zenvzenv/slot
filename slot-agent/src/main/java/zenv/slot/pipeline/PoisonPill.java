package zenv.slot.pipeline;

import lombok.Getter;
import lombok.Setter;
import zenv.slot.internal.org.slf4j.Logger;
import zenv.slot.log.SlotLogUtils;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/1 10:52
 */
@Setter
@Getter
public class PoisonPill extends AbstractValve<Void, Void> {
    private static final Logger log = SlotLogUtils.getLogger(PoisonPill.class);
    private String name = "PoisonPill";

    @Override
    public void init(PipelineContext context) {
        log.info("检测到 PoisonPill，流水线即将关闭");
    }

    @Override
    public Void process(Void input, PipelineContext context) {
        return null;
    }

    @Override
    public void setNext(Valve<?, ?> next) {
        this.next = null;
    }
}
