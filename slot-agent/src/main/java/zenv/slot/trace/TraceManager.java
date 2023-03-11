package zenv.slot.trace;

import zenv.slot.utils.ASMUtils;

import java.util.Stack;

/**
 * 考虑到内存消耗情况，调用栈中只存放 span id
 *
 * @author zhengwei AKA zenv
 * @since 2022/8/22 9:34
 */
public class TraceManager {
    private static final ThreadLocal<Stack<String>> TRACE = new ThreadLocal<>();
    public static final String ROOT_METHOD_SPAN_ID = "0";

    /**
     * 创建一个新的方法调用 span，调用栈中只存储 span id
     *
     * @return 新 span id
     */
    private synchronized static String createSpan() {
        // 获取当前调用栈
        Stack<String> stack = TRACE.get();
        if (null == stack) {
            stack = new Stack<>();
            TRACE.set(stack);
        }
        // 如果当前调用栈为空，那么则认为该方法为 root method
        // 并创建此次调用链 id
        if (stack.isEmpty()) {
            TraceContext.setTraceId(ASMUtils.genId());
        }
        return ASMUtils.genId();
    }

    /**
     * 新增一个方法调用栈，并压栈
     *
     * @return 当前 span
     */
    public synchronized static String entrySpan() {
        // 生成新的调用栈
        final String span = createSpan();
        final Stack<String> spans = TRACE.get();
        // 压栈
        spans.push(span);
        return span;
    }

    /**
     * 方法退出时同时把 span 出栈标识该方法退出调用栈
     */
    public synchronized static void exitSpan() {
        final Stack<String> spans = TRACE.get();
        if (null == spans || spans.isEmpty()) {
            TraceContext.clear();
            return;
        }
        // 如果栈为空则认为本次调用链结束
        spans.pop();
        if (spans.isEmpty()) {
            TraceContext.clear();
        }
    }

    /**
     * 获取父 span id，如果当前栈为空，则认为当前方法为 root method
     * <p>
     * 该方法应该先于 entrySpan 调用
     *
     * @return 当前方法的父 span id
     */
    public synchronized static String getParentSpan() {
        final Stack<String> spans = TRACE.get();
        if (null == spans || spans.isEmpty()) {
            return ROOT_METHOD_SPAN_ID;
        }
        return spans.peek();
    }
}
