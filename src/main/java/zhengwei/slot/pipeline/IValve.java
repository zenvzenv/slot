package zhengwei.slot.pipeline;

import java.util.function.Function;

/**
 * 数据处理器
 * <p>
 * 处理只处理指定类型的输出和输出，上一个的处理器的输出是下一个处理器的输出
 *
 * @param <I> 处理器的输入类型
 * @param <O> 处理器的输出类型
 * @author zhengwei AKA zenv
 * @since 2022/8/14 12:04
 */
public interface IValve<I, O> {
    /**
     * 获取处理器名称
     *
     * @return 处理器名称
     */
    String getName();

    /**
     * 设置处理器名称
     *
     * @param name 处理器名称
     */
    void setName(String name);

    /**
     * 处理器处理数据的逻辑
     *
     * @param function 处理逻辑
     * @return 处理器的输出
     */
    O process(Function<I, O> function);

    /**
     * 设置下一个处理器
     *
     * @param next 下一个处理器
     */
    void setNext(IValve<?, ?> next);

    /**
     * 获取下一个处理器
     *
     * @return 下一个处理器
     */
    IValve<?, ?> getNext();

    /**
     * 设置上一个处理器
     *
     * @param before 上一个处理器
     */
    void setPrev(IValve<?, ?> before);

    /**
     * 获取下一个处理器
     *
     * @return 上一个处理器
     */
    IValve<?, ?> getPrev();

    /**
     * 启动处理器逻辑
     *
     * @param input 输入
     */
    void bootstrap(I input);
}
