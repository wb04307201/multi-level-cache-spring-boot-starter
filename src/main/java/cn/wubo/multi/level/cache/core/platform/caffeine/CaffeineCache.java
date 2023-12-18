package cn.wubo.multi.level.cache.core.platform.caffeine;

import cn.wubo.multi.level.cache.config.CacheProperties;
import cn.wubo.multi.level.cache.core.platform.AbstractCache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class CaffeineCache extends AbstractCache {
    private final com.github.benmanes.caffeine.cache.Cache<Object, Object> cache;

    public CaffeineCache(CacheProperties cacheProperties) {
        super(cacheProperties.getAllowNullValues(), cacheProperties);
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder();
        if (cacheProperties.getCaffeine().getMaximumSize() > 0)
            caffeine.maximumSize(cacheProperties.getCaffeine().getMaximumSize());
        if (Boolean.TRUE.equals(cacheProperties.getCaffeine().getRecordStats())) caffeine.recordStats();
        switch (cacheProperties.getExpirytype()) {
            case "ttl":
                caffeine.expireAfterWrite(Duration.ofSeconds(cacheProperties.getExpirytime()));
                break;
            case "tti":
                caffeine.expireAfterAccess(Duration.ofSeconds(cacheProperties.getExpirytime()));
                break;
            case "none":
                break;
            default:
                throw new IllegalArgumentException("expirytype is Illegal!");
        }
        this.cache = caffeine.build();
    }

    @Override
    protected Object lookup(Object key) {
        // 获取日志
        getLog(key);
        // 从缓存中获取指定key对应的数据
        Object value = cache.getIfPresent(key);
        // 获取日志
        getLog(key, value);
        // 返回缓存中的数据
        return value;
    }


    @Override
    public Object getNativeCache() {
        return cache;
    }

    /**
     * 重写父类方法，获取指定键值的对应值。
     * 如果缓存中不存在指定键值对应的值，则通过调用给定的可调用对象加载值，并将加载的值存入缓存。
     *
     * @param key         指定键值
     * @param valueLoader 可调用对象，用于加载指定键值对应的值
     * @param <T>         指定键值对应的值的类型
     * @return 指定键值对应的值
     */
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        // 将给定的可调用对象加载的值存入缓存，并将缓存中的值作为对象的值返回
        return (T) this.fromStoreValue(cache.get(key, new LoadFunction(valueLoader)));
    }


    /**
     * 覆写父类的put方法，
     * 将key和value存入缓存中，
     * 并调用putLog方法记录此次put操作，
     * 使用toStoreValue方法将value转换为适合存储的值。
     *
     * @param key   要存入缓存的键
     * @param value 要存入缓存的值
     */
    @Override
    public void put(Object key, Object value) {
        putLog(key, value);
        cache.put(key, toStoreValue(value));
    }


    /**
     * 覆盖方法，从缓存中删除指定键的值
     */
    @Override
    public void evict(Object key) {
        cache.invalidate(key);
    }


    /**
     * 清空缓存，使所有缓存项失效
     */
    @Override
    public void clear() {
        cache.invalidateAll();
    }


    private class LoadFunction implements Function<Object, Object> {
        private final Callable<?> valueLoader;

        public LoadFunction(Callable<?> valueLoader) {
            this.valueLoader = valueLoader;
        }

        /**
         * 将传入的对象应用于当前实例，并返回结果。
         *
         * @param o 传入的对象
         * @return 应用结果
         * @throws org.springframework.cache.Cache.ValueRetrievalException 当valueLoader调用发生异常时
         */
        public Object apply(Object o) {
            try {
                return toStoreValue(this.valueLoader.call());
            } catch (Exception var3) {
                throw new org.springframework.cache.Cache.ValueRetrievalException(o, this.valueLoader, var3);
            }
        }

    }
}
