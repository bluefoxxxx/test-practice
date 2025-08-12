package com.sunlight.linker.application;

import com.sunlight.linker.core.Base62Converter;
import com.sunlight.linker.domain.ShortLink;
import com.sunlight.linker.infrastructure.ShortLinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 短链接业务服务类
 * 
 * 【第二阶段测试重点 - Service层集成测试】
 * 
 * 这是测试金字塔中的中间层 - 集成测试，具有以下特点：
 * ✅ 业务逻辑测试：验证Service层的核心业务规则
 * ✅ 依赖协作测试：验证Service与Repository的交互
 * ✅ 事务行为测试：验证@Transactional注解的正确性
 * ✅ 异常处理测试：验证各种异常情况的处理逻辑
 * 
 * 【测试技术栈学习重点】：
 * 1. @SpringBootTest：加载Spring应用上下文进行集成测试
 * 2. @MockBean：模拟Repository依赖，专注Service层逻辑测试
 * 3. Mockito.when().thenReturn()：模拟方法返回值
 * 4. Mockito.verify()：验证方法调用次数和参数
 * 5. @Transactional测试：验证事务的提交和回滚行为
 * 
 * 【业务逻辑说明】：
 * 作为短链接服务的核心业务层，负责：
 * - 长链接到短链接的转换逻辑
 * - 短链接到长链接的查询逻辑  
 * - 重复检测和防重复生成
 * - 自定义别名的冲突处理
 * - 访问统计和业务规则验证
 * 
 * @author 测试实践学习项目
 * @version 1.0
 */
@Service
@Transactional
public class ShortLinkService {
    
    private final ShortLinkRepository shortLinkRepository;
    
    /**
     * 构造方法依赖注入
     * 
     * 【测试友好设计】：
     * 使用构造方法注入而不是字段注入，便于单元测试时手动创建实例
     * final关键字确保依赖不可变，提高代码安全性
     */
    @Autowired
    public ShortLinkService(ShortLinkRepository shortLinkRepository) {
        this.shortLinkRepository = shortLinkRepository;
    }
    
    /**
     * 创建短链接（系统自动生成短码）
     * 
     * 【核心业务逻辑】：
     * 1. 验证长链接的有效性
     * 2. 检查是否已存在相同的长链接
     * 3. 如果存在，直接返回已有的短链接
     * 4. 如果不存在，创建新的短链接记录
     * 5. 基于数据库ID生成Base62短码
     * 6. 更新短码字段并保存
     * 
     * 【测试要点】：
     * - 有效长链接的正常处理流程
     * - 无效URL格式的异常处理
     * - 重复长链接的去重逻辑验证
     * - Repository方法的调用次数验证
     * - 事务回滚的测试
     * 
     * @param longUrl 原始长链接
     * @return 创建或已存在的短链接对象
     * @throws IllegalArgumentException 当长链接无效时抛出
     * @throws RuntimeException 当系统出现异常时抛出
     */
    public ShortLink createShortLink(String longUrl) {
        // 【参数验证】：确保输入的有效性
        validateLongUrl(longUrl);
        
        // 【防重复逻辑】：检查是否已存在相同的长链接
        Optional<ShortLink> existingLink = shortLinkRepository.findByLongUrl(longUrl);
        if (existingLink.isPresent()) {
            return existingLink.get();
        }
        
        // 【创建新记录】：先保存获取数据库生成的ID
        ShortLink shortLink = new ShortLink(longUrl, "temporary");
        ShortLink savedLink = shortLinkRepository.save(shortLink);
        
        // 【生成短码】：基于ID生成Base62短码
        String shortCode = Base62Converter.encode(savedLink.getId());
        savedLink.setShortCode(shortCode);
        
        // 【最终保存】：更新短码字段
        return shortLinkRepository.save(savedLink);
    }
    
