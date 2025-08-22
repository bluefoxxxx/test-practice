package com.sunlight.linker.exercises.application;

import com.sunlight.linker.application.ShortLinkService;
import com.sunlight.linker.core.Base62Converter;
import com.sunlight.linker.domain.ShortLink;
import com.sunlight.linker.infrastructure.ShortLinkRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * 【练习3】ShortLinkService真实数据库集成测试练习
 * 
 * 【第三阶段测试实践 - Testcontainers集成测试】
 * 
 * 学习目标：
 * ✅ 掌握Testcontainers的使用方法
 * ✅ 学会配置和管理测试容器
 * ✅ 理解真实数据库集成测试的重要性
 * ✅ 掌握数据持久化和查询测试
 * 
 * 【练习说明】：
 * 这是高层次的集成测试，使用真实的PostgreSQL数据库
 * 重点关注：容器配置、数据库操作、复杂查询测试
 * 
 * 【评分标准】：
 * - Testcontainers配置 (25分)
 * - 数据持久化测试 (30分)
 * - 复杂查询测试 (25分)
 * - 事务和并发测试 (20分)
 */
@SpringBootTest
@Testcontainers
@DisplayName("【练习】ShortLinkService真实数据库测试")
@TestPropertySource(properties = {"spring.jpa.hibernate.ddl-auto=create"})
class ShortLinkServiceRealDbExercise {
    
