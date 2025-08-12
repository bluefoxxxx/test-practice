package com.sunlight.linker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Quality Gate Linker - 短链接服务应用程序主类
 * 
 * 【应用程序设计说明】：
 * 这是一个基于Spring Boot 3.x的现代短链接服务，专为测试技术栈学习而设计
 * 采用分层架构模式，集成了现代Java开发的最佳实践
 * 
 * 【架构特点】：
 * - 领域驱动设计（DDD）：清晰的领域模型和业务逻辑封装
 * - 分层架构：Web层、应用层、领域层、基础设施层分离
 * - RESTful API：标准的HTTP API设计
 * - 事务管理：声明式事务确保数据一致性
 * - 配置外部化：支持多环境配置
 * 
 * 【测试体系完整覆盖】：
 * ✅ 第一阶段 - 单元测试：Base62ConverterTest（纯逻辑测试）
 * ✅ 第二阶段 - 集成测试：ShortLinkServiceTest（Mock依赖测试）
 * ✅ 第三阶段 - 数据库测试：ShortLinkServiceRealDbTest（Testcontainers）
 * ✅ 第四阶段 - API测试：ShortLinkControllerTest（MockMvc）
 * ✅ 第五阶段 - 覆盖率分析：Jacoco代码覆盖率报告
 * 
 * 【核心功能】：
 * - 长链接到短链接的转换（Base62编码）
 * - 自定义别名支持
 * - 访问统计和数据分析
 * - 重定向服务
 * - 系统监控和管理
 * 
 * 【技术栈】：
 * - Spring Boot 3.2.x（基于Java 21）
 * - Spring Data JPA（数据持久化）
 * - PostgreSQL（生产数据库）
 * - H2（测试数据库）
 * - Bean Validation（参数验证）
 * - Jackson（JSON序列化）
 * 
 * 【测试技术栈】：
 * - JUnit 5（测试框架）
 * - AssertJ（流式断言）
 * - Mockito（Mock对象）
 * - Testcontainers（容器化测试）
 * - MockMvc（Web层测试）
 * - Jacoco（代码覆盖率）
 * 
 * @author 测试实践学习项目
 * @version 1.0
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableTransactionManagement
@ConfigurationPropertiesScan
public class QualityGateLinkerApplication {
    
    /**
     * 应用程序入口点
     * 
     * 【启动流程】：
     * 1. 初始化Spring Boot应用上下文
     * 2. 自动配置数据源和JPA
     * 3. 启动嵌入式Web服务器（默认Tomcat）
     * 4. 加载应用程序配置
     * 5. 初始化业务组件
     * 
     * 【开发环境启动】：
     * - 使用H2内存数据库（快速启动）
     * - 启用SQL日志和Hibernate统计
     * - 开放所有Actuator端点
     * 
     * 【生产环境部署】：
     * - 连接PostgreSQL数据库
     * - 启用监控和健康检查
     * - 配置日志级别和输出
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 设置系统属性（可选）
        configureSystemProperties();
        
        // 启动Spring Boot应用
        SpringApplication application = new SpringApplication(QualityGateLinkerApplication.class);
        
        // 可以在这里添加自定义配置
        // application.setAdditionalProfiles("custom");
        
        // 启动应用
        application.run(args);
        
        // 输出启动成功信息
        printStartupBanner();
    }
    
    /**
     * 配置系统属性
     * 
     * 【配置说明】：
     * 设置一些JVM级别的配置，优化应用程序运行
     */
    private static void configureSystemProperties() {
        // 设置文件编码为UTF-8
        System.setProperty("file.encoding", "UTF-8");
        
        // 优化Spring Boot启动性能
        System.setProperty("spring.main.lazy-initialization", "false");
        
        // 设置时区（可选）
        System.setProperty("user.timezone", "Asia/Shanghai");
        
        // Testcontainers相关配置（仅在开发/测试环境）
        if (isTestEnvironment()) {
            System.setProperty("testcontainers.reuse.enable", "true");
        }
    }
    
    /**
     * 检查是否为测试环境
     * 
     * @return 如果是测试环境返回true
     */
    private static boolean isTestEnvironment() {
        String[] testProfiles = {"test", "dev", "local"};
        String activeProfiles = System.getProperty("spring.profiles.active", "");
        
        for (String profile : testProfiles) {
            if (activeProfiles.contains(profile)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 打印启动横幅信息
     * 
     * 【用户体验】：
     * 提供友好的启动成功提示，包含有用的访问信息
     */
    private static void printStartupBanner() {
        String port = System.getProperty("server.port", "8080");
        String contextPath = System.getProperty("server.servlet.context-path", "");
        
        System.out.println();
        System.out.println("🚀 质量门短链接服务启动成功！");
        System.out.println("📋 服务信息:");
        System.out.printf("   🌐 API地址: http://localhost:%s%s/api/v1%n", port, contextPath);
        System.out.printf("   🔗 重定向: http://localhost:%s%s/s/{shortCode}%n", port, contextPath);
        System.out.printf("   📊 监控页: http://localhost:%s%s/actuator/health%n", port, contextPath);
        System.out.println();
        System.out.println("🧪 测试相关:");
        System.out.println("   📝 运行测试: mvn clean test");
        System.out.println("   📈 覆盖率报告: mvn clean verify jacoco:report");
        System.out.println("   🐳 集成测试: mvn clean verify -Dtest=*RealDbTest");
        System.out.println();
        System.out.println("📚 API示例:");
        System.out.printf("   创建短链接: POST http://localhost:%s%s/api/v1/links%n", port, contextPath);
        System.out.printf("   查看统计: GET http://localhost:%s%s/api/v1/stats%n", port, contextPath);
        System.out.println();
        System.out.println("✨ 测试技术栈学习完成，开始探索吧！");
        System.out.println();
    }
}