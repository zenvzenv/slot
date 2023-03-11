package zenv.slot.test.databus;

import zenv.slot.conf.Constant;
import zenv.slot.databus.event.LookupSlotOutputSwitchEvent;
import zenv.slot.databus.listener.SpanOutputListener;

import java.util.concurrent.TimeUnit;

import static zenv.slot.conf.Constant.SLOT_OUTPUT_CYCLE_UNIT;

/**
 * @author zhengwei AKA zenv
 * @since 2022/9/6 9:18
 */
public class DatabusTest2 {
    public static void main(String[] args) {
        Constant.DATA_BUS.dispatch();
        Constant.DATA_BUS.subscribe(new SpanOutputListener());
        final TimeUnit unit = TimeUnit.valueOf(SLOT_OUTPUT_CYCLE_UNIT);
        // 定时检查埋点输出开关
        Constant.SCHEDULED_THREAD_POOL.scheduleAtFixedRate(
                () -> Constant.DATA_BUS.publish(LookupSlotOutputSwitchEvent.of()),
                1,
                1,
                TimeUnit.SECONDS
        );
    }
}
