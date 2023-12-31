package cn.wubo.multi.level.cache.config;

import lombok.Data;

import java.util.List;

@Data
public class CacheProperties {
    /**
     * 缓存名
     */
    private String cacheName;
    /**
     * multilevel caffeine redis redis-cluster redis-sentinel
     */
    private String cachetype = "caffeine";
    /**
     * 过期类型（none,ttl,tti）
     */
    private String expirytype = "none";

    /**
     * 过期时间（秒）
     */
    private Long expirytime;

    private CaffeineProperties caffeine = new CaffeineProperties();

    private RedisProperties redis = new RedisProperties();

    // 多级缓存串联顺序，取缓存时先读取靠前的缓存
    private List<String> level;

    Boolean allowNullValues = Boolean.TRUE;

    @Data
    public class CaffeineProperties {
        // 是否开启异步 private Boolean async = Boolean.FALSE;
        // 基于缓存内的元素个数进行驱逐，设置为0L则不启用
        private Long maximumSize = 10_000L;
        // 基于缓存内元素权重进行驱逐，设置为0L则不启用 private Long maximumWeight = 0L;
        // 权重计算类，使用权重驱逐必输 private String weighter;
        // 是否开统计
        private Boolean recordStats = Boolean.FALSE;
    }

    @Data
    public class RedisProperties {
        // 单例地址
        private String host = "localhost";
        // 单例端口路
        private Integer port = 6379;
        // 密码
        private String password;
        // 数据库
        private Integer database = 0;

        // 连接池最大连接数（使用负值表示没有限制）
        private int maxTotal = 8;
        // 连接池中的最大空闲连接
        private int maxIdle = 8;
        // 连接池中的最小空闲连接
        private int minIdle = 0;
        // 连接池最大阻塞等待时间(使用负值表示没有限制) 默认为-1
        private Long maxWait = -1L;
        //连接超时的时间
        private Integer timeout = 2000;
        //集群
        private ClusterProperties cluster;
        //哨兵
        private SentinelProperties sentinel;

        @Data
        public class ClusterProperties {
            // 集群节点，必输
            private List<String> nodes;
            // 出现异常最大重试次数
            private Integer maxAttempts = 5;
        }

        @Data
        public class SentinelProperties {
            // 烧饼节点，必输
            private List<String> nodes;
            // 主节点名称，默认为空，必输
            private String masterName;
            // 用户，默认为空
            private String user;
        }
    }
}
