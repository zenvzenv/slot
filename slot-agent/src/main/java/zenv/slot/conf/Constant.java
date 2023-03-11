package zenv.slot.conf;

import zenv.slot.databus.DefaultDataBus;
import zenv.slot.databus.IDataBus;
import zenv.slot.databus.IDataEvent;
import zenv.slot.disruptor.RingBufferWorkerPoolFactory;
import zenv.slot.enums.DisruptorWaitStrategyEnum;
import zenv.slot.internal.com.lmax.disruptor.WaitStrategy;
import zenv.slot.pipeline.PipelineContext;
import zenv.slot.pipeline.valve.InitValve;
import zenv.slot.utils.PropertiesUtils;
import zenv.slot.utils.SequenceIdGenerator;
import zenv.slot.utils.SystemUtils;

import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/26 10:41
 */
public interface Constant {
    /*
    ============================slot.properties================================
     */
    /**
     * 埋点模式
     */
    String SLOT_MODE = "slot.mode";

    /**
     * 所属产品
     */
    String SLOT_SERVICE = "slot.service";

    /**
     * 全埋点模式
     */
    String SLOT_MODE_ALL = "ALL";

    /**
     * 指定内容埋点
     */
    String SLOT_MODE_SPECIAL = "SPECIAL";

    String CLASS_PREFIX = "slot.class.";

    String PACKAGE_PREFIX = "slot.package.";


    /**
     * 是否进行埋点开关
     */
    AtomicBoolean SLOT_OUTPUT_SWITCH = new AtomicBoolean(Boolean.FALSE);

    /*
    ============================conf.properties================================
     */

    /**
     * 埋点程序所需的配置文件
     */
    Properties CONF_PROP = PropertiesUtils.loadInternalProp("conf.properties");

    /**
     * 埋点信息输出模式，console、file 等
     */
    String SLOT_OUTPUT_MODE = CONF_PROP.getProperty("slot.output.mode");

    /**
     * 写文件周期间隔
     */
    int SLOT_OUTPUT_CYCLE = Integer.parseInt(CONF_PROP.getProperty("slot.output.cycle"));

    /**
     * 写文件周期时间单位
     */
    String SLOT_OUTPUT_CYCLE_UNIT = CONF_PROP.getProperty("slot.output.cycle.unit");

    /**
     * 重命名比写文件的延迟的秒数
     */
    int SLOT_RENAME_CYCLE_DELAY = Integer.parseInt(CONF_PROP.getProperty("slot.output.rename.cycle.delay"));

    /**
     * 埋点开关文件所在位置
     */
    String SLOT_OUTPUT_SWITCH_PROP_FILE_PATH = CONF_PROP.getProperty("slot.output.switch.prop.file.path");

    /**
     * disruptor consumer 的缓存埋点数据的队列大小
     */
    int SLOT_DISRUPTOR_CONSUMER_CACHE_SIZE = Integer.parseInt(CONF_PROP.getProperty("slot.disruptor.consumer.cache.size"));

    /**
     * disruptor 的环形队列大小
     */
    int SLOT_DISRUPTOR_RING_BUFFER_SIZE = Integer.parseInt(CONF_PROP.getProperty("slot.disruptor.ring.buffer.size"));

    /**
     * disruptor 消费者等待策略
     */
    WaitStrategy DISRUPTOR_CONSUMER_WAIT_STRATEGY = DisruptorWaitStrategyEnum.parse(CONF_PROP.getProperty("slot.disruptor.consumer.wait.strategy")).getWaitStrategy();

    /**
     * 埋点数据输出路径
     */
    String SLOT_OUTPUT_PATH = CONF_PROP.getProperty("slot.output.file.path");

    /**
     * 埋点数据的过期时间间隔
     */
    int SLOT_OUTPUT_FILE_EXPIRE_DURATION = Integer.parseInt(CONF_PROP.getProperty("slot.output.file.expire.cycle"));

    /**
     * 埋点数据的过期时间间隔单位
     */
    String SLOT_OUTPUT_FILE_EXPIRE_TIME_UNIT = CONF_PROP.getProperty("slot.output.file.expire.cycle.unit");

    int SLOT_OUTPUT_SWITCH_CHECK_CYCLE = Integer.parseInt(CONF_PROP.getProperty("slot.output.switch.check.cycle"));

    TimeUnit SLOT_OUTPUT_SWITCH_CHECK_CYCLE_UNIT = TimeUnit.valueOf(CONF_PROP.getProperty("slot.output.switch.check.cycle.unit"));

    /**
     * disruptor 消费者数量
     */
    int DISRUPTOR_CONSUMER_COUNT = Integer.parseInt(CONF_PROP.getProperty("slot.disruptor.consumer.count"));

    /**
     * 埋点需要排除的方法名，使用 ',' 分割
     */
    String[] SLOT_EXCLUDE_METHOD_NAME = CONF_PROP.getProperty("slot.exclude.method.name").split("[,]");

    /**
     * # 埋点需要排除的方法名前缀，使用 ',' 分割
     */
    String[] SLOT_EXCLUDE_METHOD_PREFIX = CONF_PROP.getProperty("slot.exclude.method.prefix").split("[,]");

    /**
     * 生成 id 相关
     */
    int SLOT_GENERATOR_WORKER_ID = Integer.parseInt(CONF_PROP.getProperty("slot.generator.worker.id"));

    /**
     * 生成 id 相关
     */
    int SLOT_GENERATOR_DATA_CENTER_ID = Integer.parseInt(CONF_PROP.getProperty("slot.generator.data.center.id"));

    /**
     * traceId,spanId 生成器
     */
    SequenceIdGenerator SEQUENCE_ID_GENERATOR = new SequenceIdGenerator(SLOT_GENERATOR_WORKER_ID, SLOT_GENERATOR_DATA_CENTER_ID);

    /*
    ============================other================================
     */
    /**
     * 埋点服务名称，在 {@link InitValve#process(Properties, PipelineContext)} 进行初始化
     * <p>
     * 注意:初始化服务名，如果有操作与服务名相关那么需要在 {@link InitValve#process(Properties, PipelineContext)} 之后
     */
    AtomicReference<String> SERVICE = new AtomicReference<>("null");

    /**
     * 主机名
     */
    String HOSTNAME = SystemUtils.getHostName();

    String SLOT_CSV_TEMP_SUFFIX = ".csv.temp";

    /**
     * 初始化数据总线
     */
    IDataBus DATA_BUS = DefaultDataBus.getInstance();

    /**
     * 初始化 disruptor 队列
     */
    RingBufferWorkerPoolFactory RING_BUFFER_WORKER_POOL_FACTORY = RingBufferWorkerPoolFactory.getInstance();

    BlockingQueue<IDataEvent> EVENT_BLOCKING_QUEUE = new LinkedBlockingQueue<>(1000);

    ThreadPoolExecutor EVENT_THREAD_POOL = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() * 4,
            Runtime.getRuntime().availableProcessors() * 4,
            30L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    ScheduledExecutorService SCHEDULED_THREAD_POOL = new ScheduledThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() * 2,
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    /**
     * 写锁，确保在把数据写出到文件的时候文件不会损坏
     */
    Lock WRITE_LOCK = new ReentrantLock();

    String ALL_NODE = "*";

    String SLOT_PROPERTIES_VALUE_SPLIT = ",";

    String SLOT_CSV_SPLIT = "\1";

    String CONSTRUCTOR_METHOD_NAME = "<init>";
    String STATIC_CONSTRUCTORS_METHOD_NAME = "<clinit>";
}
