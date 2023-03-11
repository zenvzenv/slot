package zenv.slot.strategy.output;

import lombok.Setter;

/**
 * 输出策略
 *
 * @author zhengwei AKA zenv
 * @since 2022/7/28 19:18
 */
@Setter
public class SlotOutputContext<T> {
    private SlotOutputStrategy<T> slotOutputStrategy;

    public void output(T data) {
        slotOutputStrategy.output(data);
    }
}
