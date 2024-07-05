package cn.wubo.multi.level.cache.core.platform;

import cn.wubo.multi.level.cache.config.CacheProperties;
import cn.wubo.multi.level.cache.core.platform.caffeine.CaffeineCache;
import cn.wubo.multi.level.cache.core.platform.redis.JedisClusterCache;
import cn.wubo.multi.level.cache.core.platform.redis.RedisPoolCache;
import cn.wubo.multi.level.cache.exception.CacheRutimeException;
import org.springframework.cache.Cache;

public class CacheFactory {

    private CacheFactory() {
    }

    /**
     * 根据给定的缓存属性构建缓存对象
     *
     * @param cacheProperties 缓存属性对象
     * @return 缓存对象
     */
    public static Cache build(CacheProperties cacheProperties) {
        if ("caffeine".equals(cacheProperties.getCachetype())) {
            return new CaffeineCache(cacheProperties);
        } else if ("redis-cluster".equals(cacheProperties.getCachetype())) {
            return new JedisClusterCache(cacheProperties);
        } else if ("redis".equals(cacheProperties.getCachetype()) || "redis-sentinel".equals(cacheProperties.getCachetype())) {
            return new RedisPoolCache(cacheProperties);
        } else {
            throw new CacheRutimeException("不能识别的缓存类型~");
        }
    }

}
