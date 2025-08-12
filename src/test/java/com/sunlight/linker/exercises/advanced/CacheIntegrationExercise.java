package com.sunlight.linker.exercises.advanced;

import com.sunlight.linker.application.CachedShortLinkService;
import com.sunlight.linker.domain.ShortLink;
import com.sunlight.linker.infrastructure.ShortLinkRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;

/**
 * 【练习6】缓存集成测试练习
 * 
 * 【进阶挑战 - 缓存测试】
 * 
 * 学习目标：
 * ✅ 掌握Redis缓存的集成测试方法
 * ✅ 学会使用Testcontainers启动Redis容器
 * ✅ 理解缓存命中、穿透、雪崩等概念
 * ✅ 掌握缓存与数据库一致性测试
 * ✅ 学会缓存性能和并发测试
 * 
 * 【练习说明】：
 * 这是进阶挑战的第一部分，需要同时使用PostgreSQL和Redis容器
 * 重点关注：容器配置、缓存策略、性能优化、一致性保证
 * 
 * 【评分标准】：
 * - Testcontainers配置 (20分)
 * - 缓存功能测试 (30分)
 * - 缓存性能测试 (25分)
 * - 缓存一致性测试 (25分)
 */
@SpringBootTest
@Testcontainers
@DisplayName("【练习】缓存集成测试")
class CacheIntegrationExercise {
    
    /**
     * PostgreSQL测试容器
     * 
     * TODO: 配置PostgreSQL容器
     * 提示：参考之前的练习，使用postgres:15镜像
     */
    // TODO: 添加@Container注解
    // TODO: 创建PostgreSQLContainer实例
    // static PostgreSQLContainer<?> postgres = ...
    
    /**
     * Redis测试容器
     * 
     * TODO: 配置Redis容器
     * 提示：使用GenericContainer和redis:7-alpine镜像
     * 端口：6379
     */
    // TODO: 添加@Container注解
    // TODO: 创建Redis容器实例
    // static GenericContainer<?> redis = ...
    
    /**
     * 动态配置数据源和Redis
     * 
     * TODO: 实现动态属性配置
     * 提示：同时配置PostgreSQL和Redis的连接参数
     */
    // TODO: 添加@DynamicPropertySource注解
    static void configureProperties(DynamicPropertyRegistry registry) {
        // TODO: 配置PostgreSQL连接
        // registry.add("spring.datasource.url", postgres::getJdbcUrl);
        
        // TODO: 配置Redis连接
        // registry.add("spring.data.redis.host", redis::getHost);
        
        // TODO: 启用Redis缓存
        // registry.add("spring.cache.type", () -> "redis");
    }
    
    /**
     * TODO: 注入必要的服务和工具
     */
    // TODO: 注入CachedShortLinkService
    private CachedShortLinkService cachedShortLinkService;
    
    // TODO: 注入ShortLinkRepository
    private ShortLinkRepository shortLinkRepository;
    
    // TODO: 注入RedisTemplate
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 测试数据常量
     */
    private static final String TEST_LONG_URL = "https://www.example.com/cache-test";
    private static final String TEST_LONG_URL_2 = "https://www.example.com/cache-test-2";
    
    /**
     * TODO: 实现测试前的准备工作
     */
    @BeforeEach
    void setUp() {
        // TODO: 清理数据库和缓存
        // shortLinkRepository.deleteAll();
        // redisTemplate.getConnectionFactory().getConnection().flushAll();
    }
    
    /**
     * 缓存基础功能测试组
     */
    @Nested
    @DisplayName("缓存基础功能测试练习")
    class CacheBasicFunctionalityTests {

