package zenv.slot.strategy.build.slotclass;

import zenv.slot.internal.org.slf4j.Logger;
import zenv.slot.entity.SlotInfo;
import zenv.slot.log.SlotLogUtils;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static zenv.slot.conf.Constant.*;

/**
 * 指定特定的类进行埋点追踪
 *
 * @author zhengwei AKA zenv
 * @since 2022/8/1 15:30
 */
public class SpecialModeStrategy implements BuildSlotClassStrategy {
    private static final Logger log = SlotLogUtils.getLogger(SpecialModeStrategy.class);

    @Override
    public Set<SlotInfo> build(Properties properties) {
        log.info("对特定的类和方法进行埋点追踪");
        final Set<SlotInfo> result = new HashSet<>();
        properties.forEach((k, v) -> {
            final String key = k.toString();
            if (key.startsWith(CLASS_PREFIX)) {
                final String className = key.substring(CLASS_PREFIX.length()).replace(".", "/");
                if (v.equals(ALL_NODE)) {
                    final SlotInfo slotInfo = new SlotInfo();
                    slotInfo.setType(SlotInfo.CLASS_TYPE);
                    slotInfo.setPrefix(className);
                    slotInfo.setMethod(ALL_NODE);
                    result.add(slotInfo);
                } else {
                    final SlotInfo slotInfo = new SlotInfo();
                    slotInfo.setType(SlotInfo.CLASS_TYPE);
                    slotInfo.setMethod(v.toString());
                    slotInfo.setPrefix(className);
                    result.add(slotInfo);
                }
            } else if (key.startsWith(PACKAGE_PREFIX)) {
                final String packagePre = key.substring(PACKAGE_PREFIX.length()).replace(".", "/");
                if (!v.equals(ALL_NODE)) {
                    final SlotInfo slotInfo = new SlotInfo();
                    slotInfo.setType(SlotInfo.PACKAGE_TYPE);
                    final String[] packageNodes = v.toString().split("[" + SLOT_PROPERTIES_VALUE_SPLIT + "]");
                    for (String packageNode : packageNodes) {
                        slotInfo.setPrefix(packagePre + "/" + packageNode);
                    }
                    slotInfo.setMethod(ALL_NODE);
                    result.add(slotInfo);
                } else {
                    final SlotInfo slotInfo = new SlotInfo();
                    slotInfo.setType(SlotInfo.PACKAGE_TYPE);
                    slotInfo.setPrefix(packagePre);
                    slotInfo.setMethod(ALL_NODE);
                    result.add(slotInfo);
                }
            }
        });
        return result;
    }
}
