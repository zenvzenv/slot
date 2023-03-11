package zenv.slot.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/29 20:03
 */
@Getter
@Setter
public class SlotInfo {
    public static final String CLASS_TYPE = "class";
    public static final String PACKAGE_TYPE = "package";
    // 有可能是包名和类名
    private String prefix;
    // 有可能是 *，* 代表所有方法所有类
    private String method;
    // package,class
    private String type;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlotInfo slotInfo = (SlotInfo) o;
        return prefix.equals(slotInfo.prefix) && method.equals(slotInfo.method) && type.equals(slotInfo.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix, method, type);
    }
}