    /**
     * 创建自定义别名的短链接
     * 
     * 【业务逻辑】：
     * 1. 验证长链接和自定义别名的有效性
     * 2. 检查自定义别名是否已被占用
     * 3. 检查是否已存在相同的长链接
     * 4. 创建带有自定义别名的短链接记录
     * 
     * 【测试要点】：
     * - 有效自定义别名的创建流程
     * - 别名冲突的异常处理
     * - 别名格式验证（Base62字符检查）
     * - 长链接重复时的处理策略
     * 
     * @param longUrl 原始长链接
     * @param customAlias 用户自定义的别名
     * @param description 可选的描述信息
     * @return 创建的短链接对象
     * @throws IllegalArgumentException 当参数无效或别名冲突时抛出
     */
    public ShortLink createCustomShortLink(String longUrl, String customAlias, String description) {
        // 【参数验证】
        validateLongUrl(longUrl);
        validateCustomAlias(customAlias);
        
        // 【别名冲突检查】
        if (shortLinkRepository.existsByShortCode(customAlias)) {
            throw new IllegalArgumentException("自定义别名已被占用: " + customAlias);
        }
        
        // 【重复长链接检查】：如果长链接已存在，需要考虑业务策略
        Optional<ShortLink> existingLink = shortLinkRepository.findByLongUrl(longUrl);
        if (existingLink.isPresent() && !existingLink.get().getIsCustomAlias()) {
            // 如果已存在系统生成的短链接，允许用户创建自定义别名
            // 这样可以满足用户的个性化需求
        }
        
        // 【创建自定义别名短链接】
        ShortLink shortLink = new ShortLink(longUrl, customAlias, description);
        return shortLinkRepository.save(shortLink);
    }
    
    /**
     * 根据短码查找对应的长链接
     * 
     * 【核心业务逻辑】：
     * 1. 根据短码查找短链接记录
     * 2. 如果找到，增加访问计数
     * 3. 返回对应的长链接
     * 
     * 【测试要点】：
     * - 存在的短码返回正确的长链接
     * - 不存在的短码返回空Optional
     * - 访问计数的正确增加
     * - 并发访问时的数据一致性
     * 
     * @param shortCode 短码字符串
     * @return 包含长链接的Optional，如果短码不存在则为empty
     */
    @Transactional
    public Optional<String> getLongUrl(String shortCode) {
        if (shortCode == null || shortCode.trim().isEmpty()) {
            return Optional.empty();
        }
        
        Optional<ShortLink> shortLink = shortLinkRepository.findByShortCode(shortCode.trim());
        
        if (shortLink.isPresent()) {
            // 【访问统计】：增加访问次数
            ShortLink link = shortLink.get();
            link.incrementAccessCount();
            shortLinkRepository.save(link);
            
            return Optional.of(link.getLongUrl());
        }
        
        return Optional.empty();
    }
    
    /**
     * 获取短链接详细信息（不增加访问计数）
     * 
     * 【业务价值】：
     * 用于管理后台查看短链接信息，不影响访问统计
     * 
     * @param shortCode 短码字符串
     * @return 包含短链接对象的Optional
     */
    @Transactional(readOnly = true)
    public Optional<ShortLink> getShortLinkInfo(String shortCode) {
        if (shortCode == null || shortCode.trim().isEmpty()) {
            return Optional.empty();
        }
        
        return shortLinkRepository.findByShortCode(shortCode.trim());
    }
    
    /**
     * 获取热点链接列表
     * 
     * 【业务价值】：
     * 用于数据分析和缓存策略制定
     * 
     * @param minAccessCount 最小访问次数阈值
     * @return 热点链接列表
     */
    @Transactional(readOnly = true)
    public List<ShortLink> getHotLinks(Long minAccessCount) {
        if (minAccessCount == null || minAccessCount < 0) {
            minAccessCount = 1000L; // 默认阈值
        }
        
        return shortLinkRepository.findByAccessCountGreaterThanOrderByAccessCountDesc(minAccessCount);
    }
    
    /**
     * 获取系统统计信息
     * 
     * 【业务价值】：
     * 提供系统监控和报表数据
     * 
     * @return 统计信息对象
     */
    @Transactional(readOnly = true)
    public SystemStats getSystemStats() {
        long totalLinks = shortLinkRepository.count();
        long totalAccess = shortLinkRepository.getTotalAccessCount();
        long customAliases = shortLinkRepository.countCustomAliases();
        
        return new SystemStats(totalLinks, totalAccess, customAliases);
    }
    