        /**
         * 【练习6.1】短链接查询缓存测试
         * 
         * TODO: 测试短链接查询是否被正确缓存
         * 
         * 测试步骤：
         * 1. 创建短链接
         * 2. 第一次查询（应该从数据库查询并缓存）
         * 3. 验证Redis中存在缓存
         * 4. 第二次查询（应该从缓存返回）
         * 5. 验证两次查询结果一致
         */
        @Test
        @DisplayName("短链接查询应该被正确缓存")
        void shouldCacheShortLinkQueries() {
            // TODO: Given - 创建短链接
            
            // TODO: When - 第一次查询
            
            // TODO: Then - 验证结果正确和缓存存在
            
            // TODO: When - 第二次查询
            
            // TODO: Then - 验证结果一致
        }
        
        /**
         * 【练习6.2】缓存未命中测试
         * 
         * TODO: 测试缓存未命中时的数据库查询
         */
        @Test
        @DisplayName("缓存未命中时应该查询数据库")
        void shouldQueryDatabaseOnCacheMiss() {
            // TODO: Given - 直接在数据库创建数据，不通过缓存服务
            
            // TODO: 确保缓存中没有该数据
            
            // TODO: When - 通过缓存服务查询
            
            // TODO: Then - 验证返回正确结果并添加到缓存
        }
        
        /**
         * 【练习6.3】系统统计缓存测试
         * 
         * TODO: 测试系统统计信息的缓存功能
         */
        @Test
        @DisplayName("系统统计应该被缓存")
        void shouldCacheSystemStats() {
            // TODO: Given - 创建测试数据
            
            // TODO: When - 第一次查询统计
            
            // TODO: Then - 验证统计结果
            
            // TODO: When - 第二次查询统计
            
            // TODO: Then - 验证结果来自缓存
        }
    }
    
    /**
     * 缓存失效测试组
     */
    @Nested
    @DisplayName("缓存失效测试练习")
    class CacheEvictionTests {

        /**
         * 【练习6.4】缓存自动失效测试
         * 
         * TODO: 测试创建新短链接时统计缓存的自动失效
         */
        @Test
        @DisplayName("创建新短链接应该清除统计缓存")
        void shouldEvictStatsCacheOnNewLinkCreation() {
            // TODO: Given - 获取初始统计并缓存
            
            // TODO: When - 创建新短链接
            
            // TODO: Then - 统计应该自动更新（缓存被清除）
        }
        
        /**
         * 【练习6.5】手动缓存清除测试
         * 
         * TODO: 测试手动清除缓存功能
         */
        @Test
        @DisplayName("手动清除缓存应该生效")
        void shouldManuallyEvictCache() {
            // TODO: Given - 创建并缓存短链接
            
            // TODO: 确保已缓存
            
            // TODO: When - 手动清除缓存
            
            // TODO: Then - 验证缓存被清除
        }
    }
    
    /**
     * 缓存过期测试组
     */
    @Nested
    @DisplayName("缓存过期测试练习")
    class CacheExpirationTests {

        /**
         * 【练习6.6】缓存TTL设置测试
         * 
         * TODO: 测试缓存的过期时间设置
         */
        @Test
        @DisplayName("缓存应该设置正确的TTL")
        void shouldSetCorrectTTLForCache() {
            // TODO: Given - 创建短链接并触发缓存
            
            // TODO: 验证缓存存在
            
            // TODO: Then - 检查TTL设置是否在预期范围内
            // 提示：使用redisTemplate.getExpire()方法
        }
    }
    
    /**
     * 缓存并发测试组
     */
    @Nested
    @DisplayName("缓存并发测试练习")
    class CacheConcurrencyTests {

        /**
         * 【练习6.7】并发查询缓存安全性测试
         * 
         * TODO: 测试多线程并发查询相同短码的线程安全性
         */
        @Test
        @DisplayName("并发查询相同短码应该线程安全")
        void shouldHandleConcurrentQueriesThreadSafe() {
            // TODO: Given - 创建测试短链接
            
            // TODO: When - 使用CompletableFuture并发查询
            // 提示：可以参考ConcurrencyTestUtils的用法
            
            // TODO: Then - 验证所有结果一致且无异常
        }
        
