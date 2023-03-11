package zenv.slot.pipeline;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/27 10:44
 */
public abstract class AbstractValve<I, O> implements Valve<I, O> {
    /**
     * 前一个处理器
     */
    protected Valve<?, ?> prev;

    /**
     * 后一个处理器
     */
    protected Valve<?, ?> next;

    public AbstractValve() {
    }

    @Override
    public void setNext(Valve<?, ?> next) {
        this.next = next;
    }

    @Override
    public void bootstrap(I input, PipelineContext context) {
        init(context);
        final O result = process(input, context);
        if (null != getNext()) {
            ((Valve<O, ?>) getNext()).bootstrap(result, context);
        }
    }

    @Override
    public Valve<?, ?> getNext() {
        return next;
    }

    @Override
    public void setPrev(Valve<?, ?> prev) {
        this.prev = prev;
    }

    @Override
    public Valve<?, ?> getPrev() {
        return prev;
    }
}
