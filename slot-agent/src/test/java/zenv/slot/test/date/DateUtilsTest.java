package zenv.slot.test.date;

import zenv.slot.conf.Constant;
import zenv.slot.databus.event.RenameTempEvent;
import zenv.slot.utils.DateUtils;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @author zhengwei AKA zenv
 * @since 2022/9/1 14:23
 */
public class DateUtilsTest {
    public static void main(String[] args) {
        System.out.println(DateUtils.getNearNext(5, TimeUnit.MINUTES).format(DateUtils.yyyyMMddHHmmss));

        final TimeUnit unit = TimeUnit.valueOf(Constant.SLOT_OUTPUT_CYCLE_UNIT);
        Constant.SCHEDULED_THREAD_POOL.scheduleAtFixedRate(
                () -> Constant.DATA_BUS.publish(RenameTempEvent.of(LocalDateTime.now().format(DateUtils.yyyyMMddHHmm))),
                DateUtils.getRenameDaley(Constant.SLOT_OUTPUT_CYCLE, unit),
                TimeUnit.SECONDS.convert(Constant.SLOT_OUTPUT_CYCLE, unit),
                TimeUnit.SECONDS
        );
    }

    @Test
    public void testGetNowSubNearNext() {
        System.out.println(LocalDateTime.now().format(DateUtils.yyyyMMddHHmmss));
        System.out.println(DateUtils.getNowSubNearNext(5, TimeUnit.SECONDS));
    }
}
