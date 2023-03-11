package zenv.slot.utils;

import zenv.slot.entity.Span;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author zhengwei AKA zenv
 * @since 2022/12/9 13:52
 */
public class TraceArrayBlockingQueue extends ArrayBlockingQueue<Span> {
    public TraceArrayBlockingQueue(int capacity) {
        super(capacity);
    }
}
