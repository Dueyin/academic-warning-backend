package com.academicmonitor.util;

import com.academicmonitor.entity.*;
import com.academicmonitor.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 数据库初始化工具类
 * 用于生成测试数据，便于系统功能测试
 * 
 * 使用方式：
 * 1. 正常启动：mvn spring-boot:run - 不初始化数据
 * 2. 初始化数据：mvn spring-boot:run -Dspring-boot.run.arguments=--init-db
 */
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

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
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private static final String DEFAULT_PASSWORD = "123456";
    private static final Random random = new Random();
    
    // 初始化数据的命令行参数
    private static final String INIT_DB_ARG = "--init-db";

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.NEVER)
    public void run(String... args) throws Exception {
        // 检查是否包含初始化参数
        boolean shouldInitDb = Arrays.asList(args).contains(INIT_DB_ARG);
        
        if (shouldInitDb) {
            System.out.println("检测到 --init-db 参数，将执行数据库初始化...");
            try {
                // 检查表结构，如果不完整则会尝试创建
                if (!checkTableStructures()) {
                    System.out.println("数据库表结构不完整。建议：");
                    System.out.println("1. 先不使用--init-db参数启动一次应用，让Hibernate自动创建表结构");
                    System.out.println("2. 然后再使用--init-db参数重新启动应用进行数据初始化");
                    System.out.println("跳过初始化过程...");
                    return;
                }
                
                // 清空所有数据
                clearAllTablesWithSql();
                
                // 创建全新测试数据
                createAllTestData();
                
                System.out.println("数据库初始化成功完成！");
            } catch (Exception e) {
                System.err.println("初始化数据库时出错: " + e.getMessage());
                e.printStackTrace();
                // 提供错误信息但不抛出异常，让程序继续正常启动
                System.err.println("数据库初始化失败，但应用程序将继续启动。请检查日志获取详细信息。");
            }
        } else {
            System.out.println("未检测到初始化参数，跳过数据库初始化。");
            System.out.println("若需初始化数据库，请使用: mvn spring-boot:run -Dspring-boot.run.arguments=--init-db");
        }
    }
    
    /**
     * 检查表结构是否完整
     */
    private boolean checkTableStructures() {
        System.out.println("检查数据库表结构是否完整...");
        
        // 获取所有已存在表
        List<String> existingTables = new ArrayList<>();
        try {
            jdbcTemplate.query("SHOW TABLES", rs -> {
                existingTables.add(rs.getString(1).toLowerCase());
            });
            System.out.println("发现现有表: " + String.join(", ", existingTables));
        } catch (Exception e) {
            System.out.println("无法获取表列表: " + e.getMessage());
            return false;
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
            System.out.println("以下必需的表不存在: " + String.join(", ", missingTables));
            
            // 尝试触发Hibernate创建表结构
            System.out.println("尝试触发表结构创建...");
            try {
                // 调用各Repository的count方法来促使Hibernate创建表
                roleRepository.count();
                userRepository.count();
                teacherRepository.count();
                studentRepository.count();
                classGroupRepository.count();
                courseRepository.count();
                enrollmentRepository.count();
                gradeRepository.count();
                warningRuleRepository.count();
                warningRecordRepository.count();
                notificationRepository.count();
                
                // 清空Hibernate缓存
                entityManager.clear();
                
                System.out.println("表结构创建尝试完成，重新检查表结构...");
                
                // 再次获取表列表，检查是否完成创建
                List<String> newExistingTables = new ArrayList<>();
                jdbcTemplate.query("SHOW TABLES", rs -> {
                    newExistingTables.add(rs.getString(1).toLowerCase());
                });
                
                missingTables.clear();
                for (String table : requiredTables) {
                    if (!newExistingTables.contains(table.toLowerCase())) {
                        missingTables.add(table);
                    }
                }
                
                if (!missingTables.isEmpty()) {
                    System.out.println("表结构创建失败，以下表仍然缺失: " + String.join(", ", missingTables));
                    return false;
                } else {
                    System.out.println("表结构创建成功!");
                    return true;
                }
            } catch (Exception e) {
                System.err.println("创建表结构失败: " + e.getMessage());
                return false;
            }
        }
        
        System.out.println("所有必需的表结构都已存在。");
        return true;
    }

    /**
     * 使用原生SQL清空所有数据表
     * 通过禁用外键约束确保可以清空所有表
     */
    private void clearAllTablesWithSql() {
        System.out.println("开始清空所有数据表...");
        
        try {
            // 禁用外键约束检查
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
            
            // 首先检查表是否存在
            List<String> existingTables = new ArrayList<>();
            try {
                jdbcTemplate.query("SHOW TABLES", rs -> {
                    existingTables.add(rs.getString(1).toLowerCase());
                });
                System.out.println("已找到现有表: " + existingTables);
            } catch (Exception e) {
                System.out.println("获取表列表失败: " + e.getMessage());
            }
            
            // 获取所有需要清空的表
            List<String> tables = Arrays.asList(
                "notifications", "warning_records", "warning_rules", 
                "grades", "course_enrollments", "students",
                "courses", "class_groups", "teachers",
                "user_roles", "users", "roles"
            );
            
            for (String table : tables) {
                try {
                    // 检查表是否存在
                    if (existingTables.contains(table.toLowerCase())) {
                        jdbcTemplate.execute("TRUNCATE TABLE " + table);
                        System.out.println("✓ 表 " + table + " 已清空");
                    } else {
                        System.out.println("⚠ 表 " + table + " 不存在，跳过清空");
                    }
                } catch (Exception e) {
                    System.out.println("✗ 清空表 " + table + " 失败: " + e.getMessage());
                }
            }
            
            // 重新启用外键约束检查
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
            
            // 清空Hibernate缓存，确保实体状态一致
            entityManager.clear();
            
            System.out.println("所有数据表已清空");
        } catch (Exception e) {
            System.err.println("清空数据表过程中出错: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * 清空所有数据表（使用Repository接口）
     * 此方法保留但不再使用，因为它可能会因外键约束而失败
     */
    private void clearAllTables() {
        System.out.println("开始清空所有数据表...");
        
        // 使用安全的方式清空表，顺序很重要（处理外键约束）
        safeDeleteAll("通知", notificationRepository);
        safeDeleteAll("预警记录", warningRecordRepository);
        safeDeleteAll("预警规则", warningRuleRepository);
        safeDeleteAll("成绩", gradeRepository);
        safeDeleteAll("选课记录", enrollmentRepository);
        safeDeleteAll("学生", studentRepository);
        safeDeleteAll("课程", courseRepository);
        safeDeleteAll("班级", classGroupRepository);
        safeDeleteAll("教师", teacherRepository);
        safeDeleteAll("用户", userRepository);
        safeDeleteAll("角色", roleRepository);
        
        System.out.println("所有数据表已清空");
    }
    
    /**
     * 安全删除表中所有数据
     */
    private <T> void safeDeleteAll(String tableName, org.springframework.data.repository.Repository<T, ?> repository) {
        try {
            if (repository instanceof org.springframework.data.repository.CrudRepository) {
                ((org.springframework.data.repository.CrudRepository<T, ?>) repository).deleteAll();
                System.out.println("✓ " + tableName + "表清空成功");
            }
        } catch (Exception e) {
            System.out.println("✗ " + tableName + "表清空失败: " + e.getMessage());
            // 继续执行，不中断流程
        }
    }

    /**
     * 创建所有测试数据，分步执行事务
     */
    public void createAllTestData() {
        try {
            System.out.println("=================== 开始创建测试数据集 ===================");
            
            // 步骤1和2：创建角色和管理员用户
            System.out.println("步骤1-2: 开始创建角色和管理员用户...");
            List<Role> roles = null;
            User adminUser = null;
            try {
                roles = createRolesWithTransaction();
                adminUser = createAdminUserWithTransaction(roles);
                System.out.println("✓ 角色和管理员用户创建完成");
            } catch (Exception e) {
                System.err.println("创建角色和管理员用户失败: " + e.getMessage());
                e.printStackTrace();
                // 如果这些基础数据创建失败，后续步骤无法继续
                throw e;
            }

            // 步骤3和4：创建教师和班级
            System.out.println("步骤3-4: 开始创建教师和班级...");
            List<Teacher> teachers = null;
            List<ClassGroup> classes = null;
            try {
                teachers = createTeachersWithTransaction(roles);
                if (teachers == null || teachers.isEmpty()) {
                    System.err.println("警告：教师创建失败或为空，跳过创建班级");
                } else {
                    classes = createClassesWithTransaction(teachers);
                    System.out.println("✓ 教师和班级创建完成");
                }
            } catch (Exception e) {
                System.err.println("创建教师和班级失败: " + e.getMessage());
                e.printStackTrace();
                // 继续下一部分的创建
            }

            // 步骤5：创建学生
            System.out.println("步骤5: 开始创建学生用户...");
            List<Student> students = null;
            try {
                if (classes == null || classes.isEmpty()) {
                    System.err.println("警告：班级创建失败或为空，跳过创建学生");
                } else {
                    students = createStudentsWithTransaction(roles, classes);
                    System.out.println("✓ 学生创建完成");
                }
            } catch (Exception e) {
                System.err.println("创建学生失败: " + e.getMessage());
                e.printStackTrace();
                // 继续下一部分的创建
            }

            // 步骤6-7：创建课程和选课记录
            System.out.println("步骤6-7: 开始创建课程和选课记录...");
            List<Course> courses = null;
            List<CourseEnrollment> enrollments = null;
            try {
                if (teachers == null || teachers.isEmpty()) {
                    System.err.println("警告：教师创建失败或为空，跳过创建课程");
                } else {
                    courses = createCoursesWithTransaction(teachers);
                    
                    if (students == null || students.isEmpty() || courses == null || courses.isEmpty()) {
                        System.err.println("警告：学生或课程创建失败或为空，跳过创建选课记录");
                    } else {
                        enrollments = createEnrollmentsWithTransaction(students, courses);
                        System.out.println("✓ 课程和选课记录创建完成");
                    }
                }
            } catch (Exception e) {
                System.err.println("创建课程和选课记录失败: " + e.getMessage());
                e.printStackTrace();
                // 继续下一部分的创建
            }

            // 步骤8：创建成绩记录
            System.out.println("步骤8: 开始创建成绩记录...");
            try {
                if (enrollments == null || enrollments.isEmpty() || adminUser == null) {
                    System.err.println("警告：选课记录创建失败或为空，或管理员创建失败，跳过创建成绩记录");
                } else {
                    createGradesWithTransaction(enrollments, adminUser);
                    System.out.println("✓ 成绩记录创建完成");
                }
            } catch (Exception e) {
                System.err.println("创建成绩记录失败: " + e.getMessage());
                e.printStackTrace();
                // 继续下一部分的创建
            }

            // 步骤9-10：创建预警规则和预警记录
            System.out.println("步骤9-10: 开始创建预警规则和预警记录...");
            List<WarningRule> rules = null;
            try {
                rules = createWarningRulesWithTransaction();
                
                if (students == null || students.isEmpty() || rules == null || rules.isEmpty()) {
                    System.err.println("警告：学生或预警规则创建失败或为空，跳过创建预警记录");
                } else {
                    createWarningRecordsWithTransaction(students, rules);
                    System.out.println("✓ 预警规则和预警记录创建完成");
                }
            } catch (Exception e) {
                System.err.println("创建预警规则和预警记录失败: " + e.getMessage());
                e.printStackTrace();
                // 继续下一部分的创建
            }

            // 步骤11：创建通知
            System.out.println("步骤11: 开始创建通知...");
            try {
                if (students == null || students.isEmpty() || teachers == null || teachers.isEmpty() || adminUser == null) {
                    System.err.println("警告：学生、教师或管理员创建失败或为空，跳过创建通知");
                } else {
                    createNotificationsWithTransaction(students, teachers, adminUser);
                    System.out.println("✓ 通知创建完成");
                }
            } catch (Exception e) {
                System.err.println("创建通知失败: " + e.getMessage());
                e.printStackTrace();
                // 继续执行，不影响整体流程
            }
            
            System.out.println("=================== 测试数据集创建完毕 ===================");
            
            // 打印一些统计信息
            printDataSummary();
            
        } catch (Exception e) {
            System.err.println("创建测试数据过程中出错: " + e.getMessage());
            e.printStackTrace();
            // 不抛出异常，让程序继续启动
        }
    }
    
    /**
     * 打印数据统计信息
     */
    private void printDataSummary() {
        System.out.println("\n=================== 数据统计 ===================");
        System.out.println("角色数量: " + roleRepository.count());
        System.out.println("用户数量: " + userRepository.count());
        System.out.println("教师数量: " + teacherRepository.count());
        System.out.println("班级数量: " + classGroupRepository.count());
        System.out.println("学生数量: " + studentRepository.count());
        System.out.println("课程数量: " + courseRepository.count());
        System.out.println("选课记录数量: " + enrollmentRepository.count());
        System.out.println("成绩记录数量: " + gradeRepository.count());
        System.out.println("预警规则数量: " + warningRuleRepository.count());
        System.out.println("预警记录数量: " + warningRecordRepository.count());
        System.out.println("通知数量: " + notificationRepository.count());
        System.out.println("===================================================\n");
    }

    private List<Role> createRoles() {
        Role adminRole = new Role();
        adminRole.setName(Role.ROLE_ADMIN);
        adminRole.setDescription("系统管理员角色");

        Role teacherRole = new Role();
        teacherRole.setName(Role.ROLE_TEACHER);
        teacherRole.setDescription("教师角色");

        Role studentRole = new Role();
        studentRole.setName(Role.ROLE_STUDENT);
        studentRole.setDescription("学生角色");

        return roleRepository.saveAll(Arrays.asList(adminRole, teacherRole, studentRole));
    }

    private User createAdminUser(List<Role> roles) {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setName("系统管理员");
        admin.setEmail("admin@school.edu");
        admin.setPhone("13800000000");
        admin.setEnabled(true);
        admin.setRoles(Collections.singleton(findRoleByName(roles, Role.ROLE_ADMIN)));

        return userRepository.save(admin);
    }

    private List<Teacher> createTeachers(List<Role> roles) {
        List<Teacher> teachers = new ArrayList<>();
        Role teacherRole = findRoleByName(roles, Role.ROLE_TEACHER);

        String[][] teacherData = {
                {"t001", "王教授", "教授", "计算机科学", "13900000001", "wang@school.edu"},
                {"t002", "李副教授", "副教授", "数学", "13900000002", "li@school.edu"},
                {"t003", "张讲师", "讲师", "英语", "13900000003", "zhang@school.edu"},
                {"t004", "刘教授", "教授", "物理", "13900000004", "liu@school.edu"},
                {"t005", "陈副教授", "副教授", "化学", "13900000005", "chen@school.edu"}
        };

        for (String[] data : teacherData) {
            try {
                // 首先检查用户是否已存在
                Optional<User> existingUser = userRepository.findByUsername(data[0]);
                User user;
                
                if (existingUser.isPresent()) {
                    System.out.println("✓ 用户已存在: " + data[0] + "，重用现有用户");
                    user = existingUser.get();
                    // 更新现有用户信息
                    user.setName(data[1]);
                    user.setPhone(data[4]);
                    user.setEmail(data[5]);
                    user.setEnabled(true);
                    // 确保有正确的角色
                    Set<Role> roles2 = new HashSet<>(user.getRoles());
                    roles2.add(teacherRole);
                    user.setRoles(roles2);
                    user = userRepository.save(user); // 合并更新
                } else {
                    // 创建新用户
                    user = new User();
                    user.setUsername(data[0]);  // 教工号作为用户名
                    user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
                    user.setName(data[1]);
                    user.setPhone(data[4]);
                    user.setEmail(data[5]);
                    user.setEnabled(true);
                    user.setRoles(Collections.singleton(teacherRole));
                    user = userRepository.save(user);
                    System.out.println("✓ 创建用户: " + user.getUsername());
                }
                
                // 检查教师是否已存在
                Optional<Teacher> existingTeacher = teacherRepository.findByTeacherNumber(data[0]);
                if (existingTeacher.isPresent()) {
                    System.out.println("✓ 教师已存在: " + data[0] + "，更新教师信息");
                    Teacher teacher = existingTeacher.get();
                    teacher.setUser(user);
                    teacher.setTitle(data[2]);
                    teacher.setSpecialty(data[3]);
                    teachers.add(teacherRepository.save(teacher));
                } else {
                    // 创建新教师
                    Teacher teacher = new Teacher();
                    teacher.setUser(user);
                    teacher.setTeacherNumber(data[0]);
                    teacher.setTitle(data[2]);
                    teacher.setSpecialty(data[3]);
                    
                    Teacher savedTeacher = teacherRepository.save(teacher);
                    if (savedTeacher != null) {
                        teachers.add(savedTeacher);
                        System.out.println("✓ 创建教师: " + savedTeacher.getTeacherNumber());
                    } else {
                        System.err.println("创建教师失败: " + data[0]);
                    }
                }
            } catch (Exception e) {
                System.err.println("创建教师过程中出错: " + data[0] + ", 错误: " + e.getMessage());
                e.printStackTrace();
                // 继续创建其他教师
            }
        }

        System.out.println("✓ 总共创建教师: " + teachers.size() + " 个");
        return teachers;
    }

    private List<ClassGroup> createClasses(List<Teacher> teachers) {
        List<ClassGroup> classes = new ArrayList<>();

        String[][] classData = {
                {"计算机科学2021级1班", "2021", "计算机科学与技术专业2021级1班"},
                {"计算机科学2021级2班", "2021", "计算机科学与技术专业2021级2班"},
                {"数学2022级1班", "2022", "数学专业2022级1班"},
                {"物理2022级1班", "2022", "物理专业2022级1班"},
                {"英语2023级1班", "2023", "英语专业2023级1班"}
        };

        if (teachers == null || teachers.isEmpty()) {
            System.err.println("创建班级失败：没有可用的教师数据");
            return classes;
        }

        for (int i = 0; i < classData.length; i++) {
            try {
                ClassGroup classGroup = new ClassGroup();
                classGroup.setName(classData[i][0]);
                classGroup.setYear(classData[i][1]);
                classGroup.setDescription(classData[i][2]);
                
                // 获取班主任教师，确保索引不越界
                Teacher headTeacher = teachers.get(i % teachers.size());
                if (headTeacher == null) {
                    System.err.println("警告：班级 " + classData[i][0] + " 的班主任为空");
                    continue;
                }
                
                classGroup.setHeadTeacher(headTeacher);
                
                ClassGroup savedClass = classGroupRepository.save(classGroup);
                if (savedClass != null) {
                    classes.add(savedClass);
                    System.out.println("✓ 创建班级: " + savedClass.getName() + ", 班主任: " + headTeacher.getUser().getName());
                } else {
                    System.err.println("创建班级失败: " + classData[i][0]);
                }
            } catch (Exception e) {
                System.err.println("创建班级过程中出错: " + classData[i][0] + ", 错误: " + e.getMessage());
                e.printStackTrace();
                // 继续创建其他班级
            }
        }

        System.out.println("✓ 总共创建班级: " + classes.size() + " 个");
        return classes;
    }

    private List<Student> createStudents(List<Role> roles, List<ClassGroup> classes) {
        List<Student> students = new ArrayList<>();
        Role studentRole = findRoleByName(roles, Role.ROLE_STUDENT);
        
        // 跟踪所有生成的学号，避免重复
        Set<String> generatedStudentNumbers = new HashSet<>();

        // 为每个班级创建学生
        for (ClassGroup classGroup : classes) {
            // 每个班级创建20名学生
            for (int i = 1; i <= 20; i++) {
                // 创建基础学号格式（年份+序号）
                String baseStudentNumber = classGroup.getYear() + String.format("%03d", i);
                
                // 确保学号唯一（添加班级id作为后缀如果需要）
                String studentNumber = baseStudentNumber;
                int suffix = 1;
                while (generatedStudentNumbers.contains(studentNumber)) {
                    studentNumber = baseStudentNumber + "_" + suffix;
                    suffix++;
                }
                
                // 记录已使用的学号
                generatedStudentNumbers.add(studentNumber);
                
                // 创建用户
                User user = new User();
                user.setUsername(studentNumber);
                user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
                user.setName("学生" + studentNumber);
                user.setEmail(studentNumber + "@student.school.edu");
                user.setPhone("1390000" + studentNumber.substring(Math.max(0, studentNumber.length() - 4)));
                user.setEnabled(true);
                user.setRoles(Collections.singleton(studentRole));
                userRepository.save(user);

                // 创建学生
                Student student = new Student();
                student.setUser(user);
                student.setStudentNumber(studentNumber);
                student.setDateOfBirth(LocalDate.now().minusYears(18 + random.nextInt(5)));
                student.setGender(random.nextBoolean() ? "男" : "女");
                student.setAddress("某省某市某区某街道");
                student.setClassGroup(classGroup);
                student.setHasWarning(false);
                student.setEmergencyContact("家长" + i);
                student.setEmergencyPhone("1380000" + String.format("%04d", i));
                
                students.add(studentRepository.save(student));
            }
        }

        return students;
    }

    private List<Course> createCourses(List<Teacher> teachers) {
        List<Course> courses = new ArrayList<>();

        String[][] courseData = {
                {"CS101", "计算机导论", "计算机科学基础课程", "3", "2023-2024-1"},
                {"CS201", "数据结构", "计算机科学核心课程", "4", "2023-2024-1"},
                {"CS301", "算法设计与分析", "计算机科学进阶课程", "4", "2023-2024-1"},
                {"CS401", "操作系统", "计算机科学核心课程", "4", "2023-2024-1"},
                {"CS501", "计算机网络", "计算机科学核心课程", "3", "2023-2024-1"},
                {"MATH101", "高等数学", "数学基础课程", "5", "2023-2024-1"},
                {"MATH201", "线性代数", "数学基础课程", "3", "2023-2024-1"},
                {"PHY101", "大学物理", "物理基础课程", "4", "2023-2024-1"},
                {"ENG101", "大学英语", "英语基础课程", "3", "2023-2024-1"},
                {"CHEM101", "大学化学", "化学基础课程", "3", "2023-2024-1"}
        };

        for (int i = 0; i < courseData.length; i++) {
            Course course = new Course();
            course.setCourseCode(courseData[i][0]);
            course.setName(courseData[i][1]);
            course.setDescription(courseData[i][2]);
            course.setCredit(Integer.parseInt(courseData[i][3]));
            course.setSemester(courseData[i][4]);
            course.setTeacher(teachers.get(i % teachers.size()));
            
            courses.add(courseRepository.save(course));
        }

        return courses;
    }

    private List<CourseEnrollment> createEnrollments(List<Student> students, List<Course> courses) {
        List<CourseEnrollment> enrollments = new ArrayList<>();

        // 为每个学生选择4-6门课程
        for (Student student : students) {
            int courseCount = 4 + random.nextInt(3); // 4到6门课
            List<Course> selectedCourses = getRandomSubset(courses, courseCount);
            
            for (Course course : selectedCourses) {
                CourseEnrollment enrollment = new CourseEnrollment();
                enrollment.setStudent(student);
                enrollment.setCourse(course);
                enrollment.setSemester(course.getSemester());
                enrollment.setIsActive(true);
                
                enrollments.add(enrollmentRepository.save(enrollment));
            }
        }

        return enrollments;
    }

    private void createGrades(List<CourseEnrollment> enrollments, User admin) {
        String[] gradeTypes = {"期中考试", "期末考试", "平时作业", "实验"};
        int[] weights = {30, 50, 10, 10};

        for (CourseEnrollment enrollment : enrollments) {
            for (int i = 0; i < gradeTypes.length; i++) {
                Grade grade = new Grade();
                grade.setEnrollment(enrollment);
                grade.setGradeType(gradeTypes[i]);
                
                // 随机分数，可能存在不及格情况
                double score;
                if (random.nextDouble() < 0.15) { // 15%概率不及格
                    score = 30 + random.nextDouble() * 30; // 30-59分
                } else {
                    score = 60 + random.nextDouble() * 40; // 60-99分
                }
                grade.setScore(Math.round(score * 10.0) / 10.0); // 保留一位小数
                
                grade.setMaxScore(100.0);
                grade.setWeight(weights[i]);
                grade.setExamDate(LocalDate.now().minusDays(random.nextInt(30)));
                grade.setRecordedBy(admin);
                
                gradeRepository.save(grade);
            }
        }
    }

    private List<WarningRule> createWarningRules() {
        List<WarningRule> rules = new ArrayList<>();

        Object[][] ruleData = {
                {"单科不及格预警", "单门课程考试成绩不及格", "COURSE_GRADE", "score < 60", 1, "黄色"},
                {"多科不及格预警", "多门课程考试成绩不及格", "MULTIPLE_FAIL", "failCount >= 2", 2, "橙色"},
                {"学期平均分预警", "学期平均分过低", "SEMESTER_AVERAGE", "average < 70", 1, "黄色"},
                {"严重学业危机预警", "学期平均分极低或多门课程不及格", "SEVERE", "average < 60 || failCount >= 3", 3, "红色"}
        };

        for (Object[] data : ruleData) {
            WarningRule rule = new WarningRule();
            rule.setName((String) data[0]);
            rule.setDescription((String) data[1]);
            rule.setType((String) data[2]);
            rule.setCondition((String) data[3]);
            rule.setLevel((Integer) data[4]);
            rule.setColor((String) data[5]);
            rule.setIsActive(true);
            
            rules.add(warningRuleRepository.save(rule));
        }

        return rules;
    }

    private void createWarningRecords(List<Student> students, List<WarningRule> rules) {
        try {
            System.out.println("步骤10: 开始创建预警记录...");
            
            // 为约15%的学生创建预警记录
            int warningCount = (int) (students.size() * 0.15);
            List<Student> warningStudents = getRandomSubset(students, warningCount);
            
            String[] statuses = {"NEW", "READ", "PROCESSED", "RESOLVED"};
            
            // 获取完整的管理员用户对象，而不仅仅是ID
            User admin = userRepository.findByUsername("admin").orElse(null);
            
            int recordCount = 0;
            for (Student student : warningStudents) {
                try {
                    // 更新学生预警状态
                    student.setHasWarning(true);
                    studentRepository.save(student);
                    
                    // 随机选择1-2条规则生成预警
                    int ruleCount = 1 + random.nextInt(2);
                    List<WarningRule> selectedRules = getRandomSubset(rules, ruleCount);
                    
                    for (WarningRule rule : selectedRules) {
                        WarningRecord record = new WarningRecord();
                        record.setStudent(student);
                        record.setRule(rule);
                        record.setWarningContent(String.format("学生 %s 触发了 %s 预警", 
                                student.getUser().getName(), rule.getName()));
                        record.setWarningTime(LocalDateTime.now().minusDays(random.nextInt(30)));
                        
                        String status = statuses[random.nextInt(statuses.length)];
                        record.setStatus(status);
                        
                        if (!status.equals("NEW") && admin != null) {
                            record.setProcessedTime(LocalDateTime.now().minusDays(random.nextInt(15)));
                            record.setProcessedBy(admin);
                            record.setProcessNotes("已处理：" + (status.equals("RESOLVED") ? "问题已解决" : "正在跟进中"));
                        }
                        
                        warningRecordRepository.save(record);
                        recordCount++;
                    }
                } catch (Exception e) {
                    System.err.println("为学生 " + student.getStudentNumber() + " 创建预警记录时出错: " + e.getMessage());
                    e.printStackTrace();
                    // 继续处理其他学生，不中断整个过程
                }
            }
            
            System.out.println("✓ 预警记录创建完成: 共 " + recordCount + " 条记录");
        } catch (Exception e) {
            System.err.println("创建预警记录过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            throw e; // 抛出异常，让事务能够正确回滚
        }
    }

    private void createNotifications(List<Student> students, List<Teacher> teachers, User adminUser) {
        // 1. 系统通知 - 所有用户
        createSystemNotifications(students, teachers, adminUser);
        
        // 2. 预警通知 - 有预警的学生
        createWarningNotifications(students.stream()
                .filter(Student::getHasWarning)
                .collect(java.util.stream.Collectors.toList()));
        
        // 3. 成绩通知 - 所有学生
        createGradeNotifications(students);
    }

    private void createSystemNotifications(List<Student> students, List<Teacher> teachers, User adminUser) {
        try {
            System.out.println("创建系统通知...");
            
            String[] systemNotificationTitles = {
                    "系统维护通知", 
                    "功能更新公告", 
                    "新学期开始通知"
            };
            
            String[] systemNotificationContents = {
                    "系统将于本周六凌晨2:00-4:00进行维护，期间所有功能暂停使用。",
                    "系统已更新，新增成绩分析图表功能，欢迎使用！",
                    "新学期已开始，请各位同学及时查看课表和选课信息。"
            };
            
            // 使用完整的管理员用户对象
            if (adminUser == null) {
                System.err.println("无法获取管理员用户，跳过系统通知创建");
                return;
            }
            
            // 为所有用户创建系统通知
            List<User> allUsers = new ArrayList<>();
            // 添加管理员
            allUsers.add(adminUser);
            
            // 添加所有学生和教师的用户
            students.forEach(student -> {
                if (student.getUser() != null) {
                    allUsers.add(student.getUser());
                }
            });
            
            teachers.forEach(teacher -> {
                if (teacher.getUser() != null) {
                    allUsers.add(teacher.getUser());
                }
            });
            
            int notificationCount = 0;
            for (int i = 0; i < systemNotificationTitles.length; i++) {
                for (User user : allUsers) {
                    try {
                        Notification notification = new Notification();
                        notification.setUser(user);
                        notification.setTitle(systemNotificationTitles[i]);
                        notification.setContent(systemNotificationContents[i]);
                        notification.setType("SYSTEM");
                        notification.setIsRead(random.nextBoolean());
                        notification.setSender(adminUser); // 使用完整的管理员对象
                        notification.setSendTime(LocalDateTime.now().minusDays(i * 3 + random.nextInt(5)));
                        
                        if (notification.getIsRead()) {
                            notification.setReadTime(notification.getSendTime().plusHours(random.nextInt(48)));
                        }
                        
                        notificationRepository.save(notification);
                        notificationCount++;
                    } catch (Exception e) {
                        System.err.println("为用户 " + user.getUsername() + " 创建系统通知时出错: " + e.getMessage());
                        e.printStackTrace();
                        // 继续处理其他用户
                    }
                }
            }
            
            System.out.println("✓ 系统通知创建完成: 共 " + notificationCount + " 条通知");
        } catch (Exception e) {
            System.err.println("创建系统通知过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            throw e; // 抛出异常，让事务能够正确回滚
        }
    }

    private void createWarningNotifications(List<Student> warningStudents) {
        try {
            System.out.println("创建预警通知...");
            
            String[] warningTitles = {
                    "学业预警通知", 
                    "成绩不及格预警", 
                    "学业状况关注"
            };
            
            String[] warningContents = {
                    "您的学业状况已触发预警，请及时与辅导员联系。",
                    "您有课程成绩不及格，请关注学习进度，及时补救。",
                    "您的学习状况需要特别关注，请合理安排学习计划。"
            };
            
            // 获取完整的管理员用户对象
            User admin = userRepository.findByUsername("admin").orElse(null);
            
            int notificationCount = 0;
            // 为有预警的学生创建预警通知
            for (Student student : warningStudents) {
                try {
                    int index = random.nextInt(warningTitles.length);
                    
                    Notification notification = new Notification();
                    notification.setUser(student.getUser());
                    notification.setTitle(warningTitles[index]);
                    notification.setContent(warningContents[index]);
                    notification.setType("WARNING");
                    notification.setRelatedObject("WARNING");
                    notification.setIsRead(random.nextBoolean());
                    
                    // 由管理员或班主任发送
                    User sender;
                    if (random.nextBoolean() && admin != null) {
                        sender = admin;
                    } else if (student.getClassGroup() != null && student.getClassGroup().getHeadTeacher() != null) {
                        // 获取完整的班主任用户对象
                        Teacher headTeacher = student.getClassGroup().getHeadTeacher();
                        sender = headTeacher.getUser();
                        if (sender == null) {
                            continue; // 如果没有关联用户，跳过
                        }
                    } else {
                        // 如果没有班主任，跳过
                        continue;
                    }
                    
                    notification.setSender(sender);
                    notification.setSendTime(LocalDateTime.now().minusDays(random.nextInt(10)));
                    
                    if (notification.getIsRead()) {
                        notification.setReadTime(notification.getSendTime().plusHours(random.nextInt(24)));
                    }
                    
                    notificationRepository.save(notification);
                    notificationCount++;
                } catch (Exception e) {
                    System.err.println("为学生 " + student.getStudentNumber() + " 创建预警通知时出错: " + e.getMessage());
                    e.printStackTrace();
                    // 继续处理其他学生
                }
            }
            
            System.out.println("✓ 预警通知创建完成: 共 " + notificationCount + " 条通知");
        } catch (Exception e) {
            System.err.println("创建预警通知过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            throw e; // 抛出异常，确保事务回滚
        }
    }

    private void createGradeNotifications(List<Student> students) {
        try {
            System.out.println("创建成绩通知...");
            
            int notificationCount = 0;
            // 为所有学生创建成绩通知
            for (Student student : students) {
                try {
                    if (student.getClassGroup() == null || student.getClassGroup().getHeadTeacher() == null) {
                        continue; // 跳过没有班级或没有班主任的学生
                    }
                    
                    // 获取完整的班主任用户对象
                    Teacher headTeacher = student.getClassGroup().getHeadTeacher();
                    User teacher = headTeacher.getUser();
                    if (teacher == null) {
                        continue; // 如果没有关联用户，跳过
                    }
                    
                    Notification notification = new Notification();
                    notification.setUser(student.getUser());
                    notification.setTitle("成绩已发布通知");
                    notification.setContent("您的期末成绩已公布，请登录系统查看详情。");
                    notification.setType("ANNOUNCEMENT");
                    notification.setRelatedObject("GRADE");
                    notification.setIsRead(random.nextBoolean());
                    notification.setSender(teacher);
                    notification.setSendTime(LocalDateTime.now().minusDays(random.nextInt(7)));
                    
                    if (notification.getIsRead()) {
                        notification.setReadTime(notification.getSendTime().plusHours(random.nextInt(12)));
                    }
                    
                    notificationRepository.save(notification);
                    notificationCount++;
                } catch (Exception e) {
                    System.err.println("为学生 " + student.getStudentNumber() + " 创建成绩通知时出错: " + e.getMessage());
                    e.printStackTrace();
                    // 继续处理其他学生
                }
            }
            
            System.out.println("✓ 成绩通知创建完成: 共 " + notificationCount + " 条通知");
        } catch (Exception e) {
            System.err.println("创建成绩通知过程中发生错误: " + e.getMessage());
            e.printStackTrace();
            throw e; // 抛出异常，确保事务回滚
        }
    }

    // 工具方法：根据角色名查找角色
    private Role findRoleByName(List<Role> roles, String roleName) {
        return roles.stream()
                .filter(r -> r.getName().equals(roleName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("角色不存在: " + roleName));
    }

    // 工具方法：从列表中随机获取子集
    private <T> List<T> getRandomSubset(List<T> list, int count) {
        List<T> copy = new ArrayList<>(list);
        Collections.shuffle(copy);
        return copy.subList(0, Math.min(count, copy.size()));
    }

    // 带事务的方法实现

    @Transactional
    public List<Role> createRolesWithTransaction() {
        return createRoles();
    }

    @Transactional
    public User createAdminUserWithTransaction(List<Role> roles) {
        return createAdminUser(roles);
    }

    @Transactional
    public List<Teacher> createTeachersWithTransaction(List<Role> roles) {
        return createTeachers(roles);
    }

    @Transactional
    public List<ClassGroup> createClassesWithTransaction(List<Teacher> teachers) {
        return createClasses(teachers);
    }

    @Transactional
    public List<Student> createStudentsWithTransaction(List<Role> roles, List<ClassGroup> classes) {
        return createStudents(roles, classes);
    }

    @Transactional
    public List<Course> createCoursesWithTransaction(List<Teacher> teachers) {
        return createCourses(teachers);
    }

    @Transactional
    public List<CourseEnrollment> createEnrollmentsWithTransaction(List<Student> students, List<Course> courses) {
        return createEnrollments(students, courses);
    }

    @Transactional
    public void createGradesWithTransaction(List<CourseEnrollment> enrollments, User admin) {
        createGrades(enrollments, admin);
    }

    @Transactional
    public List<WarningRule> createWarningRulesWithTransaction() {
        return createWarningRules();
    }

    @Transactional
    public void createWarningRecordsWithTransaction(List<Student> students, List<WarningRule> rules) {
        createWarningRecords(students, rules);
    }

    @Transactional
    public void createNotificationsWithTransaction(List<Student> students, List<Teacher> teachers, User adminUser) {
        createNotifications(students, teachers, adminUser);
    }
} 