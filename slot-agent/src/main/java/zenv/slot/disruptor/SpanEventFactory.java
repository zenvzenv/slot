package zenv.slot.disruptor;

import zenv.slot.internal.com.lmax.disruptor.EventFactory;
import zenv.slot.entity.Span;

/**
 * @author zhengwei AKA zenv
 * @since 2022/9/6 16:47
 */
public class SpanEventFactory implements EventFactory<Span> {
    @Override
    public Span newInstance() {
        return new Span();
    }
}
