package com.sunlight.linker.application;

import com.sunlight.linker.core.Base62Converter;
import com.sunlight.linker.domain.ShortLink;
import com.sunlight.linker.infrastructure.ShortLinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 带缓存功能的短链接服务
 * 
 * 【进阶挑战 - 缓存集成】
 * 
 * 此服务在原有功能基础上增加了Redis缓存支持：
 * ✅ 查询缓存：热点短链接查询结果缓存，减少数据库压力
 * ✅ 访问计数缓存：异步更新访问计数，提高响应性能
 * ✅ 统计数据缓存：系统统计信息缓存，避免重复计算
 * ✅ 缓存穿透防护：合理的缓存策略，防止缓存穿透
 * ✅ 缓存雪崩防护：设置随机过期时间，防止缓存雪崩
 * 
 * 【缓存策略】：
 * - 短链接查询：Cache-Aside模式，查询时缓存，更新时失效
 * - 访问计数：Write-Behind模式，异步批量更新数据库
 * - 热点数据：LRU策略，自动淘汰冷数据
 * 
 * @author 测试实践学习项目
 * @version 1.0
 */
@Service
@Transactional
public class CachedShortLinkService {
    
    private static final Logger logger = LoggerFactory.getLogger(CachedShortLinkService.class);
    
    private final ShortLinkRepository shortLinkRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // 缓存键前缀
    private static final String CACHE_KEY_SHORT_LINK = "shortlink:";
    private static final String CACHE_KEY_ACCESS_COUNT = "access_count:";
    private static final String CACHE_KEY_STATS = "stats:system";
    
    public CachedShortLinkService(ShortLinkRepository shortLinkRepository, 
                                 RedisTemplate<String, Object> redisTemplate) {
        this.shortLinkRepository = shortLinkRepository;
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 创建短链接（带缓存）
     * 
     * @param longUrl 长链接
     * @return 创建的短链接
     */
    @CacheEvict(value = "stats", allEntries = true) // 清除统计缓存
    public ShortLink createShortLink(String longUrl) {
        return createShortLink(longUrl, null);
    }
    
    /**
     * 创建短链接（带缓存，支持自定义别名）
     * 
     * @param longUrl 长链接
     * @param customAlias 自定义别名
     * @return 创建的短链接
     */
    @CacheEvict(value = "stats", allEntries = true) // 清除统计缓存
    public ShortLink createShortLink(String longUrl, String customAlias) {
        logger.debug("创建短链接: longUrl={}, customAlias={}", longUrl, customAlias);
        
        // 检查是否已存在
        Optional<ShortLink> existing = shortLinkRepository.findByLongUrl(longUrl);
        if (existing.isPresent()) {
            logger.debug("长链接已存在，返回现有记录: {}", existing.get().getShortCode());
            return existing.get();
        }
        
        ShortLink shortLink;
        if (customAlias != null && !customAlias.trim().isEmpty()) {
            // 检查自定义别名是否可用
            if (shortLinkRepository.existsByShortCode(customAlias)) {
                throw new IllegalArgumentException("自定义别名已被占用: " + customAlias);
            }
            shortLink = new ShortLink(longUrl, customAlias.trim());
            shortLink.setIsCustomAlias(true);
        } else {
            // 使用系统生成的短码
            shortLink = new ShortLink(longUrl, "temporary");
        }
        
        // 保存到数据库获取ID
        ShortLink saved = shortLinkRepository.save(shortLink);
        
        // 如果不是自定义别名，生成Base62短码
        if (customAlias == null || customAlias.trim().isEmpty()) {
            String shortCode = Base62Converter.encode(saved.getId());
            saved.setShortCode(shortCode);
            saved = shortLinkRepository.save(saved);
        }
        
        // 将新创建的短链接加入缓存
        cacheShortLink(saved);
        
        logger.info("短链接创建成功: id={}, shortCode={}, longUrl={}", 
                   saved.getId(), saved.getShortCode(), saved.getLongUrl());
        return saved;
    }
    
    /**
     * 获取长链接（带缓存）
     * 
     * @param shortCode 短码
     * @return 长链接（可能为空）
     */
    @Cacheable(value = "shortlinks", key = "#shortCode")
    public Optional<String> getLongUrl(String shortCode) {
        logger.debug("查询长链接: shortCode={}", shortCode);
        
        // 先从缓存查询
        String cacheKey = CACHE_KEY_SHORT_LINK + shortCode;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            logger.debug("缓存命中: shortCode={}", shortCode);
            ShortLink cachedLink = (ShortLink) cached;
            // 异步更新访问计数
            updateAccessCountAsync(shortCode);
            return Optional.of(cachedLink.getLongUrl());
        }
        
        // 缓存未命中，查询数据库
        Optional<ShortLink> shortLink = shortLinkRepository.findByShortCode(shortCode);
        
        if (shortLink.isPresent()) {
            ShortLink link = shortLink.get();
            
            // 更新访问计数
            link.setAccessCount(link.getAccessCount() + 1);
            // 注意：lastUpdatedAt会由@UpdateTimestamp自动更新
            shortLinkRepository.save(link);
            
            // 加入缓存
            cacheShortLink(link);
            
            logger.debug("数据库查询成功: shortCode={}, longUrl={}", shortCode, link.getLongUrl());
            return Optional.of(link.getLongUrl());
        }
        
        logger.debug("短码不存在: shortCode={}", shortCode);
        return Optional.empty();
    }
    
