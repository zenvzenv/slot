package zenv.slot.log;

import zenv.slot.utils.DateUtils;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * 日志模板类
 *
 * @author zhengwei AKA zenv
 * @since 2022/7/26 8:49
 */
public class LogFormatter extends Formatter {
    /**
     * yyyy-MM-dd HH:mm:ss [level] [class:method:line] message
     */
    private static final String FORMATTER = "%s [%s] [%s:%s:%d] %s \n";

    @Override
    public String format(LogRecord record) {
        return String.format(
                FORMATTER,
                DateUtils.timestampToDate(record.getMillis()),
                record.getLevel(),
                record.getSourceClassName(),
                record.getSourceMethodName(),
                getMethodLineNumber(record.getSourceMethodName(), Thread.currentThread().getStackTrace()),
                record.getMessage()
        );
    }

    /**
     * 获取当前方法运行的行号
     *
     * @param methodName         当前运行的方法名称
     * @param stackTraceElements 当前方法的调用栈
     * @return 行号
     */
    private int getMethodLineNumber(String methodName, StackTraceElement[] stackTraceElements) {
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            if (stackTraceElement.getMethodName().equals(methodName)) {
                return stackTraceElement.getLineNumber();
            }
        }
        return 0;
    }

    public static String format(String msg, Object... args) {
        msg = msg.replace("{}", "%s");
        return String.format(msg, args);
    }
}
