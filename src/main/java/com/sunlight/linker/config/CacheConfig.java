package com.sunlight.linker.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis缓存配置类
 * 
 * 【进阶挑战 - 缓存配置】
 * 
 * 此配置类负责：
 * ✅ 启用Spring Cache抽象
 * ✅ 配置Redis作为缓存后端
 * ✅ 设置缓存序列化策略
 * ✅ 配置缓存过期时间
 * ✅ 提供RedisTemplate for 高级操作
 * 
 * 【缓存策略】：
 * - 短链接查询缓存：热点数据缓存，减少数据库访问
 * - 访问计数缓存：异步更新，提高响应速度
 * - 系统统计缓存：定期刷新，避免重复计算
 * 
 * @author 测试实践学习项目
 * @version 1.0
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 配置Redis缓存管理器
     * 
     * @param redisConnectionFactory Redis连接工厂
     * @return 配置好的缓存管理器
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 配置缓存序列化
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // 设置键的序列化方式 - 使用String
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                // 设置值的序列化方式 - 使用JSON
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                // 设置默认过期时间 - 30分钟
                .entryTtl(Duration.ofMinutes(30))
                // 不缓存null值
                .disableCachingNullValues();

        // 创建并返回缓存管理器
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .build();
    }

    /**
     * 配置RedisTemplate用于高级Redis操作
     * 
     * @param redisConnectionFactory Redis连接工厂
     * @return 配置好的RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        
        // 设置键的序列化器
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // 设置值的序列化器
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        // 设置默认序列化器
        template.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }
}
