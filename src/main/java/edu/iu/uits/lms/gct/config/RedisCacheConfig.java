package edu.iu.uits.lms.gct.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.time.Duration;

import static edu.iu.uits.lms.gct.Constants.CACHE_DRIVE_SERVICE;

@Profile("redis-cache")
@Configuration
@EnableCaching
@Slf4j
public class RedisCacheConfig {

    @Autowired
    private ToolConfig toolConfig;

    @Autowired
    private JedisConnectionFactory redisConnectionFactory;

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        final int ttl = 3600;
        return RedisCacheConfiguration.defaultCacheConfig()
              .entryTtl(Duration.ofSeconds(ttl))
              .disableCachingNullValues()
              .prefixCacheNameWith(toolConfig.getEnv() + "-gct");
    }

    @Bean(name = "GoogleCourseToolsCacheManager")
    public CacheManager cacheManager() {
        log.debug("cacheManager()");
        log.debug("Redis hostname: {}", redisConnectionFactory.getHostName());
        return RedisCacheManager.builder(redisConnectionFactory)
              .withCacheConfiguration(CACHE_DRIVE_SERVICE, cacheConfiguration())
              .build();
    }
}
