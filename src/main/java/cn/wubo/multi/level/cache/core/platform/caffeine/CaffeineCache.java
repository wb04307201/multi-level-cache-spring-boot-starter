package cn.wubo.multi.level.cache.core.platform.caffeine;

import org.springframework.cache.Cache;

import java.util.concurrent.Callable;

public class CaffeineCache implements Cache {

    private final String name;
    private final com.github.benmanes.caffeine.cache.Cache<String, String> cache;

    public CaffeineCache(String name, com.github.benmanes.caffeine.cache.Cache<String, String> cache) {
        this.name = name;
        this.cache = cache;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Object getNativeCache() {
        return null;
    }

    @Override
    public ValueWrapper get(Object key) {
        return null;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        return null;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return null;
    }

    @Override
    public void put(Object key, Object value) {

    }

    @Override
    public void evict(Object key) {

    }

    @Override
    public void clear() {

    }
}
