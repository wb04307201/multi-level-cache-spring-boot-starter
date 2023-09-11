package cn.wubo.multi.level.cache.config;

import cn.wubo.multi.level.cache.core.MultiLevelCacheManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableConfigurationProperties({MultiLevelCacheProperties.class})
public class MultiLevelCacheConfiguration {

    @Bean("multiLevelCacheManager")
    @Primary
    public MultiLevelCacheManager multiLevelCacheManager(MultiLevelCacheProperties multiLevelCacheProperties) {
        return new MultiLevelCacheManager(multiLevelCacheProperties);
    }
}
