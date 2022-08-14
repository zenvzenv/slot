package zhengwei.slot.pipeline;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zhengwei AKA zenv
 * @since 2022/8/14 12:30
 */
@Getter
@Setter
public class StringPipeline implements IPipeline<String> {
    private String name;
    private IValve<String, ?> first;
    private IValve<?, ?> last;

    @Override
    public void bootstrap(String input) {
        first.bootstrap(input);
    }

    @Override
    public void linkLast(IValve<?, ?> valve) {

    }

    @Override
    public void linkAfter(IValve<?, ?> current, IValve<?, ?> after) {

    }

    @Override
    public void linkBefore(IValve<?, ?> current, IValve<?, ?> before) {

    }
}
