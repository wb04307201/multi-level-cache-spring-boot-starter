package cn.wubo.multi.level.cache.core.platform;

import cn.wubo.multi.level.cache.config.CacheProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.cache.support.NullValue;
import org.springframework.lang.Nullable;

import java.io.*;
import java.util.concurrent.Callable;

@Slf4j
public abstract class AbstractRedisCache extends AbstractValueAdaptingCache {
    protected final CacheProperties cacheProperties;

    /**
     * Create an {@code AbstractValueAdaptingCache} with the given setting.
     *
     * @param allowNullValues whether to allow for {@code null} values
     * @param cacheProperties
     */
    protected AbstractRedisCache(boolean allowNullValues, CacheProperties cacheProperties) {
        super(allowNullValues);
        this.cacheProperties = cacheProperties;
        log.debug("初始化缓存 缓存名：{} 缓存类型：{} 缓存方式：{} 缓存时间：{}", getName(), cacheProperties.getCachetype(), cacheProperties.getExpirytype(), cacheProperties.getExpirytime());
    }

    @Override
    public String getName() {
        return cacheProperties.getCacheName();
    }

    protected String getKey(Object key) {
        return getName().concat(":").concat(key.toString());
    }

    protected Object preProcessCacheValue(@Nullable Object value) {
        if (value != null) {
            return value;
        } else {
            return isAllowNullValues() ? NullValue.INSTANCE : null;
        }
    }

    protected <T> T valueFromLoader(Object key, Callable<T> valueLoader) {
        try {
            return valueLoader.call();
        } catch (Exception var3) {
            throw new Cache.ValueRetrievalException(key, valueLoader, var3);
        }
    }

    protected byte[] serialize(Object object) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(object);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected Object deserializer(byte[] binaryByte) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(binaryByte); ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    protected void getLog(Object key) {
        log.debug("开始获取缓存值 缓存名：{} 缓存key：{}", getName(), key);
    }

    protected void getLog(Object key, Object value) {
        log.debug("获取到缓存值 缓存名：{} 缓存key：{} 缓存value:{}", getName(), key, value);
    }

    protected void putLog(Object key, Object value) {
        log.debug("写入缓存值 缓存名：{} 缓存key：{} 缓存value:{}", getName(), key, value);
    }

    protected void buildLog(Object key, Object value) {
        log.debug("未找到缓存值 缓存名：{} 缓存key：{} 生成缓存value：{}", getName(), key, value);
    }
}
