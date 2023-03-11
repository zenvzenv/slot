package zenv.slot.databus;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/25 14:36
 */
@Setter
@Getter
public abstract class AbstractDataEvent implements IDataEvent {
    private IDataBus dataBus;
}
