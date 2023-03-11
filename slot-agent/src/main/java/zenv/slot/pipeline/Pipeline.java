package zenv.slot.pipeline;

import lombok.Getter;
import lombok.Setter;

/**
 * 双向链表
 *
 * @param <I> 最开始的输出数据
 * @author zhengwei AKA zenv
 * @since 2022/7/26 19:58
 */
@Setter
@Getter
public class Pipeline<I> {
    /**
     * 管道名称
     */
    private final String name;

    /**
     * 管道中第一个处理节点
     */
    private Valve<I, ?> first;

    /**
     * 管道中最后一个处理节点
     */
    private Valve<?, ?> tail;

    private final PipelineContext context = new PipelineContext();

    public Pipeline(String name) {
        this.name = name;
    }

    /**
     * 在管道的最后添加一个处理节点
     *
     * @param valve 处理节点
     */
    public void addLast(Valve<?, ?> valve) {
        final Valve<?, ?> t = this.tail;
        tail = valve;
        if (null == t) {
            first = (Valve<I, ?>) valve;
        } else {
            t.setNext(valve);
            valve.setPrev(t);
        }
    }

    public void linkAfter() {

    }

    public void start(I input) {
        this.first.bootstrap(input, this.context);
    }
}
