package zenv.slot.test.asm;

import zenv.slot.conf.Constant;
import zenv.slot.trace.TraceContext;
import zenv.slot.trace.TraceManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/22 11:15
 */
public class HelloWorldTarget2 {
    public void m3(int a, int b) throws Throwable {
        final LocalDateTime startDate = LocalDateTime.now();
        long timer = System.currentTimeMillis();
        String parentSpanId = null;
        String spanId = null;
        String traceId = null;
        String exception = null;
        String exceptionMsg = null;
        StackTraceElement[] exceptionStack = null;
        List<Object> methodParamsValue = null;
        if (Constant.SLOT_OUTPUT_SWITCH.get()) {
            parentSpanId = TraceManager.getParentSpan();
            spanId = TraceManager.entrySpan();
            traceId = TraceContext.getTraceId();

            methodParamsValue = new ArrayList<>(2);
            methodParamsValue.add(a);
            methodParamsValue.add(b);
        }

        try {
            try {
                int c = a + b;
                TimeUnit.SECONDS.sleep(1);
                System.out.println("m2m2m2m2m2m2m2m2m2m2m2");
            } catch (InterruptedException e) {
                if (Constant.SLOT_OUTPUT_SWITCH.get()) {
                    exception = e.getClass().getName();
                    exceptionMsg = e.getMessage();
                    exceptionStack = e.getStackTrace();
                }

                throw new InterruptedException();
            } catch (Exception e1) {
                if (Constant.SLOT_OUTPUT_SWITCH.get()) {
                    exception = e1.getClass().getName();
                    exceptionMsg = e1.getMessage();
                    exceptionStack = e1.getStackTrace();
                }
            }
            if (Constant.SLOT_OUTPUT_SWITCH.get() && null != traceId) {
                timer = System.currentTimeMillis() - timer;
                TraceManager.exitSpan();
                Constant.RING_BUFFER_WORKER_POOL_FACTORY.getSpanProducer(Thread.currentThread().getName()).onData(
                        traceId,
                        spanId,
                        parentSpanId,
                        "className",
                        "methodName",
                        "methodDesc",
                        "class#method",
                        true,
                        startDate,
                        LocalDateTime.now(),
                        timer,
                        exception,
                        exceptionMsg,
                        exceptionStack,
                        methodParamsValue
                );
            }
        } catch (Throwable e) {
            if (Constant.SLOT_OUTPUT_SWITCH.get() && null != traceId) {
                timer = System.currentTimeMillis() - timer;
                exception = e.getClass().toString();
                exceptionMsg = e.getMessage();
                exceptionStack = e.getStackTrace();
                TraceManager.exitSpan();
                Constant.RING_BUFFER_WORKER_POOL_FACTORY.getSpanProducer(Thread.currentThread().getName()).onData(
                        traceId,
                        spanId,
                        parentSpanId,
                        "className",
                        "methodName",
                        "methodDesc",
                        "class#method",
                        false,
                        startDate,
                        LocalDateTime.now(),
                        timer,
                        exception,
                        exceptionMsg,
                        exceptionStack,
                        methodParamsValue
                );
            }
            throw new Exception(e);
        }
    }

    public int m5(String s) throws Exception {
        try {
//            final Object o = new Object();
//            return Integer.parseInt(s);
            System.out.println("slotslotslotslotslotslotslotslotslotslotslotslotslotslotslotslotslotslotslotslotslotslotslotslot");
            return 0;
        } catch (Throwable e) {
            throw (Exception) e;
        }
    }
}
