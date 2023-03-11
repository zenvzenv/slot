package zenv.slot.disruptor;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import zenv.slot.entity.Span;
import zenv.slot.enums.SlotOutputMode;
import zenv.slot.internal.com.lmax.disruptor.WorkHandler;
import zenv.slot.internal.org.slf4j.Logger;
import zenv.slot.log.SlotLogUtils;
import zenv.slot.strategy.output.SlotOutputContext;
import zenv.slot.utils.SpanArrayBlockingQueue;
import zenv.slot.utils.SpanLinkedList;
import zenv.slot.utils.TraceArrayBlockingQueue;
import zenv.slot.utils.TraceLinkedList;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import static zenv.slot.trace.TraceManager.ROOT_METHOD_SPAN_ID;

/**
 * @author zhengwei AKA zenv
 * @since 2022/9/6 16:50
 */
@Getter
@Setter
@ToString
public class SpanConsumer implements WorkHandler<Span> {
    private static final Logger log = SlotLogUtils.getLogger(Span.class);
    private final String name;

    /**
     * 存放 span 信息的缓冲队列
     */
    private final BlockingQueue<Span> spans;

    /**
     * 存放 trace 信息的缓冲队列，span 一般为 trace 的若干倍
     */
    private final BlockingQueue<Span> traces;

    /**
     * 埋点数据输出策略
     */
    private final SlotOutputContext<List<Span>> context = new SlotOutputContext<>();

    public SpanConsumer(int size, String outputMode, String name) {
        this.spans = new SpanArrayBlockingQueue(size * 2);
        this.traces = new TraceArrayBlockingQueue(size);
        final SlotOutputMode mode = SlotOutputMode.parse(outputMode);
        context.setSlotOutputStrategy(mode.getStrategy());
        this.name = name;
    }

    /**
     * 将埋点数据分为 trace 和 span，分别入到两种不同的表中，以提高前端的访问速度
     *
     * @param event 埋点数据
     */
    @Override
    public void onEvent(Span event) {
        log.debug("缓存到一条埋点数据: {}", event);
        // 我们认为 parent id 为 null 的 span 为整个调用链的入口
        final String parentId = event.getParentId();
        if (null == parentId || ROOT_METHOD_SPAN_ID.equals(parentId)) {
            log.debug("缓存 trace 数据: {}", event);
            if (!traces.offer(event)) {
                final List<Span> list = new TraceLinkedList();
                try {
                    traces.drainTo(list);
                    list.add(event);
                    log.debug("trace 缓存队列已满，准备写出 {} 条埋点数据", list.size());
                    context.output(list);
                } finally {
                    // help gc
                    list.clear();
                }
            }
        }
        // 缓存队列满时将数据写出
        if (!spans.offer(event)) {
            final List<Span> list = new SpanLinkedList();
            try {
                spans.drainTo(list);
                list.add(event);
                log.debug("span 缓存队列已满，准备写出 {} 条埋点数据", list.size());
                context.output(list);
            } finally {
                // help gc
                list.clear();
            }
        }
    }

    /**
     * 固定写出时间清空埋点数据缓冲队列并输出
     */
    public void drain() {
        if (spans.size() > 0) {
            final List<Span> list = new SpanLinkedList();
            try {
                spans.drainTo(list);
                log.debug("{} 写出 {} 条数据", name, list.size());
                context.output(list);
            } finally {
                list.clear();
            }
        }
        if (traces.size() > 0) {
            final List<Span> list = new TraceLinkedList();
            try {
                traces.drainTo(list);
                log.debug("{} 写出 {} 条数据", name, list.size());
                context.output(list);
            } finally {
                list.clear();
            }
        }
    }

    /**
     * 当程序正常退出时，将缓存在内存中的数据同步写出
     */
    public void drainOnShutdown() {
        if (spans.size() > 0) {
            final List<Span> list = new SpanLinkedList();
            try {
                spans.drainTo(list);
                log.debug("程序退出，{} 写出 {} 条数据", name, list.size());
                context.output(list);
            } finally {
                list.clear();
            }
        }
        if (traces.size() > 0) {
            final List<Span> list = new TraceLinkedList();
            try {
                spans.drainTo(list);
                log.debug("程序退出，{} 写出 {} 条数据", name, list.size());
                context.output(list);
            } finally {
                list.clear();
            }
        }
    }
}
