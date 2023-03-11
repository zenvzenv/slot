package zenv.slot.strategy.build.slotclass;

import zenv.slot.entity.SlotInfo;

import java.util.Properties;
import java.util.Set;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/1 15:13
 */
public interface BuildSlotClassStrategy {
    Set<SlotInfo> build(Properties properties);
}
