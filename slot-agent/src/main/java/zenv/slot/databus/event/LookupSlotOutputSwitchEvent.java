package zenv.slot.databus.event;

import lombok.Getter;
import lombok.Setter;
import zenv.slot.databus.AbstractDataEvent;

/**
 * @author zhengwei AKA zenv
 * @since 2022/9/2 10:11
 */
@Setter
@Getter
public class LookupSlotOutputSwitchEvent extends AbstractDataEvent {
    private String name = "检查产品埋点开关事件";

    public static LookupSlotOutputSwitchEvent of() {
        return new LookupSlotOutputSwitchEvent();
    }
}
