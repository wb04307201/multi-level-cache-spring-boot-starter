package cn.wubo.multi.level.cache.config;

import cn.wubo.multi.level.cache.core.MultiLevelCacheManager;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({MultiLevelCacheProperties.class})
public class MultiLevelCacheConfiguration {

    @Bean
    public MultiLevelCacheManager multiLevelCacheManager() {
        return new MultiLevelCacheManager();
    }
}
