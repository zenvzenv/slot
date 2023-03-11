package zenv.slot.pipeline.valve;

import lombok.Getter;
import lombok.Setter;
import zenv.slot.conf.Constant;
import zenv.slot.entity.SlotInfo;
import zenv.slot.internal.org.slf4j.Logger;
import zenv.slot.log.SlotLogUtils;
import zenv.slot.pipeline.AbstractValve;
import zenv.slot.pipeline.PipelineContext;
import zenv.slot.strategy.build.slotclass.AllModeStrategy;
import zenv.slot.strategy.build.slotclass.BuildSlotClassContext;
import zenv.slot.strategy.build.slotclass.SpecialModeStrategy;

import java.util.Properties;
import java.util.Set;

/**
 * 构建埋点信息，包括需要对哪些 class,method 进行埋点或者全埋点
 *
 * @author zhengwei AKA zenv
 * @since 2022/8/1 11:40
 */
@Setter
@Getter
public class BuildSlotClassValve extends AbstractValve<Properties, Set<SlotInfo>> {
    private static final Logger log = SlotLogUtils.getLogger(BuildSlotClassValve.class);
    private String name = "构建埋点信息";
    private final BuildSlotClassContext context = new BuildSlotClassContext();

    @Override
    public void init(PipelineContext context) {
        // 获取埋点模式环境
        final String slotMode = context.getByKey(Constant.SLOT_MODE).toString();
        if (Constant.SLOT_MODE_ALL.equals(slotMode)) {
            this.context.setStrategy(new AllModeStrategy());
        } else if (Constant.SLOT_MODE_SPECIAL.equals(slotMode)) {
            this.context.setStrategy(new SpecialModeStrategy());
        }
    }

    @Override
    public Set<SlotInfo> process(Properties input, PipelineContext context) {
        log.info("读取配置文件中埋点信息");
        return this.context.build(input);
    }
}
