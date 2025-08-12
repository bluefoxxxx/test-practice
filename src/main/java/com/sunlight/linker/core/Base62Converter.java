package com.sunlight.linker.core;

/**
 * Base62编码器 - 短链接服务的核心算法
 * 
 * 【测试学习重点】：
 * 这是一个纯函数工具类，非常适合进行单元测试的学习和实践：
 * 1. 无外部依赖，测试运行快速
 * 2. 输入输出明确，易于验证
 * 3. 可以覆盖边界条件、异常情况等多种测试场景
 * 4. 适合学习参数化测试、性能测试等高级测试技巧
 * 
 * 【业务价值】：
 * 将数据库中的自增ID转换为更短、更友好的字符串表示
 * 例如：ID 125 -> "1Z", ID 10086 -> "2mG"
 * 
 * 【算法说明】：
 * Base62使用0-9, a-z, A-Z共62个字符进行编码
 * 相比Base64，去除了+和/字符，避免在URL中产生歧义
 * 
 * @author 测试实践学习项目
 * @version 1.0
 */
public class Base62Converter {
    
    /**
     * Base62字符集：0-9, a-z, A-Z
     * 
     * 【设计考虑】：
     * - 数字在前：确保小数字转换结果更短
     * - 小写字母在中间：常见字符，易于识别
     * - 大写字母在后：区分大小写，增加编码空间
     */
    private static final String BASE62_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    
    /**
     * 进制基数
     */
    private static final int BASE = 62;
    
    /**
     * 将长整型ID编码为Base62字符串
     * 
     * 【测试要点】：
     * - 边界值测试：0, 1, 61, 62, Long.MAX_VALUE
     * - 负数处理：应该抛出异常
     * - 性能测试：大数字的转换时间
     * - 往返测试：encode后decode应该得到原值
     * 
     * @param id 要编码的ID，必须为非负数
     * @return Base62编码字符串，永远不会为null或空字符串
     * @throws IllegalArgumentException 当id为负数时抛出
     * 
     * 【算法时间复杂度】：O(log₆₂(id))
     * 【算法空间复杂度】：O(log₆₂(id))
     */
    public static String encode(long id) {
        // 【防御性编程】：参数验证
        if (id < 0) {
            throw new IllegalArgumentException("ID不能为负数，输入值: " + id);
        }
        
        // 【特殊情况处理】：0的特殊处理
        if (id == 0) {
            return "0";
        }
        
        // 【核心算法】：进制转换
        StringBuilder result = new StringBuilder();
        
        while (id > 0) {
            int remainder = (int) (id % BASE);
            result.append(BASE62_CHARS.charAt(remainder));
            id /= BASE;
        }
        
        // 【注意】：由于是从低位到高位构建，需要反转
        return result.reverse().toString();
    }
    
    /**
     * 将Base62字符串解码为长整型ID
     * 
     * 【测试要点】：
     * - 有效字符串测试：各种长度的合法输入
     * - 无效字符测试：包含非Base62字符的输入
     * - 空字符串和null测试：边界情况处理
     * - 大数字测试：接近Long.MAX_VALUE的情况
     * - 往返测试：decode后encode应该得到原字符串
     * 
     * @param encoded Base62编码字符串，不能为null或空
     * @return 解码后的ID值，保证非负
     * @throws IllegalArgumentException 当输入为null、空字符串或包含非法字符时抛出
     * @throws NumberFormatException 当解码结果超出Long范围时抛出
     * 
     * 【算法时间复杂度】：O(n)，其中n为字符串长度
     * 【算法空间复杂度】：O(1)
     */
    public static long decode(String encoded) {
        // 【防御性编程】：输入验证
        if (encoded == null) {
            throw new IllegalArgumentException("编码字符串不能为null");
        }
        
        if (encoded.isEmpty()) {
            throw new IllegalArgumentException("编码字符串不能为空");
        }
        
        long result = 0;
        long power = 1;
        
        // 【核心算法】：从右向左解析，逐位计算
        for (int i = encoded.length() - 1; i >= 0; i--) {
            char c = encoded.charAt(i);
            int digit = getCharValue(c);
            
            // 【溢出检查】：避免Long溢出
            // 检查 digit * power 是否会导致溢出
            if (power > 0 && digit > Long.MAX_VALUE / power) {
                throw new NumberFormatException("解码结果超出Long范围: " + encoded);
            }
            
            long digitValue = digit * power;
            
            // 检查 result + digitValue 是否会超出Long.MAX_VALUE
            if (result > Long.MAX_VALUE - digitValue) {
                throw new NumberFormatException("解码结果超出Long范围: " + encoded);
            }
            
            result += digitValue;
            
            // 【溢出检查】：power的溢出检查
            if (i > 0 && power > Long.MAX_VALUE / BASE) {
                throw new NumberFormatException("解码过程中发生溢出: " + encoded);
            }
            
            power *= BASE;
        }
        
        return result;
    }
    
    /**
     * 获取字符在Base62字符集中的数值
     * 
     * 【设计说明】：
     * 这是一个私有辅助方法，主要用于decode过程中的字符到数值转换
     * 通过indexOf查找字符位置，简单直观但不是最高效的实现
     * 
     * 【优化思路】：
     * 可以使用数组或Map进行字符到数值的映射，提高查找效率
     * 但考虑到Base62字符集较小，当前实现已足够高效
     * 
     * @param c 要转换的字符
     * @return 字符对应的数值（0-61）
     * @throws IllegalArgumentException 当字符不在Base62字符集中时抛出
     */
    private static int getCharValue(char c) {
        int index = BASE62_CHARS.indexOf(c);
        
        if (index == -1) {
            throw new IllegalArgumentException("非法的Base62字符: '" + c + "'");
        }
        
        return index;
    }
    
    /**
     * 检查字符串是否为有效的Base62编码
     * 
     * 【扩展功能】：
     * 这是一个工具方法，用于验证输入的有效性
     * 在实际应用中可以用于API参数验证
     * 
     * @param encoded 要检查的字符串
     * @return 如果是有效的Base62编码返回true，否则返回false
     */
    public static boolean isValidBase62(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return false;
        }
        
        for (char c : encoded.toCharArray()) {
            if (BASE62_CHARS.indexOf(c) == -1) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 获取Base62字符集
     * 
     * 【测试辅助】：
     * 提供给测试类使用，便于测试用例生成随机的有效字符
     * 
     * @return Base62字符集字符串
     */
    public static String getCharset() {
        return BASE62_CHARS;
    }
}