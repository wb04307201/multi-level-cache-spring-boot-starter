package cn.wubo.multi.level.cache.core.platform.redis;

import cn.wubo.multi.level.cache.config.CacheProperties;
import cn.wubo.multi.level.cache.core.platform.AbstractCache;
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

public class RedisPoolCache extends AbstractCache {
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

    /**
     * 重写父类方法，查询指定key的缓存值
     *
     * @param key 缓存的key
     * @return 查询到的缓存值
     */
    @Override
    protected Object lookup(Object key) {
        String keyStr = getKey(key); // 获取key的字符串形式
        getLog(keyStr); // 记录日志
        try (Jedis jedis = pool.getResource()) { // 获取Redis连接
            byte[] keyByte = serialize(keyStr); // 序列化key
            byte[] temp = jedis.get(keyByte); // 获取缓存值
            if ("tti".equals(cacheProperties.getExpirytype()) && temp != null)
                jedis.expire(keyByte, cacheProperties.getExpirytime()); // 设置缓存过期时间
            Object value = temp == null ? null : deserializer(temp); // 反序列化缓存值
            getLog(keyStr, value); // 记录日志
            return value; // 返回查询到的缓存值
        }
    }

    @Override
    public Object getNativeCache() {
        return this.pool;
    }


    /**
     * 重写父类方法，获取指定key对应value的方法。
     * 如果缓存中存在该key对应的value，则直接返回。
     * 如果缓存中不存在该key对应的value，则通过调用valueLoader函数获取value，并将其存入缓存中。
     *
     * @param key         键
     * @param valueLoader 通过键获取值的加载器
     * @param <T>         值的类型
     * @return 返回指定key对应的value
     */
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
        // 获取key的字符串形式
        String keyStr = getKey(key);
        // 调用日志方法记录put操作
        putLog(keyStr, value);
        // 对缓存值进行预处理
        Object cacheValue = preProcessCacheValue(value);
        // 如果缓存值为null且不允许使用null值，则抛出异常
        if (!isAllowNullValues() && cacheValue == null) {
            throw new IllegalArgumentException(String.format("Cache '%s' does not allow 'null' values. Avoid storing null via '@Cacheable(unless=\"#result == null\")' or configure RedisCache to allow 'null' via RedisCacheConfiguration.", getName()));
        } else {
            try (Jedis jedis = pool.getResource()) {
                // 序列化key为字节数组
                byte[] keyByte = serialize(keyStr);
                // 将缓存值设置为key的值
                jedis.set(keyByte, serialize(cacheValue));
                // 如果配置的过期类型是'ttl'或者'tti'，设置key的过期时间
                if ("ttl".equals(cacheProperties.getExpirytype()) || "tti".equals(cacheProperties.getExpirytype())) {
                    jedis.expire(keyByte, cacheProperties.getExpirytime());
                } else {
                    jedis.expire(keyByte, MAX_EXPIRY_TIME);
                }
            }
        }
    }

    @Override
    public void evict(Object key) {
        // 使用Jedis对象获取Redis资源
        try (Jedis jedis = pool.getResource()) {
            // 根据给定的key获取Redis中的键值对，并将其删除
            jedis.del(getKey(key));
        }
    }

    /**
     * 清空缓存数据
     */
    @Override
    public void clear() {
        try (Jedis jedis = pool.getResource()) {
            // 使用Redis的keys命令查找匹配缓存名称的键，并删除所有匹配的键
            jedis.del(jedis.keys(cacheProperties.getCacheName().concat(":").concat("*")).toArray(new String[0]));
        }
    }
}
