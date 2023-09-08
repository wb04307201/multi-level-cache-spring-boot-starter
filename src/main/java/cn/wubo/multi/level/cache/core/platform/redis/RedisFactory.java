package cn.wubo.multi.level.cache.core.platform.redis;

import redis.clients.jedis.*;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public class RedisFactory {

    private JedisPool singleton() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(8);
        poolConfig.setMaxIdle(8);
        poolConfig.setMinIdle(0);
        poolConfig.setMaxWait(Duration.ofMillis(-1L));
        return new JedisPool(poolConfig, "", 6379, 2000, null, 0);
    }

    private JedisCluster cluster() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(8);
        poolConfig.setMaxIdle(8);
        poolConfig.setMinIdle(0);
        poolConfig.setMaxWait(Duration.ofMillis(-1L));
        Set<HostAndPort> jedisClusterNodes = new HashSet<>();
        jedisClusterNodes.add(new HostAndPort("127.0.0.1", 7379));
        jedisClusterNodes.add(new HostAndPort("127.0.0.1", 7380));
        return new JedisCluster(jedisClusterNodes, 2000, 2000, 5, null, poolConfig);
    }

    private JedisSentinelPool sentinel() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(8);
        poolConfig.setMaxIdle(8);
        poolConfig.setMinIdle(0);
        poolConfig.setMaxWait(Duration.ofMillis(-1L));
        Set<String> sentinels = new HashSet<>();
        return new JedisSentinelPool("", sentinels, poolConfig, 2000, null, null, 0);
    }
}
