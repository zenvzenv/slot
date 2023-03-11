package zenv.slot.strategy.output;

import zenv.slot.entity.Span;
import zenv.slot.internal.org.slf4j.Logger;
import zenv.slot.log.SlotLogUtils;
import zenv.slot.utils.CsvUtils;

import java.util.List;

/**
 * 控制台输出内容
 *
 * @author zhengwei AKA zenv
 * @since 2022/8/22 11:23
 */
public class ConsoleSlotOutputStrategy implements SlotOutputStrategy<List<Span>> {
    private static final Logger log = SlotLogUtils.getLogger(ConsoleSlotOutputStrategy.class);

    @Override
    public void output(List<Span> data) {
        data.forEach(span -> log.info(CsvUtils.getCsvContentStr(span)));
    }
}
