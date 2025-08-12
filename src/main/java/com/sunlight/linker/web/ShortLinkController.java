package com.sunlight.linker.web;

import com.sunlight.linker.application.ShortLinkService;
import com.sunlight.linker.domain.ShortLink;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 短链接REST API控制器
 * 
 * 【第四阶段测试重点 - API Endpoint测试】
 * 
 * 这是测试金字塔中的顶层 - API测试，具有以下特点：
 * ✅ 端到端测试：验证完整的HTTP请求响应流程
 * ✅ 集成验证：验证Controller、Service、Repository的完整协作
 * ✅ HTTP协议测试：验证状态码、响应头、请求体等HTTP细节
 * ✅ 异常处理测试：验证各种异常情况的HTTP响应
 * 
 * 【测试技术栈学习重点】：
 * 1. MockMvc：Spring Boot中的API测试核心工具
 * 2. @AutoConfigureMockMvc：自动配置MockMvc环境
 * 3. HTTP方法测试：GET、POST、PUT、DELETE等
 * 4. 状态码验证：200、201、400、404、500等
 * 5. JSON序列化测试：请求体和响应体的JSON格式验证
 * 6. 请求头测试：Content-Type、Location等HTTP头的验证
 * 
 * 【RESTful API设计原则】：
 * - 资源导向：URI表示资源，HTTP方法表示操作
 * - 状态码语义：正确使用HTTP状态码表示操作结果
 * - 统一接口：标准的JSON格式和错误处理
 * - 无状态：每个请求包含完整的处理信息
 * 
 * 【业务API说明】：
 * - POST /api/v1/links：创建短链接
 * - GET /s/{shortCode}：短链接重定向
 * - GET /api/v1/links/{shortCode}：获取短链接信息
 * - GET /api/v1/stats：获取系统统计信息
 * 
 * @author 测试实践学习项目
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ShortLinkController {
    
    private final ShortLinkService shortLinkService;
    
    /**
     * 短链接服务的域名配置
     * 
     * 【配置说明】：
     * 从application.yml中读取域名配置，用于生成完整的短链接URL
     * 支持不同环境（开发、测试、生产）的域名配置
     */
    @Value("${app.short-link.domain:http://localhost:8080}")
    private String shortLinkDomain;
    
    /**
     * 构造方法依赖注入
     */
    @Autowired
    public ShortLinkController(ShortLinkService shortLinkService) {
        this.shortLinkService = shortLinkService;
    }
    
    /**
     * 创建短链接API
     * 
     * 【HTTP设计】：
     * - 方法：POST
     * - 路径：/api/v1/links
     * - 请求体：JSON格式的CreateShortLinkRequest
     * - 响应：201 Created + JSON格式的ShortLinkResponse
     * 
     * 【测试要点】：
     * - 有效请求返回201状态码和正确的响应体
     * - 无效URL格式返回400状态码和错误信息
     * - Content-Type验证：application/json
     * - 响应Location头包含新创建的短链接
     * - JSON序列化和反序列化的正确性
     * 
     * @param request 创建短链接的请求对象
     * @return 创建结果的响应实体
     */
    @PostMapping("/links")
    public ResponseEntity<ShortLinkResponse> createShortLink(@Valid @RequestBody CreateShortLinkRequest request) {
        try {
            ShortLink shortLink;
            
            // 根据是否提供自定义别名选择不同的创建方法
            if (request.getCustomAlias() != null && !request.getCustomAlias().trim().isEmpty()) {
                shortLink = shortLinkService.createCustomShortLink(
                    request.getLongUrl(), 
                    request.getCustomAlias().trim(), 
                    request.getDescription()
                );
            } else {
                shortLink = shortLinkService.createShortLink(request.getLongUrl());
            }
            
            // 构建响应对象
            ShortLinkResponse response = new ShortLinkResponse(shortLink, shortLinkDomain);
            
            // 构建Location头，指向新创建的资源
            URI location = URI.create("/api/v1/links/" + shortLink.getShortCode());
            
            return ResponseEntity.created(location).body(response);
            
        } catch (IllegalArgumentException e) {
            // 业务逻辑异常，返回400 Bad Request
            return ResponseEntity.badRequest()
                .body(new ShortLinkResponse(e.getMessage()));
        }
    }
    

    
    /**
     * 获取短链接详细信息API
     * 
     * 【HTTP设计】：
     * - 方法：GET
     * - 路径：/api/v1/links/{shortCode}
     * - 响应：200 OK + JSON格式的ShortLinkResponse
     * 
     * 【业务价值】：
     * 用于管理后台查看短链接的详细信息，不增加访问计数
     * 支持数据分析和链接管理功能
     * 
     * 【测试要点】：
     * - 存在的短码返回200状态码和详细信息
     * - 不存在的短码返回404状态码
     * - 响应JSON格式的正确性
     * - 不影响访问计数（与重定向API的区别）
     * 
     * @param shortCode 短码字符串
     * @return 短链接信息响应实体
     */
    @GetMapping("/links/{shortCode}")
    public ResponseEntity<ShortLinkResponse> getShortLinkInfo(@PathVariable String shortCode) {
        Optional<ShortLink> shortLink = shortLinkService.getShortLinkInfo(shortCode);
        
        if (shortLink.isPresent()) {
            ShortLinkResponse response = new ShortLinkResponse(shortLink.get(), shortLinkDomain);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 检查短码可用性API
     * 
     * 【HTTP设计】：
     * - 方法：GET
     * - 路径：/api/v1/links/check-availability?shortCode={shortCode}
     * - 响应：200 OK + JSON格式的可用性结果
     * 
     * 【业务价值】：
     * 用于前端实时检查用户输入的自定义别名是否可用
     * 提升用户体验，避免提交后才发现冲突
     * 
     * @param shortCode 要检查的短码
     * @return 可用性检查结果
     */
    @GetMapping("/links/check-availability")
    public ResponseEntity<AvailabilityResponse> checkShortCodeAvailability(
            @RequestParam @NotBlank(message = "短码不能为空") String shortCode) {
        
        boolean available = shortLinkService.isShortCodeAvailable(shortCode);
        AvailabilityResponse response = new AvailabilityResponse(shortCode, available);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取系统统计信息API
     * 
     * 【HTTP设计】：
     * - 方法：GET
     * - 路径：/api/v1/stats
     * - 响应：200 OK + JSON格式的统计信息
     * 
     * 【业务价值】：
     * 提供系统监控数据，用于运营分析和系统监控
     * 支持管理后台的数据展示
     * 
     * @return 系统统计信息响应实体
     */
    @GetMapping("/stats")
    public ResponseEntity<SystemStatsResponse> getSystemStats() {
        ShortLinkService.SystemStats stats = shortLinkService.getSystemStats();
        SystemStatsResponse response = new SystemStatsResponse(stats);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取热门短链接列表API
     * 
     * 【HTTP设计】：
     * - 方法：GET
     * - 路径：/api/v1/links/hot?minAccessCount={count}
     * - 响应：200 OK + JSON格式的短链接列表
     * 
     * @param minAccessCount 最小访问次数阈值，默认1000
     * @return 热门短链接列表
     */
    @GetMapping("/links/hot")
    public ResponseEntity<List<ShortLinkResponse>> getHotLinks(
            @RequestParam(defaultValue = "1000") Long minAccessCount) {
        
        List<ShortLink> hotLinks = shortLinkService.getHotLinks(minAccessCount);
        List<ShortLinkResponse> responses = hotLinks.stream()
            .map(link -> new ShortLinkResponse(link, shortLinkDomain))
            .toList();
        
        return ResponseEntity.ok(responses);
    }
    
    /**
     * 获取指定时间范围内创建的短链接API
     * 
     * 【HTTP设计】：
     * - 方法：GET  
     * - 路径：/api/v1/links/created-between?start={start}&end={end}
     * - 响应：200 OK + JSON格式的短链接列表
     * 
     * @param start 开始时间（ISO格式字符串）
     * @param end 结束时间（ISO格式字符串）
     * @return 时间范围内的短链接列表
     */
    @GetMapping("/links/created-between")
    public ResponseEntity<List<ShortLinkResponse>> getLinksCreatedBetween(
            @RequestParam String start,
            @RequestParam String end) {
        
        try {
            LocalDateTime startTime = LocalDateTime.parse(start);
            LocalDateTime endTime = LocalDateTime.parse(end);
            
            List<ShortLink> links = shortLinkService.getLinksCreatedBetween(startTime, endTime);
            List<ShortLinkResponse> responses = links.stream()
                .map(link -> new ShortLinkResponse(link, shortLinkDomain))
                .toList();
            
            return ResponseEntity.ok(responses);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // ========== 请求和响应DTO类 ==========
    
    /**
     * 创建短链接请求DTO
     * 
     * 【设计说明】：
     * 使用DTO（Data Transfer Object）模式分离API层和业务层的数据结构
     * 包含Bean Validation注解，在Controller层进行参数验证
     * 
     * 【测试要点】：
     * - JSON反序列化的正确性
     * - Bean Validation注解的验证逻辑
     * - 字段的边界值测试
     */
    public static class CreateShortLinkRequest {
        
        @NotBlank(message = "长链接不能为空")
        @Size(max = 2048, message = "长链接长度不能超过2048字符")
        private String longUrl;
        
        @Size(max = 20, message = "自定义别名长度不能超过20字符")
        private String customAlias;
        
        @Size(max = 500, message = "描述长度不能超过500字符")
        private String description;
        
        // 构造方法
        public CreateShortLinkRequest() {}
        
        public CreateShortLinkRequest(String longUrl) {
            this.longUrl = longUrl;
        }
        
        public CreateShortLinkRequest(String longUrl, String customAlias, String description) {
            this.longUrl = longUrl;
            this.customAlias = customAlias;
            this.description = description;
        }
        
        // Getter和Setter方法
        public String getLongUrl() { return longUrl; }
        public void setLongUrl(String longUrl) { this.longUrl = longUrl; }
        
        public String getCustomAlias() { return customAlias; }
        public void setCustomAlias(String customAlias) { this.customAlias = customAlias; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
    
    /**
     * 短链接响应DTO
     * 
     * 【设计说明】：
     * 封装短链接信息的响应格式，包含完整的短链接URL
     * 支持成功响应和错误响应两种格式
     */
    public static class ShortLinkResponse {
        private String id;
        private String longUrl;
        private String shortCode;
        private String shortUrl;
        private Boolean isCustomAlias;
        private Long accessCount;
        private LocalDateTime createdAt;
        private String description;
        private String error; // 错误信息字段
        
        // 成功响应构造方法
        public ShortLinkResponse(ShortLink shortLink, String domain) {
            this.id = shortLink.getId() != null ? shortLink.getId().toString() : null;
            this.longUrl = shortLink.getLongUrl();
            this.shortCode = shortLink.getShortCode();
            this.shortUrl = shortLink.generateFullShortUrl(domain);
            this.isCustomAlias = shortLink.getIsCustomAlias();
            this.accessCount = shortLink.getAccessCount();
            this.createdAt = shortLink.getCreatedAt();
            this.description = shortLink.getDescription();
        }
        
        // 错误响应构造方法
        public ShortLinkResponse(String error) {
            this.error = error;
        }
        
        // Getter方法
        public String getId() { return id; }
        public String getLongUrl() { return longUrl; }
        public String getShortCode() { return shortCode; }
        public String getShortUrl() { return shortUrl; }
        public Boolean getIsCustomAlias() { return isCustomAlias; }
        public Long getAccessCount() { return accessCount; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public String getDescription() { return description; }
        public String getError() { return error; }
    }
    
    /**
     * 可用性检查响应DTO
     */
    public static class AvailabilityResponse {
        private String shortCode;
        private Boolean available;
        private String message;
        
        public AvailabilityResponse(String shortCode, Boolean available) {
            this.shortCode = shortCode;
            this.available = available;
            this.message = available ? "短码可用" : "短码已被占用";
        }
        
        // Getter方法
        public String getShortCode() { return shortCode; }
        public Boolean getAvailable() { return available; }
        public String getMessage() { return message; }
    }
    
    /**
     * 系统统计响应DTO
     */
    public static class SystemStatsResponse {
        private Long totalLinks;
        private Long totalAccess;
        private Long customAliases;
        private LocalDateTime generatedAt;
        
        public SystemStatsResponse(ShortLinkService.SystemStats stats) {
            this.totalLinks = stats.getTotalLinks();
            this.totalAccess = stats.getTotalAccess();
            this.customAliases = stats.getCustomAliases();
            this.generatedAt = LocalDateTime.now();
        }
        
        // Getter方法
        public Long getTotalLinks() { return totalLinks; }
        public Long getTotalAccess() { return totalAccess; }
        public Long getCustomAliases() { return customAliases; }
        public LocalDateTime getGeneratedAt() { return generatedAt; }
    }
}