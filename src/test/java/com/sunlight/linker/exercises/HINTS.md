# 练习提示和解答指南

## 💡 通用提示

### 基础注解使用
```java
// JUnit 5 基础注解
@Test                           // 标记测试方法
@DisplayName("测试描述")         // 测试显示名称
@BeforeEach                     // 每个测试前执行
@Nested                         // 嵌套测试类

// Spring Boot 测试注解
@SpringBootTest                 // 完整Spring上下文
@WebMvcTest(Controller.class)   // 只加载Web层
@MockBean                       // 创建Mock Bean
@Autowired                      // 注入依赖
```

### AssertJ 断言语法
```java
// 基础断言
assertThat(result).isNotNull();
assertThat(result).isEqualTo(expected);
assertThat(result).isNotEmpty();

// 字符串断言
assertThat(result).hasSize(5);
assertThat(result).startsWith("prefix");
assertThat(result).matches("[a-zA-Z0-9]+");

// 异常断言
assertThatThrownBy(() -> method())
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessageContaining("错误信息");
```

## 🔧 练习1：单元测试提示

### 1.1 基础编码测试
```java
@Test
@DisplayName("应该正确编码基础有效值")
void shouldEncodeValidBasicValues() {
    // 测试关键用例
    assertThat(Base62Converter.encode(0))
        .as("ID为0时应该返回字符'0'")
        .isEqualTo("0");
    
    assertThat(Base62Converter.encode(1))
        .as("ID为1时应该返回字符'1'")
        .isEqualTo("1");
        
    // 边界值测试
    assertThat(Base62Converter.encode(61))
        .as("ID为61时应该返回最后一个Base62字符")
        .isEqualTo("Z");
}
```

### 1.2 参数化测试提示
```java
@ParameterizedTest(name = "编码ID {0} 应该产生非空结果")
@ValueSource(longs = {0, 1, 10, 61, 62, 100, 1000, 10000})
@DisplayName("参数化测试 - 编码结果非空验证")
void shouldReturnNonEmptyStringForValidIds(long id) {
    String result = Base62Converter.encode(id);
    assertThat(result)
        .as("编码结果不应为null或空")
        .isNotNull()
        .isNotEmpty()
        .matches("[0-9a-zA-Z]+"); // 验证只包含Base62字符
}
```

### 1.3 异常测试提示
```java
@Test
@DisplayName("负数应该抛出异常")
void shouldThrowExceptionForNegativeNumbers() {
    // 测试多个负数值
    assertThatThrownBy(() -> Base62Converter.encode(-1))
        .isInstanceOf(IllegalArgumentException.class);
        
    assertThatThrownBy(() -> Base62Converter.encode(-100))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("负数");
}
```

### 1.4 MethodSource提示
```java
@ParameterizedTest
@MethodSource("provideInvalidStrings")
void shouldThrowExceptionForInvalidStrings(String invalidString) {
    assertThatThrownBy(() -> Base62Converter.decode(invalidString))
        .isInstanceOf(IllegalArgumentException.class);
}

static Stream<String> provideInvalidStrings() {
    return Stream.of(
        "hello world",  // 包含空格
        "test@123",     // 包含特殊符号
        "测试",         // 包含中文
        "hello-world"   // 包含连字符
    );
}
```

## 🔧 练习2：Service集成测试提示

### 2.1 基础Mock配置
```java
@SpringBootTest
@ActiveProfiles("test")
class ShortLinkServiceExercise {
    
    @Autowired
    private ShortLinkService shortLinkService;
    
    @MockBean
    private ShortLinkRepository shortLinkRepository;
    
    @BeforeEach
    void setUp() {
        reset(shortLinkRepository); // 重置Mock状态
        // 创建测试数据
        testShortLink = createShortLinkWithId(1L, VALID_LONG_URL, VALID_SHORT_CODE);
    }
}
```

### 2.2 Mock行为定义
```java
@Test
void shouldCreateNewShortLink() {
    // Given - 配置Mock行为
    when(shortLinkRepository.findByLongUrl(VALID_LONG_URL))
        .thenReturn(Optional.empty());
    
    ShortLink savedWithId = createShortLinkWithId(1L, VALID_LONG_URL, "temp");
    ShortLink finalLink = createShortLinkWithId(1L, VALID_LONG_URL, "1");
    
    when(shortLinkRepository.save(any(ShortLink.class)))
        .thenReturn(savedWithId)    // 第一次保存
        .thenReturn(finalLink);     // 第二次保存
    
    // When
    ShortLink result = shortLinkService.createShortLink(VALID_LONG_URL);
    
    // Then
    assertThat(result)
        .isNotNull()
        .satisfies(link -> {
            assertThat(link.getLongUrl()).isEqualTo(VALID_LONG_URL);
            assertThat(link.getShortCode()).isEqualTo("1");
            assertThat(link.getIsCustomAlias()).isFalse();
        });
    
    // 验证方法调用
    verify(shortLinkRepository, times(1)).findByLongUrl(VALID_LONG_URL);
    verify(shortLinkRepository, times(2)).save(any(ShortLink.class));
}
```