        /**
         * 【练习6.8】并发创建缓存一致性测试
         * 
         * TODO: 测试并发创建不同短链接时的缓存一致性
         */
        @Test
        @DisplayName("并发创建不同短链接应该正确处理")
        void shouldHandleConcurrentCreationCorrectly() {
            // TODO: When - 并发创建不同的短链接
            
            // TODO: Then - 验证所有短码不同且无异常
        }
    }
    
    /**
     * 缓存性能测试组
     */
    @Nested
    @DisplayName("缓存性能测试练习")
    class CachePerformanceTests {

        /**
         * 【练习6.9】缓存命中性能测试
         * 
         * TODO: 验证缓存命中相比数据库查询的性能提升
         */
        @Test
        @DisplayName("缓存命中应该显著提高查询性能")
        void shouldImproveQueryPerformanceWithCache() {
            // TODO: Given - 创建测试短链接并预热缓存
            
            // TODO: When & Then - 测试缓存命中性能
            // 执行多次查询，测量耗时
            // 验证性能在可接受范围内
        }
        
        /**
         * 【练习6.10】缓存容量测试
         * 
         * TODO: 测试缓存在大量数据下的表现
         * 这是一个可选的高级练习
         */
        @Test
        @DisplayName("缓存应该正确处理大量数据")
        void shouldHandleLargeDatasetCorrectly() {
            // TODO: 创建大量测试数据
            
            // TODO: 测试缓存的LRU淘汰策略
            
            // 注意：这个测试可能需要较长时间，可以标记为@Disabled
        }
    }
    
    /**
     * 缓存一致性测试组
     */
    @Nested
    @DisplayName("缓存一致性测试练习")
    class CacheConsistencyTests {

        /**
         * 【练习6.11】缓存数据库一致性测试
         * 
         * TODO: 验证缓存和数据库数据的一致性
         */
        @Test
        @DisplayName("缓存和数据库应该保持一致")
        void shouldMaintainConsistencyBetweenCacheAndDatabase() {
            // TODO: Given - 创建短链接
            
            // TODO: 通过缓存查询
            
            // TODO: 直接从数据库查询
            
            // TODO: Then - 验证结果一致
        }
        
        /**
         * 【练习6.12】缓存更新一致性测试
         * 
         * TODO: 测试数据更新时的缓存一致性
         */
        @Test
        @DisplayName("数据更新时缓存应该正确失效")
        void shouldInvalidateCacheOnDataUpdate() {
            // TODO: Given - 创建并缓存短链接
            
            // TODO: When - 直接修改数据库数据
            
            // TODO: Then - 验证缓存失效策略
        }
    }
    
    /**
     * 缓存故障恢复测试组
     */
    @Nested
    @DisplayName("缓存故障恢复测试练习")
    class CacheFailureRecoveryTests {

        /**
         * 【练习6.13】Redis故障降级测试
         * 
         * TODO: 测试Redis不可用时的降级策略
         * 这是一个高级练习，可能需要模拟容器故障
         */
        @Test
        @DisplayName("Redis不可用时应该降级到数据库查询")
        void shouldFallbackToDatabaseWhenRedisUnavailable() {
            // TODO: Given - 创建短链接
            
            // TODO: 模拟Redis连接问题（可选）
            
            // TODO: When - 查询短链接
            
            // TODO: Then - 验证仍能正常返回结果
        }
    }
    
    /**
     * 缓存监控测试组
     */
    @Nested
    @DisplayName("缓存监控测试练习")
    class CacheMonitoringTests {

        /**
         * 【练习6.14】缓存命中率统计测试
         * 
         * TODO: 实现缓存命中率的统计和监控
         * 这是一个扩展练习
         */
        @Test
        @DisplayName("应该能够统计缓存命中率")
        void shouldTrackCacheHitRatio() {
            // TODO: 这是一个扩展练习
            // 可以实现简单的缓存统计功能
            
            // 提示：可以在缓存服务中添加计数器
            // 统计总查询次数和缓存命中次数
        }
    }
}
