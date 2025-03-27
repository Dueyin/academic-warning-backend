import com.academicmonitor.AcademicMonitoringApplication;
import com.academicmonitor.util.DatabaseInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 数据库初始化独立程序
 * 用于单独初始化测试数据，不启动Web服务
 */
public class InitializeDatabase {
    public static void main(String[] args) {
        System.out.println("开始运行数据库初始化程序...");
        // 启动Spring上下文但不启动Web服务
        SpringApplication app = new SpringApplication(AcademicMonitoringApplication.class);
        app.setWebApplicationType(org.springframework.boot.WebApplicationType.NONE);
        ConfigurableApplicationContext context = app.run(args);
        
        try {
            // 手动执行DatabaseInitializer
            DatabaseInitializer initializer = context.getBean(DatabaseInitializer.class);
            initializer.initializeTestData(true); // 传入true强制重新初始化
            
            System.out.println("数据库初始化完成！");
            System.out.println("初始管理员账户: admin / admin123");
            System.out.println("教师测试账户: t001-t005 / 123456");
            System.out.println("学生测试账户: 学号（如2021001）/ 123456");
        } catch (Exception e) {
            System.err.println("初始化数据库时发生错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭Spring上下文
            context.close();
            System.out.println("程序已完成，已关闭Spring上下文");
        }
    }
} 