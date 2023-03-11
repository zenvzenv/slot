package zenv.slot.pipeline;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 管道全局变量
 *
 * @author zhengwei AKA zenv
 * @since 2022/7/27 8:41
 */
public class PipelineContext {
    // LRU
    private static final Map<String, Object> CACHE = new LinkedHashMap<>(16, 0.75F, true);

    public synchronized void cache(String key, Object value) {
        CACHE.put(key, value);
    }

    public synchronized Object getByKey(String key) {
        return CACHE.get(key);
    }
}
