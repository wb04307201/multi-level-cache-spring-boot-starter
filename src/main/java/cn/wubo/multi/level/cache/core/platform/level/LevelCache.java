package cn.wubo.multi.level.cache.core.platform.level;

import cn.wubo.multi.level.cache.config.CacheProperties;
import cn.wubo.multi.level.cache.core.platform.AbstractCache;
import org.springframework.cache.Cache;

import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;

public class LevelCache extends AbstractCache {

    private final CopyOnWriteArrayList<Cache> caches;

    public LevelCache(CacheProperties cacheProperties, List<Cache> caches) {
        super(cacheProperties.getAllowNullValues(), cacheProperties);
        this.caches = new CopyOnWriteArrayList<>(caches);
    }

    @Override
    protected Object lookup(Object key) {
        getLog(key);
        Object value = null;
        int num = -1;
        for (Cache cache : caches) {
            num++;
            Cache.ValueWrapper result = cache.get(key);
            if (result != null) value = result.get();
            if (value != null) break;
        }
        for (int i = num < this.caches.size() ? num - 1 : -1; i >= 0; i--)
            caches.get(i).put(key, value);
        getLog(key, value);
        return value;
    }

    @Override
    public Object getNativeCache() {
        return caches;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        Cache.ValueWrapper result = this.get(key);
        if (result != null) {
            return (T) result.get();
        } else {
            T value = valueFromLoader(key, valueLoader);
            put(key, value);
            return value;
        }
    }

    @Override
    public void put(Object key, Object value) {
        putLog(key, value);
        ListIterator<Cache> listIterator = caches.listIterator(caches.size());
        while (listIterator.hasPrevious()) {
            listIterator.previous().put(key, value);
        }
    }

    @Override
    public void evict(Object key) {
        caches.forEach(cache -> cache.evict(key));
    }

    @Override
    public void clear() {
        caches.forEach(Cache::clear);
    }
}
