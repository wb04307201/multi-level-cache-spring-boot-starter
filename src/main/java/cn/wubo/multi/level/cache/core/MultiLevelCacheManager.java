package cn.wubo.multi.level.cache.core;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class MultiLevelCacheManager implements CacheManager {

    private static ConcurrentHashMap<String, CopyOnWriteArrayList<Cache>> caches = new ConcurrentHashMap<>();

    @Override
    public Cache getCache(String name) {
        return null;
    }

    @Override
    public Collection<String> getCacheNames() {
        return null;
    }
}
