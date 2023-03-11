package zenv.slot.utils;

import zenv.slot.annotation.CsvField;
import zenv.slot.entity.Span;
import zenv.slot.enums.SlotDataType;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

import static zenv.slot.conf.Constant.SLOT_CSV_SPLIT;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/8 17:17
 */
public final class CsvUtils {
    private CsvUtils() {
    }

    /**
     * 根据对象生成 csv 内容
     *
     * @param t   输出对象，该对象字段需要被 CsvField 注解标识
     * @param <T> 输出对象类型
     * @return 逗号分割的 csv 内容
     */
    public static <T> String getCsvContentStr(T t) {
        final Field[] declaredFields = t.getClass().getDeclaredFields();
        return Arrays.stream(declaredFields)
                .filter(field -> field.isAnnotationPresent(CsvField.class))
                .sorted(Comparator.comparingInt(field -> field.getAnnotation(CsvField.class).index()))
                .map(field -> {
                    field.setAccessible(true);
                    try {
                        return field.get(t);
                    } catch (IllegalAccessException e) {
                        return null;
                    }
                })
                .map(field -> {
                    if (field instanceof StackTraceElement[]) {
                        return Arrays.toString((StackTraceElement[]) field);
                    } else {
                        return Objects.toString(field);
                    }
                })
                .collect(Collectors.joining(SLOT_CSV_SPLIT))
                .replace("\n", "") + "\n";
    }

    /**
     * 根据不同的输出埋点数据类型决定输出哪些字段
     *
     * @param t    数据实体
     * @param type 埋点数据类型
     * @return 需要输出的字段内容
     */
    public static <T> String getCsvContentStr(T t, SlotDataType type) {
        if (null == type || SlotDataType.OTHER == type) {
            return getCsvContentStr(t);
        } else {
            final Field[] declaredFields = t.getClass().getDeclaredFields();
            return Arrays.stream(declaredFields)
                    .filter(field -> {
                        if (field.isAnnotationPresent(CsvField.class)) {
                            return (SlotDataType.TRACE == type && field.getAnnotation(CsvField.class).trace())
                                    || (SlotDataType.SPAN == type && field.getAnnotation(CsvField.class).span());
                        } else {
                            return false;
                        }
                    })
                    .sorted(Comparator.comparingInt(field -> field.getAnnotation(CsvField.class).index()))
                    .map(field -> {
                        field.setAccessible(true);
                        try {
                            return field.get(t);
                        } catch (IllegalAccessException e) {
                            return null;
                        }
                    })
                    .map(field -> {
                        if (field instanceof StackTraceElement[]) {
                            return Arrays.toString((StackTraceElement[]) field);
                        } else {
                            return Objects.toString(field);
                        }
                    })
                    .collect(Collectors.joining(SLOT_CSV_SPLIT))
                    .replace("\n", "") + "\n";
        }
    }

    public static String genCsvHeader(Class<?> clazz, SlotDataType type) {
        final Field[] declaredFields = clazz.getDeclaredFields();
        return Arrays.stream(declaredFields)
                .filter(field -> {
                    if (field.isAnnotationPresent(CsvField.class)) {
                        return (SlotDataType.TRACE == type && field.getAnnotation(CsvField.class).trace())
                                || (SlotDataType.SPAN == type && field.getAnnotation(CsvField.class).span());
                    } else {
                        return false;
                    }
                })
                .sorted(Comparator.comparingInt(field -> field.getAnnotation(CsvField.class).index()))
                .map(Field::getName)
                .collect(Collectors.joining(","));
    }

    public static void main(String[] args) {
        System.out.println(genCsvHeader(Span.class, SlotDataType.TRACE));
        System.out.println(genCsvHeader(Span.class, SlotDataType.SPAN));
    }
}
