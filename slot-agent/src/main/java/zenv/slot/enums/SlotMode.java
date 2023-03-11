package zenv.slot.enums;

/**
 * 埋点模式
 *
 * @author zhengwei AKA zenv
 * @since 2022/7/26 11:32
 */
public enum SlotMode {
    /**
     * 全埋点，无差别埋点，所有类和方法都会进行监听
     */
    ALL,

    /**
     * 指定特定的类或方法进行监听
     */
    SPECIAL
}
