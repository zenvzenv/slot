package zenv.slot.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/1 10:01
 */
@Setter
@Getter
public class SlotMethod {
    private String methodName;
    private String methodDesc;
    private String className;
    private int methodAccess;
}
