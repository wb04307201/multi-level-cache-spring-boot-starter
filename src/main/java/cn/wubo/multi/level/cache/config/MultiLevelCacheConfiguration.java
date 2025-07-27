package cn.wubo.multi.level.cache.config;

import cn.wubo.multi.level.cache.core.MultiLevelCacheManager;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({MultiLevelCacheProperties.class})
public class MultiLevelCacheConfiguration {

        /**
     * 创建并返回一个 MultiLevelCacheManager 实例
     *
     * @param multiLevelCacheProperties 多级缓存属性
     * @return MultiLevelCacheManager 实例
     */
    @Bean("multiLevelCacheManager")
    public MultiLevelCacheManager multiLevelCacheManager(MultiLevelCacheProperties multiLevelCacheProperties) {
        if (multiLevelCacheProperties == null) {
            throw new IllegalArgumentException("multiLevelCacheProperties cannot be null");
        }

        try {
            return new MultiLevelCacheManager(multiLevelCacheProperties);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create MultiLevelCacheManager", e);
        }
    }


}