### 2.3 ArgumentCaptor使用
```java
@Test
void shouldUpdateAccessCountCorrectly() {
    // Given
    when(shortLinkRepository.findByShortCode(VALID_SHORT_CODE))
        .thenReturn(Optional.of(testShortLink));
    
    ArgumentCaptor<ShortLink> linkCaptor = ArgumentCaptor.forClass(ShortLink.class);
    when(shortLinkRepository.save(linkCaptor.capture()))
        .thenReturn(testShortLink);
    
    // When
    shortLinkService.getLongUrl(VALID_SHORT_CODE);
    
    // Then - 验证捕获的参数
    ShortLink capturedLink = linkCaptor.getValue();
    assertThat(capturedLink.getAccessCount())
        .isEqualTo(testShortLink.getAccessCount() + 1);
}
```

## 🔧 练习3：Testcontainers提示

### 3.1 容器配置
```java
@SpringBootTest
@Testcontainers
class ShortLinkServiceRealDbExercise {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

### 3.2 数据库集成测试
```java
@Test
void shouldPersistShortLinkDataCompletely() {
    // Given
    String testUrl = "https://example.com/test";
    
    // When - 通过Service创建
    ShortLink result = shortLinkService.createShortLink(testUrl);
    
    // Then - 验证Service返回
    assertThat(result)
        .isNotNull()
        .satisfies(link -> {
            assertThat(link.getId()).isNotNull();
            assertThat(link.getLongUrl()).isEqualTo(testUrl);
            assertThat(link.getShortCode()).isNotEmpty();
            assertThat(link.getCreatedAt()).isNotNull();
        });
    
    // 直接查询数据库验证持久化
    Optional<ShortLink> saved = shortLinkRepository.findById(result.getId());
    assertThat(saved).isPresent();
    assertThat(saved.get().getLongUrl()).isEqualTo(testUrl);
}
```

## 🔧 练习4：API测试提示

### 4.1 MockMvc基础用法
```java
@WebMvcTest(ShortLinkController.class)
class ShortLinkControllerExercise {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ShortLinkService shortLinkService;
    
    @Autowired
    private ObjectMapper objectMapper;
}
```

### 4.2 POST请求测试
```java
@Test
void shouldCreateShortLinkSuccessfully() throws Exception {
    // Given
    when(shortLinkService.createShortLink(anyString()))
        .thenReturn(testShortLink);
    
    String requestJson = "{\"longUrl\":\"" + VALID_LONG_URL + "\"}";
    
    // When & Then
    mockMvc.perform(post("/api/v1/links")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.shortCode").value(VALID_SHORT_CODE))
        .andExpect(jsonPath("$.longUrl").value(VALID_LONG_URL))
        .andExpect(jsonPath("$.isCustomAlias").value(false));
}
```

### 4.3 GET请求和错误处理
```java
@Test
void shouldReturn404ForNonExistentShortCode() throws Exception {
    // Given
    when(shortLinkService.getShortLinkDetails("nonexistent"))
        .thenReturn(null);
    
    // When & Then
    mockMvc.perform(get("/api/v1/links/nonexistent"))
        .andExpect(status().isNotFound());
}
```

### 4.4 参数验证测试
```java
@Test
void shouldReturn400ForInvalidRequests() throws Exception {
    // 测试空请求体
    mockMvc.perform(post("/api/v1/links")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());
    
    // 测试无效URL
    String invalidJson = "{\"longUrl\":\"not-a-url\"}";
    mockMvc.perform(post("/api/v1/links")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson))
        .andExpected(status().isBadRequest());
}
```

## 🔧 练习5：重定向测试提示

### 5.1 302重定向测试
```java
@Test
void shouldRedirectToLongUrlSuccessfully() throws Exception {
    // Given
    when(shortLinkService.getLongUrl(VALID_SHORT_CODE))
        .thenReturn(Optional.of(VALID_LONG_URL));
    
    // When & Then
    mockMvc.perform(get("/s/" + VALID_SHORT_CODE))
        .andExpect(status().isFound())                    // 302状态码
        .andExpect(header().string("Location", VALID_LONG_URL))
        .andExpect(redirectedUrl(VALID_LONG_URL));        // 重定向URL验证
    
    // 验证Service调用
    verify(shortLinkService, times(1)).getLongUrl(VALID_SHORT_CODE);
}
```

### 5.2 404错误测试
```java
@Test
void shouldReturn404ForNonExistentShortCode() throws Exception {
    // Given
    when(shortLinkService.getLongUrl("nonexistent"))
        .thenReturn(Optional.empty());
    
    // When & Then
    mockMvc.perform(get("/s/nonexistent"))
        .andExpect(status().isNotFound())
        .andExpect(header().doesNotExist("Location"));  // 确保没有Location头
}
```

### 5.3 异常处理测试
```java
@Test
void shouldHandleServiceExceptions() throws Exception {
    // Given
    when(shortLinkService.getLongUrl(anyString()))
        .thenThrow(new RuntimeException("数据库连接失败"));
    
    // When & Then
    mockMvc.perform(get("/s/error"))
        .andExpect(status().isInternalServerError());  // 500错误
}
```

## 🎯 调试技巧

### 1. 打印HTTP请求响应
```java
mockMvc.perform(get("/api/v1/links"))
    .andDo(print())  // 打印详细的请求响应信息
    .andExpect(status().isOk());