    /**
     * 查找指定时间范围内创建的短链接
     * 
     * 【业务价值】：
     * 支持时间维度的数据分析
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 时间范围内的短链接列表
     */
    @Transactional(readOnly = true)
    public List<ShortLink> getLinksCreatedBetween(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("开始时间和结束时间不能为null");
        }
        
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }
        
        return shortLinkRepository.findByCreatedAtBetween(startTime, endTime);
    }
    
    /**
     * 检查短码是否可用
     * 
     * 【业务价值】：
     * 用于自定义别名的可用性检查
     * 
     * @param shortCode 要检查的短码
     * @return 如果可用返回true，否则返回false
     */
    @Transactional(readOnly = true)
    public boolean isShortCodeAvailable(String shortCode) {
        if (shortCode == null || shortCode.trim().isEmpty()) {
            return false;
        }
        
        // 验证短码格式
        if (!Base62Converter.isValidBase62(shortCode.trim())) {
            return false;
        }
        
        return !shortLinkRepository.existsByShortCode(shortCode.trim());
    }
    
    // ========== 私有辅助方法 ==========
    
    /**
     * 验证长链接的有效性
     * 
     * 【验证规则】：
     * 1. 不能为null或空字符串
     * 2. 必须是有效的URL格式
     * 3. 协议必须是http或https
     * 4. 长度不能超过限制
     * 
     * 【测试要点】：
     * - 各种无效URL格式的异常处理
     * - 边界长度的验证
     * - 协议限制的验证
     * 
     * @param longUrl 要验证的长链接
     * @throws IllegalArgumentException 当URL无效时抛出
     */
    private void validateLongUrl(String longUrl) {
        if (longUrl == null || longUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("长链接不能为空");
        }
        
        String trimmedUrl = longUrl.trim();
        
        // 长度验证
        if (trimmedUrl.length() > 2048) {
            throw new IllegalArgumentException("长链接长度不能超过2048字符");
        }
        
        // URL格式验证
        try {
            URL url = new URL(trimmedUrl);
            String protocol = url.getProtocol().toLowerCase();
            
            if (!"http".equals(protocol) && !"https".equals(protocol)) {
                throw new IllegalArgumentException("仅支持HTTP和HTTPS协议的链接");
            }
            
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("无效的URL格式: " + trimmedUrl, e);
        }
    }
    
    /**
     * 验证自定义别名的有效性
     * 
     * 【验证规则】：
     * 1. 不能为null或空字符串
     * 2. 只能包含Base62字符（0-9, a-z, A-Z）
     * 3. 长度限制在1-20字符之间
     * 4. 不能与系统保留词冲突
     * 
     * @param customAlias 要验证的自定义别名
     * @throws IllegalArgumentException 当别名无效时抛出
     */
    private void validateCustomAlias(String customAlias) {
        if (customAlias == null || customAlias.trim().isEmpty()) {
            throw new IllegalArgumentException("自定义别名不能为空");
        }
        
        String trimmedAlias = customAlias.trim();
        
        // 长度验证
        if (trimmedAlias.length() > 20) {
            throw new IllegalArgumentException("自定义别名长度不能超过20字符");
        }
        
        // 字符格式验证
        if (!Base62Converter.isValidBase62(trimmedAlias)) {
            throw new IllegalArgumentException("自定义别名只能包含数字、大小写字母");
        }
        
        // 系统保留词检查
        if (isReservedWord(trimmedAlias)) {
            throw new IllegalArgumentException("自定义别名不能使用系统保留词: " + trimmedAlias);
        }
    }
    
    /**
     * 检查是否为系统保留词
     * 
     * 【业务考虑】：
     * 避免与系统API路径冲突，如 /api, /admin, /health 等
     * 
     * @param alias 要检查的别名
     * @return 如果是保留词返回true
     */
    private boolean isReservedWord(String alias) {
        String lowerAlias = alias.toLowerCase();
        
        // 系统保留词列表
        String[] reservedWords = {
            "api", "admin", "health", "metrics", "docs", "swagger", 
            "actuator", "management", "error", "login", "logout",
            "index", "home", "about", "help", "contact", "privacy",
            "terms", "www", "ftp", "mail", "email"
        };
        
        for (String reserved : reservedWords) {
            if (reserved.equals(lowerAlias)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 系统统计信息数据类
     * 
     * 【设计说明】：
     * 使用Java 14+的record语法创建不可变数据类
     * 适合作为方法返回值，提供统计数据
     */
    public static class SystemStats {
        private final long totalLinks;
        private final long totalAccess;
        private final long customAliases;
        
        public SystemStats(long totalLinks, long totalAccess, long customAliases) {
            this.totalLinks = totalLinks;
            this.totalAccess = totalAccess;
            this.customAliases = customAliases;
        }
        
        public long getTotalLinks() { return totalLinks; }
        public long getTotalAccess() { return totalAccess; }
        public long getCustomAliases() { return customAliases; }
        
        @Override
        public String toString() {
            return String.format("SystemStats{totalLinks=%d, totalAccess=%d, customAliases=%d}", 
                               totalLinks, totalAccess, customAliases);
        }
    }
}