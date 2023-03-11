package zenv.slot.databus;

import zenv.slot.conf.Constant;
import zenv.slot.internal.org.slf4j.Logger;
import zenv.slot.log.SlotLogUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * 数据总线的默认实现
 *
 * @author zhengwei AKA zenv
 * @since 2022/7/25 14:56
 */
public class DefaultDataBus implements IDataBus {
    private static final Logger log = SlotLogUtils.getLogger(DefaultDataBus.class);

    private String name;
    private static final DefaultDataBus INSTANCE = new DefaultDataBus();

    private final Set<IListener> listeners = new HashSet<>();

    public static IDataBus getInstance() {
        return INSTANCE;
    }

    @Override
    public void subscribe(IListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void unsubscribe(IListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void publish(IDataEvent event) {
        event.setDataBus(this);
        // 不让业务方法阻塞
        Constant.EVENT_BLOCKING_QUEUE.offer(event);
    }

    @Override
    public void dispatch() {
        Constant.EVENT_THREAD_POOL.execute(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    final IDataEvent event = Constant.EVENT_BLOCKING_QUEUE.take();
                    log.debug("开始分发事件...");
                    log.debug("监听到 {}", event.getName());
                    listeners.forEach(listener -> listener.accept(event));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void setDataBusName(String name) {
        this.name = name;
    }

    @Override
    public String getDataBusName() {
        return this.name;
    }
}
