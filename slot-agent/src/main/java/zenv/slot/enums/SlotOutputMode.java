package zenv.slot.enums;

import lombok.Getter;
import zenv.slot.entity.Span;
import zenv.slot.strategy.output.ConsoleSlotOutputStrategy;
import zenv.slot.strategy.output.CsvFileSlotOutputStrategy;
import zenv.slot.strategy.output.SlotOutputStrategy;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/29 8:42
 */
@Getter
public enum SlotOutputMode {
    CONSOLE("console", new ConsoleSlotOutputStrategy()),
    CSV("csv", new CsvFileSlotOutputStrategy()),
    ;

    private final String mode;
    private final SlotOutputStrategy<List<Span>> strategy;

    SlotOutputMode(String mode, SlotOutputStrategy<List<Span>> strategy) {
        this.mode = mode;
        this.strategy = strategy;
    }

    private static final Map<String, SlotOutputMode> lookup = new HashMap<>();

    static {
        EnumSet.allOf(SlotOutputMode.class).forEach(o -> lookup.put(o.mode, o));
    }

    public static SlotOutputMode parse(String mode) {
        return lookup.getOrDefault(mode, CONSOLE);
    }
}
