package com.sunlight.linker.exercises.application;

import com.sunlight.linker.application.ShortLinkService;
import com.sunlight.linker.core.Base62Converter;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
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
    @Autowired
    private ShortLinkService shortLinkService;
    
    /**
     * Mock的Repository依赖
     * 
     * TODO: 创建Repository的Mock实例
     * 提示：使用@MockBean注解
     */
    // TODO: 创建ShortLinkRepository的Mock
    @MockBean
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
        testShortLink = createShortLinkWithId(1L, VALID_LONG_URL, VALID_SHORT_CODE);
        
        // TODO: 重置所有Mock对象的状态
        // 提示：使用reset()方法
        reset(shortLinkRepository);
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
            when(shortLinkRepository.findByLongUrl(anyString())).thenReturn(Optional.empty());
            
            // 模拟保存操作，注意需要两次保存
            // 第一次保存：返回带ID的临时对象
            ShortLink savedWithId = createShortLinkWithId(1L, VALID_LONG_URL, "temporary");
            // 第二次保存：返回最终对象
            ShortLink finalLink = createShortLinkWithId(1L, VALID_LONG_URL, Base62Converter.encode(1L));

            when(shortLinkRepository.save(any(ShortLink.class)))
                    .thenReturn(savedWithId)    // 第一次调用 save 时，返回这个
                    .thenReturn(finalLink);     // 第二次调用 save 时，返回这个
            // TODO: When - 执行被测试的方法
            // ShortLink result = ...
            ShortLink result = shortLinkService.createShortLink(VALID_LONG_URL);
            // TODO: Then - 验证结果和方法调用
            // 验证返回对象的属性
            // 验证Repository方法的调用次数
            // 使用ArgumentCaptor验证保存的数据
            assertThat(result).isNotNull();
            assertThat(result.getShortCode()).isEqualTo(Base62Converter.encode(1L));

            //创建一个 ShortLink 类型的参数捕获器
            ArgumentCaptor<ShortLink> linkCaptor = ArgumentCaptor.forClass(ShortLink.class);

            // 验证 save 方法被调用了2次，并捕获传入的参数
            verify(shortLinkRepository, times(2)).save(linkCaptor.capture());

            // 获取所有被捕获的参数
            List<ShortLink> capturedLinks = linkCaptor.getAllValues();

            // 对捕获到的参数进行精确断言
            assertThat(capturedLinks).hasSize(2); // 确保捕获到了2个参数

            ShortLink firstSaveArgument = capturedLinks.get(0);
            assertThat(firstSaveArgument.getLongUrl()).isEqualTo(VALID_LONG_URL);
            assertThat(firstSaveArgument.getShortCode()).isEqualTo("temporary"); // 验证第一次保存时，短码是临时的

            ShortLink secondSaveArgument = capturedLinks.get(1);
            assertThat(secondSaveArgument.getId()).isEqualTo(1L); // 验证第二次保存时，ID已经存在
            assertThat(secondSaveArgument.getShortCode()).isEqualTo(Base62Converter.encode(1L)); // 验证第二次保存时，短码已被更新
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
            when(shortLinkRepository.findByLongUrl(VALID_LONG_URL)).thenReturn(Optional.of(testShortLink));
            ShortLink result = shortLinkService.createShortLink(VALID_LONG_URL);
            // 验证返回值
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testShortLink.getId());
            assertThat(result.getShortCode()).isEqualTo(testShortLink.getShortCode());
            // 验证交互
            verify(shortLinkRepository, times(1)).findByLongUrl(VALID_LONG_URL);
            verify(shortLinkRepository, never()).save(any(ShortLink.class));
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
            when(shortLinkRepository.existsByShortCode(CUSTOM_ALIAS)).thenReturn(false);

            ShortLink customLink = new ShortLink(VALID_LONG_URL, CUSTOM_ALIAS);
            customLink.setIsCustomAlias(true);
            // 手动设置id
            ReflectionTestUtils.setField(customLink, "id", 2L);
            when(shortLinkRepository.save(any(ShortLink.class))).thenReturn(customLink);

            ShortLink result = shortLinkService.createCustomShortLink(VALID_LONG_URL, CUSTOM_ALIAS, "A custom link description");
            // 验证结果
            assertThat(result).isNotNull();
            assertThat(result.getLongUrl()).isEqualTo(VALID_LONG_URL);
            assertThat(result.getShortCode()).isEqualTo(CUSTOM_ALIAS);
            assertThat(result.getIsCustomAlias()).isTrue();
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
            when(shortLinkRepository.findByShortCode(VALID_SHORT_CODE))
                    .thenReturn(Optional.of(testShortLink));
            // 2. Mock Repository.save() 更新访问计数
            when(shortLinkRepository.save(any(ShortLink.class))).thenAnswer(invocation -> invocation.getArgument(0));
            // 3. 调用Service.getLongUrl()
            Optional<String> result = shortLinkService.getLongUrl(VALID_SHORT_CODE);
            // 4. 验证返回的Optional包含正确的URL
            assertThat(result).isPresent(); // 断言 Optional 不是空的
            assertThat(result).contains(VALID_LONG_URL); // 断言 Optional 包含期望的长链接
            // 5. 验证访问计数被正确更新
            verify(shortLinkRepository, times(1)).findByShortCode(VALID_SHORT_CODE);
            verify(shortLinkRepository, times(1)).save(any(ShortLink.class));
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
            String nonExistentCode = "non-existent-code";
            when(shortLinkRepository.findByShortCode(nonExistentCode))
                    .thenReturn(Optional.empty());

            Optional<String> result = shortLinkService.getLongUrl(nonExistentCode);

            // 验证返回的是一个空的 Optional
            assertThat(result).isEmpty();

            verify(shortLinkRepository, never()).save(any(ShortLink.class));
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
            // 记录初始访问次数
            long initialAccessCount = testShortLink.getAccessCount();

            when(shortLinkRepository.findByShortCode(VALID_SHORT_CODE))
                    .thenReturn(Optional.of(testShortLink));
            when(shortLinkRepository.save(any(ShortLink.class))).thenReturn(testShortLink);

            shortLinkService.getLongUrl(VALID_SHORT_CODE);

            // 创建一个参数捕获器
            ArgumentCaptor<ShortLink> linkCaptor = ArgumentCaptor.forClass(ShortLink.class);

            // 验证 save 方法被调用，并捕获其参数
            verify(shortLinkRepository).save(linkCaptor.capture());

            // 获取被捕获的参数对象
            ShortLink capturedLink = linkCaptor.getValue();

            // 断言访问次数是否正确增加了 1
            assertThat(capturedLink.getAccessCount())
                    .as("访问次数应该增加 1")
                    .isEqualTo(initialAccessCount + 1);
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
            String availableAlias = "availableAlias";
            when(shortLinkRepository.existsByShortCode(availableAlias)).thenReturn(false);

            assertThat(shortLinkService.isShortCodeAvailable(availableAlias)).isTrue();

            String occupiedAlias = "occupiedAlias";
            when(shortLinkRepository.existsByShortCode(occupiedAlias)).thenReturn(true);

            assertThat(shortLinkService.isShortCodeAvailable(occupiedAlias)).isFalse();
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

            when(shortLinkRepository.existsByShortCode(CUSTOM_ALIAS)).thenReturn(true);

            assertThatThrownBy(() -> {
                shortLinkService.createCustomShortLink(VALID_LONG_URL, CUSTOM_ALIAS, "description");
            })
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("自定义别名已被占用");

            verify(shortLinkRepository, never()).save(any(ShortLink.class));
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
            // 场景1: 短码可用 (不存在)
            String availableCode = "newcode123";
            when(shortLinkRepository.existsByShortCode(availableCode)).thenReturn(false);
            assertThat(shortLinkService.isShortCodeAvailable(availableCode)).isTrue();

            // 场景2: 短码不可用 (已存在)
            String occupiedCode = "existingcode456";
            when(shortLinkRepository.existsByShortCode(occupiedCode)).thenReturn(true);

            assertThat(shortLinkService.isShortCodeAvailable(occupiedCode)).isFalse();
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

            LocalDateTime startTime = LocalDateTime.of(2025, 8, 1, 0, 0);
            LocalDateTime endTime = LocalDateTime.of(2025, 8, 31, 23, 59);

            List<ShortLink> mockLinks = Arrays.asList(
                    createShortLinkWithId(10L, "urlA", "linkA"),
                    createShortLinkWithId(11L, "urlB", "linkB")
            );

            when(shortLinkRepository.findByCreatedAtBetween(startTime, endTime))
                    .thenReturn(mockLinks);

            //调用被测试的方法
            List<ShortLink> result = shortLinkService.getLinksCreatedBetween(startTime, endTime);

            // 验证返回的列表
            assertThat(result)
                    .isNotNull()
                    .hasSize(2)
                    .containsExactlyInAnyOrderElementsOf(mockLinks); // 验证内容是否一致
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
            LocalDateTime startTime = LocalDateTime.of(2025, 1, 1, 0, 0);
            LocalDateTime endTime = LocalDateTime.of(2025, 1, 31, 23, 59);

            when(shortLinkRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            List<ShortLink> result = shortLinkService.getLinksCreatedBetween(startTime, endTime);

            assertThat(result)
                    .isNotNull()
                    .isEmpty();
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
            long totalLinks = 100L;
            long totalAccess = 5000L;
            long customAliases = 10L;

            when(shortLinkRepository.count()).thenReturn(totalLinks);
            when(shortLinkRepository.getTotalAccessCount()).thenReturn(totalAccess);
            when(shortLinkRepository.countCustomAliases()).thenReturn(customAliases);

            ShortLinkService.SystemStats result = shortLinkService.getSystemStats();

            assertThat(result).isNotNull();
            assertThat(result.getTotalLinks()).isEqualTo(totalLinks);
            assertThat(result.getTotalAccess()).isEqualTo(totalAccess);
            assertThat(result.getCustomAliases()).isEqualTo(customAliases);
        }
    }

    /**
     * 【辅助方法】创建带ID的ShortLink对象
     * 
     * TODO: 实现这个辅助方法，用于创建测试数据
     * 提示：使用ReflectionTestUtils.setField()设置私有字段ID
     */
    private ShortLink createShortLinkWithId(Long id, String longUrl, String shortCode) {
        ShortLink link = new ShortLink(longUrl, shortCode);
        ReflectionTestUtils.setField(link, "id", id);
        return link;
    }
}
