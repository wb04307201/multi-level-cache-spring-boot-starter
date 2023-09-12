# multi-level-cache-spring-boot-starter

[![](https://jitpack.io/v/com.gitee.wb04307201/multi-level-cache-spring-boot-starter.svg)](https://jitpack.io/#com.gitee.wb04307201/multi-level-cache-spring-boot-starter)

> 这是一个缓存适配器  
> 可配置多个caffeine本地缓存和redis缓存服务，并可以串联多个缓存配置形成多级缓存  
> 与spring-cache相结合，支持@Caching、@Cacheable、@CacahePut、@CacheEvict等注解的使用

## [代码示例](https://gitee.com/wb04307201/multi-level-cache-demo)

## 第一步 增加 JitPack 仓库

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

## 第二步 引入jar

```xml
<dependency>
    <groupId>com.gitee.wb04307201</groupId>
    <artifactId>multi-level-cache-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 第三步 在启动类上加上`@EnableMultiLevelCache`注解

```java
@EnableMultiLevelCache
@SpringBootApplication
public class MultiLevelCacheDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiLevelCacheDemoApplication.class, args);
    }

}
```

## 第四步 `application.yml`配置文件中添加以下相关配置，可以配置多个缓存然后形成多级缓存使用

```yaml
multi:
  level:
    caches:
      - cacheName: caffeine-1
        # 缓存类型 5种 multilevel(多级) caffeine(默认) redis(单例) redis-cluster(集群) redis-sentinel(哨兵)
        cachetype: caffeine
        # 缓存方式 3种 none(默认，不过期) ttl(Time To Live,自创建以后可以存活的时间) tti(Time To Idle,自最后一次被使用以后可以存活的时间)
        expirytype: ttl
        # 存活时间 秒级 缓存方式选择tti,ttl expirytime必输
        expirytime: 10
        caffeine:
          # 基于缓存内的元素个数进行驱逐，默认10_000L,设置为0则不启用
          maximumSize: 10000
          # 是否开统计 默认false
          recordStats: false
      - cacheName: redis-1
        cachetype: redis
        expirytype: ttl
        expirytime: 30
        redis:
          # 单例地址,默认localhost
          host: localhost
          # 单例端口路，默认6379
          port: 6379
          # 密码，默认空
          password:
          # 数据库，默认0
          database: 0
          # 连接池最大连接数（使用负值表示没有限制），默认8
          maxTotal: 8
          # 连接池中的最大空闲连接，默认8
          maxIdle: 8
          # 连接池中的最小空闲连接，默认0
          minIdle: 0
          # 连接池最大阻塞等待时间(使用负值表示没有限制) 默认为-1
          maxWait: -1L
          #连接超时的时间，，默认2000
          timeout: 2000
      - cacheName: redis-cluster-1
        cachetype: redis-cluster
        expirytype: ttl
        expirytime: 60
        redis:
          password:
          database: 0
          maxTotal: 8
          maxIdle: 8
          minIdle: 0
          maxWait: -1L
          timeout: 2000
          cluster:
            # 集群节点，必输
            nodes:
              - ip1:port1
              - ip2:port2
              - ip3:port3
            # 出现异常最大重试次数，默认5
            maxAttempts: 5
      - cacheName: redis-sentinel-1
        cachetype: redis-sentinel
        expirytype: none
        redis:
          password:
          database: 0
          maxTotal: 8
          maxIdle: 8
          minIdle: 0
          maxWait: -1L
          timeout: 2000
          sentinel:
            # 烧饼节点，必输
            nodes:
              - ip1:port1
              - ip2:port2
              - ip3:port3
            # 主节点名称，默认为空，必输
            masterName:
            # 用户，默认为空
            user:
      - cacheName: multilevel-1
        cachetype: multilevel
        # 多级缓存串联顺序，取缓存时先读取靠前的缓存
        level:
          - caffeine-1
          - redis-1
          - redis-cluster-1
          - redis-sentinel-1
```

## 第五步 通过注解使用缓存

```java
@Service
public class TestService {

    @CachePut(value = "level-4", key = "#testDTO.id")
    public Object put(TestDTO testDTO) {
        return testDTO;
    }

    @Cacheable(value = "level-4", key = "#key")
    public Object get(String key) {
        return new TestDTO("", "没有缓存数据");
    }
}
```