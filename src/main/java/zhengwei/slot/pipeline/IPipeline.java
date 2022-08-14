package zhengwei.slot.pipeline;

/**
 * 管道接口规范
 * <p>
 * 管道是一个容器，用来容纳处理数据处理器的容器。内部可以理解为数据处理器的双向链表，处理器之间存在约束关系，即上一个处理器的输出是下一个处理器
 * 的输入，这样可以确保流水线的从头到尾的流畅执行
 *
 * @param <I> 第一个处理需要的输入
 * @author zhengwei AKA zenv
 * @since 2022/8/14 11:57
 */
public interface IPipeline<I> {
    /**
     * 获取管道名称
     *
     * @return 管道名称
     */
    String getName();

    /**
     * 设置管道名称
     */
    void setName(String name);

    /**
     * 启动流水线
     */
    void bootstrap(I input);

    /**
     * 在管道的最后添加一个处理器
     */
    void linkLast(IValve<?, ?> valve);

    /**
     * 在指定的处理器之后插入一个处理器
     *
     * @param current 当前处理器
     * @param after   后一个处理器
     */
    void linkAfter(IValve<?, ?> current, IValve<?, ?> after);

    /**
     * 在指定的处理器之前插入一个处理器
     *
     * @param current 当前处理器
     * @param before  前一个处理器
     */
    void linkBefore(IValve<?, ?> current, IValve<?, ?> before);
}
