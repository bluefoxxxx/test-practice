package com.sunlight.linker.web;

import com.sunlight.linker.application.ShortLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URI;
import java.util.Optional;

/**
 * 短链接重定向控制器
 * 
 * 【设计说明】：
 * 专门处理短链接重定向功能的控制器，独立于REST API控制器
 * 重定向功能通常放在根路径下，提供更简洁的短链接URL
 * 
 * 【测试重点】：
 * - 短链接重定向是系统的核心功能，需要重点测试
 * - HTTP 302重定向的正确性
 * - Location头的URL格式验证
 * - 访问计数的增加逻辑
 * 
 * @author 测试实践学习项目
 * @version 1.0
 */
@Controller
public class RedirectController {
    
    private final ShortLinkService shortLinkService;
    
    @Autowired
    public RedirectController(ShortLinkService shortLinkService) {
        this.shortLinkService = shortLinkService;
    }
    
    /**
     * 短链接重定向API
     * 
     * 【HTTP设计】：
     * - 方法：GET
     * - 路径：/s/{shortCode}
     * - 响应：302 Found + Location头指向原始长链接
     * 
     * 【业务逻辑】：
     * 这是短链接服务的核心功能，用户访问短链接时重定向到原始长链接
     * 同时增加访问统计计数，用于数据分析
     * 
     * 【测试要点】：
     * - 存在的短码返回302状态码和正确的Location头
     * - 不存在的短码返回404状态码
     * - Location头的URL格式正确性
     * - 访问计数的正确增加（通过Service层测试验证）
     * 
     * @param shortCode 短码字符串
     * @return 重定向响应实体
     */
    @GetMapping("/s/{shortCode}")
    public ResponseEntity<Void> redirectToLongUrl(@PathVariable String shortCode) {
        try {
            Optional<String> longUrl = shortLinkService.getLongUrl(shortCode);
            
            if (longUrl.isPresent()) {
                // 302临时重定向到原始长链接
                return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(longUrl.get()))
                    .build();
            } else {
                // 短码不存在，返回404
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            // 服务层异常，返回500状态码
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}