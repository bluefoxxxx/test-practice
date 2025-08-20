package com.sunlight.linker.exercises.core;

import com.sunlight.linker.core.Base62Converter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * 【练习1】Base62Converter单元测试练习
 * 
 * 【第一阶段测试实践 - 核心逻辑单元测试】
 * 
 * 这是测试金字塔中的基础层 - 单元测试练习，学习目标：
 * ✅ 掌握JUnit 5基本注解的使用
 * ✅ 学会使用AssertJ进行流式断言
 * ✅ 理解参数化测试的不同应用场景
 * ✅ 学会测试组织和异常测试
 * 
 * 【练习说明】：
 * 请根据TODO提示完成各个测试方法的实现
 * 重点关注：测试覆盖策略、边界值分析、异常处理
 * 
 * 【评分标准】：
 * - 基础功能测试 (30分)
 * - 边界值和异常测试 (25分) 
 * - 参数化测试应用 (25分)
 * - 测试代码质量和可读性 (20分)
 */
@DisplayName("【练习】Base62转换器测试")
class Base62ConverterExercise {

    /**
     * 编码功能测试组
     * 
     * 【练习要点】：
     * 使用@Nested注解组织相关测试，提高代码结构清晰度
     */
    @Nested
    @DisplayName("编码测试练习 - encode(long)")
    class EncodeTests {

        /**
         * 【练习1.1】基础有效值测试
         * 
         * TODO: 请完成以下测试用例的实现
         * 提示：使用assertThat().isEqualTo()进行断言
         * 
         * 测试用例：
         * - ID=0 应该返回 "0"
         * - ID=1 应该返回 "1" 
         * - ID=61 应该返回 "Z"
         * - ID=62 应该返回 "10"
         */
        @Test
        @DisplayName("应该正确编码基础有效值")
        void shouldEncodeValidBasicValues() {
            assertThat(Base62Converter.encode(0)).isEqualTo("0");
            assertThat(Base62Converter.encode(1)).isEqualTo("1");
            assertThat(Base62Converter.encode(61)).isEqualTo("Z");
            assertThat(Base62Converter.encode(62)).isEqualTo("10");
        }

        /**
         * 【练习1.2】参数化测试 - @ValueSource
         * 
         * TODO: 使用@ParameterizedTest和@ValueSource完成参数化测试
         * 提示：验证编码结果不为null且不为空字符串
         * 
         * 参数值：{0, 1, 10, 61, 62, 100, 1000, 10000}
         */
        @ParameterizedTest(name = "编码ID {0} 应该产生非空结果")
        @ValueSource(longs = {0, 1, 10, 61, 62, 100, 1000, 10000})
        @DisplayName("参数化测试 - 编码结果非空验证")
        void shouldReturnNonEmptyStringForValidIds(long id) {
            assertThat(Base62Converter.encode(id)).isNotNull().isNotEmpty();
        }

        /**
         * 【练习1.3】CSV参数化测试
         * 
         * TODO: 使用@CsvSource完成已知输入输出对的测试
         * 提示：第一列是输入ID，第二列是期望的编码结果
         */
        @ParameterizedTest(name = "ID {0} 应该编码为 '{1}'")
        @CsvSource({
            "0, 0",
            "1, 1", 
            "35, z",
            "61, Z",
            "62, 10",
            "3843, ZZ"
        })
        @DisplayName("CSV参数化测试 - 已知编码对验证")
        void shouldEncodeKnownValues(long id, String expectedCode) {
            assertThat(Base62Converter.encode(id)).isEqualTo(expectedCode);
        }