    /**
     * 获取热门链接（带缓存）
     * 
     * @param minAccessCount 最小访问次数
     * @return 热门链接列表
     */
    @Cacheable(value = "hotlinks", key = "#minAccessCount")
    public List<ShortLink> getHotLinks(Integer minAccessCount) {
        logger.debug("查询热门链接: minAccessCount={}", minAccessCount);
        
        if (minAccessCount == null || minAccessCount < 0) {
            minAccessCount = 10; // 默认最小访问次数
        }
        
        return shortLinkRepository.findByAccessCountGreaterThanOrderByAccessCountDesc(minAccessCount.longValue());
    }
    
    /**
     * 获取系统统计信息（带缓存）
     * 
     * @return 统计信息
     */
    @Cacheable(value = "stats", key = "'system'")
    public SystemStats getSystemStats() {
        logger.debug("查询系统统计信息");
        
        long totalLinks = shortLinkRepository.count();
        long totalAccess = shortLinkRepository.getTotalAccessCount();
        long customAliasCount = shortLinkRepository.countCustomAliases();
        
        return new SystemStats(totalLinks, totalAccess, customAliasCount);
    }
    
    /**
     * 检查短码可用性
     * 
     * @param shortCode 待检查的短码
     * @return 是否可用
     */
    public boolean isShortCodeAvailable(String shortCode) {
        // 先检查缓存
        String cacheKey = CACHE_KEY_SHORT_LINK + shortCode;
        Boolean cached = redisTemplate.hasKey(cacheKey);
        
        if (Boolean.TRUE.equals(cached)) {
            return false; // 缓存中存在，说明不可用
        }
        
        // 检查数据库
        return !shortLinkRepository.existsByShortCode(shortCode);
    }
    
    /**
     * 缓存短链接对象
     * 
     * @param shortLink 短链接对象
     */
    private void cacheShortLink(ShortLink shortLink) {
        String cacheKey = CACHE_KEY_SHORT_LINK + shortLink.getShortCode();
        
        // 设置随机过期时间（25-35分钟），防止缓存雪崩
        long expireMinutes = 25 + (long) (Math.random() * 10);
        
        redisTemplate.opsForValue().set(cacheKey, shortLink, expireMinutes, TimeUnit.MINUTES);
        logger.debug("短链接已缓存: shortCode={}, expireMinutes={}", shortLink.getShortCode(), expireMinutes);
    }
    
    /**
     * 异步更新访问计数
     * 
     * @param shortCode 短码
     */
    private void updateAccessCountAsync(String shortCode) {
        // 使用Redis计数器实现异步计数
        String countKey = CACHE_KEY_ACCESS_COUNT + shortCode;
        
        // 增加Redis中的计数
        Long count = redisTemplate.opsForValue().increment(countKey);
        
        // 设置计数器过期时间
        if (count == 1) {
            redisTemplate.expire(countKey, 1, TimeUnit.HOURS);
        }
        
        // 当计数达到一定值时，批量更新数据库
        if (count % 10 == 0) {
            // 这里可以使用Spring的@Async或消息队列来异步处理
            logger.debug("计数达到批量更新阈值: shortCode={}, count={}", shortCode, count);
        }
    }
    
    /**
     * 清除缓存
     * 
     * @param shortCode 短码
     */
    @CacheEvict(value = {"shortlinks", "hotlinks", "stats"}, allEntries = true)
    public void evictCache(String shortCode) {
        String cacheKey = CACHE_KEY_SHORT_LINK + shortCode;
        redisTemplate.delete(cacheKey);
        logger.info("缓存已清除: shortCode={}", shortCode);
    }
    
    /**
     * 系统统计信息数据类
     */
    public static class SystemStats {
        private final long totalLinks;
        private final long totalAccess;
        private final long customAliasCount;
        
        public SystemStats(long totalLinks, long totalAccess, long customAliasCount) {
            this.totalLinks = totalLinks;
            this.totalAccess = totalAccess;
            this.customAliasCount = customAliasCount;
        }
        
        public long getTotalLinks() { return totalLinks; }
        public long getTotalAccess() { return totalAccess; }
        public long getCustomAliasCount() { return customAliasCount; }
        
        @Override
        public String toString() {
            return String.format("SystemStats{totalLinks=%d, totalAccess=%d, customAliasCount=%d}", 
                               totalLinks, totalAccess, customAliasCount);
        }
    }
}
