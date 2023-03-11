package zenv.slot.databus.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import zenv.slot.databus.AbstractDataEvent;
import zenv.slot.databus.IDataEvent;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/25 14:41
 */
@Getter
@Setter
@RequiredArgsConstructor
public class MessageData extends AbstractDataEvent {
    private final String message;
    private String name;

    public static IDataEvent of(final String message) {
        return new MessageData(message);
    }
}
