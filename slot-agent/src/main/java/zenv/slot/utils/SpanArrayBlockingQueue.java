package zenv.slot.utils;

import zenv.slot.entity.Span;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author zhengwei AKA zenv
 * @since 2022/12/9 13:53
 */
public class SpanArrayBlockingQueue extends ArrayBlockingQueue<Span> {
    public SpanArrayBlockingQueue(int capacity) {
        super(capacity);
    }
}
