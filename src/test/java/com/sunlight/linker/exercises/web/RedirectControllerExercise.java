package com.sunlight.linker.exercises.web;

import com.sunlight.linker.application.ShortLinkService;
import com.sunlight.linker.web.RedirectController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 【练习5】RedirectController 重定向测试练习
 * 
 * 【重定向功能测试 - HTTP 302重定向】
 * 
 * 学习目标：
 * ✅ 掌握HTTP重定向的测试方法
 * ✅ 学会验证Location响应头
 * ✅ 理解302状态码的使用场景
 * ✅ 掌握URL编码和安全性测试
 * 
 * 【练习说明】：
 * 这是短链接服务的核心功能测试
 * 重点关注：HTTP重定向、Location头、状态码、异常处理
 * 
 * 【评分标准】：
 * - HTTP重定向测试 (40分)
 * - 异常情况处理 (30分)
 * - 安全性验证 (20分)
 * - 边界条件测试 (10分)
 */
@WebMvcTest(RedirectController.class)
@ActiveProfiles("test")
@DisplayName("【练习】RedirectController 重定向测试")
class RedirectControllerExercise {
    
    /**
     * TODO: 注入MockMvc实例
     */
    // TODO: 使用@Autowired注入MockMvc
    @Autowired
    private MockMvc mockMvc;
    
    /**
     * TODO: 创建ShortLinkService的Mock
     */
    // TODO: 使用@MockBean创建Mock实例
    @MockBean
    private ShortLinkService shortLinkService;
    
    /**
     * 测试数据常量
     */
    private static final String VALID_SHORT_CODE = "abc123";
    private static final String VALID_LONG_URL = "https://www.example.com/target-page";
    private static final String REDIRECT_PATH = "/s/";
    
    /**
     * 每个测试前的准备工作
     * 
     * TODO: 重置Mock对象状态
     */
    @BeforeEach
    void setUp() {
        // TODO: 重置shortLinkService的Mock状态
        reset(shortLinkService);
    }
    
    /**
     * 【练习5.1】成功重定向测试
     * 
     * TODO: 测试GET /s/{shortCode} 成功重定向到长链接
     * 
     * 测试要点：
     * 1. Mock Service返回有效的长链接
     * 2. 发送GET请求到 /s/abc123
     * 3. 验证返回302 Found状态码
     * 4. 验证Location头包含正确的长链接
     * 5. 验证Service方法被正确调用
     */
    @Test
    @DisplayName("有效短码应该重定向到长链接")
    void shouldRedirectToLongUrlSuccessfully() throws Exception {
        // TODO: Given - Mock Service返回长链接
         when(shortLinkService.getLongUrl(VALID_SHORT_CODE))
             .thenReturn(Optional.of(VALID_LONG_URL));
        
         // TODO: When & Then - 执行请求并验证重定向
         mockMvc.perform(get(REDIRECT_PATH + VALID_SHORT_CODE))
             .andExpect(status().isFound())  // 302状态码
             .andExpect(header().string("Location", VALID_LONG_URL));
        
         // TODO: 验证Service方法调用
         verify(shortLinkService, times(1)).getLongUrl(VALID_SHORT_CODE);
    }
    
    /**
     * 【练习5.2】短码不存在测试
     * 
     * TODO: 测试不存在的短码应该返回404错误
     */
    @Test
    @DisplayName("不存在的短码应该返回404")
    void shouldReturn404ForNonExistentShortCode() throws Exception {
        // TODO: Given - Mock Service返回空Optional
        when(shortLinkService.getLongUrl(anyString()))
                .thenReturn(Optional.empty());
        // TODO: When & Then - 验证404响应
        // 验证状态码为404 Not Found
        // 验证没有Location头
        mockMvc.perform(get(REDIRECT_PATH + "nonexistentcode"))
                .andExpect(status().isNotFound())
                .andExpect(header().doesNotExist("Location"));
    }
    
