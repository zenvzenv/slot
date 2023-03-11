package zenv.slot.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import zenv.slot.annotation.CsvField;
import zenv.slot.conf.Constant;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/22 9:35
 */
@Getter
@Setter
@ToString
public class Span {
    /**
     * 本次调用追踪 id
     */
    @CsvField(index = 0, trace = true)
    private String traceId;

    /**
     * 每次方法调用时生成 id
     */
    @CsvField(index = 1)
    private String spanId;

    /**
     * 调用此方法的父 span id，如果此方法为 root method 则 parent id 为 null
     */
    @CsvField(index = 2)
    private String parentId;

    /**
     * 所属产品
     */
    @CsvField(index = 3, trace = true)
    private String serviceName;

    /**
     * 主机名称
     */
    @CsvField(index = 4, trace = true)
    private String hostname = Constant.HOSTNAME;

    /**
     * 方法所属类
     */
    @CsvField(index = 5, trace = true)
    private String className;

    /**
     * 本次调用的方法名称
     */
    @CsvField(index = 6, trace = true)
    private String methodName;

    /**
     * 方法描述符
     */
    @CsvField(index = 7, trace = true)
    private String methodDesc;

    /**
     * 方法全限定名，class#method
     */
    @CsvField(index = 8, span = false)
    private String classMethod;

    /**
     * 方法调用结果，true-成功，false-失败
     * <p>
     * 如果方法执行过程中进入了异常处理代码块或抛出了异常，则认为方法执行失败
     */
    @CsvField(index = 9, trace = true)
    private boolean success;

    /**
     * 方法调用开始时间
     */
    @CsvField(index = 10, trace = true)
    private LocalDateTime startDate;

    /**
     * 方法调用结束时间
     */
    @CsvField(index = 11, trace = true)
    private LocalDateTime endDate;

    /**
     * 方法耗时，单位为 ms
     */
    @CsvField(index = 12, trace = true)
    private long duration;

    /**
     * 如果方法执行失败则记录失败时的异常
     * <p>
     * 只有当方法执行失败时才会记录，否则该字段为 null
     */
    @CsvField(index = 13)
    private String exception;

    /**
     * 如果方法执行失败则记录失败时的异常信息
     * <p>
     * 只有当方法执行失败时才会记录，否则该字段为 null
     */
    @CsvField(index = 14)
    private String exceptionMsg;

    /**
     * 如果方法执行失败则记录方法异常栈
     * <p>
     * 只有当方法执行失败时才会记录，否则该字段为 null
     */
    @CsvField(index = 15)
    private StackTraceElement[] exceptionStack;

    /**
     * 方法实际传入的参数值
     */
    @CsvField(index = 16)
    private List<Object> methodParamsValue;
}
