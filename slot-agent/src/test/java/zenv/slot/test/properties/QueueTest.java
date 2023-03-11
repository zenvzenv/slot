package zenv.slot.test.properties;

import zenv.slot.conf.Constant;
import zenv.slot.entity.Span;
import zenv.slot.utils.ASMUtils;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @author zhengwei AKA zenv
 * @since 2022/9/2 17:17
 */
public class QueueTest {
    public static void main(String[] args) throws InterruptedException {
        TimeUnit.SECONDS.sleep(10);
        long timer = System.currentTimeMillis();

        for (int i = 0; i < 20000; i++) {
            final Span span = new Span();
            span.setSpanId(ASMUtils.genId());
            span.setTraceId(ASMUtils.genId());
            span.setParentId(ASMUtils.genId());
            span.setClassName(ASMUtils.genId());
            span.setMethodName(ASMUtils.genId());
            span.setMethodDesc(ASMUtils.genId());
            span.setEndDate(LocalDateTime.now());
            span.setClassMethod(ASMUtils.genId());
            Constant.SERVICE.set("test");

            if (i % 500 == 0) {
                TimeUnit.SECONDS.sleep(1);
            }
        }

        System.out.println(System.currentTimeMillis() - timer);
    }
}
