package cn.wubo.multi.level.cache.core.platform.redis;

import cn.wubo.multi.level.cache.config.CacheProperties;
import cn.wubo.multi.level.cache.core.platform.AbstractCache;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.cache.Cache;
import redis.clients.jedis.Connection;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class JedisClusterCache extends AbstractCache {

    private final JedisCluster cluster;

    public JedisClusterCache(CacheProperties cacheProperties) {
        super(cacheProperties.getAllowNullValues(), cacheProperties);
        GenericObjectPoolConfig<Connection> poolConfig = new GenericObjectPoolConfig<>();
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

    /**
     * 重写父类方法，查找并返回指定键的值
     *
     * @param key 指定的键
     * @return 指定键的值
     */
    @Override
    protected Object lookup(Object key) {
        // 获取指定键的字符串形式
        String keyStr = getKey(key);
        // 记录日志
        getLog(keyStr);
        // 序列化指定键的字符串形式
        byte[] keyByte = serialize(keyStr);
        // 从集群中获取指定键的值
        byte[] temp = cluster.get(keyByte);
        // 若缓存过期类型为"tti"且获取到的值不为空，则设置过期时间
        if ("tti".equals(cacheProperties.getExpirytype()) && temp != null)
            cluster.expire(keyByte, cacheProperties.getExpirytime().intValue());
        // 将获取到的值反序列化为对象
        Object value = temp == null ? null : deserializer(temp);
        // 记录日志
        getLog(keyStr, value);
        // 返回指定键的值
        return value;
    }

    @Override
    public Object getNativeCache() {
        return cluster;
    }

    /**
     * 根据指定的键获取值。
     * 如果缓存存在，则直接返回缓存值。
     * 如果缓存不存在，则通过调用valueLoader加载值，并将加载的值放入缓存中。
     *
     * @param key         键
     * @param valueLoader 值的加载器
     * @param <T>         值的类型
     * @return 加载的值
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
        // 获取键的字符串形式
        String keyStr = getKey(key);
        // 记录存储日志
        putLog(keyStr, value);
        // 对缓存值进行预处理
        Object cacheValue = preProcessCacheValue(value);
        // 如果不允许空值，并且缓存值为空，则抛出异常
        if (!isAllowNullValues() && cacheValue == null) {
            throw new IllegalArgumentException(String.format("Cache '%s' does not allow 'null' values. Avoid storing null via '@Cacheable(unless=\"#result == null\")' or configure RedisCache to allow 'null' via RedisCacheConfiguration.", getName()));
        } else {
            // 序列化键的字节数组
            byte[] keyByte = serialize(keyStr);
            // 将键值对存储到集群中
            cluster.set(keyByte, serialize(cacheValue));
            // 根据缓存过期类型设置键的过期时间
            if ("ttl".equals(cacheProperties.getExpirytype()) || "tti".equals(cacheProperties.getExpirytype())) {
                cluster.expire(keyByte, cacheProperties.getExpirytime().intValue());
            } else {
                cluster.expire(keyByte, MAX_EXPIRY_TIME.intValue());
            }
        }
    }

    /**
     * 覆盖父类方法，驱逐指定键的元素。
     *
     * @param key 要驱逐的键元素
     */
    @Override
    public void evict(Object key) {
        cluster.del(getKey(key));
    }

    /**
     * 清空缓存
     */
    @Override
    public void clear() {
        cluster.del(cluster.keys(cacheProperties.getCacheName().concat(":").concat("*")).toArray(new String[0]));
    }
}
