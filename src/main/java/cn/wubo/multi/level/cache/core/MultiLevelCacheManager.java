package cn.wubo.multi.level.cache.core;

import cn.wubo.multi.level.cache.config.CacheProperties;
import cn.wubo.multi.level.cache.config.MultiLevelCacheProperties;
import cn.wubo.multi.level.cache.core.platform.CacheFactory;
import cn.wubo.multi.level.cache.core.platform.level.LevelCache;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class MultiLevelCacheManager implements CacheManager {

    private ConcurrentHashMap<String, Cache> caches = new ConcurrentHashMap<>();

    public MultiLevelCacheManager(MultiLevelCacheProperties multiLevelCacheProperties) {
        List<CacheProperties> list = new ArrayList<>();
        multiLevelCacheProperties.getCaches().forEach(cache -> {
            if ("multilevel".equals(cache.getCachetype())) list.add(cache);
            else caches.put(cache.getCacheName(), Objects.requireNonNull(CacheFactory.build(cache)));
        });
        list.stream().forEach(cache -> caches.put(cache.getCacheName(), new LevelCache(cache, cache.getLevel().stream().map(name -> caches.get(name)).collect(Collectors.toList()))));
    }

    @Override
    public Cache getCache(String name) {
        return caches.get(name);
    }

    @Override
    public Collection<String> getCacheNames() {
        return caches.keySet();
    }
}
