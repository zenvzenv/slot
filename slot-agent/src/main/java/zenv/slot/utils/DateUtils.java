package zenv.slot.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import static zenv.slot.conf.Constant.SLOT_RENAME_CYCLE_DELAY;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/26 9:18
 */
public final class DateUtils {
    public static final DateTimeFormatter SAMPLE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DEFAULT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    public static final DateTimeFormatter yyyyMMddHHmm = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    public static final DateTimeFormatter yyyyMMddHHmmss = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    public static final DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final ZoneId SYSTEM_DEFAULT_ZONE_ID = ZoneId.systemDefault();
    public static final ZoneOffset DEFAULT_OFFSET = ZoneOffset.of("+8");

    /**
     * 时间戳转 yyyy-MM-dd HH:mm:ss.SSS
     *
     * @param timestamp 时间戳
     * @return yyyy-MM-dd HH:mm:ss.SSS
     */
    public static String timestampToDate(long timestamp) {
        final Instant instant = Instant.ofEpochMilli(timestamp);
        return LocalDateTime.ofInstant(instant, SYSTEM_DEFAULT_ZONE_ID).format(DEFAULT_FORMAT);
    }

    public static String timestampToDate(long timestamp, DateTimeFormatter formatter) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), SYSTEM_DEFAULT_ZONE_ID).format(formatter);
    }

    /**
     * 获取日志文件名中的时间
     */
    public static String genSlotLogDate() {
        return LocalDateTime.now().format(yyyyMMdd);
    }

    /**
     * 获取下一个最近的固定间隔整点时间
     */
    public static LocalDateTime getNearNext(int gap, TimeUnit unit) {
        final LocalDateTime now = LocalDateTime.now();
        if (unit == TimeUnit.SECONDS) {
            return now.plusSeconds(gap - (now.getSecond()) % gap);
        } else if (unit == TimeUnit.MINUTES) {
            return now.withSecond(0).plusMinutes(gap - (now.getMinute()) % gap);
        } else if (unit == TimeUnit.HOURS) {
            return now.withSecond(0).withMinute(0).plusHours(gap - (now.getHour()) % gap);
        } else if (unit == TimeUnit.DAYS) {
            return now.withSecond(0).withMinute(0).withHour(0).plusDays(gap - (now.getDayOfMonth()) % gap);
        } else {
            throw new RuntimeException("生成下个固定间隔整点时间失败！");
        }
    }

    /**
     * 得到距离下一个最近固定间隔相差的秒数、分钟数、小时数或天数
     * <p>
     * 例如：现在的时间为 20220916134318 ，传入的参数为 5 SECONDS 那么返回 2，因为20220916134320 - 20220916134318 = 2，
     * 即下一个整5秒(能被5整除)的时间的间隔
     *
     * @param gap  时间间隔
     * @param unit 时间单位
     */
    public static long getNowSubNearNext(int gap, TimeUnit unit) {
        final LocalDateTime now = LocalDateTime.now();
        if (unit == TimeUnit.SECONDS) {
            final LocalDateTime next = now.plusSeconds(gap - (now.getSecond()) % gap);
            return next.toEpochSecond(DEFAULT_OFFSET) - now.toEpochSecond(DEFAULT_OFFSET);
        } else if (unit == TimeUnit.MINUTES) {
            final LocalDateTime next = now.withSecond(0).plusMinutes(gap - (now.getMinute()) % gap);
            return (next.toEpochSecond(DEFAULT_OFFSET) - now.toEpochSecond(DEFAULT_OFFSET)) / 60;
        } else if (unit == TimeUnit.HOURS) {
            final LocalDateTime next = now.withSecond(0).withMinute(0).plusHours(gap - (now.getHour()) % gap);
            return (next.toEpochSecond(DEFAULT_OFFSET) - now.toEpochSecond(DEFAULT_OFFSET)) / 60 / 60;
        } else if (unit == TimeUnit.DAYS) {
            final LocalDateTime next = now.withSecond(0).withMinute(0).withHour(0).plusDays(gap - (now.getDayOfMonth()) % gap);
            return (next.toEpochSecond(DEFAULT_OFFSET) - now.toEpochSecond(DEFAULT_OFFSET)) / 60 / 60 / 24;
        } else {
            throw new RuntimeException("得到距离下一个最近固定分钟相差的分钟数发生错误");
        }
    }

    /**
     * 重命名事件比写文件延迟的秒数
     * <p>
     * 例如：写文件的间隔时间为5分钟，那么重命名的间隔时间为5分5秒。写文件的截至时间20220901142500-重命名时间20220901142505
     *
     * @param gap  写文件的固定间隔
     * @param unit 时间单位
     * @return 比写文件延迟5秒的时间
     */
    public static long getRenameDaley(int gap, TimeUnit unit) {
        final LocalDateTime now = LocalDateTime.now();
        if (unit == TimeUnit.SECONDS) {
            final LocalDateTime next = now.plusSeconds(gap - (now.getSecond()) % gap);
            return next.toEpochSecond(DEFAULT_OFFSET) - now.toEpochSecond(DEFAULT_OFFSET);
        } else if (unit == TimeUnit.MINUTES) {
            final LocalDateTime next = now.withSecond(0).plusMinutes(gap - (now.getMinute()) % gap);
            return (next.toEpochSecond(DEFAULT_OFFSET) - now.toEpochSecond(DEFAULT_OFFSET)) + SLOT_RENAME_CYCLE_DELAY;
        } else if (unit == TimeUnit.HOURS) {
            final LocalDateTime next = now.withSecond(0).withMinute(0).plusHours(gap - (now.getHour()) % gap);
            return (next.toEpochSecond(DEFAULT_OFFSET) - now.toEpochSecond(DEFAULT_OFFSET)) + SLOT_RENAME_CYCLE_DELAY;
        } else if (unit == TimeUnit.DAYS) {
            final LocalDateTime next = now.withSecond(0).withMinute(0).withHour(0).plusDays(gap - (now.getDayOfMonth()) % gap);
            return (next.toEpochSecond(DEFAULT_OFFSET) - now.toEpochSecond(DEFAULT_OFFSET)) + SLOT_RENAME_CYCLE_DELAY;
        } else {
            throw new RuntimeException("获取重命名时间延迟秒数失败");
        }
    }

    public static long strToLong(String date) {
        return LocalDateTime.parse(date, yyyyMMddHHmm).toEpochSecond(DEFAULT_OFFSET);
    }
}
