package com.sunlight.linker.infrastructure;

import com.sunlight.linker.domain.ShortLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 短链接数据访问接口
 * 
 * 【测试学习重点】：
 * Repository层是数据访问的抽象，测试重点包括：
 * 1. Spring Data JPA方法命名约定的测试
 * 2. 自定义查询方法的测试
 * 3. 数据库约束（唯一性、外键等）的验证
 * 4. 分页和排序功能的测试
 * 5. 事务行为的测试
 * 
 * 【测试策略】：
 * - 第二阶段：使用@MockBean模拟Repository行为，专注Service层逻辑测试
 * - 第三阶段：使用Testcontainers进行真实数据库环境的集成测试
 * - 第四阶段：通过API层测试验证完整的数据流转
 * 
 * 【设计模式】：
 * 采用Repository模式，将数据访问逻辑与业务逻辑分离
 * 继承JpaRepository获得标准的CRUD操作
 * 通过方法命名约定和@Query注解扩展查询功能
 * 
 * @author 测试实践学习项目
 * @version 1.0
 */
@Repository
public interface ShortLinkRepository extends JpaRepository<ShortLink, Long> {
    
    /**
     * 根据短码查找短链接
     * 
     * 【业务重要性】：
     * 这是短链接服务最核心的查询方法，用于重定向功能
     * 性能要求极高，需要数据库索引支持
     * 
     * 【测试要点】：
     * - 存在的短码应该返回对应的ShortLink
     * - 不存在的短码应该返回Optional.empty()
     * - null和空字符串的处理
     * - 大小写敏感性测试
     * - 并发访问测试
     * 
     * @param shortCode 短码字符串
     * @return 包含ShortLink的Optional，如果不存在则为empty
     */
    Optional<ShortLink> findByShortCode(String shortCode);
    
    /**
     * 根据长链接查找短链接
     * 
     * 【业务价值】：
     * 用于防止重复生成，如果长链接已存在则直接返回原有的短链接
     * 支持去重逻辑，提高存储效率
     * 
     * 【测试要点】：
     * - 完全匹配的长链接查找
     * - URL参数顺序不同的情况（业务层处理）
     * - 协议差异（http vs https）的处理
     * - 超长URL的查询性能
     * 
     * @param longUrl 长链接URL
     * @return 包含ShortLink的Optional，如果不存在则为empty
     */
    Optional<ShortLink> findByLongUrl(String longUrl);
    
    /**
     * 检查短码是否已存在
     * 
     * 【业务价值】：
     * 用于自定义别名的冲突检测
     * 比完整查询更高效，只返回boolean结果
     * 
     * 【测试要点】：
     * - 存在的短码返回true
     * - 不存在的短码返回false
     * - 性能测试：与findByShortCode的效率对比
     * 
     * @param shortCode 要检查的短码
     * @return 如果短码已存在返回true，否则返回false
     */
    boolean existsByShortCode(String shortCode);
    
    /**
     * 根据访问次数范围查找热点链接
     * 
     * 【业务价值】：
     * 用于分析和缓存策略，识别高频访问的短链接
     * 支持系统性能优化和用户行为分析
     * 
     * 【查询说明】：
     * 使用方法命名约定，Spring Data JPA自动生成查询
     * GreaterThan表示大于指定值的条件
     * OrderByAccessCountDesc表示按访问次数降序排列
     * 
     * @param accessCount 最小访问次数阈值
     * @return 访问次数大于阈值的短链接列表，按访问次数降序排列
     */
    List<ShortLink> findByAccessCountGreaterThanOrderByAccessCountDesc(Long accessCount);
    
    /**
     * 查找指定时间范围内创建的短链接
     * 
     * 【业务价值】：
     * 支持时间维度的数据分析和报表功能
     * 可用于定期数据清理和归档
     * 
     * 【测试要点】：
     * - 边界时间的包含性测试
     * - 时区处理的正确性
     * - 大数据量时的查询性能
     * - 日期格式的兼容性
     * 
     * @param startTime 开始时间（包含）
     * @param endTime 结束时间（包含）
     * @return 指定时间范围内创建的短链接列表
     */
    List<ShortLink> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查找自定义别名的短链接
     * 
     * 【业务价值】：
     * 区分系统生成的短码和用户自定义的别名
     * 用于不同类型短链接的分别管理和统计
     * 
     * @param isCustomAlias 是否为自定义别名
     * @return 指定类型的短链接列表
     */
    List<ShortLink> findByIsCustomAlias(Boolean isCustomAlias);
    
