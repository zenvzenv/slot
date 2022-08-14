package zhengwei.slot.pipeline;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 流水线的上下文
 * <p>
 * 用于存放流水线在运行过程中的运行时参数和缓存，使用 LRU 来存放参数和缓存
 *
 * @author zhengwei AKA zenv
 * @since 2022/8/14 12:19
 */
public final class PipelineContext {
    private static final Map<String, Object> CACHE = new LinkedHashMap<>(32, 0.75f, true);

    /**
     * 往上下文中添加缓存
     *
     * @param key   key
     * @param value 缓存值
     */
    public synchronized static void put(String key, Object value) {
        CACHE.put(key, value);
    }

    /**
     * 获取缓存值
     *
     * @param key key
     * @return 缓存值
     */
    public synchronized static Object get(String key) {
        return CACHE.get(key);
    }
}
