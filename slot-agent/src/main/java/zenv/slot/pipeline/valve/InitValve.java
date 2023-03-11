package zenv.slot.pipeline.valve;

import lombok.Getter;
import lombok.Setter;
import zenv.slot.conf.Constant;
import zenv.slot.databus.event.DeleteExpiredEvent;
import zenv.slot.databus.event.LookupSlotOutputSwitchEvent;
import zenv.slot.databus.event.RenameTempEvent;
import zenv.slot.databus.event.WriteSpanOutEvent;
import zenv.slot.databus.listener.CSVListener;
import zenv.slot.databus.listener.SpanOutputListener;
import zenv.slot.disruptor.SpanConsumer;
import zenv.slot.entity.thread.ShutDownHookThread;
import zenv.slot.internal.com.lmax.disruptor.dsl.ProducerType;
import zenv.slot.internal.org.slf4j.Logger;
import zenv.slot.log.SlotLogUtils;
import zenv.slot.pipeline.AbstractValve;
import zenv.slot.pipeline.PipelineContext;
import zenv.slot.pipeline.PoisonPill;
import zenv.slot.utils.DateUtils;
import zenv.slot.utils.FileUtils;
import zenv.slot.utils.SlotSwitchUtils;

import java.io.File;
import java.lang.invoke.SwitchPoint;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static zenv.slot.conf.Constant.*;

/**
 * 初始化必要环境变量
 *
 * @author zhengwei AKA zenv
 * @since 2022/8/1 10:32
 */
@Setter
@Getter
public class InitValve extends AbstractValve<Properties, Properties> {
    private static final Logger log = SlotLogUtils.getLogger(InitValve.class);
    private String name = "初始化环境节点";

    @Override
    public void init(PipelineContext context) {
        log.info("{} 开始初始化", this.name);
        // 数据总线开始分发事件
        Constant.DATA_BUS.dispatch();
        Constant.DATA_BUS.subscribe(new CSVListener());
        // 添加埋点信息输出监听器
        Constant.DATA_BUS.subscribe(new SpanOutputListener());
        initDataDir();
        initDisruptor();
    }

    /**
     * 初始化埋点数据输出路径
     */
    private void initDataDir() {
        final File file = new File(SLOT_OUTPUT_PATH);
        FileUtils.mkdirs(file);
    }

    /**
     * 初始化 disruptor
     */
    private void initDisruptor() {
        log.info("disruptor 等待策略 : {}", DISRUPTOR_CONSUMER_WAIT_STRATEGY.getClass().getName());
        // disruptor
        final SpanConsumer[] spanConsumers;
        if (DISRUPTOR_CONSUMER_COUNT <= 0) {
            spanConsumers = new SpanConsumer[Runtime.getRuntime().availableProcessors() * 2];
        } else {
            spanConsumers = new SpanConsumer[DISRUPTOR_CONSUMER_COUNT];
        }
        log.info("disruptor 消费者数量 : {}", spanConsumers.length);
        for (int i = 0; i < spanConsumers.length; i++) {
            spanConsumers[i] = new SpanConsumer(SLOT_DISRUPTOR_CONSUMER_CACHE_SIZE, SLOT_OUTPUT_MODE, "slot_consumer_" + i);
        }
        RING_BUFFER_WORKER_POOL_FACTORY.initAndStart(
                ProducerType.MULTI,
                SLOT_DISRUPTOR_RING_BUFFER_SIZE,
                DISRUPTOR_CONSUMER_WAIT_STRATEGY,
                spanConsumers
        );
    }

    /**
     * 读取埋点配置文件
     *
     * @param initProp 埋点配置文件信息
     * @param context  管道上线文
     * @return 配置信息
     */
    @Override
    public Properties process(Properties initProp, PipelineContext context) {
        // 注意:初始化服务名，如果有操作与服务名相关那么需要在此操作之后
        SERVICE.set(initProp.getProperty(SLOT_SERVICE));
        context.cache("POISON_PILL", new PoisonPill());
        context.cache(Constant.SLOT_MODE, initProp.get(Constant.SLOT_MODE));

        SlotSwitchUtils.initAndUpdateSwitch();
        // 固定周期发送写出事件
        // 当程序启动后，每整点5秒钟写出一次数据
        final TimeUnit unit = TimeUnit.valueOf(SLOT_OUTPUT_CYCLE_UNIT);
        SCHEDULED_THREAD_POOL.scheduleAtFixedRate(
                () -> {
                    if (!SLOT_OUTPUT_SWITCH.get()) {
                        log.info("埋点开关关闭，不再写出埋点数据");
                    } else {
                        Constant.DATA_BUS.publish(WriteSpanOutEvent.of());
                    }
                },
                DateUtils.getNowSubNearNext(SLOT_OUTPUT_CYCLE, TimeUnit.SECONDS),
                SLOT_OUTPUT_CYCLE,
                TimeUnit.SECONDS
        );

        // 固定事件发送重命名事件
        SCHEDULED_THREAD_POOL.scheduleAtFixedRate(
                () -> Constant.DATA_BUS.publish(RenameTempEvent.of(LocalDateTime.now().format(DateUtils.yyyyMMddHHmm))),
                DateUtils.getRenameDaley(SLOT_OUTPUT_CYCLE, unit),
                TimeUnit.SECONDS.convert(SLOT_OUTPUT_CYCLE, unit),
                TimeUnit.SECONDS
        );

        // 定时检查埋点输出开关
        SCHEDULED_THREAD_POOL.scheduleAtFixedRate(
                () -> Constant.DATA_BUS.publish(LookupSlotOutputSwitchEvent.of()),
                SLOT_OUTPUT_SWITCH_CHECK_CYCLE,
                SLOT_OUTPUT_SWITCH_CHECK_CYCLE,
                SLOT_OUTPUT_SWITCH_CHECK_CYCLE_UNIT
        );

        // 定时删除过期数据
        SCHEDULED_THREAD_POOL.scheduleAtFixedRate(
                () -> DATA_BUS.publish(DeleteExpiredEvent.of(
                        SERVICE.get(),
                        ".csv",
                        SLOT_OUTPUT_FILE_EXPIRE_DURATION,
                        TimeUnit.valueOf(SLOT_OUTPUT_FILE_EXPIRE_TIME_UNIT),
                        LocalDateTime.now())
                ),
                DateUtils.getNowSubNearNext(SLOT_OUTPUT_CYCLE, unit),
                SLOT_OUTPUT_CYCLE,
                unit
        );

        // 添加程序关闭钩子，以确保缓存内埋点数据被写出和资源释放
        Runtime.getRuntime().addShutdownHook(new ShutDownHookThread());

        return initProp;
    }
}