    /**
     * 使用自定义查询统计总访问次数
     * 
     * 【业务价值】：
     * 提供系统级别的访问统计数据
     * 用于业务报表和系统监控
     * 
     * 【技术特点】：
     * 使用@Query注解编写JPQL查询
     * 演示聚合函数在Repository中的使用
     * 
     * 【测试要点】：
     * - 空数据库时的返回值（应该是0而不是null）
     * - 大数值的求和精度
     * - 查询性能测试
     * 
     * @return 所有短链接的总访问次数
     */
    @Query("SELECT COALESCE(SUM(sl.accessCount), 0) FROM ShortLink sl")
    Long getTotalAccessCount();
    
    /**
     * 查找最受欢迎的短链接（Top N）
     * 
     * 【业务价值】：
     * 提供热门内容排行，支持数据分析和推荐功能
     * 可用于缓存策略的制定
     * 
     * 【技术特点】：
     * 演示JPQL中ORDER BY和LIMIT的使用
     * 通过参数控制返回结果数量，提高查询灵活性
     * 
     * 【测试要点】：
     * - 不同limit值的结果验证
     * - 访问次数相同时的排序稳定性
     * - 空数据库的处理
     * - limit超过总记录数的情况
     * 
     * @param limit 返回记录的最大数量
     * @return 按访问次数降序排列的前N个短链接
     */
    @Query("SELECT sl FROM ShortLink sl ORDER BY sl.accessCount DESC LIMIT :limit")
    List<ShortLink> findTopAccessedLinks(@Param("limit") int limit);
    
    /**
     * 根据短码模糊查找
     * 
     * 【业务价值】：
     * 支持管理后台的搜索功能
     * 帮助用户查找模糊记忆的短码
     * 
     * 【技术说明】：
     * 使用Spring Data JPA的Containing关键字
     * 自动转换为SQL的LIKE '%pattern%'查询
     * 
     * 【性能警告】：
     * 模糊查询可能导致全表扫描，生产环境需要谨慎使用
     * 建议配合分页和其他条件使用
     * 
     * @param pattern 搜索模式
     * @return 短码包含指定模式的短链接列表
     */
    List<ShortLink> findByShortCodeContaining(String pattern);
    
    /**
     * 批量删除过期或无效的短链接
     * 
     * 【业务价值】：
     * 支持数据清理和系统维护
     * 可用于定期清理长期未访问的链接
     * 
     * 【技术特点】：
     * 使用@Query注解编写删除语句
     * 演示Repository中的批量删除操作
     * 需要配合@Modifying注解使用
     * 
     * 【测试要点】：
     * - 删除条件的正确性验证
     * - 返回删除记录数的准确性
     * - 事务回滚时的数据一致性
     * - 并发删除的安全性
     * 
     * @param beforeDate 删除此日期之前创建的记录
     * @return 删除的记录数量
     */
    @Query("DELETE FROM ShortLink sl WHERE sl.createdAt < :beforeDate AND sl.accessCount = 0")
    int deleteUnusedLinksCreatedBefore(@Param("beforeDate") LocalDateTime beforeDate);
    
    /**
     * 统计自定义别名的数量
     * 
     * 【业务价值】：
     * 用于监控用户自定义别名的使用情况
     * 支持功能使用统计和分析
     * 
     * @return 自定义别名的总数量
     */
    @Query("SELECT COUNT(sl) FROM ShortLink sl WHERE sl.isCustomAlias = true")
    Long countCustomAliases();
    
    /**
     * 统计指定短码的数量
     * 
     * 【业务价值】：
     * 用于验证短码的唯一性，支持并发测试验证
     * 
     * @param shortCode 要统计的短码
     * @return 该短码的数量（0或1）
     */
    Long countByShortCode(String shortCode);
    
    /**
     * 查找指定域名下访问次数最多的短链接
     * 
     * 【业务价值】：
     * 支持多域名部署场景的数据分析
     * 可用于不同域名的效果对比
     * 
     * 【实现说明】：
     * 这里简化处理，实际可能需要单独的域名字段
     * 当前通过长链接的域名部分进行匹配
     * 
     * @param domain 域名模式
     * @return 指定域名下访问次数最多的短链接
     */
    @Query("SELECT sl FROM ShortLink sl WHERE sl.longUrl LIKE CONCAT('%', :domain, '%') ORDER BY sl.accessCount DESC LIMIT 1")
    Optional<ShortLink> findMostAccessedLinkByDomain(@Param("domain") String domain);
}