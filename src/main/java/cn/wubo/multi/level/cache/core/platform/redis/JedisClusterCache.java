package cn.wubo.multi.level.cache.core.platform.redis;

import org.springframework.cache.Cache;
import redis.clients.jedis.JedisCluster;

import java.util.concurrent.Callable;

public class JedisClusterCache implements Cache {

    private final String name;
    private final JedisCluster cluster;

    public JedisClusterCache(String name, JedisCluster cluster) {
        this.name = name;
        this.cluster = cluster;
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
