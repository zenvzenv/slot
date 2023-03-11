package zenv.slot.test.asm;

import zenv.slot.conf.Constant;
import zenv.slot.databus.IDataEvent;
import zenv.slot.databus.event.CacheSpanOutputEvent;
import zenv.slot.entity.Span;
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
public class HelloWorldTarget {
    public void m3(int a, int b) throws Exception {
        final Span currentSpan = new Span();
        long timer = System.currentTimeMillis();
        if (Constant.SLOT_OUTPUT_SWITCH.get()) {
            final String parentSpanId = TraceManager.getParentSpan();
            final String spanId = TraceManager.entrySpan();
            final String traceId = TraceContext.getTraceId();
            currentSpan.setTraceId(traceId);
            currentSpan.setSpanId(spanId);
            currentSpan.setParentId(parentSpanId);
            currentSpan.setMethodName("m2");
            currentSpan.setClassMethod("class#method");
            currentSpan.setServiceName("aaaa");

            final List<Object> methodParamsValue = new ArrayList<>(2);
            methodParamsValue.add(a);
            methodParamsValue.add(b);
            currentSpan.setMethodParamsValue(methodParamsValue);
        }


        try {
            int c = a + b;
            TimeUnit.SECONDS.sleep(1);
            System.out.println("m2m2m2m2m2m2m2m2m2m2m2");
        } catch (InterruptedException e) {
            if (Constant.SLOT_OUTPUT_SWITCH.get()) {
                currentSpan.setException(e.getClass().getName());
                currentSpan.setExceptionMsg(((Throwable) e).getMessage());

                currentSpan.setSuccess(false);
                timer = System.currentTimeMillis() - timer;
                currentSpan.setDuration(timer);
                currentSpan.setEndDate(LocalDateTime.now());

                currentSpan.setException(e.getClass().getName());
                currentSpan.setExceptionMsg(e.getMessage());

                TraceManager.exitSpan();
                final IDataEvent event = CacheSpanOutputEvent.of(currentSpan);
                Constant.DATA_BUS.publish(event);
            }

            throw new InterruptedException();
        } catch (Exception e1) {
            if (Constant.SLOT_OUTPUT_SWITCH.get()) {
                currentSpan.setException(e1.getClass().getName());
                currentSpan.setExceptionMsg(((Throwable) e1).getMessage());
            }
        }

        if (Constant.SLOT_OUTPUT_SWITCH.get()) {
            currentSpan.setSuccess(true);
            timer = System.currentTimeMillis() - timer;
            currentSpan.setDuration(timer);
            currentSpan.setEndDate(LocalDateTime.now());

            TraceManager.exitSpan();
            final IDataEvent event = CacheSpanOutputEvent.of(currentSpan);
            Constant.DATA_BUS.publish(event);
        }
    }

    /*public int m4(int a, int b) {
        Span currentSpan = TraceManager.getParentSpan();
        if (null == currentSpan) {
            final String traceId = ASMUtils.genId();
            TraceContext.setTraceId(traceId);
        }
        currentSpan = TraceManager.entrySpan();
        currentSpan.setMethodName("m2");
        currentSpan.setClassMethod("class#method");
        currentSpan.setProduct("aaaa");
        boolean success = true;
        long timer = System.currentTimeMillis();

        final List<Object> methodParamsValue = currentSpan.getMethodParamsValue();
        methodParamsValue.add(a);
        methodParamsValue.add(b);

        int c = a + b;
        String s = " aaa";
        double d = a + b;
        System.out.println("aaaaa");


        timer = System.currentTimeMillis() - timer;
        currentSpan.setDuration(timer);
        currentSpan.setSuccess(success);
        currentSpan.setEndDate(LocalDateTime.now());
        System.out.println(currentSpan);
        return c;
    }*/
}