    /**
     * 【练习5.3】各种短码格式测试
     * 
     * TODO: 测试不同格式的短码处理
     */
    @Test
    @DisplayName("应该处理各种短码格式")
    void shouldHandleVariousShortCodeFormats() throws Exception {
        // TODO: 测试不同的短码格式
        // 1. 纯数字短码 "123"
        // 2. 字母数字混合 "a1b2c3" 
        // 3. 单字符短码 "a"
        // 4. 最大长度短码
        
        // 对每种格式都要：
        // 1. Mock Service返回对应的长链接
        // 2. 发送请求
        // 3. 验证重定向成功
        String numericCode = "12345";
        String numericUrl = "https://example.com/numeric";
        when(shortLinkService.getLongUrl(numericCode)).thenReturn(Optional.of(numericUrl));

        mockMvc.perform(get(REDIRECT_PATH + numericCode))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", numericUrl));

        // 场景2: 测试字母数字混合短码
        String alphanumericCode = "a1B2c3D4";
        String alphanumericUrl = "https://example.com/alphanumeric";
        when(shortLinkService.getLongUrl(alphanumericCode)).thenReturn(Optional.of(alphanumericUrl));

        mockMvc.perform(get(REDIRECT_PATH + alphanumericCode))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", alphanumericUrl));

        // 场景3: 测试单字符短码
        String singleChar = "Z";
        String singleCharUrl = "https://example.com/singlechar";
        when(shortLinkService.getLongUrl(singleChar)).thenReturn(Optional.of(singleCharUrl));

        mockMvc.perform(get(REDIRECT_PATH + singleChar))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", singleCharUrl));

        // 场景4: (新增) 测试最大长度短码 (20个字符)
        String maxLengthCode = "AbcDef123GhijKl456Mn"; // 一个20个字符的字符串
        String maxLengthUrl = "https://example.com/max-length";
        when(shortLinkService.getLongUrl(maxLengthCode)).thenReturn(Optional.of(maxLengthUrl));

        mockMvc.perform(get(REDIRECT_PATH + maxLengthCode))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", maxLengthUrl));
    }
    
    /**
     * 【练习5.4】特殊字符短码测试
     * 
     * TODO: 测试包含特殊字符的短码（安全性测试）
     */
    @Test
    @DisplayName("应该安全处理特殊字符短码")
    void shouldHandleSpecialCharactersSecurely() throws Exception {
        // TODO: 测试包含特殊字符的短码
        // 这些短码应该被正确处理或返回适当错误
        
        // 测试用例：
        // - 包含空格的短码
        // - 包含特殊符号的短码  
        // - 包含URL编码字符的短码
        // 模拟对于任何无效/恶意的输入，服务层都返回空
        when(shortLinkService.getLongUrl(anyString())).thenReturn(Optional.empty());

        // 测试用例1: 包含空格的短码
        mockMvc.perform(get(REDIRECT_PATH + "invalid code"))
                .andExpect(status().isNotFound());

        // 测试用例2: 尝试路径遍历的短码
        // Spring Boot本身有安全机制会处理，但我们仍要确保应用层行为符合预期
        mockMvc.perform(get(REDIRECT_PATH + "../../etc/passwd"))
                .andExpect(status().isNotFound());

        // 测试用例3: 包含特殊符号的短码
        mockMvc.perform(get(REDIRECT_PATH + "test@123!"))
                .andExpect(status().isNotFound());

        // 测试用例4: 包含URL编码字符的短码（非Base62）
        mockMvc.perform(get(REDIRECT_PATH + "test%20space")) // %20是空格的URL编码
                .andExpect(status().isNotFound());
    }
    
    /**
     * 【练习5.5】超长短码测试
     * 
     * TODO: 测试异常长度的短码处理
     */
    @Test
    @DisplayName("应该处理超长短码")
    void shouldHandleVeryLongShortCode() throws Exception {
        // TODO: 创建一个很长的短码（如100个字符）
        // 验证系统能够正确处理而不会出现异常
        String veryLongCode = "a".repeat(100);

        when(shortLinkService.getLongUrl(veryLongCode))
                .thenReturn(Optional.empty());

        // 期望控制器能够优雅地处理这个超长输入，并返回 404
        mockMvc.perform(get(REDIRECT_PATH + veryLongCode))
                .andExpect(status().isNotFound());
    }
    
