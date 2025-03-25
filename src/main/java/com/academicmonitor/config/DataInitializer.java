package com.academicmonitor.config;

import com.academicmonitor.entity.Role;
import com.academicmonitor.entity.User;
import com.academicmonitor.repository.RoleRepository;
import com.academicmonitor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 初始化角色
        initRoles();
        
        // 初始化管理员账户
        initAdminUser();
    }

    private void initRoles() {
        // 只有当角色表为空时才初始化
        if (roleRepository.count() == 0) {
            Role adminRole = new Role();
            adminRole.setName(Role.ROLE_ADMIN);
            adminRole.setDescription("系统管理员");
            roleRepository.save(adminRole);

            Role teacherRole = new Role();
            teacherRole.setName(Role.ROLE_TEACHER);
            teacherRole.setDescription("教师");
            roleRepository.save(teacherRole);

            Role studentRole = new Role();
            studentRole.setName(Role.ROLE_STUDENT);
            studentRole.setDescription("学生");
            roleRepository.save(studentRole);
            
            System.out.println("角色初始化完成");
        }
    }

    private void initAdminUser() {
        // 检查是否已有管理员账户
        if (!userRepository.existsByUsername("admin")) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setEmail("admin@example.com");
            adminUser.setName("系统管理员");
            adminUser.setEnabled(true);

            Role adminRole = roleRepository.findByName(Role.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("管理员角色未找到"));
            
            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            adminUser.setRoles(roles);

            userRepository.save(adminUser);
            
            System.out.println("管理员账户初始化完成");
        }
    }
} 