package zenv.slot.strategy.output;

import lombok.Getter;
import lombok.Setter;
import zenv.slot.entity.Span;
import zenv.slot.enums.SlotDataType;
import zenv.slot.internal.org.slf4j.Logger;
import zenv.slot.log.SlotLogUtils;
import zenv.slot.utils.FileUtils;
import zenv.slot.utils.SpanLinkedList;
import zenv.slot.utils.TraceLinkedList;

import java.util.List;

/**
 * 启动消费者线程进行埋点数据消费
 *
 * @author zhengwei AKA zenv
 * @since 2022/7/28 19:20
 */
@Setter
@Getter
public class CsvFileSlotOutputStrategy implements SlotOutputStrategy<List<Span>> {
    private static final Logger log = SlotLogUtils.getLogger(CsvFileSlotOutputStrategy.class);

    @Override
    public void output(List<Span> data) {
        log.debug("csv 文件输出");
        if (data instanceof SpanLinkedList) {
            FileUtils.writeCsvOut(data, SlotDataType.SPAN);
        } else if (data instanceof TraceLinkedList) {
            FileUtils.writeCsvOut(data, SlotDataType.TRACE);
        } else {
            FileUtils.writeCsvOut(data, SlotDataType.OTHER);
        }
    }
}