    /**
     * 【练习5.6】Service异常处理测试
     * 
     * TODO: 测试Service层抛出异常时的处理
     */
    @Test
    @DisplayName("Service异常应该被适当处理")
    void shouldHandleServiceExceptions() throws Exception {
        // TODO: Mock Service抛出RuntimeException
        // when(shortLinkService.getLongUrl(anyString()))
        //     .thenThrow(new RuntimeException("数据库连接失败"));
        
        // TODO: 验证异常被正确处理
        when(shortLinkService.getLongUrl(anyString()))
                .thenThrow(new RuntimeException("数据库连接失败"));

        mockMvc.perform(get(REDIRECT_PATH + "anycode"))
                .andExpect(status().isInternalServerError());
    }
    
    /**
     * 【练习5.7】重定向URL安全性测试
     * 
     * TODO: 测试重定向URL的安全性检查
     */
    @Test
    @DisplayName("应该安全处理重定向URL")
    void shouldHandleRedirectUrlsSecurely() throws Exception {
        // TODO: 测试各种可能存在安全风险的URL
        // 1. JavaScript伪协议：javascript:alert('xss')
        // 2. 数据协议：data:text/html,<script>alert('xss')</script>
        // 3. 文件协议：file:///etc/passwd
        
        // 注意：这个练习需要根据实际的安全策略调整
        // 可能需要在Service层或Controller层添加URL验证
        // 场景1: 测试 JavaScript 伪协议 (XSS风险)
        String xssUrl = "javascript:alert('xss')";
        String xssCode = "xss-code";
        when(shortLinkService.getLongUrl(xssCode)).thenReturn(Optional.of(xssUrl));

        mockMvc.perform(get(REDIRECT_PATH + xssCode))
                .andExpect(status().isFound()) // 控制器仍然会发出302重定向
                .andExpect(header().string("Location", xssUrl)); // Location头包含了恶意脚本

        // 场景2: 测试 data: 协议 (XSS风险)
        String dataProtocolUrl = "data:text/html,<script>alert('xss')</script>";
        String dataProtocolCode = "data-protocol-code";
        when(shortLinkService.getLongUrl(dataProtocolCode)).thenReturn(Optional.of(dataProtocolUrl));

        mockMvc.perform(get(REDIRECT_PATH + dataProtocolCode))
                .andExpect(status().isInternalServerError());

        // 场景3: 测试 file: 协议 (本地文件泄露风险)
        String fileProtocolUrl = "file:///etc/passwd";
        String fileProtocolCode = "file-protocol-code";
        when(shortLinkService.getLongUrl(fileProtocolCode)).thenReturn(Optional.of(fileProtocolUrl));

        mockMvc.perform(get(REDIRECT_PATH + fileProtocolCode))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", fileProtocolUrl));
    }
    
    /**
     * 【练习5.8】访问计数增加验证
     * 
     * TODO: 验证成功重定向时访问计数是否正确增加
     */
    @Test
    @DisplayName("成功重定向应该触发访问计数增加")
    void shouldTriggerAccessCountIncrement() throws Exception {
        // TODO: 这是一个集成性测试
        // 1. Mock Service.getLongUrl()返回URL
        // 2. 发送重定向请求
        // 3. 验证Service.getLongUrl()被调用（间接验证计数逻辑）
        
        // 注意：由于我们Mock了Service，无法直接测试计数增加
        // 这里主要验证方法调用的正确性

        when(shortLinkService.getLongUrl(VALID_SHORT_CODE))
                .thenReturn(Optional.of(VALID_LONG_URL));

        mockMvc.perform(get(REDIRECT_PATH + VALID_SHORT_CODE));

        verify(shortLinkService, times(1)).getLongUrl(VALID_SHORT_CODE);
    }
    
