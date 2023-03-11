package zenv.slot.disruptor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import zenv.slot.conf.Constant;
import zenv.slot.entity.Span;
import zenv.slot.internal.com.lmax.disruptor.InsufficientCapacityException;
import zenv.slot.internal.com.lmax.disruptor.RingBuffer;
import zenv.slot.internal.org.slf4j.Logger;
import zenv.slot.log.SlotLogUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author zhengwei AKA zenv
 * @since 2022/9/6 18:32
 */
@Setter
@Getter
@ToString
@RequiredArgsConstructor
public class SpanProducer {
    private static final Logger log = SlotLogUtils.getLogger(SpanProducer.class);
    private final String producerId;

    private final RingBuffer<Span> ringBuffer;

    public void onData(String traceId,
                       String spanId,
                       String parentId,
                       String className,
                       String methodName,
                       String methodDesc,
                       String classMethod,
                       boolean success,
                       LocalDateTime startDate,
                       LocalDateTime endDate,
                       long duration,
                       String exception,
                       String exceptionMsg,
                       StackTraceElement[] exceptionStack,
                       List<Object> methodParamsValue) {
        try {
            // 如果 ring buffer 已满则直接将数据丢弃
            final long sequence = ringBuffer.tryNext();
            final Span span = ringBuffer.get(sequence);
            span.setTraceId(traceId);
            span.setSpanId(spanId);
            span.setParentId(parentId);
            span.setServiceName(Constant.SERVICE.get());
            span.setClassName(className);
            span.setMethodName(methodName);
            span.setMethodDesc(methodDesc);
            span.setClassMethod(classMethod);
            span.setSuccess(success);
            span.setStartDate(startDate);
            span.setEndDate(endDate);
            span.setDuration(duration);
            span.setException(exception);
            span.setExceptionMsg(exceptionMsg);
            span.setExceptionStack(exceptionStack);
            span.setMethodParamsValue(methodParamsValue);
            ringBuffer.publish(sequence);
            log.debug("生成一个埋点数据 - {} - {} - {}", className, methodName, methodDesc);
        } catch (InsufficientCapacityException e) {
            log.error("队列已满，丢弃数据", e.fillInStackTrace());
            log.debug("数据为 : {},{},{},{},{},{},{},{},{},{},{},{},{}",
                    traceId,
                    spanId,
                    parentId,
                    className,
                    methodName,
                    methodDesc,
                    classMethod,
                    success,
                    startDate,
                    endDate,
                    duration,
                    exception,
                    exceptionMsg);
        }
    }
}
