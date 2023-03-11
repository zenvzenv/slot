package zenv.slot.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LRU 缓存
 *
 * @author zhengwei AKA zenv
 * @since 2022/9/30 8:59
 */
public class LRUCache<K, V> {
    private final int MAX_CACHE_SIZE;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private final LinkedHashMap<K, V> map;

    public LRUCache(int cacheSize) {
        this.MAX_CACHE_SIZE = cacheSize;
        final int cap = (int) (Math.ceil(MAX_CACHE_SIZE / DEFAULT_LOAD_FACTOR) + 1);
        map = new LinkedHashMap<K, V>(cap, DEFAULT_LOAD_FACTOR, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        };
    }

    public synchronized void put(K key, V value) {
        map.put(key, value);
    }

    public synchronized V get(K key) {
        return map.get(key);
    }

    public synchronized void remove(K key) {
        map.remove(key);
    }
}
