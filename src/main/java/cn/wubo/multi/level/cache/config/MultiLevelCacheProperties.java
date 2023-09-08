package cn.wubo.multi.level.cache.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "index.cache.redis")
public class MultiLevelCacheProperties {
    private List<CacheProperties> caches = new ArrayList<>();
}
