# multi-level-cache-spring-boot-starter

> 这是一个缓存适配器  
> 可配置多个caffeine本地缓存和redis缓存服务，并可以串联多个缓存配置形成多级缓存  
> 与spring-cache相结合，支持@Caching、@Cacheable、@CacahePut、@CacheEvict等注解的使用

```yaml
multi:
  level:
    caches:
      - cacheName: caff-1
        cachetype: caffeine
        expirytype: ttl
        expirytime: 10
      - cacheName: caff-2
        cachetype: caffeine
        expirytype: ttl
        expirytime: 30
      - cacheName: caff-3
        cachetype: caffeine
        expirytype: ttl
        expirytime: 60
      - cacheName: caff-4
        cachetype: caffeine
        expirytype: none
      - cacheName: level-4
        cachetype: multilevel
        level:
          - caff-1
          - caff-2
          - caff-3
          - caff-4
      - cacheName: redis-3
        cachetype: redis
        expirytype: none
        redis:
          host: 127.0.0.1
          port: 6379
          password: mypassword
      - cacheName: level-3
        cachetype: multilevel
        level:
          - caff-1
          - caff-2
          - redis-3
```