package com.sunlight.linker.exercises.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunlight.linker.application.ShortLinkService;
import com.sunlight.linker.domain.ShortLink;
import com.sunlight.linker.web.ShortLinkController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 【练习4】ShortLinkController API测试练习
 * 
 * 【第四阶段测试实践 - API Endpoint测试】
 * 
 * 学习目标：
 * ✅ 掌握Spring Boot Web层测试方法
 * ✅ 学会使用MockMvc进行HTTP接口测试
 * ✅ 掌握JSON请求响应的测试技巧
 * ✅ 理解HTTP状态码和响应格式验证
 * 
 * 【练习说明】：
 * 这是API层测试，专注于HTTP接口的正确性
 * 重点关注：HTTP协议、JSON序列化、状态码、错误处理
 * 
 * 【评分标准】：
 * - Web层测试配置 (20分)
 * - HTTP请求响应测试 (35分)
 * - JSON格式验证 (25分)
 * - 异常和边界情况处理 (20分)
 */
@WebMvcTest(ShortLinkController.class)
@ActiveProfiles("test")
@DisplayName("【练习】ShortLinkController API测试")
class ShortLinkControllerExercise {
    
    /**
     * MockMvc - Web测试核心工具
     * 
     * TODO: 注入MockMvc实例
     * 提示：使用@Autowired注解
     */
    // TODO: 注入MockMvc
    @Autowired
    private MockMvc mockMvc;
    
    /**
     * Mock的Service依赖
     * 
     * TODO: 创建Service的Mock实例
     * 提示：使用@MockBean注解
     */
    // TODO: 创建ShortLinkService的Mock
    @MockBean
    private ShortLinkService shortLinkService;
    
    /**
     * JSON序列化工具
     * 
     * TODO: 注入ObjectMapper用于JSON操作
     */
    // TODO: 注入ObjectMapper
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 测试数据准备
     */
    private static final String API_BASE_PATH = "/api/v1/links";
    private static final String VALID_LONG_URL = "https://www.example.com/very/long/path";
    private static final String VALID_SHORT_CODE = "abc123";
    
    private ShortLink testShortLink;
    
    /**
     * 每个测试前的准备工作
     * 
     * TODO: 完成测试数据初始化
     */
    @BeforeEach
    void setUp() {
        // TODO: 创建测试用的ShortLink对象
        testShortLink = createTestShortLink();
        // TODO: 重置Mock对象状态
        reset(shortLinkService);
    }
    
    /**
     * 创建短链接API测试组
     */
    @Nested
    @DisplayName("创建短链接API测试练习")
    class CreateShortLinkTests {

        /**
         * 【练习4.1】成功创建短链接API测试
         * 
         * TODO: 测试POST /api/v1/links 成功创建短链接
         * 
         * 测试要点：
         * 1. 发送POST请求，Content-Type为application/json
         * 2. 请求体包含longUrl字段
         * 3. 验证响应状态码为201 Created
         * 4. 验证响应体JSON格式和内容
         * 5. 验证响应头Content-Type为application/json
         */
        @Test
        @DisplayName("成功创建短链接应该返回201和短链接信息")
        void shouldCreateShortLinkSuccessfully() throws Exception {
            // TODO: Given - 准备Mock数据
            when(shortLinkService.createShortLink(anyString())).thenReturn(testShortLink);
            // TODO: 创建请求体JSON
            String requestJson = "{\"longUrl\":\"" + VALID_LONG_URL + "\"}";
            // TODO: When & Then - 执行请求并验证响应
             mockMvc.perform(post(API_BASE_PATH)
                     .contentType(MediaType.APPLICATION_JSON)
                     .content(requestJson))
                 .andExpect(status().isCreated())
                 .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                 .andExpect(jsonPath("$.shortCode").value(VALID_SHORT_CODE))
                 .andExpect(jsonPath("$.longUrl").value(VALID_LONG_URL))
                 .andExpect(jsonPath("$.isCustomAlias").value(false));
        }

