package zenv.slot.databus;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/25 9:21
 */
public interface IDataBus {
    /**
     * 注册一个数据处理器到数据总线中已开始接收事件
     *
     * @param listener 数据处理器
     */
    void subscribe(final IListener listener);

    /**
     * 从数据总线中移除一个数据处理器
     *
     * @param listener 数据处理器
     */
    void unsubscribe(final IListener listener);

    /**
     * 发布数据事件到总线中，凡是在总线中注册的监听器都会接收到此事件，是否需要进行处理由每个监听器自己决定
     *
     * @param event 数据事件
     */
    void publish(final IDataEvent event);

    /**
     * 分发事件到各个监听器
     */
    void dispatch();

    void setDataBusName(String name);

    String getDataBusName();
}
