package zenv.slot.trace;

/**
 * 获取本次调用链的全局追踪 id，每次请求中的所涉及的方法的追踪 id 一致
 *
 * @author zhengwei AKA zenv
 * @since 2022/8/22 9:38
 */
public class TraceContext {
    private static final ThreadLocal<String> TRACE_LOCAL = new ThreadLocal<>();

    public synchronized static void clear() {
        TRACE_LOCAL.remove();
    }

    public synchronized static String getTraceId() {
        return TRACE_LOCAL.get();
    }

    public synchronized static void setTraceId(String traceId) {
        TRACE_LOCAL.set(traceId);
    }
}
