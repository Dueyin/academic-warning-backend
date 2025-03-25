package com.academicmonitor.controller;

import com.academicmonitor.dto.request.LoginRequest;
import com.academicmonitor.dto.request.SignupRequest;
import com.academicmonitor.dto.response.JwtResponse;
import com.academicmonitor.dto.response.MessageResponse;
import com.academicmonitor.entity.Role;
import com.academicmonitor.entity.User;
import com.academicmonitor.repository.RoleRepository;
import com.academicmonitor.repository.UserRepository;
import com.academicmonitor.security.JwtUtils;
import com.academicmonitor.security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:5173", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("尝试登录用户: {}", loginRequest.getUsername());
        
        try {
            // 验证用户存在
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElse(null);
            
            if (user == null) {
                logger.error("登录失败: 用户不存在 - {}", loginRequest.getUsername());
                return ResponseEntity.badRequest().body(new MessageResponse("错误：用户不存在"));
            }
            
            logger.info("用户存在，尝试认证: {}", loginRequest.getUsername());
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            logger.info("登录成功: {}", loginRequest.getUsername());
            
            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    user.getName(),
                    roles));
        } catch (Exception e) {
            logger.error("登录过程中发生错误: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(new MessageResponse("登录失败: " + e.getMessage()));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("错误: 用户名已被使用!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("错误: 邮箱已被使用!"));
        }

        // 创建新用户账号
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setName(signUpRequest.getName());
        user.setPhone(signUpRequest.getPhone());
        user.setEnabled(true);

        Set<String> strRoles = signUpRequest.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName("ROLE_STUDENT")
                    .orElseThrow(() -> new RuntimeException("错误: 角色未找到."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                                .orElseThrow(() -> new RuntimeException("错误: 角色未找到."));
                        roles.add(adminRole);
                        break;
                    case "teacher":
                        Role teacherRole = roleRepository.findByName("ROLE_TEACHER")
                                .orElseThrow(() -> new RuntimeException("错误: 角色未找到."));
                        roles.add(teacherRole);
                        break;
                    default:
                        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                                .orElseThrow(() -> new RuntimeException("错误: 角色未找到."));
                        roles.add(studentRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("用户注册成功!"));
    }
} 