package com.academicmonitor.controller;

import com.academicmonitor.entity.Role;
import com.academicmonitor.entity.User;
import com.academicmonitor.repository.RoleRepository;
import com.academicmonitor.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/init")
public class InitController {
    private static final Logger logger = LoggerFactory.getLogger(InitController.class);
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @GetMapping("/reset")
    public Map<String, Object> resetDatabase() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 清除现有数据
            logger.info("清理现有数据...");
            userRepository.deleteAll();
            roleRepository.deleteAll();
            
            // 创建角色
            logger.info("创建角色...");
            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            adminRole.setDescription("系统管理员");
            adminRole.setCreatedAt(LocalDateTime.now());
            adminRole.setUpdatedAt(LocalDateTime.now());
            adminRole.setIsDeleted(false);
            adminRole = roleRepository.save(adminRole);
            
            Role teacherRole = new Role();
            teacherRole.setName("ROLE_TEACHER");
            teacherRole.setDescription("教师");
            teacherRole.setCreatedAt(LocalDateTime.now());
            teacherRole.setUpdatedAt(LocalDateTime.now());
            teacherRole.setIsDeleted(false);
            roleRepository.save(teacherRole);
            
            Role studentRole = new Role();
            studentRole.setName("ROLE_STUDENT");
            studentRole.setDescription("学生");
            studentRole.setCreatedAt(LocalDateTime.now());
            studentRole.setUpdatedAt(LocalDateTime.now());
            studentRole.setIsDeleted(false);
            roleRepository.save(studentRole);
            
            // 创建管理员用户
            logger.info("创建管理员用户...");
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setName("系统管理员");
            adminUser.setEmail("admin@example.com");
            adminUser.setEnabled(true);
            adminUser.setCreatedAt(LocalDateTime.now());
            adminUser.setUpdatedAt(LocalDateTime.now());
            adminUser.setIsDeleted(false);
            
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            adminUser.setRoles(roles);
            
            userRepository.save(adminUser);
            
            result.put("success", true);
            result.put("message", "数据库重置成功");
            logger.info("数据库初始化成功");
        } catch (Exception e) {
            logger.error("数据库重置失败", e);
            result.put("success", false);
            result.put("message", "数据库重置失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @GetMapping("/check")
    public Map<String, Object> checkData() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            long roleCount = roleRepository.count();
            long userCount = userRepository.count();
            
            result.put("success", true);
            result.put("roleCount", roleCount);
            result.put("userCount", userCount);
            
            if (roleCount > 0) {
                roleRepository.findAll().forEach(role -> {
                    logger.info("角色: {}", role.getName());
                });
            }
            
            if (userCount > 0) {
                userRepository.findAll().forEach(user -> {
                    logger.info("用户: {}, 密码长度: {}, 角色: {}", 
                            user.getUsername(), 
                            user.getPassword().length(),
                            user.getRoles());
                });
            }
        } catch (Exception e) {
            logger.error("数据检查失败", e);
            result.put("success", false);
            result.put("message", "数据检查失败: " + e.getMessage());
        }
        
        return result;
    }
} 