package zenv.slot.databus.listener;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import zenv.slot.databus.IDataEvent;
import zenv.slot.databus.IListener;
import zenv.slot.databus.event.MessageData;
import zenv.slot.databus.event.StartingData;
import zenv.slot.databus.event.StoppedData;

import java.time.LocalDateTime;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/25 14:39
 */
@Setter
@Getter
@RequiredArgsConstructor
public class StatusListener implements IListener {
    private final String id;
    private LocalDateTime started;
    private LocalDateTime stopped;

    @Override
    public void accept(IDataEvent event) {
        if (event instanceof StartingData) {
            handleEvent((StartingData) event);
        } else if (event instanceof StoppedData) {
            handleEvent((StoppedData) event);
        }
    }

    private void handleEvent(StartingData data) {
        started = data.getWhen();
        System.out.printf("receiver %s sees app started at %s", id, started);
    }

    private void handleEvent(StoppedData data) {
        stopped = data.getWhen();
        System.out.printf("receiver %s sees app stopping at %s", id, started);
        System.out.printf("receiver %s sending goodbye message", id);
        data.getDataBus().publish(MessageData.of(String.format("Goodbye cruel world from #%s", id)));
    }
}
