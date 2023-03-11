package zenv.slot.entity.thread;

import zenv.slot.conf.Constant;
import zenv.slot.disruptor.SpanConsumer;
import zenv.slot.internal.org.slf4j.Logger;
import zenv.slot.log.SlotLogUtils;
import zenv.slot.utils.FileUtils;

import java.io.File;

/**
 * @author zhengwei AKA zenv
 * @since 2022/9/8 16:12
 */
public class ShutDownHookThread extends Thread {
    private static final Logger log = SlotLogUtils.getLogger(ShutDownHookThread.class);

    @Override
    public void run() {
        clean();
    }

    private void clean() {
        log.info("程序即将关闭...");
        log.info("将剩余数据写出");
        for (SpanConsumer consumer : Constant.RING_BUFFER_WORKER_POOL_FACTORY.getConsumers()) {
            consumer.drainOnShutdown();
        }

        // 将所有 .csv.temp 结尾的文件重命名为 .csv
        log.info("所有 .csv.temp 结尾的文件重命名为 .csv");
        FileUtils.renameSlotTempCSV(new File(Constant.SLOT_OUTPUT_PATH), Constant.SLOT_CSV_TEMP_SUFFIX);

        log.info("关闭线程池");
        Constant.EVENT_THREAD_POOL.shutdown();
    }
}
