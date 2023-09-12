package cn.wubo.multi.level.cache.core.platform.redis;

import cn.wubo.multi.level.cache.config.CacheProperties;
import cn.wubo.multi.level.cache.core.platform.AbstractRedisCache;
import org.springframework.cache.Cache;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.util.Pool;

import java.time.Duration;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.Callable;

public class RedisPoolCache extends AbstractRedisCache {
    private final Pool<Jedis> pool;

    public RedisPoolCache(CacheProperties cacheProperties) {
        super(cacheProperties.getAllowNullValues(), cacheProperties);
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(cacheProperties.getRedis().getMaxTotal());
        poolConfig.setMaxIdle(cacheProperties.getRedis().getMaxIdle());
        poolConfig.setMinIdle(cacheProperties.getRedis().getMinIdle());
        poolConfig.setMaxWait(Duration.ofMillis(cacheProperties.getRedis().getMaxWait()));
        if ("redis-sentinel".equals(cacheProperties.getExpirytype()))
            this.pool = new JedisSentinelPool(Objects.requireNonNull(cacheProperties.getRedis().getSentinel().getMasterName()), new HashSet<>(cacheProperties.getRedis().getSentinel().getNodes()), poolConfig, cacheProperties.getRedis().getTimeout(), cacheProperties.getRedis().getSentinel().getUser(), cacheProperties.getRedis().getPassword(), cacheProperties.getRedis().getDatabase());
        else
            this.pool = new JedisPool(poolConfig, cacheProperties.getRedis().getHost(), cacheProperties.getRedis().getPort(), cacheProperties.getRedis().getTimeout(), cacheProperties.getRedis().getPassword(), cacheProperties.getRedis().getDatabase());
    }

    @Override
    protected Object lookup(Object key) {
        String keyStr = getKey(key);
        getLog(keyStr);
        try (Jedis jedis = pool.getResource()) {
            byte[] keyByte = serialize(keyStr);
            byte[] temp = jedis.get(keyByte);
            if ("tti".equals(cacheProperties.getExpirytype()) && temp != null)
                jedis.expire(keyByte, cacheProperties.getExpirytime());
            Object value = temp == null ? null : deserializer(temp);
            getLog(keyStr, value);
            return value;
        }
    }

    @Override
    public Object getNativeCache() {
        return this.pool;
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
            try (Jedis jedis = pool.getResource()) {
                byte[] keyByte = serialize(keyStr);
                jedis.set(keyByte, serialize(cacheValue));
                if ("ttl".equals(cacheProperties.getExpirytype()) || "tti".equals(cacheProperties.getExpirytype()))
                    jedis.expire(keyByte, cacheProperties.getExpirytime());
                else jedis.expire(keyByte, MAX_EXPIRY_TIME);
            }
        }
    }

    @Override
    public void evict(Object key) {
        try (Jedis jedis = pool.getResource()) {
            jedis.del(getKey(key));
        }
    }

    @Override
    public void clear() {
        try (Jedis jedis = pool.getResource()) {
            jedis.del(jedis.keys(cacheProperties.getCacheName().concat(":").concat("*")).toArray(new String[0]));
        }
    }
}
