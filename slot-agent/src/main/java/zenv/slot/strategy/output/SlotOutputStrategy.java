package zenv.slot.strategy.output;

/**
 * 把埋点数据写出策略
 *
 * @author zhengwei AKA zenv
 * @since 2022/7/28 19:18
 */
public interface SlotOutputStrategy<T> {
    void output(T data);
}
