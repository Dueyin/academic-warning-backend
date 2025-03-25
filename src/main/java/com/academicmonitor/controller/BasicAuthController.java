package com.academicmonitor.controller;

import com.academicmonitor.entity.User;
import com.academicmonitor.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/basic")
public class BasicAuthController {
    private static final Logger logger = LoggerFactory.getLogger(BasicAuthController.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "API is working");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> credentials) {
        logger.info("尝试登录用户: {}", credentials.get("username"));
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");
            
            if (username == null || password == null) {
                response.put("success", false);
                response.put("message", "用户名和密码不能为空");
                return response;
            }
            
            User user = userRepository.findByUsername(username).orElse(null);
            
            if (user == null) {
                logger.info("用户不存在: {}", username);
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }
            
            boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
            
            if (passwordMatches) {
                logger.info("登录成功: {}", username);
                response.put("success", true);
                response.put("message", "登录成功");
                response.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(), 
                    "name", user.getName(),
                    "email", user.getEmail()
                ));
            } else {
                logger.info("密码错误: {}", username);
                response.put("success", false);
                response.put("message", "密码错误");
            }
        } catch (Exception e) {
            logger.error("登录时发生错误", e);
            response.put("success", false);
            response.put("message", "服务器错误: " + e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/checkDatabase")
    public Map<String, Object> checkDatabase() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            long userCount = userRepository.count();
            response.put("success", true);
            response.put("userCount", userCount);
            response.put("message", "数据库连接正常");
            
            // 打印所有用户信息用于调试
            userRepository.findAll().forEach(user -> {
                logger.info("用户: ID={}, Username={}, Enabled={}, Roles={}", 
                    user.getId(), user.getUsername(), user.getEnabled(), user.getRoles());
            });
        } catch (Exception e) {
            logger.error("数据库检查错误", e);
            response.put("success", false);
            response.put("message", "数据库错误: " + e.getMessage());
        }
        
        return response;
    }
} 