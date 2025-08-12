package com.sunlight.linker.exercises.application;

import com.sunlight.linker.application.ShortLinkService;
import com.sunlight.linker.domain.ShortLink;
import com.sunlight.linker.infrastructure.ShortLinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 【练习2】ShortLinkService 集成测试练习
 * 
 * 【第二阶段测试实践 - Service层集成测试】
 * 
 * 学习目标：
 * ✅ 掌握Spring Boot集成测试的编写方法
 * ✅ 学会使用@MockBean进行依赖模拟
 * ✅ 掌握Mockito高级用法和验证技巧
 * ✅ 理解Service层业务逻辑测试策略
 * 
 * 【练习说明】：
 * 这是中间层测试，需要启动Spring容器但模拟数据库操作
 * 重点关注：Mock配置、业务逻辑验证、方法调用验证
 * 
 * 【评分标准】：
 * - Spring集成测试配置 (20分)
 * - Mock对象使用和配置 (30分)
 * - 业务逻辑测试覆盖 (30分)
 * - Mockito验证和断言 (20分)
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("【练习】ShortLinkService集成测试")
class ShortLinkServiceExercise {
    
    /**
     * 被测试的Service实例
     * 
     * TODO: 注入真实的Service实例
     * 提示：使用@Autowired注解
     */
    // TODO: 注入ShortLinkService
    private ShortLinkService shortLinkService;
    
    /**
     * Mock的Repository依赖
     * 
     * TODO: 创建Repository的Mock实例
     * 提示：使用@MockBean注解
     */
    // TODO: 创建ShortLinkRepository的Mock
    private ShortLinkRepository shortLinkRepository;
    
    /**
     * 测试数据准备
     */
    private static final String VALID_LONG_URL = "https://www.example.com/very/long/path/to/resource";
    private static final String VALID_SHORT_CODE = "abc123";
    private static final String CUSTOM_ALIAS = "mylink";
    
    private ShortLink testShortLink;
    
    /**
     * 每个测试方法执行前的准备工作
     * 
     * TODO: 完成测试数据初始化和Mock重置
     */
    @BeforeEach
    void setUp() {
        // TODO: 创建测试用的ShortLink对象
        // 提示：使用createShortLinkWithId辅助方法
        
        // TODO: 重置所有Mock对象的状态
        // 提示：使用reset()方法
    }
    
    /**
     * 创建短链接功能测试组
     */
    @Nested
    @DisplayName("创建短链接测试练习")
    class CreateShortLinkTests {

        /**
         * 【练习2.1】新短链接创建测试
         * 
         * TODO: 测试为新的长链接创建短链接的完整流程
         * 
         * 测试步骤：
         * 1. Mock Repository.findByLongUrl() 返回 Optional.empty()
         * 2. Mock Repository.save() 返回带ID的对象和最终对象
         * 3. 调用Service.createShortLink()
         * 4. 验证返回结果的正确性
         * 5. 验证Repository方法的调用次数和参数
         */
        @Test
        @DisplayName("应该为新的长链接创建短链接")
        void shouldCreateNewShortLink() {
            // TODO: Given - 准备测试数据和Mock行为
            // 模拟查询不存在的长链接
            // when(shortLinkRepository.findByLongUrl(...)).thenReturn(...);
            
            // 模拟保存操作，注意需要两次保存
            // 第一次保存：返回带ID的临时对象
            // 第二次保存：返回最终对象
            
            // TODO: When - 执行被测试的方法
            // ShortLink result = ...
            
            // TODO: Then - 验证结果和方法调用
            // 验证返回对象的属性
            // 验证Repository方法的调用次数
            // 使用ArgumentCaptor验证保存的数据
        }

        /**
         * 【练习2.2】重复长链接处理测试
         * 
         * TODO: 测试当长链接已存在时，应该返回现有的短链接
         */
        @Test
        @DisplayName("重复长链接应该返回现有短链接")
        void shouldReturnExistingShortLinkForDuplicateUrl() {
            // TODO: 实现重复URL测试
            // 提示：mock findByLongUrl返回已存在的记录
        }

        /**
         * 【练习2.3】自定义别名创建测试
         * 
         * TODO: 测试使用自定义别名创建短链接
         */
        @Test
        @DisplayName("应该支持自定义别名创建")
        void shouldCreateShortLinkWithCustomAlias() {
            // TODO: 实现自定义别名测试
            // 注意：需要验证isCustomAlias字段设置为true
        }
    }

    /**
     * 获取长链接功能测试组
     */
    @Nested
    @DisplayName("获取长链接测试练习")
    class GetLongUrlTests {

