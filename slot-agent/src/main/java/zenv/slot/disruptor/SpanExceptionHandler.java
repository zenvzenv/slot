package zenv.slot.disruptor;

import zenv.slot.internal.com.lmax.disruptor.ExceptionHandler;
import zenv.slot.entity.Span;

/**
 * @author zhengwei AKA zenv
 * @since 2022/9/6 18:47
 */
public class SpanExceptionHandler implements ExceptionHandler<Span> {
    @Override
    public void handleEventException(Throwable ex, long sequence, Span event) {

    }

    @Override
    public void handleOnStartException(Throwable ex) {

    }

    @Override
    public void handleOnShutdownException(Throwable ex) {

    }
}
