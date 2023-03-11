package zenv.slot.databus.listener;

import zenv.slot.conf.Constant;
import zenv.slot.databus.IDataEvent;
import zenv.slot.databus.IListener;
import zenv.slot.databus.event.DeleteExpiredEvent;
import zenv.slot.databus.event.RenameTempEvent;
import zenv.slot.internal.org.slf4j.Logger;
import zenv.slot.log.SlotLogUtils;
import zenv.slot.utils.DateUtils;
import zenv.slot.utils.FileUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static zenv.slot.conf.Constant.SLOT_OUTPUT_PATH;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/26 14:57
 */
public class CSVListener implements IListener {
    private static final Logger log = SlotLogUtils.getLogger(CSVListener.class);

    @Override
    public void accept(IDataEvent event) {
        if (event instanceof RenameTempEvent) {
            handle((RenameTempEvent) event);
        } else if (event instanceof DeleteExpiredEvent) {
            handle((DeleteExpiredEvent) event);
        }
    }

    /**
     * 将 yyyyMMddHHmm.csv.temp 结尾的文件重命名为 yyyyMMddHHmm.csv
     */
    private void handle(RenameTempEvent event) {
        log.debug("重命名 csv 文件...");
        final String date = event.getDate();
        final String suffix = date + Constant.SLOT_CSV_TEMP_SUFFIX;
        log.debug("重命名的 csv 文件名日期为 {},现在的时间为 {},文件名后缀为 {}", date, LocalDateTime.now().format(DateUtils.yyyyMMddHHmm), suffix);
        FileUtils.renameSlotTempCSV(new File(SLOT_OUTPUT_PATH), suffix);
    }

    /**
     * 处理过期文件删除，处理的粒度到分钟(到秒级意义不大)
     */
    private void handle(DeleteExpiredEvent event) {
        final int duration = event.getDuration();
        final TimeUnit timeUnit = event.getTimeUnit();
        final String prefix = event.getPrefix();
        final String suffix = event.getSuffix();
        final LocalDateTime dateTime = event.getDateTime();
        log.info("开始清除 {} {} 前过期数据, 过期时间为: {}", duration, timeUnit, dateTime);
        final String expireDate = dateTime.minusSeconds(timeUnit.toSeconds(duration)).format(DateUtils.yyyyMMddHHmm);
        FileUtils.deleteExpireSlotFile(prefix, suffix, expireDate);
    }
}
