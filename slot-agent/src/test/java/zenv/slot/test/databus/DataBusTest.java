package zenv.slot.test.databus;

import zenv.slot.databus.DefaultDataBus;
import zenv.slot.databus.IDataBus;
import zenv.slot.databus.event.MessageData;
import zenv.slot.databus.event.StartingData;
import zenv.slot.databus.event.StoppedData;
import zenv.slot.databus.listener.MessageListener;
import zenv.slot.databus.listener.StatusListener;
import zenv.slot.utils.ASMUtils;

import java.time.LocalDateTime;

/**
 * @author zhengwei AKA zenv
 * @since 2022/7/25 16:08
 */
public class DataBusTest {
    public static void main(String[] args) {
        final IDataBus bus = DefaultDataBus.getInstance();
        final StatusListener statusListener1 = new StatusListener(ASMUtils.genId());
        final StatusListener statusListener2 = new StatusListener(ASMUtils.genId());
        final MessageListener foo = new MessageListener("foo");
        final MessageListener bar = new MessageListener("bar");

        bus.subscribe(statusListener1);
        bus.subscribe(statusListener2);
        bus.subscribe(foo);

        bus.publish(StartingData.of(LocalDateTime.now()));
        bus.publish(MessageData.of("only foo should see this."));

        bus.subscribe(bar);
        bus.publish(MessageData.of("foo and bar should see this"));
        bus.unsubscribe(foo);
        bus.publish(MessageData.of("only bar should see this"));
        bus.publish(StoppedData.of(LocalDateTime.now()));
    }
}
