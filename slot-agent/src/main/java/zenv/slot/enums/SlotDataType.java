package zenv.slot.enums;

import lombok.Getter;

/**
 * @author zhengwei AKA zenv
 * @since 2022/12/9 14:11
 */
@Getter
public enum SlotDataType {
    TRACE("trace"),
    SPAN("span"),
    OTHER("other")
    ;

    private final String type;

    SlotDataType(String type) {
        this.type = type;
    }
}
