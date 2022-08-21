package zhengwei.slot.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/20 11:36
 */
@Getter
@Setter
public class SlotOutput {
    private String parentId;
    private String spanId;
    private LocalDateTime when = LocalDateTime.now();
    private long duration;
    private String where;
    private String params;
    private boolean success;
}
