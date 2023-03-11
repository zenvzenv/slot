package zenv.slot.utils;

import lombok.SneakyThrows;
import zenv.slot.entity.Span;
import zenv.slot.enums.SlotDataType;
import zenv.slot.internal.org.slf4j.Logger;
import zenv.slot.log.SlotLogUtils;

import java.io.*;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardOpenOption.*;
import static zenv.slot.conf.Constant.*;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/8 14:01
 */
public final class FileUtils {
    private static final Logger log = SlotLogUtils.getLogger(FileUtils.class);

    private FileUtils() {
    }

    public static void writeCsvOut(List<Span> contents, SlotDataType type) {
        if (contents.size() <= 0) return;
        final String fullPath = genCsvFileName(type);
        log.debug("将 {} 条埋点数据写出到 {}", contents.size(), fullPath);
        WRITE_LOCK.lock();
        try (final BufferedWriter writer = Files.newBufferedWriter(Paths.get(fullPath), APPEND, WRITE, CREATE)) {
            for (Span span : contents) {
                writer.write(CsvUtils.getCsvContentStr(span, type));
            }
        } catch (IOException e) {
            log.error("埋点写出失败，失败原因: {}", e);
        } finally {
            WRITE_LOCK.unlock();
        }
    }

    public static void writeCsvOut(Span span) {
        WRITE_LOCK.lock();
        // 追加写，文件不存在则新建
        try (final BufferedWriter writer = Files.newBufferedWriter(Paths.get(genCsvFileName()), APPEND, WRITE, CREATE)) {
            writer.write(CsvUtils.getCsvContentStr(span));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            WRITE_LOCK.unlock();
        }
    }

    /**
     * 根据相对路径获取绝对路径
     *
     * @param relativePath 相对路径
     * @return 绝对路径
     */
    @SneakyThrows
    public static String getFilePath(String relativePath) {
        final String path = Objects.requireNonNull(FileUtils.class.getResource("/")).getPath();
        final String encode = URLDecoder.decode(path, "utf-8");
        return encode + relativePath;
    }

    public static byte[] readBytes(String filePath) {
        final File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("file not exists " + filePath);
        }

        try (final FileInputStream fis = new FileInputStream(file);
             final BufferedInputStream bis = new BufferedInputStream(fis);
             final ByteArrayOutputStream bao = new ByteArrayOutputStream()) {
            IOUtils.copy(bis, bao);
            return bao.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("can not read file " + filePath);
    }