    /**
     * 【练习5.9】URL编码处理测试
     * 
     * TODO: 测试URL编码字符的正确处理
     */
    @Test
    @DisplayName("应该正确处理URL编码的短码")
    void shouldDecodeUrlEncodedShortCode() throws Exception {
        // TODO: 测试URL编码的短码
        // 例如：空格编码为%20，+号编码为%2B等

        // Given
        String decodedShortCode = "test code"; // 这是我们期望控制器最终接收到的原始字符串
        String targetUrl = "https://example.com/decoded-url";

        // Mock Service期望接收到的是已经被解码后的原始字符串
        when(shortLinkService.getLongUrl(decodedShortCode)).thenReturn(Optional.of(targetUrl));

        // MockMvc 会自动将其正确编码为 /s/test%20code 或 /s/test+code 并发送请求。
        mockMvc.perform(get(REDIRECT_PATH + "{shortCode}", decodedShortCode))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", targetUrl));

        verify(shortLinkService, times(1)).getLongUrl(decodedShortCode);

    }
    
    /**
     * 【练习5.10】HTTP方法限制测试
     * 
     * TODO: 测试只允许GET方法访问重定向端点
     */
    @Test
    @DisplayName("应该只接受GET请求")
    void shouldOnlyAcceptGetRequests() throws Exception {
        // TODO: 测试其他HTTP方法应该返回405 Method Not Allowed
        
        // 测试POST请求
        mockMvc.perform(post(REDIRECT_PATH + VALID_SHORT_CODE))
                .andExpect(status().isMethodNotAllowed());
        
        // TODO: 测试PUT、DELETE等方法
        // 测试 PUT 请求
        mockMvc.perform(put(REDIRECT_PATH + VALID_SHORT_CODE))
                .andExpect(status().isMethodNotAllowed());

        // 测试 DELETE 请求
        mockMvc.perform(delete(REDIRECT_PATH + VALID_SHORT_CODE))
                .andExpect(status().isMethodNotAllowed());
    }
    
    /**
     * 【练习5.11】并发访问测试
     * 
     * TODO: 模拟并发访问同一短码
     * 注意：这是一个高级练习，需要多线程编程知识
     */
    @Test
    @DisplayName("应该正确处理并发访问")
    void shouldHandleConcurrentAccess() throws Exception {
        // TODO: 这是一个可选的高级练习
        // 可以使用CompletableFuture模拟并发请求
        // 验证所有请求都能正确处理
        // Given - 准备测试数据和Mock行为
        // 模拟Service层对于有效的短码总是返回同一个长链接
        when(shortLinkService.getLongUrl(VALID_SHORT_CODE))
                .thenReturn(Optional.of(VALID_LONG_URL));

        int concurrentRequests = 20; // 定义并发请求的数量
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // When - 使用CompletableFuture模拟并发发送多个HTTP请求
        for (int i = 0; i < concurrentRequests; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    // 每个异步任务都模拟一次HTTP GET请求，并验证其响应
                    mockMvc.perform(get(REDIRECT_PATH + VALID_SHORT_CODE))
                            .andExpect(status().isFound())
                            .andExpect(header().string("Location", VALID_LONG_URL));
                } catch (Exception e) {
                    // 如果在异步操作中发生异常，将其包装在运行时异常中抛出
                    // 这样 CompletableFuture.allOf(...).join() 就会捕获到并使测试失败
                    throw new RuntimeException(e);
                }
            });
            futures.add(future);
        }

        // 等待所有异步请求完成。如果有任何一个请求的断言失败，join()会抛出异常。
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Then - 验证Service方法被调用的总次数
        // 这是关键一步：确认即使在并发情况下，每个请求都正确地调用了Service层一次
        verify(shortLinkService, times(concurrentRequests)).getLongUrl(VALID_SHORT_CODE);
    }
    
    /**
     * 【练习5.12】响应头验证测试
     * 
     * TODO: 验证重定向响应的完整性
     */
    @Test
    @DisplayName("应该设置正确的响应头")
    void shouldSetCorrectResponseHeaders() throws Exception {
        // TODO: 验证重定向响应包含必要的HTTP头
        // 1. Location头（已在其他测试中验证）
        // 2. Cache-Control头（如果有的话）
        // 3. 其他相关的响应头

        when(shortLinkService.getLongUrl(VALID_SHORT_CODE))
                .thenReturn(Optional.of(VALID_LONG_URL));

        mockMvc.perform(get(REDIRECT_PATH + VALID_SHORT_CODE))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", VALID_LONG_URL))
                .andReturn();
    }
}
