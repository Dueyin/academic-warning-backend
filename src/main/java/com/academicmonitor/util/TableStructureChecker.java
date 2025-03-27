package com.academicmonitor.util;

import com.academicmonitor.entity.*;
import com.academicmonitor.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 表结构检查工具类
 * 用于确保数据库中所有必要的表结构都被创建
 */
@Configuration
@RequiredArgsConstructor
public class TableStructureChecker {

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ClassGroupRepository classGroupRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final GradeRepository gradeRepository;
    private final WarningRuleRepository warningRuleRepository;
    private final WarningRecordRepository warningRecordRepository;
    private final NotificationRepository notificationRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * 在应用启动时检查并创建所有必需的表结构
     */
    @Bean
    public CommandLineRunner checkTablesOnStartup() {
        return args -> {
            System.out.println("======== 正在检查数据库表结构 ========");
            
            // 获取所有已存在表
            List<String> existingTables = new ArrayList<>();
            try {
                jdbcTemplate.query("SHOW TABLES", rs -> {
                    existingTables.add(rs.getString(1).toLowerCase());
                });
                System.out.println("发现现有表: " + String.join(", ", existingTables));
            } catch (Exception e) {
                System.out.println("无法获取表列表: " + e.getMessage());
                return;
            }
            
            // 必需的表列表
            List<String> requiredTables = Arrays.asList(
                "roles", "users", "user_roles", "teachers", "students", 
                "class_groups", "courses", "course_enrollments", "grades",
                "warning_rules", "warning_records", "notifications"
            );
            
            // 检查缺失的表
            List<String> missingTables = new ArrayList<>();
            for (String table : requiredTables) {
                if (!existingTables.contains(table.toLowerCase())) {
                    missingTables.add(table);
                }
            }
            
            if (!missingTables.isEmpty()) {
                System.out.println("检测到缺失的表: " + String.join(", ", missingTables));
                
                // 首先尝试执行SQL脚本创建所有表
                if (missingTables.size() > 2) {
                    System.out.println("多个表缺失，尝试通过SQL脚本创建所有表结构...");
                    if (executeCreateTableScript()) {
                        System.out.println("表结构创建成功！");
                        return;
                    }
                }
                
                // 如果脚本执行失败，则尝试单独创建缺失的表
                
                // 检查是否需要创建warning_rules表
                if (missingTables.contains("warning_rules")) {
                    System.out.println("尝试创建warning_rules表...");
                    createWarningRulesTable();
                }
                
                // 尝试触发Hibernate创建所有缺失的表结构
                System.out.println("尝试触发所有缺失表的创建...");
                try {
                    // 调用各Repository的count方法来促使Hibernate创建表
                    if (missingTables.contains("roles"))
                        roleRepository.count();
                    if (missingTables.contains("users") || missingTables.contains("user_roles"))
                        userRepository.count();
                    if (missingTables.contains("teachers"))
                        teacherRepository.count();
                    if (missingTables.contains("students"))
                        studentRepository.count();
                    if (missingTables.contains("class_groups"))
                        classGroupRepository.count();
                    if (missingTables.contains("courses"))
                        courseRepository.count();
                    if (missingTables.contains("course_enrollments"))
                        enrollmentRepository.count();
                    if (missingTables.contains("grades"))
                        gradeRepository.count();
                    if (missingTables.contains("warning_rules"))
                        warningRuleRepository.count();
                    if (missingTables.contains("warning_records"))
                        warningRecordRepository.count();
                    if (missingTables.contains("notifications"))
                        notificationRepository.count();
                    
                    // 清空Hibernate缓存
                    entityManager.clear();
                    
                    System.out.println("表结构创建请求已发送，请稍后检查数据库结构");
                } catch (Exception e) {
                    System.err.println("创建表结构失败: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("所有必需的表结构都已存在，无需创建");
            }
            
            System.out.println("======== 表结构检查完成 ========");
        };
    }
    
    /**
     * 执行SQL脚本创建表结构
     */
    @Transactional
    private boolean executeCreateTableScript() {
        try {
            System.out.println("正在执行表结构创建脚本...");
            
            // 尝试从类路径中读取
            String sqlScript = "";
            try {
                Resource resource = new ClassPathResource("create-tables.sql");
                try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                    sqlScript = FileCopyUtils.copyToString(reader);
                }
            } catch (IOException e) {
                System.out.println("从类路径读取脚本失败，尝试从文件系统读取...");
                // 尝试从项目根目录读取
                File file = new File("create-tables.sql");
                if (file.exists()) {
                    sqlScript = new String(Files.readAllBytes(Paths.get("create-tables.sql")), StandardCharsets.UTF_8);
                } else {
                    System.err.println("找不到SQL脚本文件，请确保create-tables.sql存在于项目根目录或资源目录中");
                    return false;
                }
            }
            
            if (sqlScript.isEmpty()) {
                System.err.println("SQL脚本内容为空");
                return false;
            }
            
            // 分割SQL语句并执行
            String[] sqlStatements = sqlScript.split(";");
            for (String sql : sqlStatements) {
                sql = sql.trim();
                if (!sql.isEmpty() && !sql.startsWith("--") && !sql.startsWith("SELECT")) {
                    try {
                        jdbcTemplate.execute(sql);
                    } catch (Exception e) {
                        System.out.println("执行SQL语句失败: " + sql);
                        System.out.println("错误信息: " + e.getMessage());
                        // 继续执行其他语句
                    }
                }
            }
            
            System.out.println("SQL脚本执行完成");
            return true;
        } catch (Exception e) {
            System.err.println("执行表结构创建脚本失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 手动创建warning_rules表（如果自动创建失败）
     */
    @Transactional
    private void createWarningRulesTable() {
        try {
            System.out.println("尝试手动创建warning_rules表...");
            
            String createTableSQL = 
                "CREATE TABLE IF NOT EXISTS warning_rules (" +
                "  id BIGINT NOT NULL AUTO_INCREMENT, " +
                "  created_at DATETIME, " +
                "  updated_at DATETIME, " +
                "  name VARCHAR(100) NOT NULL, " +
                "  description VARCHAR(255), " +
                "  type VARCHAR(50) NOT NULL, " +
                "  rule_condition VARCHAR(255) NOT NULL, " +
                "  level INT NOT NULL, " +
                "  color VARCHAR(50), " +
                "  is_active BIT(1), " +
                "  PRIMARY KEY (id)" +
                ")";
            
            jdbcTemplate.execute(createTableSQL);
            System.out.println("warning_rules表创建成功！");
        } catch (Exception e) {
            System.err.println("手动创建warning_rules表失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 