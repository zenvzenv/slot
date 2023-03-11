package zenv.slot.test.date;

import zenv.slot.utils.DateUtils;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @author zhengwei AKA zenv
 * @since 2022/9/1 11:01
 */
public class TimerTest {
    public static void main(String[] args) {
        final LocalDateTime now = LocalDateTime.now();
        System.out.println(now.format(DateUtils.yyyyMMddHHmmss));
        final long nowSubNearNextDaley = DateUtils.getRenameDaley(5, TimeUnit.DAYS);
        System.out.println(nowSubNearNextDaley);
        System.out.println(now.plusSeconds(nowSubNearNextDaley).format(DateUtils.yyyyMMddHHmmss));
    }
}
