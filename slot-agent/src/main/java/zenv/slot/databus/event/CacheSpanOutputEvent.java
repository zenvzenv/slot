package zenv.slot.databus.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import zenv.slot.databus.AbstractDataEvent;
import zenv.slot.databus.IDataEvent;
import zenv.slot.entity.Span;

/**
 * 生成埋点输出事件
 *
 * @author zhengwei AKA zenv
 * @since 2022/8/26 14:43
 */
@RequiredArgsConstructor
@Getter
@Setter
public class CacheSpanOutputEvent extends AbstractDataEvent {
    private final Span span;
    private String name = "缓存埋点信息事件";

    public static IDataEvent of(Span span) {
        return new CacheSpanOutputEvent(span);
    }
}
