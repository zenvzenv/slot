package zenv.slot.databus;

/**
 * 数据事件
 * <p>
 * 该事件将会在数据总线中进行传递，传递到对应的处理器中进行处理
 *
 * @author zhengwei AKA zenv
 * @since 2022/7/25 9:23
 */
public interface IDataEvent {
    String getName();

    void setName(String name);
    /**
     * 返回该数据事件所属的数据总线
     *
     * @return 数据总线
     */
    IDataBus getDataBus();

    /**
     * 设置该数据事件需要往哪个数据总线上发送
     *
     * @param dataBus 数据总线
     */
    void setDataBus(IDataBus dataBus);
}
