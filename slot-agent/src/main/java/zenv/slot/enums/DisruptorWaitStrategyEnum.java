package zenv.slot.enums;

import zenv.slot.internal.com.lmax.disruptor.*;
import zenv.slot.internal.org.slf4j.Logger;
import zenv.slot.log.SlotLogUtils;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * disruptor 消费者等待策略枚举类
 *
 * @author zhengwei AKA zenv
 * @since 2022/10/21 9:29
 */
public enum DisruptorWaitStrategyEnum {
    /**
     * Blocking strategy that uses a lock and condition variable for {@link EventProcessor}s waiting on a barrier.
     * <p>
     * This strategy can be used when throughput and low-latency are not as important as CPU resource.
     */
    BLOCKING("blocking", new BlockingWaitStrategy()),

    /**
     * Sleeping strategy that initially spins, then uses a Thread.yield(), and
     * eventually sleep (<code>LockSupport.parkNanos(n)</code>) for the minimum
     * number of nanos the OS and JVM will allow while the
     * {@link EventProcessor}s are waiting on a barrier.
     * <p>
     * This strategy is a good compromise between performance and CPU resource.
     * Latency spikes can occur after quiet periods.  It will also reduce the impact
     * on the producing thread as it will not need signal any conditional variables
     * to wake up the event handling thread.
     */
    SLEEPING("sleeping", new SleepingWaitStrategy()),

    /**
     * Variation of the {@link BlockingWaitStrategy} that attempts to elide conditional wake-ups when
     * the lock is uncontended.  Shows performance improvements on microbenchmarks.  However this
     * wait strategy should be considered experimental as I have not full proved the correctness of
     * the lock elision code.
     */
    LITE_BLOCKING("lite_blocking", new LiteBlockingWaitStrategy()),

    /**
     * Yielding strategy that uses a Thread.yield() for {@link zenv.slot.internal.com.lmax.disruptor.EventProcessor}s waiting on a barrier
     * after an initially spinning.
     * <p>
     * This strategy will use 100% CPU, but will more readily give up the CPU than a busy spin strategy if other threads
     * require CPU resource.
     */
    YIELDING("yielding", new YieldingWaitStrategy()),

    /**
     * Busy Spin strategy that uses a busy spin loop for {@link zenv.slot.internal.com.lmax.disruptor.EventProcessor}s waiting on a barrier.
     * <p>
     * This strategy will use CPU resource to avoid syscalls which can introduce latency jitter.  It is best
     * used when threads can be bound to specific CPU cores.
     */
    BUSY_SPIN("busy_spin", new BusySpinWaitStrategy()),
    ;

    private final String waitStrategyName;
    private final WaitStrategy waitStrategy;
    private static final Logger log = SlotLogUtils.getLogger(DisruptorWaitStrategyEnum.class);

    DisruptorWaitStrategyEnum(String waitStrategyName, WaitStrategy waitStrategy) {
        this.waitStrategyName = waitStrategyName;
        this.waitStrategy = waitStrategy;
    }

    private static final Map<String, DisruptorWaitStrategyEnum> lookup = new HashMap<>(8);

    static {
        EnumSet.allOf(DisruptorWaitStrategyEnum.class).forEach(o -> lookup.put(o.waitStrategyName, o));
    }

    public static DisruptorWaitStrategyEnum parse(String waitStrategyName) {
        log.info("埋点配置消费者等待策略 : {}", waitStrategyName);
        return lookup.getOrDefault(waitStrategyName, BLOCKING);
    }

    public WaitStrategy getWaitStrategy() {
        return waitStrategy;
    }
}
