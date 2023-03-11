package zenv.slot.databus.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import zenv.slot.databus.AbstractDataEvent;

/**
 * @author zhengwei AKA zenv
 * @since 2022/9/8 8:41
 */
@Getter
@Setter
@RequiredArgsConstructor
public class DisruptorProducerEvent extends AbstractDataEvent {
    private String name = "发布 span 事件";
    private final String producerId;

    public static DisruptorProducerEvent of(String producerId) {
        return new DisruptorProducerEvent(producerId);
    }
}
