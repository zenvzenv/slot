package zenv.slot.databus.listener;

import zenv.slot.databus.IDataEvent;
import zenv.slot.databus.IListener;
import zenv.slot.databus.event.LookupSlotOutputSwitchEvent;
import zenv.slot.databus.event.WriteSpanOutEvent;
import zenv.slot.disruptor.SpanConsumer;
import zenv.slot.internal.org.slf4j.Logger;
import zenv.slot.log.SlotLogUtils;
import zenv.slot.utils.SlotSwitchUtils;

import static zenv.slot.conf.Constant.RING_BUFFER_WORKER_POOL_FACTORY;

/**
 * 埋点数据输出监听器
 *
 * @author zhengwei AKA zenv
 * @since 2022/8/8 10:46
 */
public class SpanOutputListener implements IListener {
    private static final Logger log = SlotLogUtils.getLogger(SpanOutputListener.class);

    @Override
    public void accept(IDataEvent event) {
        if (event instanceof WriteSpanOutEvent) {
            handle((WriteSpanOutEvent) event);
        } else if (event instanceof LookupSlotOutputSwitchEvent) {
            handle((LookupSlotOutputSwitchEvent) event);
        }
    }

    private void handle(WriteSpanOutEvent event) {
        log.info("定时写出事件，将埋点数据写出");
        for (SpanConsumer consumer : RING_BUFFER_WORKER_POOL_FACTORY.getConsumers()) {
            consumer.drain();
        }
    }

    /**
     * 检查埋点开关
     * <p>
     * 1. 默认埋点开启
     * <p>
     * 2. 如果配置文件中没有该服务名称则认为开启埋点
     * <p>
     * 3. 配置文件中开关状态为 false-关闭埋点，true-开启埋点
     */
    private void handle(LookupSlotOutputSwitchEvent event) {
        SlotSwitchUtils.initAndUpdateSwitch();
    }
}
