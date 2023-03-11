package zenv.slot.databus.event;

import lombok.Getter;
import lombok.Setter;
import zenv.slot.databus.AbstractDataEvent;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/30 14:08
 */
@Setter
@Getter
public class WriteSpanOutEvent extends AbstractDataEvent {
    private String name = "写出埋点数据事件";

    public static WriteSpanOutEvent of() {
        return new WriteSpanOutEvent();
    }
}
