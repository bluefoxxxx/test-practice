# 短链接服务测试实践练习

## 📚 练习概述

本练习基于 **测试金字塔** 理论，通过一个完整的短链接服务项目，让学习者系统掌握Java测试技术栈的各个层面。

### 🎯 学习目标

- **掌握JUnit 5 & AssertJ**：现代Java测试框架的核心用法
- **理解测试金字塔**：单元测试、集成测试、API测试的层次关系
- **学会Mock技术**：Mockito在不同场景下的应用
- **掌握Spring Boot测试**：@SpringBootTest、@WebMvcTest等注解的使用
- **学习Testcontainers**：容器化测试环境的构建和管理
- **理解测试策略**：如何设计有效的测试用例

## 🏗️ 测试架构（测试金字塔）

```
        🔺 API测试 (练习4-5)
       ▫️▫️▫️ MockMvc + JSON + HTTP
      ▫️▫️▫️▫️▫️ 
     🔸 集成测试 (练习3)
    ▫️▫️▫️▫️▫️▫️▫️ Testcontainers + PostgreSQL
   ▫️▫️▫️▫️▫️▫️▫️▫️▫️
  🔹 Service测试 (练习2)
 ▫️▫️▫️▫️▫️▫️▫️▫️▫️▫️▫️ @SpringBootTest + @MockBean
▫️▫️▫️▫️▫️▫️▫️▫️▫️▫️▫️▫️▫️
🟦 单元测试 (练习1)
🟦🟦🟦🟦🟦🟦🟦🟦🟦🟦🟦🟦🟦🟦🟦 JUnit 5 + AssertJ + Mockito
```

## 📁 练习结构

### 练习1：核心逻辑单元测试
**文件**: `core/Base62ConverterExercise.java`
**技术栈**: JUnit 5 + AssertJ + 参数化测试
**重点**: 
- 基础断言和流式断言
- 参数化测试（@ValueSource、@CsvSource、@MethodSource）
- 异常测试和边界值测试
- 往返测试（Round-trip Testing）

### 练习2：Service层集成测试
**文件**: `application/ShortLinkServiceExercise.java`
**技术栈**: Spring Boot + Mockito + @MockBean
**重点**:
- Spring Boot测试配置
- Mock对象的创建和行为定义
- ArgumentCaptor的使用
- 业务逻辑验证

### 练习3：真实数据库集成测试
**文件**: `application/ShortLinkServiceRealDbExercise.java`
**技术栈**: Testcontainers + PostgreSQL + Spring Boot
**重点**:
- 容器化测试环境配置
- 真实数据库操作测试
- 数据持久化验证
- 复杂查询测试

### 练习4：REST API测试
**文件**: `web/ShortLinkControllerExercise.java`
**技术栈**: MockMvc + JSON + @WebMvcTest
**重点**:
- HTTP协议测试
- JSON序列化/反序列化
- 状态码和响应头验证
- 错误处理测试

### 练习5：重定向功能测试
**文件**: `web/RedirectControllerExercise.java`
**技术栈**: MockMvc + HTTP重定向
**重点**:
- HTTP 302重定向测试
- Location响应头验证
- 安全性测试
- 异常处理

## 🚀 开始练习

### 环境准备

1. **Java 21+**: 确保安装了Java 21或更高版本
2. **Maven 3.8+**: 项目构建工具
3. **Docker**: Testcontainers需要Docker环境
4. **IDE**: 推荐使用IntelliJ IDEA或VS Code

### 运行方式

```bash
# 运行所有参考实现的测试
mvn test

# 运行单个练习（需要先完成实现）
mvn test -Dtest=Base62ConverterExercise
```

### 完成步骤

1. **从练习1开始**：按照TODO提示完成Base62转换器的测试
2. **逐层推进**：完成一个练习后再进行下一个
3. **参考注释**：每个TODO都有详细的提示和要求
4. **运行验证**：完成后运行测试验证正确性

## 📋 练习清单

### 练习1：单元测试 (预计2-3小时)
- [ ] 1.1 基础编码测试
- [ ] 1.2 参数化测试 (@ValueSource)
- [ ] 1.3 CSV参数化测试 (@CsvSource)
- [ ] 1.4 异常测试
- [ ] 1.5 解码测试
- [ ] 1.6 MethodSource参数化测试
- [ ] 1.7 往返测试
- [ ] 1.8 边界值测试

### 练习2：Service集成测试 (预计3-4小时)
- [ ] 2.1 新短链接创建测试
- [ ] 2.2 重复链接处理测试
- [ ] 2.3 自定义别名测试
- [ ] 2.4-2.6 获取链接功能测试
- [ ] 2.7-2.8 别名可用性测试
- [ ] 2.9-2.12 其他业务功能测试

### 练习3：Testcontainers测试 (预计4-5小时)
- [ ] 3.1-3.3 数据持久化测试
- [ ] 3.4-3.5 查询和统计测试
- [ ] 3.6-3.7 自定义别名数据流测试
- [ ] 3.8-3.10 复杂查询测试
- [ ] 3.11-3.13 高级功能测试

### 练习4：API测试 (预计3-4小时)
- [ ] 4.1-4.4 创建API测试
- [ ] 4.5-4.6 查询API测试
- [ ] 4.7-4.18 其他API功能测试

### 练习5：重定向测试 (预计2-3小时)
- [ ] 5.1-5.2 基础重定向测试
- [ ] 5.3-5.12 高级重定向功能测试

## 💡 学习提示

### 测试编写原则
1. **AAA模式**: Arrange(准备) → Act(执行) → Assert(断言)
2. **测试命名**: 使用描述性的测试方法名
3. **独立性**: 每个测试应该独立运行
4. **可重复性**: 测试结果应该一致

### 常见问题
1. **Mock配置**: 注意when().thenReturn()的使用
2. **JSON测试**: 使用jsonPath()验证响应结构
3. **异常测试**: 使用assertThatThrownBy()
4. **容器配置**: 确保Docker服务正常运行

### 调试技巧
1. 使用`.andDo(print())`打印HTTP请求响应
2. 使用`@Sql`注解预置测试数据
3. 使用断点调试复杂的Mock行为

## 📊 评分标准

| 练习 | 满分 | 评分重点 |
|------|------|----------|
| 练习1 | 100分 | 基础测试技能、参数化测试、异常处理 |
| 练习2 | 100分 | Spring集成、Mock使用、业务逻辑验证 |
| 练习3 | 100分 | 容器配置、数据库操作、复杂查询 |
| 练习4 | 100分 | HTTP协议、JSON处理、API设计 |
| 练习5 | 100分 | 重定向机制、安全性、异常处理 |

**总分**: 500分

## 🎓 进阶学习

完成基础练习后，可以继续学习：

1. **性能测试**: JMeter、Gatling
2. **契约测试**: Spring Cloud Contract
3. **端到端测试**: Selenium、Playwright
4. **测试覆盖率优化**: JaCoCo深度分析
5. **CI/CD集成**: GitHub Actions、Jenkins

## 🔗 参考资源

- [JUnit 5 官方文档](https://junit.org/junit5/docs/current/user-guide/)
- [AssertJ 官方文档](https://assertj.github.io/doc/)
- [Spring Boot 测试指南](https://spring.io/guides/gs/testing-web/)
- [Testcontainers 官方文档](https://www.testcontainers.org/)
- [Mockito 官方文档](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)

---

**祝您学习愉快！如有问题，请随时查看参考实现或寻求帮助。**
