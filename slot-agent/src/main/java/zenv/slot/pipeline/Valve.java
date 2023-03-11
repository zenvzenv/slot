package zenv.slot.pipeline;

/**
 * 管道中的处理业务逻辑的节点
 *
 * @param <I> 输入的数据类型
 * @param <O> 输出的数据类型
 * @author zhengwei AKA zenv
 * @since 2022/7/26 20:04
 */
public interface Valve<I, O> {
    /**
     * 初始化
     *
     * @param context 流水线上下文
     */
    void init(PipelineContext context);

    /**
     * 处理业务逻辑
     *
     * @param input   输入的数据类型
     * @param context 管道上线文
     * @return 输出
     */
    O process(I input, PipelineContext context);

    /**
     * 获取下一个节点
     *
     * @return 下一个处理节点
     */
    Valve<?, ?> getNext();

    /**
     * 设置下一个处理节点
     *
     * @param next 下一个处理节点
     */
    void setNext(Valve<?, ?> next);

    /**
     * 设置上一个处理节点
     *
     * @param prev 上一个梳理节点
     */
    void setPrev(Valve<?, ?> prev);

    /**
     * 获取上一个处理节点
     *
     * @return 处理节点
     */
    Valve<?, ?> getPrev();

    /**
     * 唤起执行节点
     *
     * @param input   输出
     * @param context 流水线上下文
     */
    void bootstrap(I input, PipelineContext context);

    void setName(String name);

    String getName();
}
