package zenv.slot.disruptor;

import zenv.slot.conf.Constant;
import zenv.slot.entity.Span;
import zenv.slot.internal.com.lmax.disruptor.RingBuffer;
import zenv.slot.internal.com.lmax.disruptor.SequenceBarrier;
import zenv.slot.internal.com.lmax.disruptor.WaitStrategy;
import zenv.slot.internal.com.lmax.disruptor.WorkerPool;
import zenv.slot.internal.com.lmax.disruptor.dsl.ProducerType;
import zenv.slot.internal.org.slf4j.Logger;
import zenv.slot.log.SlotLogUtils;
import zenv.slot.utils.LRUCache;

import java.util.Arrays;

/**
 * @author zhengwei AKA zenv
 * @since 2022/9/6 18:31
 */
public class RingBufferWorkerPoolFactory {
    private static final Logger log = SlotLogUtils.getLogger(RingBufferWorkerPoolFactory.class);
    static final int MAXIMUM_CAPACITY = 1 << 30;

    private RingBufferWorkerPoolFactory() {
    }

    private static final RingBufferWorkerPoolFactory INSTANCE = new RingBufferWorkerPoolFactory();

    private final LRUCache<String, SpanProducer> produces = new LRUCache<>(Runtime.getRuntime().availableProcessors() * 4);

    private SpanConsumer[] consumers;

    private RingBuffer<Span> ringBuffer;

    public void initAndStart(ProducerType producerType,
                             int bufferSize,
                             WaitStrategy waitStrategy,
                             SpanConsumer[] spanConsumers) {
        bufferSize = ringBufferSizeFor(bufferSize);
        this.ringBuffer = RingBuffer.create(producerType, new SpanEventFactory(), bufferSize, waitStrategy);
        final SequenceBarrier sequenceBarrier = this.ringBuffer.newBarrier();
        final WorkerPool<Span> workerPool = new WorkerPool<>(
                this.ringBuffer,
                sequenceBarrier,
                new SpanExceptionHandler(),
                spanConsumers
        );

        this.consumers = spanConsumers;
        log.debug("埋点数据消费者: {}", Arrays.toString(consumers));

        this.ringBuffer.addGatingSequences(workerPool.getWorkerSequences());
        workerPool.start(Constant.EVENT_THREAD_POOL);
    }

    /**
     * 埋点数据的产生与业务处理息息相关，业务运行在业务线程中，我们认为处理的业务的线程是有限的，我们应该缓存一些生产者以提高生成效率
     *
     * @param producerId 生产者 id，即业务处理线程线程名
     * @return 埋点数据生产者
     */
    public SpanProducer getSpanProducer(String producerId) {
        SpanProducer spanProducer = produces.get(producerId);
        log.debug("尝试获取生产者： {}", spanProducer);
        if (null == spanProducer) {
            spanProducer = new SpanProducer(producerId, this.ringBuffer);
            log.debug("生产者缓存为 null，构建生产者: {}", spanProducer);
            this.produces.put(producerId, spanProducer);
        }
        return spanProducer;
    }

    public SpanConsumer[] getConsumers() {
        return consumers;
    }

    public static RingBufferWorkerPoolFactory getInstance() {
        return INSTANCE;
    }

    /**
     * 确保 ring buffer 的大小为2的 n 次幂，
     * 总是获取大于且离指定值最近的2次幂值，
     * <p>
     * 例如：指定值为16380，那么经过计算的值为16384
     *
     * @param size 从配置文件中获取的 ring buffer 的大小
     * @return 大于等于配置文件中的大小且离该值最近的2次幂值
     */
    private static int ringBufferSizeFor(int size) {
        int n = size - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }
}
