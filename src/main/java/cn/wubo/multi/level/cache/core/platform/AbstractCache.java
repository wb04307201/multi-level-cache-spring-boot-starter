package cn.wubo.multi.level.cache.core.platform;

import cn.wubo.multi.level.cache.config.CacheProperties;
import cn.wubo.multi.level.cache.exception.CacheRutimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.cache.support.NullValue;
import org.springframework.lang.Nullable;

import java.io.*;
import java.util.concurrent.Callable;

@Slf4j
public abstract class AbstractCache extends AbstractValueAdaptingCache {
    protected final CacheProperties cacheProperties;

    protected static final Long MAX_EXPIRY_TIME = 3153600000L;

    /**
     * Create an {@code AbstractValueAdaptingCache} with the given setting.
     *
     * @param allowNullValues whether to allow for {@code null} values
     * @param cacheProperties
     */
    protected AbstractCache(boolean allowNullValues, CacheProperties cacheProperties) {
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

    /**
     * 在缓存值之前进行预处理。
     *
     * @param value 要预处理的值
     * @return 预处理后的值
     */
    protected Object preProcessCacheValue(@Nullable Object value) {
        if (value != null) {
            return value;
        } else {
            return isAllowNullValues() ? NullValue.INSTANCE : null;
        }
    }

    /**
     * 从加载器中获取值
     *
     * @param key   键
     * @param valueLoader   加载器，用于获取值
     * @return      返回加载器获取到的值
     * @throws Cache.ValueRetrievalException   如果获取值时发生异常
     */
    protected <T> T valueFromLoader(Object key, Callable<T> valueLoader) {
        try {
            return valueLoader.call();
        } catch (Exception var3) {
            throw new Cache.ValueRetrievalException(key, valueLoader, var3);
        }
    }

    /**
     * 序列化对象为字节数组
     *
     * @param object 要序列化的对象
     * @return 序列化后的字节数组
     * @throws CacheRutimeException 序列化过程中出现异常时抛出
     */
    protected byte[] serialize(Object object) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(object);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new CacheRutimeException(e.getMessage(), e);
        }
    }

    /**
     * 反序列化二进制字节数组为对象
     *
     * @param binaryByte 二进制字节数组
     * @return 反序列化后的对象
     * @throws CacheRutimeException 反序列化过程中出现异常时抛出
     */
    protected Object deserializer(byte[] binaryByte) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(binaryByte);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new CacheRutimeException(e.getMessage(), e);
        }
    }

    protected void getLog(Object key) {
        log.debug("开始获取缓存值 缓存名：{} 缓存类型：{} 缓存key：{}", getName(), cacheProperties.getCachetype(), key);
    }

    protected void getLog(Object key, Object value) {
        log.debug("获取到缓存值 缓存名：{} 缓存类型：{} 缓存key：{} 缓存value:{}", getName(), cacheProperties.getCachetype(), key, value);
    }

    protected void putLog(Object key, Object value) {
        log.debug("写入缓存值 缓存名：{} 缓存类型：{} 缓存key：{} 缓存value:{}", getName(), cacheProperties.getCachetype(), key, value);
    }

    protected void buildLog(Object key, Object value) {
        log.debug("未找到缓存值 缓存名：{} 缓存类型：{} 缓存key：{} 生成缓存value：{}", getName(), cacheProperties.getCachetype(), key, value);
    }
}
