package zenv.slot.databus.listener;

import lombok.RequiredArgsConstructor;
import zenv.slot.databus.IDataEvent;
import zenv.slot.databus.IListener;
import zenv.slot.databus.event.MessageData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/25 15:20
 */
@RequiredArgsConstructor
public class MessageListener implements IListener {
    private final String name;
    private final List<String> messages = new ArrayList<>();

    @Override
    public void accept(IDataEvent event) {
        if (event instanceof MessageData) {
            handleEvent((MessageData) event);
        }
    }

    private void handleEvent(MessageData data) {
        System.out.printf("%s sees message %s", name, data.getMessage());
        messages.add(data.getMessage());
    }

    public List<String> getMessages() {
        return messages;
    }
}