        /**
         * 【练习4.2】自定义别名创建测试
         * 
         * TODO: 测试带自定义别名的短链接创建
         */
        @Test
        @DisplayName("应该支持自定义别名创建")
        void shouldCreateShortLinkWithCustomAlias() throws Exception {
            // TODO: 实现自定义别名创建测试
            // 请求体应包含longUrl和customAlias字段
            final String CUSTOM_ALIAS = "my-custom-link";
            ShortLink customShortLink = new ShortLink(VALID_LONG_URL, CUSTOM_ALIAS, "A custom link");
            customShortLink.setIsCustomAlias(true);
            // 模拟数据库已为其生成ID和时间戳
            ReflectionTestUtils.setField(customShortLink, "id", 2L);
            ReflectionTestUtils.setField(customShortLink, "createdAt", LocalDateTime.now());

            when(shortLinkService.createCustomShortLink(eq(VALID_LONG_URL), eq(CUSTOM_ALIAS), any()))
                    .thenReturn(customShortLink);

            String requestJson = "{\"longUrl\":\"" + VALID_LONG_URL + "\", \"customAlias\":\"" + CUSTOM_ALIAS + "\"}";

            mockMvc.perform(post(API_BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.shortCode").value(CUSTOM_ALIAS)) // 验证返回的 shortCode 是自定义的别名
                    .andExpect(jsonPath("$.isCustomAlias").value(true));

            // 验证调用了正确的 service 方法
            verify(shortLinkService, times(1)).createCustomShortLink(eq(VALID_LONG_URL), eq(CUSTOM_ALIAS), any());
            // 确保创建普通链接的方法没有被调用
            verify(shortLinkService, never()).createShortLink(anyString());
        }

        /**
         * 【练习4.3】无效请求体测试
         * 
         * TODO: 测试各种无效请求的处理
         */
        @Test
        @DisplayName("无效请求应该返回400错误")
        void shouldReturn400ForInvalidRequests() throws Exception {
            // 因为缺少必需的 'longUrl' 字段，违反了 @NotBlank 约束，应返回 400
            mockMvc.perform(post(API_BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest()); // 验证状态码为 400

            // 发送一个 'longUrl' 字段为空字符串的请求
            // 应返回 400
            String emptyUrlJson = "{\"longUrl\":\"\"}";
            mockMvc.perform(post(API_BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(emptyUrlJson))
                    .andExpect(status().isBadRequest());

            // 'longUrl' 字段的值不是一个有效的URL格式
            // 预期：这将在 Service 层被验证，模拟 Service 层抛出异常来测试 Controller 的反应
            String invalidUrl = "this-is-not-a-valid-url";
            when(shortLinkService.createShortLink(invalidUrl))
                    .thenThrow(new IllegalArgumentException("无效的URL格式"));

            String invalidUrlJson = "{\"longUrl\":\"" + invalidUrl + "\"}";
            mockMvc.perform(post(API_BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidUrlJson))
                    .andExpect(status().isBadRequest()); // 验证 Controller 能正确捕获 Service 异常并返回 400
        }

        /**
         * 【练习4.4】Content-Type验证测试
         * 
         * TODO: 测试不正确的Content-Type处理
         */
        @Test
        @DisplayName("错误的Content-Type应该返回415错误")
        void shouldReturn415ForWrongContentType() throws Exception {
            // TODO: 实现Content-Type测试
            // 发送text/plain类型的请求
            // 将 Content-Type 设置为 'text/plain'
            mockMvc.perform(post(API_BASE_PATH)
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("this is just plain text")) // 发送一些文本内容
                    .andExpect(status().isUnsupportedMediaType()); // 验证：HTTP 响应状态码必须是 415
        }
    }

    /**
     * 获取短链接信息API测试组
     */
    @Nested
    @DisplayName("获取短链接信息测试练习")
    class GetShortLinkInfoTests {

        /**
         * 【练习4.5】成功获取短链接信息
         * 
         * TODO: 测试GET /api/v1/links/{shortCode} 成功获取信息
         */
        @Test
        @DisplayName("应该成功获取短链接信息")
        void shouldGetShortLinkInfoSuccessfully() throws Exception {
            // TODO: Given - Mock Service返回短链接信息
            when(shortLinkService.getShortLinkInfo(VALID_SHORT_CODE))
                    .thenReturn(Optional.of(testShortLink));
            // TODO: When & Then - 执行GET请求并验证
            // 验证状态码200
            // 验证JSON响应格式
            // 验证所有字段值
            mockMvc.perform(get(API_BASE_PATH + "/" + VALID_SHORT_CODE))
                    .andExpect(status().isOk()) // HTTP 状态码是 200 OK
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON)) //响应内容类型为 JSON
                    .andExpect(jsonPath("$.id").value(testShortLink.getId()))
                    .andExpect(jsonPath("$.longUrl").value(testShortLink.getLongUrl()))
                    .andExpect(jsonPath("$.shortCode").value(testShortLink.getShortCode()));
        }

        /**
         * 【练习4.6】短码不存在测试
         * 
         * TODO: 测试查询不存在的短码
         */
        @Test
        @DisplayName("不存在的短码应该返回404")
        void shouldReturn404ForNonExistentShortCode() throws Exception {
            // TODO: 实现404测试
            final String nonExistentCode = "nonexistentcode";
            // Mock Service返回null
            when(shortLinkService.getShortLinkInfo(nonExistentCode))
                    .thenReturn(Optional.empty());
            // 验证404状态码
            mockMvc.perform(get(API_BASE_PATH + "/" + nonExistentCode))
                    .andExpect(status().isNotFound());
        }
    }

    /**
     * 热门链接API测试组
     */
    @Nested
    @DisplayName("热门链接API测试练习")
    class HotLinksTests {

        /**
         * 【练习4.7】获取热门链接列表
         * 
         * TODO: 测试GET /api/v1/links/hot 获取热门链接
         */
        @Test
        @DisplayName("应该返回热门链接列表")
        void shouldGetHotLinks() throws Exception {
            // TODO: Given - 准备热门链接列表数据
            // List<ShortLink> hotLinks = Arrays.asList(...);
            ShortLink hotLink2 = new ShortLink("https://www.another-example.com", "hot456");
            ReflectionTestUtils.setField(hotLink2, "id", 2L);
            hotLink2.setAccessCount(2000L);
            List<ShortLink> hotLinks = Arrays.asList(testShortLink, hotLink2);
            // TODO: When & Then - 测试热门链接API
            when(shortLinkService.getHotLinks(anyLong())).thenReturn(hotLinks);
            // 验证状态码200
            // 验证返回列表格式
            // 验证列表元素内容
            mockMvc.perform(get(API_BASE_PATH + "/hot"))
                    .andExpect(status().isOk()) // 状态码为 200 OK
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON)) // 内容类型为 JSON
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].shortCode").value(testShortLink.getShortCode()))
                    .andExpect(jsonPath("$[0].longUrl").value(testShortLink.getLongUrl()))
                    .andExpect(jsonPath("$[1].shortCode").value(hotLink2.getShortCode()));
        }

