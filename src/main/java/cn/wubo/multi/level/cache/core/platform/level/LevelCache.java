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

    /**
     * 重写父类方法，查找指定键的值
     *
     * @param key 要查找的键
     * @return 查找到的值，若未找到则返回null
     */
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
        // 获取缓存值
        Cache.ValueWrapper result = this.get(key);
        if (result != null) {
            // 如果缓存存在，则直接返回缓存值
            return (T) result.get();
        } else {
            // 如果缓存不存在，则通过调用valueLoader加载值
            T value = valueFromLoader(key, valueLoader);
            // 将加载的值放入缓存中
            put(key, value);
            // 返回加载的值
            return value;
        }
    }


    /**
     * 覆写父类的put方法，用于向缓存中添加键值对
     */
    @Override
    public void put(Object key, Object value) {
        // 调用父类的putLog方法，将键值对记录到日志中
        putLog(key, value);

        // 获取ListIterator迭代器，从缓存列表的末尾开始遍历
        ListIterator<Cache> listIterator = caches.listIterator(caches.size());

        // 当前迭代器指向缓存列表的最后一个元素之后的位置
        while (listIterator.hasPrevious()) {
            // 获取当前迭代器指向的缓存对象
            Cache currentCache = listIterator.previous();

            // 向当前缓存对象中添加键值对
            currentCache.put(key, value);
        }
    }


    /**
     * 重写方法，用于将指定键从所有缓存中删除。
     *
     * @param key 要删除的键对象
     */
    @Override
    public void evict(Object key) {
        caches.forEach(cache -> cache.evict(key));
    }

    /**
     * 清除所有缓存
     *
     * @Override 重写父类方法
     */
    public void clear() {
        caches.forEach(Cache::clear);
    }

}
