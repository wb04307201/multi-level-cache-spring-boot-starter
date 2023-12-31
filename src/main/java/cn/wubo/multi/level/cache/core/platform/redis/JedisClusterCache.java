package cn.wubo.multi.level.cache.core.platform.redis;

import cn.wubo.multi.level.cache.config.CacheProperties;
import cn.wubo.multi.level.cache.core.platform.AbstractCache;
import org.springframework.cache.Cache;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class JedisClusterCache extends AbstractCache {

    private final JedisCluster cluster;

    public JedisClusterCache(CacheProperties cacheProperties) {
        super(cacheProperties.getAllowNullValues(), cacheProperties);
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(cacheProperties.getRedis().getMaxTotal());
        poolConfig.setMaxIdle(cacheProperties.getRedis().getMaxIdle());
        poolConfig.setMinIdle(cacheProperties.getRedis().getMinIdle());
        poolConfig.setMaxWait(Duration.ofMillis(cacheProperties.getRedis().getMaxWait()));
        Set<HostAndPort> jedisClusterNodes = cacheProperties.getRedis().getCluster().getNodes().stream().map(str -> {
            String[] temp = str.split(":");
            return new HostAndPort(temp[0], Integer.parseInt(temp[1]));
        }).collect(Collectors.toSet());
        this.cluster = new JedisCluster(jedisClusterNodes, cacheProperties.getRedis().getTimeout(), cacheProperties.getRedis().getTimeout(), cacheProperties.getRedis().getCluster().getMaxAttempts(), cacheProperties.getRedis().getPassword(), poolConfig);
    }

    @Override
    protected Object lookup(Object key) {
        String keyStr = getKey(key);
        getLog(keyStr);
        byte[] keyByte = serialize(keyStr);
        byte[] temp = cluster.get(keyByte);
        if ("tti".equals(cacheProperties.getExpirytype()) && temp != null)
            cluster.expire(keyByte, cacheProperties.getExpirytime().intValue());
        Object value = temp == null ? null : deserializer(temp);
        getLog(keyStr, value);
        return value;
    }

    @Override
    public Object getNativeCache() {
        return cluster;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        Cache.ValueWrapper result = this.get(key);
        if (result != null) {
            return (T) result.get();
        } else {
            T value = valueFromLoader(key, valueLoader);
            buildLog(key, value);
            put(key, value);
            return value;
        }
    }

    @Override
    public void put(Object key, Object value) {
        String keyStr = getKey(key);
        putLog(keyStr, value);
        Object cacheValue = preProcessCacheValue(value);
        if (!isAllowNullValues() && cacheValue == null) {
            throw new IllegalArgumentException(String.format("Cache '%s' does not allow 'null' values. Avoid storing null via '@Cacheable(unless=\"#result == null\")' or configure RedisCache to allow 'null' via RedisCacheConfiguration.", getName()));
        } else {
            byte[] keyByte = serialize(keyStr);
            cluster.set(keyByte, serialize(cacheValue));
            if ("ttl".equals(cacheProperties.getExpirytype()) || "tti".equals(cacheProperties.getExpirytype()))
                cluster.expire(keyByte, cacheProperties.getExpirytime().intValue());
            else cluster.expire(keyByte, MAX_EXPIRY_TIME.intValue());
        }
    }

    @Override
    public void evict(Object key) {
        cluster.del(getKey(key));
    }

    @Override
    public void clear() {
        cluster.del(cluster.keys(cacheProperties.getCacheName().concat(":").concat("*")).toArray(new String[0]));
    }
}