        /**
         * 【练习2.4】成功获取长链接测试
         * 
         * TODO: 测试通过短码成功获取长链接
         */
        @Test
        @DisplayName("应该成功获取存在的长链接")
        void shouldReturnLongUrlForExistingShortCode() {
            // TODO: 实现成功获取测试
            // 步骤：
            // 1. Mock Repository.findByShortCode() 返回存在的记录
            // 2. Mock Repository.save() 更新访问计数
            // 3. 调用Service.getLongUrl()
            // 4. 验证返回的Optional包含正确的URL
            // 5. 验证访问计数被正确更新
        }

        /**
         * 【练习2.5】短码不存在测试
         * 
         * TODO: 测试短码不存在时返回empty Optional
         */
        @Test
        @DisplayName("不存在的短码应该返回空Optional")
        void shouldReturnEmptyForNonExistentShortCode() {
            // TODO: 实现短码不存在测试
        }

        /**
         * 【练习2.6】访问计数更新验证
         * 
         * TODO: 专门验证访问计数的更新逻辑
         * 使用ArgumentCaptor验证保存的对象
         */
        @Test
        @DisplayName("应该正确更新访问计数")
        void shouldUpdateAccessCountCorrectly() {
            // TODO: 实现访问计数更新测试
            // 重点：使用ArgumentCaptor验证保存的对象
        }
    }

    /**
     * 自定义别名功能测试组
     */
    @Nested
    @DisplayName("自定义别名测试练习")
    class CustomAliasTests {

        /**
         * 【练习2.7】别名可用性检查测试
         * 
         * TODO: 测试检查别名是否可用的功能
         */
        @Test
        @DisplayName("应该正确检查别名可用性")
        void shouldCheckAliasAvailability() {
            // TODO: 实现别名可用性检查
            // 测试别名不存在时返回true
            // 测试别名已存在时返回false
        }

        /**
         * 【练习2.8】重复别名处理测试
         * 
         * TODO: 测试当别名已被占用时的处理逻辑
         */
        @Test
        @DisplayName("重复别名应该抛出异常")
        void shouldThrowExceptionForDuplicateAlias() {
            // TODO: 实现重复别名异常测试
            // 提示：使用assertThatThrownBy()
        }
    }

    /**
     * 短码可用性测试组
     */
    @Nested
    @DisplayName("短码可用性测试练习")
    class ShortCodeAvailabilityTests {

        /**
         * 【练习2.9】短码可用性检查
         * 
         * TODO: 测试系统生成的短码可用性检查
         */
        @Test
        @DisplayName("应该正确检查短码可用性")
        void shouldCheckShortCodeAvailability() {
            // TODO: 实现短码可用性检查
        }
    }

    /**
     * 时间范围查询测试组
     */
    @Nested
    @DisplayName("时间范围查询测试练习")
    class TimeRangeQueryTests {

        /**
         * 【练习2.10】时间范围内链接查询
         * 
         * TODO: 测试查询指定时间范围内创建的链接
         */
        @Test
        @DisplayName("应该返回时间范围内的链接")
        void shouldReturnLinksInTimeRange() {
            // TODO: 实现时间范围查询测试
            // 提示：需要创建多个测试数据，包含不同的创建时间
        }

        /**
         * 【练习2.11】空结果处理
         * 
         * TODO: 测试时间范围内无数据时的处理
         */
        @Test
        @DisplayName("无数据时应该返回空列表")
        void shouldReturnEmptyListWhenNoDataInRange() {
            // TODO: 实现空结果测试
        }
    }

    /**
     * 系统统计测试组
     */
    @Nested
    @DisplayName("系统统计测试练习")
    class SystemStatsTests {

        /**
         * 【练习2.12】系统统计数据测试
         * 
         * TODO: 测试获取系统整体统计信息
         */
        @Test
        @DisplayName("应该返回正确的系统统计")
        void shouldReturnCorrectSystemStats() {
            // TODO: 实现系统统计测试
            // 需要mock Repository的count()方法
        }
    }

    /**
     * 【辅助方法】创建带ID的ShortLink对象
     * 
     * TODO: 实现这个辅助方法，用于创建测试数据
     * 提示：使用ReflectionTestUtils.setField()设置私有字段ID
     */
    private ShortLink createShortLinkWithId(Long id, String longUrl, String shortCode) {
        // TODO: 创建ShortLink对象并设置ID
        // ShortLink link = new ShortLink(longUrl, shortCode);
        // 使用Spring测试工具类设置私有字段ID
        // org.springframework.test.util.ReflectionTestUtils.setField(link, "id", id);
        // return link;
        return null; // 替换为实际实现
    }
}