    public static void writeBytes(String filePath, byte[] bytes) {
        final File file = new File(filePath);
        final File dirFile = file.getParentFile();
        mkdirs(dirFile);

        try (final FileOutputStream fos = new FileOutputStream(file);
             final BufferedOutputStream buff = new BufferedOutputStream(fos)) {
            buff.write(bytes);
            buff.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void mkdirs(File dirFile) {
        final boolean exists = dirFile.exists();
        if (exists && dirFile.isDirectory()) {
            return;
        }
        if (exists && dirFile.isFile()) {
            throw new RuntimeException("not a dir " + dirFile);
        }
        if (!exists) {
            if (dirFile.mkdirs()) {
                log.info("{} 目录创建成功", dirFile.getPath());
            } else {
                log.warn("{} 目录创建失败", dirFile.getPath());
            }
        }
    }

    /**
     * 将 csv 临时文件重命名，如果 .csv.temp 改名之后存在相同文件，那么则把临时文件合并到 .csv 文件，并删除 .csv.temp 文件
     *
     * @param dir    输出文件目录
     * @param suffix 临时文件后缀 - xxx.csv.temp
     */
    public static void renameSlotTempCSV(File dir, String suffix) {
        log.info("开始重命名...");
        WRITE_LOCK.lock();
        try {
            final File[] files = dir.listFiles((d, name) -> name.endsWith(suffix));
            if (null == files || files.length == 0) {
                log.info("暂无文件需要重命名...");
                return;
            }
            for (File file : files) {
                // temp 文件名
                final String tempFileName = file.getName();
                // 重命名的全路径
                final String csvFileFullPath = dir.getAbsolutePath() + File.separator + tempFileName.substring(0, tempFileName.lastIndexOf('.'));
                if (file.renameTo(new File(csvFileFullPath))) {
                    log.info("{} 重命名成功 {}", tempFileName, csvFileFullPath);
                } else {
                    try (final FileOutputStream fos = new FileOutputStream(csvFileFullPath, true);
                         final FileChannel csvChannel = fos.getChannel();
                         final FileInputStream tempFileStream = new FileInputStream(file);
                         final FileChannel tempFileChannel = tempFileStream.getChannel()) {
                        csvChannel.transferFrom(tempFileChannel, csvChannel.size(), tempFileChannel.size());
                    } catch (IOException e) {
                        log.error("合并文件失败，失败原因: {}", e);
                    } finally {
                        log.info("合并文件删除 - {}", file.delete());
                    }
                }
            }
        } finally {
            WRITE_LOCK.unlock();
        }
    }

    private static final String SLOT_FILE_NAME_SPLIT = "#";

    /**
     * 生成 csv 临时文件名
     *
     * @return 临时文件名 - /tmp/slot/data/[serviceName]#[hostname]#slot#[yyyyMMddHHmm].csv.temp
     */
    public static String genCsvFileName() {
        final LocalDateTime nearNextGapMin = DateUtils.getNearNext(SLOT_OUTPUT_CYCLE, TimeUnit.valueOf(SLOT_OUTPUT_CYCLE_UNIT));
        return SLOT_OUTPUT_PATH + File.separator + SERVICE.get() + SLOT_FILE_NAME_SPLIT + HOSTNAME + SLOT_FILE_NAME_SPLIT + "slot" + SLOT_FILE_NAME_SPLIT + nearNextGapMin.format(DateUtils.yyyyMMddHHmm) + SLOT_CSV_TEMP_SUFFIX;
    }

    /**
     * 将 trace 和 span 数据分开
     *
     * @param type trace 或 span
     * @return /tmp/slot/data/[serviceName]#[hostname]#slot#[trace/span]#[yyyyMMddHHmm].csv.temp
     */
    public static String genCsvFileName(SlotDataType type) {
        final LocalDateTime nearNextGapMin = DateUtils.getNearNext(SLOT_OUTPUT_CYCLE, TimeUnit.valueOf(SLOT_OUTPUT_CYCLE_UNIT));
        if (null != type) {
            return SLOT_OUTPUT_PATH
                    + File.separator
                    + SERVICE.get()
                    + SLOT_FILE_NAME_SPLIT
                    + HOSTNAME
                    + SLOT_FILE_NAME_SPLIT
                    + "slot"
                    + SLOT_FILE_NAME_SPLIT
                    + type.getType()
                    + SLOT_FILE_NAME_SPLIT
                    + nearNextGapMin.format(DateUtils.yyyyMMddHHmm)
                    + SLOT_CSV_TEMP_SUFFIX;
        } else {
            return genCsvFileName();
        }
    }

    /**
     * 删除过期埋点数据文件
     *
     * @param prefix 文件名前缀
     * @param suffix 文件名后缀
     * @param date   文件名中包含的时间，即过期时间
     */
    public static void deleteExpireSlotFile(String prefix, String suffix, String date) {
        log.info("准备删除过期埋点数据");
        final File outputDir = new File(SLOT_OUTPUT_PATH);
        final File[] files = outputDir.listFiles((dir, name) -> {
            if (null != name && 0 < name.length() && name.startsWith(prefix) && name.endsWith(suffix)) {
                log.debug("name: {}", name);
                try {
                    final String[] split = name.substring(0, name.lastIndexOf(".csv")).split("[_]");
                    String fileNameDate = split[split.length - 1];
                    final long fileNameDateLong = DateUtils.strToLong(fileNameDate);
                    final long expireDateLong = DateUtils.strToLong(date);
                    return fileNameDateLong < expireDateLong;
                } catch (Exception e) {
                    log.error("ex:{}, name: {}", e.getMessage(), name);
                }
            }
            return false;
        });
        if (null == files || files.length == 0) {
            log.info("无过期文件需要删除");
            return;
        }
        for (File file : files) {
            log.info("删除 {} {}", file.getName(), file.delete() ? "成功" : "失败");
        }
    }
}
