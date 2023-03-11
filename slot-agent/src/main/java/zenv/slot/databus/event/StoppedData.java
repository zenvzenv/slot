package zenv.slot.databus.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import zenv.slot.databus.AbstractDataEvent;
import zenv.slot.databus.IDataEvent;

import java.time.LocalDateTime;

/**
 * 埋点结束时间
 *
 * @author zhengwei AKA zenv
 * @since 2022/7/25 14:45
 */
@RequiredArgsConstructor
@Getter
@Setter
public class StoppedData extends AbstractDataEvent {
    private final LocalDateTime when;
    private String name;

    public static IDataEvent of(final LocalDateTime when) {
        return new StoppedData(when);
    }
}