```

### 2. Mock验证调试
```java
// 验证Mock是否被调用
verify(shortLinkService, times(1)).createShortLink(anyString());

// 验证Mock从未被调用
verify(shortLinkService, never()).deleteShortLink(anyString());

// 验证Mock调用参数
verify(shortLinkService).createShortLink(eq(VALID_LONG_URL));
```

### 3. ArgumentCaptor调试
```java
ArgumentCaptor<ShortLink> linkCaptor = ArgumentCaptor.forClass(ShortLink.class);
verify(mockRepository).save(linkCaptor.capture());

ShortLink capturedLink = linkCaptor.getValue();
System.out.println("Captured: " + capturedLink); // 调试打印
```

## ⚠️ 常见错误

### 1. Mock配置错误
```java
// ❌ 错误：没有配置Mock行为
when(mockService.someMethod()).thenReturn(null); // 会导致NullPointerException

// ✅ 正确：配置有意义的返回值
when(mockService.someMethod()).thenReturn(expectedValue);
```

### 2. 参数化测试错误
```java
// ❌ 错误：方法不是static
Stream<String> provideTestData() { ... }

// ✅ 正确：方法必须是static
static Stream<String> provideTestData() { ... }
```

### 3. JSON路径错误
```java
// ❌ 错误：路径不正确
.andExpect(jsonPath("shortCode").value("abc123"))

// ✅ 正确：使用$符号表示根路径
.andExpect(jsonPath("$.shortCode").value("abc123"))
```

## 🚀 进阶挑战提示

### 练习6：Redis缓存测试

#### 6.1 Redis容器配置
```java
@Container
static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
        .withExposedPorts(6379)
        .withReuse(true);

