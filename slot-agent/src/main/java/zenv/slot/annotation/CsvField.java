package zenv.slot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/18 10:46
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface CsvField {
    /**
     * 输出到 csv 文件时的列号，从0开始
     *
     * @return 列号
     */
    int index();

    /**
     * 在输出到埋点数据文件时，需要输出到 trace 文件的字段
     * <p>
     * 默认不输出到 trace 文件
     *
     * @return true-输出到 trace 字段，false-不输出到 trace 文件
     */
    boolean trace() default false;

    /**
     * 在输出到埋点数据文件时，需要输出到 span 文件的字段
     * <p>
     * 默认输出到 span 文件
     *
     * @return true-输出到 span 字段，false-不输出到 span 文件
     */
    boolean span() default true;
}
