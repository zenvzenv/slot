package zenv.slot.strategy.build.slotclass;

import lombok.Setter;
import zenv.slot.entity.SlotInfo;

import java.util.Properties;
import java.util.Set;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/1 15:03
 */
@Setter
public class BuildSlotClassContext {
    private BuildSlotClassStrategy strategy;

    public Set<SlotInfo> build(Properties properties) {
        return strategy.build(properties);
    }
}
