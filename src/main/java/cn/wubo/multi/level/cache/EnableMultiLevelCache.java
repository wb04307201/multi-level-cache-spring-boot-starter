package cn.wubo.multi.level.cache;

import cn.wubo.multi.level.cache.config.MultiLevelCacheConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({MultiLevelCacheConfiguration.class})
@EnableCaching
public @interface EnableMultiLevelCache{
}
