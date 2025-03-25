# 学业监测预警系统 - 后端服务

## 项目概述
学业监测预警系统是一个基于Spring Boot和Vue.js的Web应用，旨在为教育机构提供学业成绩监测、预警和干预的功能。该系统包含管理端、教师端和学生端三个主要模块，支持用户管理、课程管理、成绩录入与统计、学业预警和通知管理等功能。

## 技术栈
- **后端**: Spring Boot 2.7.9, Spring Security, Spring Data JPA, JWT
- **数据库**: MySQL 8.0
- **构建工具**: Maven

## 功能模块
1. **用户认证与授权**
   - JWT基础的登录认证
   - 基于角色的权限控制 (RBAC)

2. **用户管理**
   - 管理员、教师和学生三种角色
   - 用户信息管理
   - 账户状态控制

3. **班级管理**
   - 班级创建和管理
   - 学生-班级关联

4. **课程管理**
   - 课程信息管理
   - 课程-教师关联

5. **选课管理**
   - 学生选课功能
   - 课程名单管理

6. **成绩管理**
   - 成绩录入 (手动/批量)
   - 成绩查询和统计报表

7. **学业预警**
   - 预警规则设置
   - 自动学业评估和预警
   - 预警记录管理

8. **通知管理**
   - 系统通知
   - 预警通知
   - 教师反馈

## 快速开始
### 环境要求
- JDK 11+
- MySQL 8.0+
- Maven 3.6+

### 数据库配置
1. 创建MySQL数据库:
```sql
CREATE DATABASE academic_monitor;
```

2. 修改`application.properties`中的数据库连接配置:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/academic_monitor?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=你的数据库用户名
spring.datasource.password=你的数据库密码
```

### 运行项目
1. 克隆项目:
```bash
git clone [项目URL]
```

2. 进入项目目录:
```bash
cd academic-monitoring-system
```

3. 构建项目:
```bash
mvn clean install
```

4. 运行应用:
```bash
mvn spring-boot:run
```

### 初始账户信息
系统初始化后将自动创建管理员账户:
- 用户名: admin
- 密码: admin123

## API文档
系统主要API端点:
- `/api/auth/signin` - 用户登录
- `/api/auth/signup` - 用户注册
- `/api/users` - 用户管理
- `/api/students` - 学生管理
- `/api/teachers` - 教师管理
- `/api/classes` - 班级管理
- `/api/courses` - 课程管理
- `/api/enrollments` - 选课管理
- `/api/grades` - 成绩管理
- `/api/warnings` - 预警管理
- `/api/notifications` - 通知管理

## 项目结构
```
src/main/java/com/academicmonitor/
├── config/             # 配置类
├── controller/         # REST控制器
├── dto/                # 数据传输对象
├── entity/             # 实体类
├── exception/          # 异常处理
├── repository/         # 数据访问层
├── security/           # 安全配置
├── service/            # 业务逻辑层
└── util/               # 工具类
```

## 项目开发进度
- [x] 项目基础架构搭建
- [x] 安全认证模块
- [x] 用户管理基础功能
- [x] 学生管理模块
- [ ] 教师管理模块
- [ ] 班级管理模块
- [ ] 课程管理模块
- [ ] 成绩管理模块
- [ ] 预警规则管理
- [ ] 通知管理模块
- [ ] 统计报表功能 