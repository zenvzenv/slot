package zenv.slot.conf;

import jdk.internal.org.objectweb.asm.Type;
import zenv.slot.databus.event.CacheSpanOutputEvent;
import zenv.slot.disruptor.RingBufferWorkerPoolFactory;
import zenv.slot.disruptor.SpanProducer;
import zenv.slot.entity.Span;
import zenv.slot.trace.TraceContext;
import zenv.slot.trace.TraceManager;
import zenv.slot.utils.ASMUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/22 15:31
 */
public interface ASMConf {
    Type SPAN_TYPE = Type.getType(Span.class);
    Type TRACE_MANAGER_TYPE = Type.getType(TraceManager.class);
    Type TRACE_CONTEXT_TYPE = Type.getType(TraceContext.class);
    Type STRING_TYPE = Type.getType(String.class);
    Type SYSTEM_TYPE = Type.getType(System.class);
    Type OBJECT_TYPE = Type.getType(Object.class);
    Type LOCAL_DATE_TIME_TYPE = Type.getType(LocalDateTime.class);
    Type LIST_TYPE = Type.getType(List.class);
    Type INT_OBJ_TYPE = Type.getType(Integer.class);
    Type LONG_OBJ_TYPE = Type.getType(Long.class);
    Type CHAR_OBJ_TYPE = Type.getType(CharSequence.class);
    Type BOOLEAN_OBJ_TYPE = Type.getType(Boolean.class);
    Type BYTE_OBJ_TYPE = Type.getType(Byte.class);
    Type FLOAT_OBJ_TYPE = Type.getType(Float.class);
    Type DOUBLE_OBJ_TYPE = Type.getType(Double.class);
    Type SHORT_OBJ_TYPE = Type.getType(Short.class);
    Type ASM_UTILS_TYPE = Type.getType(ASMUtils.class);
    Type SPAN_OUTPUT_EVENT = Type.getType(CacheSpanOutputEvent.class);
    Type ATOMIC_BOOLEAN_TYPE = Type.getType(AtomicBoolean.class);
    Type CONSTANT_TYPE = Type.getType(Constant.class);
    Type THROWABLE_TYPE = Type.getType(Throwable.class);
    Type SPAN_PRODUCER_TYPE = Type.getType(SpanProducer.class);
    Type RING_BUFFER_WORKER_POOL_FACTORY = Type.getType(RingBufferWorkerPoolFactory.class);
    Type THREAD_TYPE = Type.getType(Thread.class);
    Type STACK_TRAC_ELEMENT_ARRAY_TYPE = Type.getType("[Ljava/lang/StackTraceElement;");
    Type EXCEPTION_TYPE = Type.getType(Exception.class);
}