        /**
         * 【练习4.8】热门链接参数测试
         * 
         * TODO: 测试minAccessCount参数的处理
         */
        @Test
        @DisplayName("应该正确处理minAccessCount参数")
        void shouldHandleMinAccessCountParameter() throws Exception {
            // TODO: 实现参数处理测试
            // 测试带参数的请求：/api/v1/links/hot?minAccessCount=10
            long minAccessCount = 10L;
            when(shortLinkService.getHotLinks(minAccessCount)).thenReturn(Collections.emptyList());

            mockMvc.perform(get(API_BASE_PATH + "/hot")
                            .param("minAccessCount", String.valueOf(minAccessCount)))
                    .andExpect(status().isOk()); // 验证状态码为 200 OK

            verify(shortLinkService, times(1)).getHotLinks(minAccessCount);
        }
    }

    /**
     * 时间范围查询API测试组
     */
    @Nested
    @DisplayName("时间范围查询测试练习")
    class CreatedBetweenTests {

        /**
         * 【练习4.9】时间范围查询测试
         * 
         * TODO: 测试GET /api/v1/links/created-between 时间范围查询
         */
        @Test
        @DisplayName("应该正确处理时间范围查询")
        void shouldGetLinksInTimeRange() throws Exception {
            // TODO: 实现时间范围查询测试
            // URL: /api/v1/links/created-between?start=2023-01-01T00:00:00&end=2023-12-31T23:59:59
            String startTimeStr = "2023-01-01T00:00:00";
            String endTimeStr = "2023-12-31T23:59:59";

            LocalDateTime startTime = LocalDateTime.parse(startTimeStr);
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr);

            when(shortLinkService.getLinksCreatedBetween(startTime, endTime))
                    .thenReturn(Collections.singletonList(testShortLink));

            mockMvc.perform(get(API_BASE_PATH + "/created-between")
                            .param("start", startTimeStr)
                            .param("end", endTimeStr))
                    .andExpect(status().isOk()) // 验证状态码 200 OK
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(1))) // 验证返回的数组包含一个元素
                    .andExpect(jsonPath("$[0].shortCode").value(testShortLink.getShortCode()));

            // 确认 service 方法是被使用正确的 LocalDateTime 对象调用的
            verify(shortLinkService, times(1)).getLinksCreatedBetween(startTime, endTime);
        }

        /**
         * 【练习4.10】无效时间格式测试
         * 
         * TODO: 测试无效的时间格式参数
         */
        @Test
        @DisplayName("无效时间格式应该返回400错误")
        void shouldReturn400ForInvalidTimeFormat() throws Exception {
            // TODO: 实现无效时间格式测试
            mockMvc.perform(get(API_BASE_PATH + "/created-between")
                            .param("start", "not-a-valid-date") // 提供无效的日期格式
                            .param("end", "2023-12-31T23:59:59"))
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * 短码可用性检查API测试组
     */
    @Nested
    @DisplayName("短码可用性检查测试练习")
    class CheckAvailabilityTests {

        /**
         * 【练习4.11】可用短码检查
         * 
         * TODO: 测试GET /api/v1/links/check-availability 可用性检查
         */
        @Test
        @DisplayName("可用短码应该返回true")
        void shouldReturnTrueForAvailableShortCode() throws Exception {
            // TODO: 实现可用性检查测试
            // Mock Service返回true
            // 验证JSON响应：{"available": true}
            String availableCode = "availablecode";
            when(shortLinkService.isShortCodeAvailable(availableCode)).thenReturn(true);

            mockMvc.perform(get(API_BASE_PATH + "/check-availability")
                            .param("shortCode", availableCode))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.available").value(true))
                    .andExpect(jsonPath("$.shortCode").value(availableCode));
        }

        /**
         * 【练习4.12】已占用短码检查
         * 
         * TODO: 测试已被占用的短码检查
         */
        @Test
        @DisplayName("已占用短码应该返回false")
        void shouldReturnFalseForOccupiedShortCode() throws Exception {
            // TODO: 实现已占用短码测试
            String occupiedCode = "occupiedcode";
            when(shortLinkService.isShortCodeAvailable(occupiedCode)).thenReturn(false);

            mockMvc.perform(get(API_BASE_PATH + "/check-availability")
                            .param("shortCode", occupiedCode))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.available").value(false));
        }

        /**
         * 【练习4.13】缺少参数测试
         * 
         * TODO: 测试缺少shortCode参数的情况
         */
        @Test
        @DisplayName("缺少shortCode参数应该返回400错误")
        void shouldReturn400ForMissingParameter() throws Exception {
            // TODO: 实现参数缺失测试
            mockMvc.perform(get(API_BASE_PATH + "/check-availability"))
                    .andExpect(status().isBadRequest());
        }
    }

    /**
     * 系统统计API测试组
     */
    @Nested
    @DisplayName("系统统计API测试练习")
    class SystemStatsTests {

        /**
         * 【练习4.14】系统统计信息测试
         * 
         * TODO: 测试GET /api/v1/links/stats 系统统计API
         */
        @Test
        @DisplayName("应该返回系统统计信息")
        void shouldReturnSystemStats() throws Exception {
            // TODO: 实现系统统计测试
            // 准备统计数据Mock
            // 验证JSON响应格式
            ShortLinkService.SystemStats stats = new ShortLinkService.SystemStats(100L, 5000L, 20L);
            when(shortLinkService.getSystemStats()).thenReturn(stats);

            // When & Then
            mockMvc.perform(get("/api/v1/stats"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.totalLinks").value(100))
                    .andExpect(jsonPath("$.totalAccess").value(5000))
                    .andExpect(jsonPath("$.customAliases").value(20));
        }
    }

    /**
     * HTTP方法和头部测试组
     */
    @Nested
    @DisplayName("HTTP协议细节测试练习")
    class HttpProtocolTests {

        /**
         * 【练习4.15】OPTIONS请求测试
         * 
         * TODO: 测试CORS预检请求的处理
         */
        @Test
        @DisplayName("应该正确处理OPTIONS预检请求")
        void shouldHandleOptionsRequest() throws Exception {
            // TODO: 实现OPTIONS请求测试
            // 这是CORS相关的高级练习
            mockMvc.perform(options(API_BASE_PATH) // 使用 options() 方法模拟 OPTIONS 请求
                            .header("Origin", "https://another-domain.com") // 模拟请求来源域
                            .header("Access-Control-Request-Method", "POST") // 询问是否允许POST方法
                            .header("Access-Control-Request-Headers", "Content-Type")) // 询问是否允许Content-Type头
                    .andExpect(status().isOk()) // 预检请求应该返回 200 OK
                    .andExpect(header().string("Access-Control-Allow-Origin", "*"))
                    .andExpect(header().string("Access-Control-Allow-Methods", containsString("POST")))
                    .andExpect(header().string("Access-Control-Allow-Headers", containsString("Content-Type")));
        }

        /**
         * 【练习4.16】Accept头测试
         * 
         * TODO: 测试不同Accept头的处理
         */
        @Test
        @DisplayName("应该正确处理Accept头")
        void shouldHandleAcceptHeader() throws Exception {
            // TODO: 实现Accept头测试
            // 测试application/json和text/plain等不同Accept值
            // 场景1: 客户端明确表示接受JSON
            when(shortLinkService.getShortLinkInfo(VALID_SHORT_CODE)).thenReturn(Optional.of(testShortLink));
            mockMvc.perform(get(API_BASE_PATH + "/" + VALID_SHORT_CODE)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // 场景2: 客户端表示只接受XML，而我们的API不支持
            mockMvc.perform(get(API_BASE_PATH + "/" + VALID_SHORT_CODE)
                            .accept(MediaType.APPLICATION_XML))
                    .andExpect(status().isNotAcceptable());
        }
    }

    /**
     * 异常处理测试组
     */
    @Nested
    @DisplayName("异常处理测试练习")
    class ExceptionHandlingTests {

        /**
         * 【练习4.17】Service异常处理测试
         * 
         * TODO: 测试Service层抛出异常时的处理
         */
        @Test
        @DisplayName("Service异常应该返回适当的错误响应")
        void shouldHandleServiceExceptions() throws Exception {
            // TODO: 实现异常处理测试
            // Mock Service抛出异常
            // 验证错误响应格式
            when(shortLinkService.createShortLink(anyString()))
                    .thenThrow(new IllegalArgumentException("数据库宕机"));

            String requestJson = "{\"longUrl\":\"" + VALID_LONG_URL + "\"}";

            mockMvc.perform(post(API_BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());
        }

        /**
         * 【练习4.18】参数校验异常测试
         * 
         * TODO: 测试参数校验失败的处理
         */
        @Test
        @DisplayName("参数校验失败应该返回400错误")
        void shouldHandleValidationErrors() throws Exception {
            // TODO: 实现参数校验测试
            // 发送无效参数
            // 验证400错误和错误信息
            String longUrl = "https://".repeat(200); // 构造一个超长URL
            String longAlias = "a".repeat(30); // 构造一个超长别名
            String requestJson = "{\"longUrl\":\"" + longUrl + "\", \"customAlias\":\"" + longAlias + "\"}";

            mockMvc.perform(post(API_BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest()); // 预期 @Size 校验会失败，返回 400
        }
    }

    /**
     * 【辅助方法】创建测试用的ShortLink对象
     * 
     * TODO: 实现这个辅助方法
     */
    private ShortLink createTestShortLink() {
        // TODO: 创建并返回测试用的ShortLink对象
        ShortLink link = new ShortLink(VALID_LONG_URL, VALID_SHORT_CODE);
        // 在测试中，我们经常需要模拟一个已经存在于数据库中的对象，所以手动设置ID和时间戳
        try {
            Field idField = ShortLink.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(link, 1L);

            Field createdAtField = ShortLink.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(link, LocalDateTime.now());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return link;
    }

    /**
     * 【辅助方法】将对象转换为JSON字符串
     * 
     * TODO: 实现JSON序列化辅助方法
     */
    private String asJsonString(Object obj) throws Exception {
        // TODO: 使用ObjectMapper将对象转换为JSON字符串
        // return objectMapper.writeValueAsString(obj);
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
