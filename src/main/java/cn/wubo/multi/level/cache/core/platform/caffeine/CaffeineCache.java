package cn.wubo.multi.level.cache.core.platform.caffeine;

import cn.wubo.multi.level.cache.config.CacheProperties;
import cn.wubo.multi.level.cache.core.platform.AbstractRedisCache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class CaffeineCache extends AbstractRedisCache {
    private final com.github.benmanes.caffeine.cache.Cache<Object, Object> cache;

    public CaffeineCache(CacheProperties cacheProperties) {
        super(cacheProperties.getAllowNullValues(), cacheProperties);
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder();
        if (cacheProperties.getCaffeine().getMaximumSize() > 0)
            caffeine.maximumSize(cacheProperties.getCaffeine().getMaximumSize());
        if (cacheProperties.getCaffeine().getMaximumWeight() > 0)
            caffeine.maximumWeight(cacheProperties.getCaffeine().getMaximumWeight());
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
        getLog(key);
        Object value = cache.getIfPresent(key);
        getLog(key, value);
        return value;
    }

    @Override
    public String getName() {
        return cacheProperties.getCacheName();
    }

    @Override
    public Object getNativeCache() {
        return cache;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return (T) this.fromStoreValue(cache.get(key, new LoadFunction(valueLoader)));
    }

    @Override
    public void put(Object key, Object value) {
        putLog(key, value);
        cache.put(key, toStoreValue(value));
    }

    @Override
    public void evict(Object key) {
        cache.invalidate(key);
    }

    @Override
    public void clear() {
        cache.invalidateAll();
    }

    private class LoadFunction implements Function<Object, Object> {
        private final Callable<?> valueLoader;

        public LoadFunction(Callable<?> valueLoader) {
            this.valueLoader = valueLoader;
        }

        public Object apply(Object o) {
            try {
                return toStoreValue(this.valueLoader.call());
            } catch (Exception var3) {
                throw new org.springframework.cache.Cache.ValueRetrievalException(o, this.valueLoader, var3);
            }
        }
    }
}
