package zenv.slot.databus.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import zenv.slot.databus.AbstractDataEvent;

/**
 * 将 .csv.tmp 文件重命名为 .csv 文件的事件
 *
 * @author zhengwei AKA zenv
 * @since 2022/8/18 14:16
 */
@RequiredArgsConstructor
@Getter
@Setter
public class RenameTempEvent extends AbstractDataEvent {
    /**
     * 需要重命名的文件时间
     */
    private final String date;
    private String name = "重命名 csv 临时文件事件";

    public static RenameTempEvent of(String date) {
        return new RenameTempEvent(date);
    }
}
