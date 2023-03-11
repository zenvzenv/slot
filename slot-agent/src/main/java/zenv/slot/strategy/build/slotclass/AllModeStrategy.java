package zenv.slot.strategy.build.slotclass;

import zenv.slot.internal.org.slf4j.Logger;
import zenv.slot.entity.SlotInfo;
import zenv.slot.log.SlotLogUtils;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static zenv.slot.conf.Constant.ALL_NODE;
import static zenv.slot.conf.Constant.PACKAGE_PREFIX;

/**
 * 在全量模式中，配置文件中应该存在一条业务相关类开头的包名配置，否则埋点系统会对业务系统引用的第三方包进行埋点操作
 *
 * @author zhengwei AKA zenv
 * @since 2022/8/1 15:22
 */
public class AllModeStrategy implements BuildSlotClassStrategy {
    private static final Logger log = SlotLogUtils.getLogger(AllModeStrategy.class);

    @Override
    public Set<SlotInfo> build(Properties properties) {
        log.info("对全量类和方法进行追踪");
        final Set<SlotInfo> result = new HashSet<>();
        properties.forEach((k, v) -> {
            final String key = k.toString();
            if (key.startsWith(PACKAGE_PREFIX)) {
                final String packagePre = key.substring(PACKAGE_PREFIX.length()).replace(".", "/");
                if (v.equals(ALL_NODE)) {
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