        /**
         * 【练习1.4】异常测试
         * 
         * TODO: 测试负数输入应该抛出IllegalArgumentException
         * 提示：使用assertThatThrownBy()或assertThrows()
         */
        @Test
        @DisplayName("负数应该抛出异常")
        void shouldThrowExceptionForNegativeNumbers() {
            assertThatThrownBy(() -> Base62Converter.encode(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ID不能为负数");

            assertThatThrownBy(() -> Base62Converter.encode(-10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ID不能为负数");

            assertThatThrownBy(() -> Base62Converter.encode(-100))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ID不能为负数");
        }
    }

    /**
     * 解码功能测试组
     */
    @Nested
    @DisplayName("解码测试练习 - decode(String)")
    class DecodeTests {

        /**
         * 【练习2.1】基础解码测试
         * 
         * TODO: 实现基础的解码功能测试
         * 测试用例：
         * - "0" -> 0
         * - "1" -> 1
         * - "Z" -> 61
         * - "10" -> 62
         */
        @Test
        @DisplayName("应该正确解码基础有效值")
        void shouldDecodeValidBasicValues() {
            assertThat(Base62Converter.decode("0")).isEqualTo(0);
            assertThat(Base62Converter.decode("1")).isEqualTo(1);
            assertThat(Base62Converter.decode("Z")).isEqualTo(61);
            assertThat(Base62Converter.decode("10")).isEqualTo(62);
        }

        /**
         * 【练习2.2】参数化解码测试
         * 
         * TODO: 使用@CsvSource完成已知解码对的测试
         */
        @ParameterizedTest(name = "字符串 '{0}' 应该解码为 {1}")
        @CsvSource({
            "0, 0",
            "1, 1",
            "z, 35", 
            "Z, 61",
            "10, 62",
            "ZZ, 3843"
        })
        @DisplayName("CSV参数化解码测试")
        void shouldDecodeKnownValues(String code, long expectedId) {
            assertThat(Base62Converter.decode(code)).isEqualTo(expectedId);
        }

        /**
         * 【练习2.3】异常输入测试
         * 
         * TODO: 测试各种异常输入情况
         * - null输入
         * - 空字符串
         * - 包含非法字符的字符串
         */
        @Test
        @DisplayName("异常输入应该抛出相应异常")
        void shouldThrowExceptionForInvalidInput() {
            assertThatThrownBy(() -> Base62Converter.decode(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("编码字符串不能为null");

            assertThatThrownBy(() -> Base62Converter.decode(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("编码字符串不能为空");

            assertThatThrownBy(() -> Base62Converter.decode("123++"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("非法的Base62字符");
        }

        /**
         * 【练习2.4】@MethodSource参数化测试
         * 
         * TODO: 创建一个静态方法提供测试数据，使用@MethodSource
         * 测试包含非法字符的各种情况
         */
        @ParameterizedTest(name = "非法字符串 '{0}' 应该抛出异常")
        @MethodSource("provideInvalidStrings")
        @DisplayName("MethodSource参数化异常测试")
        void shouldThrowExceptionForInvalidStrings(String invalidString) {
            assertThatThrownBy(() -> Base62Converter.decode(invalidString))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        /**
         * TODO: 实现为上面测试提供数据的静态方法
         * 提示：返回Stream<Arguments>或Stream<String>
         */
        static Stream<String> provideInvalidStrings() {
            // 例如：包含空格、特殊符号、中文字符等
            return Stream.of(
                    "hello world",
                    "123@qq.com",
                    "尼豪"
            );
        }
    }

    /**
     * 往返测试组
     */
    @Nested
    @DisplayName("往返测试练习 - encode后decode回原值")
    class RoundTripTests {

        /**
         * 【练习3.1】基础往返测试
         * 
         * TODO: 测试编码后解码能否回到原值
         * 这是测试算法正确性的重要方法
         */
        @Test
        @DisplayName("编码解码往返应该得到原始值")
        void shouldReturnOriginalValueAfterRoundTrip() {
            assertThat(Base62Converter.decode(Base62Converter.encode(0))).isEqualTo(0);
            assertThat(Base62Converter.decode(Base62Converter.encode(1))).isEqualTo(1);
            assertThat(Base62Converter.decode(Base62Converter.encode(35))).isEqualTo(35);
            assertThat(Base62Converter.decode(Base62Converter.encode(3843))).isEqualTo(3843);
        }

        /**
         * 【练习3.2】参数化往返测试
         * 
         * TODO: 使用@ValueSource进行大量往返测试
         */
        @ParameterizedTest(name = "ID {0} 往返测试")
        @ValueSource(longs = {0, 1, 10, 35, 61, 62, 100, 1000, 999999})
        @DisplayName("参数化往返测试")
        void shouldMaintainValueThroughRoundTrip(long originalId) {
            assertThat(Base62Converter.decode(Base62Converter.encode(originalId))).isEqualTo(originalId);
        }

        /**
         * 【练习3.3】边界值往返测试
         * 
         * TODO: 专门测试边界值的往返正确性
         * 重点测试：0, 1, 61, 62, Long.MAX_VALUE等
         */
        @Test
        @DisplayName("边界值往返测试")
        void shouldHandleBoundaryValuesCorrectly() {

            long[] values = {0, 1, 61, 62, Long.MAX_VALUE - 1, Long.MAX_VALUE};

            for(long value : values) {
                assertThat(Base62Converter.decode(Base62Converter.encode(value))).isEqualTo(value);
            }
        }
    }

    /**
     * 工具方法测试组
     */
    @Nested
    @DisplayName("工具方法测试练习")
    class UtilityMethodTests {

        /**
         * 【练习4.1】字符值映射测试
         * 
         * TODO: 如果Base62Converter有getCharValue等辅助方法，进行测试
         * 这是白盒测试的体现，测试内部实现细节
         */
        @Test
        @DisplayName("字符到数值的映射测试")
        void shouldMapCharactersToValuesCorrectly() throws Exception {
            // TODO: 如果有公开的辅助方法，进行测试
            // 这个练习可以根据实际的Base62Converter实现调整
            Method getCharValueMethod = Base62Converter.class.getDeclaredMethod("getCharValue", char.class);

            getCharValueMethod.setAccessible(true);

            assertThat((Integer) getCharValueMethod.invoke(null, '0')).isEqualTo(0);
            assertThat((Integer) getCharValueMethod.invoke(null, '9')).isEqualTo(9);
            assertThat((Integer) getCharValueMethod.invoke(null, 'a')).isEqualTo(10);
            assertThat((Integer) getCharValueMethod.invoke(null, 'z')).isEqualTo(35);
            assertThat((Integer) getCharValueMethod.invoke(null, 'A')).isEqualTo(36);
            assertThat((Integer) getCharValueMethod.invoke(null, 'Z')).isEqualTo(61);

            // 测试非法字符应该抛出异常
            // 当被调用的方法内部抛出异常时，反射会把它包装在 InvocationTargetException 里
            assertThatThrownBy(() -> getCharValueMethod.invoke(null, '-'))
                    .isInstanceOf(InvocationTargetException.class)
                    .getCause() // 获取内部异常
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("非法的Base62字符");
        }
    }

    /**
     * 性能和边界测试组
     */
    @Nested
    @DisplayName("性能和边界测试练习")
    class PerformanceAndBoundaryTests {

        /**
         * 【练习5.1】大数值测试
         * 
         * TODO: 测试接近Long.MAX_VALUE的大数值
         * 验证算法在极限情况下的正确性
         */
        @Test
        @DisplayName("大数值编码测试")
        void shouldHandleLargeNumbers() {
            // 测试 long 类型最大值
            long maxValue = Long.MAX_VALUE;
            String encodedMax = Base62Converter.encode(maxValue);
            // 基本测试
            assertThat(encodedMax).isNotNull().isNotEmpty();
            System.out.println("Long.MAX_VALUE (" + maxValue + ") -> Base62: " + encodedMax);
            // 往返测试
            assertThat(Base62Converter.decode(encodedMax))
                    .isEqualTo(maxValue);

            // 测试 Long.MAX_VALUE - 1
            assertThat(Base62Converter.decode(Base62Converter.encode(Long.MAX_VALUE - 1))).isEqualTo(Long.MAX_VALUE - 1);
        }

        /**
         * 【练习5.2】性能基准测试
         * 
         * TODO: 简单的性能测试，验证算法效率
         * 注意：这不是严格的性能测试，只是基本验证
         */
        @Test
        @DisplayName("基础性能测试")
        @Timeout(1)
        void shouldPerformReasonablyFast() {
            // 测试大量编码/解码操作的耗时
            for(int i = 0; i < 10000; i++) {
                Base62Converter.decode(Base62Converter.encode(i));
            }
        }
    }
}
