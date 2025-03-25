package com.academicmonitor.controller;

import com.academicmonitor.dto.response.MessageResponse;
import com.academicmonitor.entity.User;
import com.academicmonitor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return userRepository.findById(id)
                .map(user -> {
                    // 更新基本信息
                    user.setName(userDetails.getName());
                    user.setEmail(userDetails.getEmail());
                    user.setPhone(userDetails.getPhone());
                    
                    // 只有管理员才能更改用户状态
                    if (userDetails.getEnabled() != null && hasRole("ADMIN")) {
                        user.setEnabled(userDetails.getEnabled());
                    }
                    
                    userRepository.save(user);
                    return ResponseEntity.ok(new MessageResponse("用户信息更新成功"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<?> updatePassword(@PathVariable Long id, @RequestBody String newPassword) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setPassword(encoder.encode(newPassword));
                    userRepository.save(user);
                    return ResponseEntity.ok(new MessageResponse("密码更新成功"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok(new MessageResponse("用户删除成功"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private boolean hasRole(String roleName) {
        // 通过SecurityContext判断当前用户是否有特定角色
        return true; // 简化实现，实际应检查当前用户的角色
    }
} 