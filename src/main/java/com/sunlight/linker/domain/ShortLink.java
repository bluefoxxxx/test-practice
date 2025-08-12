package com.sunlight.linker.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 短链接领域实体
 * 
 * 【测试学习重点】：
 * 这是一个JPA实体类，为后续的Repository层和Service层测试提供数据模型基础
 * 测试关注点：
 * 1. 实体验证注解的测试（Bean Validation）
 * 2. JPA映射的正确性测试  
 * 3. 业务逻辑方法的单元测试
 * 4. equals()和hashCode()的契约测试
 * 
 * 【业务模型说明】：
 * 代表一个短链接记录，包含原始长链接、生成的短码、创建时间等信息
 * 支持自定义别名功能，满足用户个性化需求
 * 
 * 【设计模式】：
 * 采用充血模型（Rich Domain Model），在实体中包含业务逻辑方法
 * 相比贫血模型，这种方式更符合DDD（领域驱动设计）理念
 * 
 * @author 测试实践学习项目
 * @version 1.0
 */
@Entity
@Table(name = "short_links", indexes = {
    @Index(name = "idx_short_code", columnList = "shortCode", unique = true),
    @Index(name = "idx_long_url", columnList = "longUrl"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
public class ShortLink {
    
    /**
     * 主键ID - 用于Base62转换生成短码
     * 
     * 【设计说明】：
     * 使用IDENTITY策略让数据库自动生成递增ID
     * 这个ID是Base62转换的输入源，确保短码的唯一性
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 原始长链接
     * 
     * 【业务规则】：
     * - 不能为空或null
     * - 长度限制：1-2048字符（符合HTTP规范建议）
     * - 需要是有效的URL格式（由业务层验证）
     */
    @Column(name = "long_url", nullable = false, length = 2048)
    @NotBlank(message = "长链接不能为空")
    @Size(max = 2048, message = "长链接长度不能超过2048字符")
    private String longUrl;
    
    /**
     * 生成的短码
     * 
     * 【业务规则】：
     * - 由Base62算法基于ID生成，或由用户自定义
     * - 在整个系统中必须唯一
     * - 长度限制：1-20字符（满足短链接需求）
     * - 只能包含Base62字符：0-9, a-z, A-Z
     */
    @Column(name = "short_code", nullable = false, unique = true, length = 20)
    @NotBlank(message = "短码不能为空")
    @Size(max = 20, message = "短码长度不能超过20字符")
    private String shortCode;
    
    /**
     * 是否为自定义别名
     * 
     * 【业务规则】：
     * - true: 用户自定义的别名
     * - false: 系统基于ID自动生成的短码
     * - 影响短码的生成策略和冲突处理
     */
    @Column(name = "is_custom_alias", nullable = false)
    private Boolean isCustomAlias = false;
    
    /**
     * 访问次数统计
     * 
     * 【业务价值】：
     * - 提供基础的访问统计功能
     * - 为后续扩展分析功能做准备
     * - 可用于热点数据的缓存策略
     */
    @Column(name = "access_count", nullable = false)
    private Long accessCount = 0L;
    
    /**
     * 创建时间
     * 
     * 【技术实现】：
     * 使用@CreationTimestamp由Hibernate自动设置
     * 不可更新，记录链接的创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 最后更新时间
     * 
     * 【技术实现】：
     * 使用@UpdateTimestamp由Hibernate自动维护
     * 每次实体更新时自动刷新
     */
    @UpdateTimestamp
    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;
    
    /**
     * 备注信息（可选）
     * 
     * 【扩展功能】：
     * 允许用户为短链接添加备注说明
     * 支持后续的链接管理和分类功能
     */
    @Column(name = "description", length = 500)
    @Size(max = 500, message = "备注长度不能超过500字符")
    private String description;
    
    // ========== 构造方法 ==========
    
    /**
     * JPA要求的无参构造方法
     */
    protected ShortLink() {
        // JPA规范要求
    }
    
    /**
     * 创建系统生成短码的短链接
     * 
     * 【使用场景】：
     * 用户提交长链接，系统自动生成短码的标准流程
     * 
     * @param longUrl 原始长链接
     * @param shortCode 系统生成的短码
     */
    public ShortLink(String longUrl, String shortCode) {
        this.longUrl = longUrl;
        this.shortCode = shortCode;
        this.isCustomAlias = false;
    }
    
    /**
     * 创建用户自定义别名的短链接
     * 
     * 【使用场景】：
     * 用户为长链接指定自定义的短码别名
     * 
     * @param longUrl 原始长链接
     * @param customAlias 用户自定义的别名
     * @param description 备注信息
     */
    public ShortLink(String longUrl, String customAlias, String description) {
        this.longUrl = longUrl;
        this.shortCode = customAlias;
        this.isCustomAlias = true;
        this.description = description;
    }
    
    // ========== 业务方法 ==========
    
    /**
     * 增加访问次数
     * 
     * 【业务逻辑】：
     * 每次短链接被访问时调用此方法
     * 使用线程安全的方式增加计数（在Service层处理并发）
     * 
     * 【测试要点】：
     * - 验证计数是否正确增加
     * - 测试边界情况（Long.MAX_VALUE附近）
     */
    public void incrementAccessCount() {
        this.accessCount++;
    }
    
    /**
     * 检查是否为热点链接
     * 
     * 【业务规则】：
     * 访问次数超过1000次视为热点链接
     * 可用于缓存策略和性能优化
     * 
     * @return 如果是热点链接返回true
     * 
     * 【测试要点】：
     * - 边界值测试（999, 1000, 1001）
     * - 新创建链接的默认行为
     */
    public boolean isHotLink() {
        return this.accessCount > 1000;
    }
    
    /**
     * 检查短码是否为系统生成
     * 
     * @return 如果是系统生成返回true，自定义别名返回false
     */
    public boolean isSystemGenerated() {
        return !this.isCustomAlias;
    }
    
    /**
     * 生成短链接的完整URL
     * 
     * 【业务逻辑】：
     * 基于短码生成完整的短链接URL
     * 实际使用时域名应该从配置中获取
     * 
     * @param domain 短链接服务的域名
     * @return 完整的短链接URL
     * 
     * 【测试要点】：
     * - null/空域名的处理
     * - URL格式的正确性
     * - 特殊字符的处理
     */
    public String generateFullShortUrl(String domain) {
        if (domain == null || domain.trim().isEmpty()) {
            throw new IllegalArgumentException("域名不能为空");
        }
        
        // 确保域名以http://或https://开头
        String normalizedDomain = domain.trim();
        if (!normalizedDomain.startsWith("http://") && !normalizedDomain.startsWith("https://")) {
            normalizedDomain = "https://" + normalizedDomain;
        }
        
        // 确保域名不以/结尾
        if (normalizedDomain.endsWith("/")) {
            normalizedDomain = normalizedDomain.substring(0, normalizedDomain.length() - 1);
        }
        
        return normalizedDomain + "/s/" + this.shortCode;
    }
    
    // ========== Getter和Setter方法 ==========
    
    public Long getId() {
        return id;
    }
    
    public String getLongUrl() {
        return longUrl;
    }
    
    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }
    
    public String getShortCode() {
        return shortCode;
    }
    
    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }
    
    public Boolean getIsCustomAlias() {
        return isCustomAlias;
    }
    
    public void setIsCustomAlias(Boolean isCustomAlias) {
        this.isCustomAlias = isCustomAlias;
    }
    
    public Long getAccessCount() {
        return accessCount;
    }
    
    public void setAccessCount(Long accessCount) {
        this.accessCount = accessCount;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    // ========== Object方法重写 ==========
    
    /**
     * equals方法实现
     * 
     * 【业务规则】：
     * 基于shortCode进行比较，因为shortCode在业务上具有唯一性
     * 这符合DDD中实体的identity概念
     * 
     * 【测试要点】：
     * - 相同shortCode的不同实例应该相等
     * - null值处理
     * - 自反性、对称性、传递性测试
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        ShortLink shortLink = (ShortLink) o;
        return Objects.equals(shortCode, shortLink.shortCode);
    }
    
    /**
     * hashCode方法实现
     * 
     * 【契约要求】：
     * 与equals方法保持一致，基于shortCode计算hash值
     * 满足equals相等的对象必须有相同的hashCode
     * 
     * 【测试要点】：
     * - equals相等的对象hashCode必须相等
     * - hashCode的稳定性测试
     */
    @Override
    public int hashCode() {
        return Objects.hash(shortCode);
    }
    
    /**
     * toString方法实现
     * 
     * 【实现说明】：
     * 提供易于阅读的字符串表示，便于调试和日志记录
     * 不包含敏感信息，适合在日志中输出
     * 
     * 【测试要点】：
     * - 输出格式的一致性
     * - null字段的处理
     * - 字符串内容的正确性
     */
    @Override
    public String toString() {
        return String.format("ShortLink{id=%d, shortCode='%s', longUrl='%s', isCustomAlias=%s, accessCount=%d, createdAt=%s}", 
                           id, shortCode, longUrl, isCustomAlias, accessCount, createdAt);
    }
}