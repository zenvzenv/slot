package zenv.slot.databus.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import zenv.slot.databus.AbstractDataEvent;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 清理过期的埋点数据，防止磁盘爆满
 *
 * @author zhengwei AKA zenv
 * @since 2022/9/16 8:35
 */
@Setter
@Getter
@RequiredArgsConstructor
public class DeleteExpiredEvent extends AbstractDataEvent {
    private String name = "删除过期数据事件";

    private final String prefix;

    private final String suffix;

    private final int duration;

    private final TimeUnit timeUnit;

    private final LocalDateTime dateTime;

    public static DeleteExpiredEvent of(String prefix, String suffix, int duration, TimeUnit timeUnit, LocalDateTime dateTime) {
        return new DeleteExpiredEvent(prefix, suffix, duration, timeUnit, dateTime);
    }
}