@DynamicPropertySource
static void configureProperties(DynamicPropertyRegistry registry) {
    // 配置Redis连接
    registry.add("spring.data.redis.host", redis::getHost);
    registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    registry.add("spring.cache.type", () -> "redis");
}
```

#### 6.2 缓存功能测试
```java
@Test
void shouldCacheShortLinkQueries() {
    // Given - 创建短链接
    ShortLink created = cachedShortLinkService.createShortLink(TEST_LONG_URL);
    
    // When - 查询并检查缓存
    Optional<String> result = cachedShortLinkService.getLongUrl(created.getShortCode());
    
    // Then - 验证缓存存在
    String cacheKey = "shortlink:" + created.getShortCode();
    assertThat(redisTemplate.hasKey(cacheKey)).isTrue();
}
```

#### 6.3 缓存并发测试
```java
@Test
void shouldHandleConcurrentQueriesThreadSafe() {
    List<CompletableFuture<Optional<String>>> futures = new ArrayList<>();
    
    for (int i = 0; i < 10; i++) {
        futures.add(CompletableFuture.supplyAsync(() -> 
            cachedShortLinkService.getLongUrl(shortCode)));
    }
    
    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    
    // 验证所有结果一致
    futures.forEach(future -> {
        assertThat(future.join()).isPresent().contains(expectedUrl);
    });
}
```

### 练习7：并发测试

#### 7.1 并发测试工具使用
```java
@Test
void shouldCreateDifferentShortLinksThreadSafe() {
    ConcurrencyTestUtils.ConcurrentTestResult<ShortLink> result = 
        ConcurrencyTestUtils.runConcurrentTasks(
            () -> shortLinkService.createShortLink("https://example.com/" + System.nanoTime()),
            10,  // 线程数
            5,   // 每线程任务数
            30   // 超时秒数
        );
    
    assertThat(result.hasExceptions()).isFalse();
    assertThat(result.getSuccessCount()).isEqualTo(50);
}
```

#### 7.2 竞态条件测试
```java
@Test
void shouldPreventRaceConditionInCustomAliasCreation() {
    AtomicInteger successCount = new AtomicInteger(0);
    
    ConcurrencyTestUtils.runReadWriteConcurrentTest(
        () -> { // 读任务
            try {
                shortLinkService.createShortLink(url1, "popular-alias");
                successCount.incrementAndGet();
                return "success";
            } catch (Exception e) {
                return e;
            }
        },
        () -> { // 写任务
            try {
                shortLinkService.createShortLink(url2, "popular-alias");
                successCount.incrementAndGet();
                return "success";
            } catch (Exception e) {
                return e;
            }
        },
        5, 5, 2
    );
    
    // 验证只有一个成功
    await().untilAsserted(() -> {
        long count = shortLinkRepository.countByShortCode("popular-alias");
        assertThat(count).isLessThanOrEqualTo(1);
    });
}
```

### 练习8：契约测试

#### 8.1 HTTP契约验证
```java
@Test
void shouldComplyWithCreateSuccessContract() throws Exception {
    when(shortLinkService.createShortLink(anyString())).thenReturn(testShortLink);
    
    String requestBody = """
        {
            "longUrl": "https://www.example.com/test"
        }
        """;
    
    mockMvc.perform(post("/api/v1/links")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        // 验证响应字段契约
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.shortCode").value(matchesPattern("^[a-zA-Z0-9]+$")))
        .andExpect(jsonPath("$.longUrl").value(startsWith("https://")))
        .andExpect(jsonPath("$.isCustomAlias").isBoolean())
        // 验证响应头契约
        .andExpect(header().string("Content-Type", "application/json"))
        .andExpect(header().string("X-Content-Type-Options", "nosniff"));
}
```

#### 8.2 错误响应契约
```java
@Test
void shouldComplyWithValidationErrorContract() throws Exception {
    String invalidRequestBody = """
        {
            "longUrl": "not-a-valid-url"
        }
        """;
    
    mockMvc.perform(post("/api/v1/links")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidRequestBody))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        // 标准错误格式契约
        .andExpect(jsonPath("$.error").isString())
        .andExpect(jsonPath("$.message").isString())
        .andExpect(jsonPath("$.timestamp").isString())
        .andExpect(jsonPath("$.path").value("/api/v1/links"))
        .andExpect(jsonPath("$.status").value(400));
}
```

#### 8.3 重定向契约验证
```java
@Test
void shouldComplyWithRedirectSuccessContract() throws Exception {
    when(shortLinkService.getLongUrl("abc123"))
        .thenReturn(Optional.of("https://www.example.com/target"));
    
    mockMvc.perform(get("/s/abc123"))
        .andExpect(status().isFound())  // 302
        .andExpect(header().string("Location", "https://www.example.com/target"))
        .andExpect(header().string("Cache-Control", "no-cache"))
        .andExpect(content().string(emptyString()));  // 无响应体
}
```

## 🔧 进阶调试技巧

### 1. Redis调试
```java
// 查看缓存内容
String cacheKey = "shortlink:" + shortCode;
Object cached = redisTemplate.opsForValue().get(cacheKey);
System.out.println("Cached value: " + cached);

// 查看TTL
Long ttl = redisTemplate.getExpire(cacheKey, TimeUnit.SECONDS);
System.out.println("TTL: " + ttl + " seconds");
```

### 2. 并发测试调试
```java
// 监控线程状态
ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
System.out.println("Active threads: " + threadBean.getThreadCount());

// 监控数据库连接
HikariDataSource dataSource = (HikariDataSource) applicationContext.getBean(DataSource.class);
System.out.println("Active connections: " + dataSource.getHikariPoolMXBean().getActiveConnections());
```

### 3. 契约测试调试
```java
// 打印完整的HTTP交互
mockMvc.perform(post("/api/v1/links")...)
    .andDo(print())  // 打印请求响应详情
    .andDo(result -> {
        System.out.println("Response body: " + result.getResponse().getContentAsString());
        System.out.println("Response headers: " + result.getResponse().getHeaderNames());
    });
```

## ⚡ 进阶最佳实践

### 1. 缓存测试
- 总是在测试前清理缓存状态
- 使用@DirtiesContext注解隔离缓存影响
- 测试缓存的TTL和过期策略
- 验证缓存在异常情况下的行为

### 2. 并发测试
- 使用足够的线程数和操作次数
- 验证数据的最终一致性
- 使用Awaitility等待异步操作完成
- 监控系统资源使用情况

### 3. 契约测试
- 保持API响应格式的稳定性
- 使用JSON Schema验证复杂响应
- 测试不同HTTP方法的行为
- 验证错误响应的一致性

---

**记住：进阶测试需要更深入的系统理解和更细致的验证策略。好的进阶测试能够发现系统在复杂场景下的问题，确保生产环境的稳定性！**