    /**
     * PostgreSQL测试容器
     * 
     * TODO: 配置PostgreSQL测试容器
     * 提示：使用@Container注解和PostgreSQLContainer
     * 
     * 配置要求：
     * - 使用postgres:15镜像
     * - 数据库名：testdb
     * - 用户名：test  
     * - 密码：test
     * - 启用容器重用以提高性能
     */
    // TODO: 配置@Container注解
    // TODO: 创建PostgreSQLContainer实例
    // static PostgreSQLContainer<?> postgres = ...
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);
    /**
     * 动态配置Spring Boot数据源
     * 
     * TODO: 实现动态属性配置方法
     * 提示：使用@DynamicPropertySource注解
     * 
     * 需要配置的属性：
     * - spring.datasource.url
     * - spring.datasource.username  
     * - spring.datasource.password
     */
    // TODO: 添加@DynamicPropertySource注解
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // TODO: 配置数据源URL
        // registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        // TODO: 配置用户名和密码
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    /**
     * 注入被测试的Service
     * 
     * TODO: 注入真实的Service实例
     */
    // TODO: 使用@Autowired注入ShortLinkService
    @Autowired
    private ShortLinkService shortLinkService;
    
    /**
     * 注入Repository用于数据验证
     * 
     * TODO: 注入Repository用于直接数据库操作验证
     */
    // TODO: 使用@Autowired注入ShortLinkRepository
    @Autowired
    private ShortLinkRepository shortLinkRepository;

    // 注入PlatformTransactionManager
    @Autowired
    private PlatformTransactionManager transactionManager;
    /**
     * 测试数据常量
     */
    private static final String TEST_LONG_URL_1 = "https://www.example.com/page1";
    private static final String TEST_LONG_URL_2 = "https://www.example.com/page2";
    private static final String CUSTOM_ALIAS = "custom123";
    
    /**
     * 每个测试方法执行前清理数据
     * 
     * TODO: 实现测试前的数据清理
     * 提示：删除所有测试数据，确保测试隔离
     */
    @BeforeEach
    void setUp() {
        // TODO: 清理所有现有数据
        // shortLinkRepository.deleteAll();
        shortLinkRepository.deleteAll();
    }
    
    /**
     * 创建短链接数据流测试组
     */
    @Nested
    @DisplayName("创建短链接数据流测试练习")
    class CreateShortLinkDataFlowTests {

        /**
         * 【练习3.1】完整数据持久化测试
         * 
         * TODO: 测试创建短链接的完整数据流
         * 
         * 测试步骤：
         * 1. 创建短链接并验证返回结果
         * 2. 直接从数据库查询验证数据已保存
         * 3. 验证所有字段的正确性
         * 4. 验证时间戳字段的设置
         */
        @Test
        @DisplayName("应该完整保存短链接数据")
        void shouldPersistShortLinkDataCompletely() {
            // TODO: Given - 准备测试数据
            
            // TODO: When - 调用Service创建短链接
            // ShortLink result = shortLinkService.createShortLink(...);
            ShortLink result = shortLinkService.createShortLink(TEST_LONG_URL_1);
            // TODO: Then - 验证Service返回结果
            // 验证返回对象不为null
            // 验证ID已设置
            // 验证URL正确
            // 验证短码不为空
            assertThat(result).isNotNull();
            assertThat(result.getId()).isNotNull(); // 数据库应该已经生成了ID
            assertThat(result.getLongUrl()).isEqualTo(TEST_LONG_URL_1);
            assertThat(result.getShortCode()).isNotNull().isNotEmpty(); // 短码应该已被生成并回填
            assertThat(result.getCreatedAt()).isNotNull(); // 创建时间应该由Hibernate自动设置
            assertThat(result.getLastUpdatedAt()).isNotNull(); // 更新时间也应该被设置

            // TODO: 直接从数据库验证数据持久化
            // Optional<ShortLink> saved = shortLinkRepository.findById(result.getId());
            Optional<ShortLink> savedOptional = shortLinkRepository.findById(result.getId());
            // 验证数据库中的记录
            assertThat(savedOptional).isPresent();

            ShortLink savedInDb = savedOptional.get();
            // 验证创建时间和更新时间
            assertThat(savedInDb.getLongUrl()).isEqualTo(TEST_LONG_URL_1);
            assertThat(savedInDb.getShortCode()).isEqualTo(result.getShortCode());
            assertThat(savedInDb.getIsCustomAlias()).isFalse(); // 默认应为false
            assertThat(savedInDb.getAccessCount()).isEqualTo(0L); // 初始访问次数应为0
            assertThat(savedInDb.getCreatedAt()).isEqualTo(result.getCreatedAt());
        }

        /**
         * 【练习3.2】重复URL防重机制测试
         * 
         * TODO: 测试相同URL的防重复创建机制
         */
        @Test
        @DisplayName("相同URL应该返回现有记录")
        void shouldReturnExistingRecordForDuplicateUrl() {
            // TODO: 实现防重测试
            // 1. 创建第一个短链接
            ShortLink firstLink = shortLinkService.createShortLink(TEST_LONG_URL_1);
            assertThat(firstLink).isNotNull();
            assertThat(firstLink.getId()).isNotNull();
            // 2. 用相同URL再次创建
            ShortLink secondLink = shortLinkService.createShortLink(TEST_LONG_URL_1);
            assertThat(secondLink).isNotNull();
            // 3. 验证返回的是同一个记录
            assertThat(secondLink.getId()).isEqualTo(firstLink.getId());
            assertThat(secondLink.getShortCode()).isEqualTo(firstLink.getShortCode());
            // 4. 验证数据库中只有一条记录
            long count = shortLinkRepository.count();
            assertThat(count).isEqualTo(1L);
        }

        /**
         * 【练习3.3】Base62编码生成测试
         * 
         * TODO: 测试ID到短码的Base62编码过程
         */
        @Test
        @DisplayName("应该正确生成Base62短码")
        void shouldGenerateCorrectBase62ShortCode() {
            // TODO: 实现Base62编码测试
            // 创建短链接后验证短码符合Base62规则
            ShortLink createdLink = shortLinkService.createShortLink(TEST_LONG_URL_1);
            Long databaseId = createdLink.getId();
            String expectedShortId = Base62Converter.encode(databaseId);
            assertThat(createdLink.getShortCode()).isEqualTo(expectedShortId);

            // 再次验证数据库中的数据
            Optional<ShortLink> savedOptional = shortLinkRepository.findById(databaseId);
            assertThat(savedOptional).isPresent();
            assertThat(savedOptional.get().getShortCode()).isEqualTo(expectedShortId);
        }
    }

    /**
     * 查询和访问统计测试组
     */
    @Nested
    @DisplayName("查询和访问统计测试练习")
    class QueryAndAccessStatsTests {

        /**
         * 【练习3.4】URL查询和访问计数测试
         * 
         * TODO: 测试通过短码查询URL并更新访问计数
         */
        @Test
        @DisplayName("应该正确查询URL并更新访问计数")
        void shouldQueryUrlAndUpdateAccessCount() {
            // TODO: Given - 创建测试短链接
            ShortLink createdLink = shortLinkService.createShortLink(TEST_LONG_URL_1);
            String shortCode = createdLink.getShortCode();
            Long initialAccessCount = createdLink.getAccessCount();

            // 确认初始访问次数是0
            assertThat(initialAccessCount).isEqualTo(0L);
            // TODO: When - 多次访问短链接
            // 第一次访问
            Optional<String> url1 = shortLinkService.getLongUrl(shortCode);
            // 第二次访问
            Optional<String> url2 = shortLinkService.getLongUrl(shortCode);
            // 第三次访问
            Optional<String> url3 = shortLinkService.getLongUrl(shortCode);

            // TODO: Then - 验证访问计数正确更新
            // 从数据库直接查询验证计数
            assertThat(url1).isPresent().contains(TEST_LONG_URL_1);
            assertThat(url2).isPresent().contains(TEST_LONG_URL_1);
            assertThat(url3).isPresent().contains(TEST_LONG_URL_1);

            Optional<ShortLink> updatedLinkOptional = shortLinkRepository.findById(createdLink.getId());
            assertThat(updatedLinkOptional).isPresent();
            ShortLink updatedLinkInDb = updatedLinkOptional.get();
            assertThat(updatedLinkInDb.getAccessCount()).isEqualTo(initialAccessCount + 3);
        }

        /**
         * 【练习3.5】不存在短码查询测试
         * 
         * TODO: 测试查询不存在的短码
         */
        @Test
        @DisplayName("不存在的短码应该返回空结果")
        void shouldReturnEmptyForNonExistentShortCode() {
            // TODO: 实现不存在短码测试
            String nonExistentCode = "nonExistentCode";

            Optional<String> result = shortLinkService.getLongUrl(nonExistentCode);

            assertThat(result).isNotPresent();
        }
    }

    /**
     * 自定义别名数据流测试组
     */
    @Nested
    @DisplayName("自定义别名数据流测试练习")
    class CustomAliasDataFlowTests {

        /**
         * 【练习3.6】自定义别名创建和查询
         * 
         * TODO: 测试自定义别名的完整数据流
         */
        @Test
        @DisplayName("应该支持自定义别名的创建和查询")
        void shouldSupportCustomAliasCreationAndQuery() {
            // TODO: 实现自定义别名测试
            // 1. 使用自定义别名创建短链接
            String description = "一个自定义别名";
            ShortLink createdLink = shortLinkService.createCustomShortLink(TEST_LONG_URL_2, CUSTOM_ALIAS, description);

            // 2. 验证isCustomAlias字段为true
            assertThat(createdLink).isNotNull();
            assertThat(createdLink.getShortCode()).isEqualTo(CUSTOM_ALIAS); // 短码应为我们自定义的别名
            assertThat(createdLink.getLongUrl()).isEqualTo(TEST_LONG_URL_2);
            assertThat(createdLink.getIsCustomAlias()).isTrue(); // isCustomAlias 字段应为 true
            assertThat(createdLink.getDescription()).isEqualTo(description);
            // 3. 通过别名查询URL

            Optional<String> resultUrl = shortLinkService.getLongUrl(CUSTOM_ALIAS);
            assertThat(resultUrl).isPresent().contains(TEST_LONG_URL_2);
            // 4. 验证查询结果正确

            Optional<ShortLink> savedInDb = shortLinkRepository.findByShortCode(CUSTOM_ALIAS);
            assertThat(savedInDb).isPresent();
            assertThat(savedInDb.get().getIsCustomAlias()).isTrue();
        }

        /**
         * 【练习3.7】别名唯一性约束测试
         * 
         * TODO: 测试别名的唯一性约束
         */
        @Test
        @DisplayName("重复别名应该触发约束异常")
        void shouldEnforceAliasUniqueness() {
            // TODO: 实现唯一性约束测试
            // 注意：这里应该测试数据库层面的约束
            shortLinkService.createCustomShortLink(TEST_LONG_URL_1, CUSTOM_ALIAS, "链接1");
            assertThatThrownBy(() -> {
                shortLinkService.createCustomShortLink(TEST_LONG_URL_2, CUSTOM_ALIAS, "链接2");
            })
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("自定义别名已被占用");

            // 确认数据库中该别名对应的记录仍然只有一条，并且内容是第一次创建时的信息
            long count = shortLinkRepository.countByShortCode(CUSTOM_ALIAS);
            assertThat(count).isEqualTo(1);

            Optional<ShortLink> linkInDb = shortLinkRepository.findByShortCode(CUSTOM_ALIAS);
            assertThat(linkInDb).isPresent();
            assertThat(linkInDb.get().getLongUrl()).isEqualTo(TEST_LONG_URL_1); // 确认数据库里的数据没被覆盖
        }
    }

    /**
     * 复杂查询测试组
     */
    @Nested
    @DisplayName("复杂查询测试练习")
    class ComplexQueryTests {

        /**
         * 【练习3.8】时间范围查询测试
         * 
         * TODO: 测试按时间范围查询短链接
         */
        @Test
        @DisplayName("应该正确执行时间范围查询")
        void shouldPerformTimeRangeQueriesCorrectly() throws InterruptedException {
            // TODO: Given - 创建不同时间的测试数据
            // 创建3个短链接，模拟不同的创建时间
            // 可以通过直接操作数据库或使用Thread.sleep()
            LocalDateTime now = LocalDateTime.now();
            ShortLink link1 = shortLinkService.createShortLink(TEST_LONG_URL_1);

            LocalDateTime startTime = LocalDateTime.now(); // 查询开始时间
            Thread.sleep(20);
            ShortLink link2 = shortLinkService.createShortLink(TEST_LONG_URL_2);
            LocalDateTime endTime = LocalDateTime.now(); // 查询结束时间

            Thread.sleep(20);
            ShortLink link3 = shortLinkService.createShortLink("https://www.example.com/page3");
            
            // TODO: When - 执行时间范围查询
            List<ShortLink> results = shortLinkService.getLinksCreatedBetween(startTime,endTime);

            // TODO: Then - 验证查询结果
            // 验证返回的记录数量和内容
            assertThat(results)
                    .isNotNull()
                    .hasSize(1)
                    .extracting(ShortLink::getId)
                    .containsExactly(link2.getId()); // 验证结果中只包含link2的id
        }

        /**
         * 【练习3.9】热门链接查询测试
         * 
         * TODO: 测试按访问次数查询热门链接
         */
        @Test
        @DisplayName("应该正确查询热门链接")
        void shouldQueryHotLinksCorrectly() {
            // TODO: 实现热门链接查询测试
            // 1. 创建多个短链接
            ShortLink linkA = shortLinkService.createShortLink("https://example.com/linkA"); // 访问10次
            ShortLink linkB = shortLinkService.createShortLink("https://example.com/linkB"); // 访问3次
            ShortLink linkC = shortLinkService.createShortLink("https://example.com/linkC"); // 访问20次
            ShortLink linkD = shortLinkService.createShortLink("https://example.com/linkD"); // 访问5次 (不满足热门阈值)

            // 2. 模拟不同的访问次数
            simulateAccesses(linkA.getShortCode(), 10);
            simulateAccesses(linkB.getShortCode(), 3);
            simulateAccesses(linkC.getShortCode(), 20);
            simulateAccesses(linkD.getShortCode(), 5);

            // 3. 查询热门链接

            // 查询访问次数 > 5 的热门链接
            long minAccessCount = 5L;
            List<ShortLink> hotLinks = shortLinkService.getHotLinks(minAccessCount);
            // 4. 验证结果按访问次数排序
            assertThat(hotLinks)
                    .isNotNull()
                    .hasSize(2) // 只有 linkC (20次) 和 linkA (10次) 满足 > 5 的条件
                    .extracting(ShortLink::getId)
                    // linkC 在 linkA 前面
                    .containsExactly(linkC.getId(), linkA.getId());
        }

        /**
         * 辅助方法，用于模拟对特定短码的多次访问
         * @param shortCode 要访问的短码
         * @param times 访问次数
         */
        private void simulateAccesses(String shortCode, int times) {
            for (int i = 0; i < times; i++) {
                shortLinkService.getLongUrl(shortCode);
            }
        }

        /**
         * 【练习3.10】系统统计查询测试
         * 
         * TODO: 测试系统整体统计信息查询
         */
        @Test
        @DisplayName("应该返回正确的系统统计")
        void shouldReturnCorrectSystemStatistics() {
            // TODO: 实现系统统计测试
            // 创建 3 个系统生成的链接
            ShortLink linkSys1 = shortLinkService.createShortLink("https://example.com/link11");
            ShortLink linkSys2 = shortLinkService.createShortLink("https://example.com/link22");
            ShortLink linkSys3 = shortLinkService.createShortLink("https://example.com/link33");

            // 创建 2 个自定义别名的链接
            shortLinkService.createCustomShortLink("https://example.com/custom1", "custom1", null);
            shortLinkService.createCustomShortLink("https://example.com/custom2", "custom2", null);

            // 模拟一些访问
            simulateAccesses(linkSys1.getShortCode(), 10); // 访问10次
            simulateAccesses(linkSys2.getShortCode(), 5);  // 访问5次

            // 调用获取系统统计信息的方法
            ShortLinkService.SystemStats stats = shortLinkService.getSystemStats();

            // 验证统计数据是否完全符合我们创建的场景
            assertThat(stats).isNotNull();

            // 总链接数应该是 5
            assertThat(stats.getTotalLinks()).isEqualTo(5L);

            // 总访问次数应该是 15
            assertThat(stats.getTotalAccess()).isEqualTo(15L);

            // 自定义别名链接数应该是 2
            assertThat(stats.getCustomAliases()).isEqualTo(2L);
        }
    }



    /**
     * 事务和并发测试组
     */
    @Nested
    @DisplayName("事务和并发测试练习")
    class TransactionAndConcurrencyTests {

        /**
         * 【练习3.11】事务回滚测试
         * 
         * TODO: 测试事务回滚机制
         * 提示：可以通过抛出异常来触发回滚
         */
        @Test
        @DisplayName("异常情况应该触发事务回滚")
        void shouldRollbackTransactionOnException() {
            // TODO: 实现事务回滚测试
            // 这是一个高级练习，需要模拟异常情况
            long countBefore = shortLinkRepository.count();
            assertThat(countBefore).isEqualTo(0);

            // 创建一个 TransactionTemplate 实例，用于编程式事务管理
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

            // 这个包含事务操作和异常的代码块会抛出异常
            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> {
                        transactionTemplate.execute(status -> {
                            // 这部分代码在一个新的数据库事务中执行
                            shortLinkService.createShortLink("https://example.com/tx-test");
                            // 在事务提交之前检查数据库，可以看到数据已插入
                            assertThat(shortLinkRepository.count()).isEqualTo(countBefore + 1);
                            //手动抛出一个运行时异常来触发回滚
                            throw new RuntimeException("抛出异常来测试回滚");
                        });
                    });

            // 在捕获到异常之后，事务应该已经回滚
            long countAfter = shortLinkRepository.count();
            assertThat(countAfter)
                    .as("数据库记录数在事务回滚后应该和之前一样")
                    .isEqualTo(countBefore);
        }

        /**
         * 【练习3.12】并发访问测试
         * 
         * TODO: 测试并发访问时的数据一致性
         * 提示：使用CompletableFuture模拟并发操作
         */
        @Test
        @DisplayName("并发访问应该保持数据一致性")
        void shouldMaintainDataConsistencyUnderConcurrency() {
            // TODO: 实现并发测试
            // 这是一个高级练习，需要多线程编程知识
            int totalCreations = 200;
            // 确认数据库初始状态为空
            assertThat(shortLinkRepository.count()).isEqualTo(0);

            // 创建 CompletableFuture 任务列表
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            // 使用一个原子计数器来确保每个长链接的唯一性
            AtomicInteger counter = new AtomicInteger(0);

            // 启动所有并发创建任务
            for (int i = 0; i < totalCreations; i++) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    // 为每个线程生成一个唯一的长链接
                    String uniqueLongUrl = "https://example.com/concurrent-creation-" + counter.incrementAndGet();
                    // 每个异步任务都调用 createShortLink
                    shortLinkService.createShortLink(uniqueLongUrl);
                });
                futures.add(future);
            }

            // 等待所有并发任务执行完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            long finalCount = shortLinkRepository.count();
            // 断言最终记录数
            assertThat(finalCount)
                    .as("数据库中的总记录数应该等于并发创建的总数")
                    .isEqualTo(totalCreations);
        }
    }

    /**
     * 性能基准测试组
     */
    @Nested
    @DisplayName("性能基准测试练习")
    class PerformanceBenchmarkTests {

        /**
         * 【练习3.13】批量操作性能测试
         * 
         * TODO: 测试大量数据的创建和查询性能
         * 注意：这不是严格的性能测试，只是基本验证
         */
        @Test
        @DisplayName("批量操作应该在合理时间内完成")
        void shouldPerformBulkOperationsReasonably() {
            // TODO: 实现性能基准测试
            // 创建大量短链接（如100个）
            // 测量执行时间
            // 验证性能在可接受范围内
            int numberOfLinks = 100;
            // 定义一个我们认为可接受的最大耗时
            long timeoutMillis = 2000L;

            // 记录开始时间
            long startTime = System.currentTimeMillis();

            // 在循环中连续创建100个不同的短链接
            for (int i = 0; i < numberOfLinks; i++) {
                shortLinkService.createShortLink("https://example.com/bulk-" + i);
            }

            // 记录结束时间并计算总耗时
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            System.out.println("创建 " + numberOfLinks + " 个链接耗时: " + duration + "ms");
            // 验证数据库中确实创建了100条记录
            assertThat(shortLinkRepository.count()).isEqualTo(numberOfLinks);

            // 验证执行时间在我们的预期范围内
            assertThat(duration)
                    .as("批量创建 %d 个链接的耗时不应超过 %d ms", numberOfLinks, timeoutMillis)
                    .isLessThan(timeoutMillis);
        }
    }
}
