package zenv.slot.databus;

import java.util.function.Consumer;

/**
 * 事件处理监听器，只处理此监听器感兴趣的事件
 *
 * @author zhengwei AKA zenv
 * @since 2022/7/25 9:22
 */
public interface IListener extends Consumer<IDataEvent> {
    /**
     * 事件处理
     *
     * @param event 数据事件
     */
    void accept(IDataEvent event);
}
