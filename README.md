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
项目提供了多种方式运行：

#### 最佳实践：两阶段初始化（推荐）
为了确保数据库结构和测试数据的正确创建，建议采用两阶段方法：

##### 阶段一：创建数据库表结构
首先，仅启动应用以创建表结构，不初始化数据：

```powershell
# Windows PowerShell
mvn clean install -DskipTests
mvn spring-boot:run
```

启动后，让应用程序运行几秒钟，然后可以按Ctrl+C停止。此时所有表结构应已创建。

##### 阶段二：初始化测试数据
然后，使用初始化参数重新启动应用，填充测试数据：

```powershell
# Windows PowerShell
mvn spring-boot:run "-Dspring-boot.run.arguments=--init-db"

# Linux/Mac/Windows CMD
mvn spring-boot:run -Dspring-boot.run.arguments=--init-db
```

#### 方式一：仅启动应用，不初始化数据
如果您不需要测试数据，只需启动应用：

```bash
mvn clean install -DskipTests
mvn spring-boot:run
```

> **注意**: 在首次启动时，应用会自动检查并尝试创建所有必需的表结构。如果遇到表结构不完整的问题，请查看下方的"表结构问题解决"部分。

#### 方式二：直接启动并初始化数据
如果您希望一步完成（不推荐用于首次设置），可以尝试：

```powershell
# Windows PowerShell
mvn spring-boot:run "-Dspring-boot.run.arguments=--init-db"

# Linux/Mac/Windows CMD
mvn spring-boot:run -Dspring-boot.run.arguments=--init-db
```

### 重置数据库
如果需要完全重置数据库并重新初始化测试数据，请按照以下步骤操作：

1. 首先，重置MySQL数据库：
```sql
DROP DATABASE academic_monitor;
CREATE DATABASE academic_monitor;
```

2. 然后按照上述两阶段方法重新创建表结构和测试数据。

### 常见问题解决

#### 实体关系级联配置问题
如果在初始化数据时遇到"detached entity passed to persist"错误，这通常是由于JPA/Hibernate中的级联关系配置问题导致的：

1. **错误表现**
   - 错误信息：`detached entity passed to persist: com.academicmonitor.entity.User`
   - 发生场景：通常在创建教师或学生等实体，并与已存在的User实体关联时

2. **解决方法**
   - 已更新的实体关系配置：我们已经修改了Teacher和Student实体中的`@OneToOne`关系配置，从`cascade = CascadeType.ALL`改为`cascade = {CascadeType.MERGE, CascadeType.REFRESH}`
   - 这样修改确保了在保存Teacher/Student实体时不会尝试再次持久化已经存在的User实体

3. **技术原理**
   - 当使用`CascadeType.ALL`或`CascadeType.PERSIST`时，JPA会尝试级联保存关联实体
   - 如果关联的实体已经存在于数据库中但处于"游离"状态，Hibernate会尝试对其执行`persist`操作而非`merge`操作
   - 这会导致`PersistentObjectException: detached entity passed to persist`错误
   - 解决方案是在关联已存在实体时，只使用`MERGE`和`REFRESH`级联操作，而不使用`PERSIST`

4. **如果问题仍然存在**
   - 检查其他实体关系中是否也存在类似配置问题
   - 确保在同一个事务中操作关联实体
   - 使用`entityManager.merge()`而非`entityManager.persist()`操作可能已经存在的实体

这个修复解决了Teacher和Student实体创建过程中的级联持久化问题，使系统初始化过程更加稳定可靠。

#### 表结构不完整问题
如果应用启动后发现部分表（如warning_rules等）没有被创建，可通过以下方法解决：

1. **使用新增的表结构检查工具**
   
   系统现在增加了自动表结构检查机制，每次启动时都会检查缺失的表并尝试创建。如果自动创建失败，请尝试以下方法：

2. **手动执行SQL脚本创建表**

   项目提供了完整的表结构创建脚本，可以在MySQL中执行：
   
   ```sql
   -- 使用你的数据库
   USE academic_monitor;
   
   -- 执行脚本
   SOURCE D:/路径/到你的项目/src/main/resources/create-tables.sql;
   ```

3. **直接在Hibernate中调整配置**
   
   如果以上方法仍未解决问题，尝试在`application.properties`中临时将Hibernate的配置调整为：
   
   ```properties
   spring.jpa.hibernate.ddl-auto=create
   ```
   
   启动应用一次后，再改回：
   
   ```properties
   spring.jpa.hibernate.ddl-auto=update
   ```

4. **处理MySQL关键字冲突问题**

   如果遇到包含 `You have an error in your SQL syntax` 的错误，特别是提到 `condition` 列名的错误，这是因为 `condition` 是MySQL的保留关键字。
   
   解决方法：
   - SQL脚本中已将 `condition` 改为 `rule_condition`
   - 实体类中添加了 `@Column(name = "rule_condition")` 注解
   - 如果还有问题，可以手动执行以下SQL修复表结构：
   
   ```sql
   ALTER TABLE warning_rules CHANGE `condition` rule_condition VARCHAR(255) NOT NULL;
   ```

5. **处理集合不可修改异常**

   如果遇到 `java.lang.UnsupportedOperationException` 错误，尤其是与 `Collections$1.remove` 相关的错误，这通常是因为在Hibernate处理关联实体时，尝试修改不可变集合。
   
   解决方法：
   - 在数据初始化过程中，我们已对关联实体的处理进行了优化，使用ID引用而不是完整对象
   - 如果仍然出现相关错误，可尝试：
     * 使用简单的引用对象（只设置ID）而不是完整加载的实体
     * 避免使用 `Collections.singleton()` 或 `Collections.unmodifiableList()` 等返回不可修改集合的方法
     * 使用新的集合包装原有集合，例如：`new HashSet<>(originalSet)`
   
   如果错误仍然存在，可以尝试分步初始化，先创建基础数据（用户、角色等），再创建关联数据。

6. **检查实体类映射**
   
   如果特定的表一直无法创建，请检查对应的实体类是否有正确的`@Entity`和`@Table`注解。

### 测试数据内容
系统初始化后会创建以下测试数据：

1. **用户账户**
   - 管理员账户: admin/admin123
   - 教师账户: t001-t005/123456
   - 学生账户: {学号}/123456 (学号格式: 年级+序号，如2021001)

2. **测试数据集**
   - 5个教师用户
   - 5个班级，每个班级20名学生，共100名学生 
   - 10门课程
   - 每个学生选修4-6门课程
   - 每门课程有期中、期末、平时作业、实验四种成绩
   - 4种预警规则
   - 约15%的学生有学业预警
   - 系统通知、预警通知、成绩通知等

可通过初始管理员账户登录系统，浏览和测试各项功能。测试数据使应用各个功能都有数据可用，便于功能测试和系统演示。

### 使用 GitHub Token 克隆或推送代码
如果需要使用 GitHub Token 进行权限验证，可以按照以下步骤操作：

1. 克隆仓库时使用 Token：
```bash
git clone https://{TOKEN}@github.com/Dueyin/academic-warning-backend.git
```

2. 或者对于已克隆的仓库，配置远程 URL 包含 Token：
```bash
git remote set-url origin https://{TOKEN}@github.com/Dueyin/academic-warning-backend.git
```

3. 设置本地仓库的用户信息（不影响全局配置）：
```bash
git config --local user.name "Dueyin"
git config --local user.email "dueyin@github.com"
```

4. 然后进行正常的 Git 操作：
```bash
git add .
git commit -m "提交信息"
git push -u origin master
```

注意：请将 `{TOKEN}` 替换为您的实际 GitHub Token。

### 初始账户信息
系统初始化后将自动创建管理员账户:
- 用户名: admin
- 密码: admin123

## API文档
系统主要API端点:
- `/auth/signin` - 用户登录
- `/auth/signup` - 用户注册
- `/users` - 用户管理
- `/students` - 学生管理
- `/teachers` - 教师管理
- `/classes` - 班级管理
- `/courses` - 课程管理
- `/enrollments` - 选课管理
- `/grades` - 成绩管理
- `/warnings` - 预警管理
  - `GET /warnings` - 获取预警列表(分页)
  - `GET /warnings/{id}` - 获取单个预警
  - `POST /warnings` - 创建预警
  - `PUT /warnings/{id}` - 更新预警
  - `POST /warnings/{id}/resolve` - 解决预警
  - `DELETE /warnings/{id}` - 删除预警
  - `GET /warnings/types` - 获取预警类型
  - `GET /warnings/recent` - 获取最近预警
  - `GET /warnings/student/{studentId}` - 获取学生的预警
- `/statistics` - 统计数据
  - `GET /statistics/dashboard` - 获取仪表盘统计
  - `GET /statistics/warnings/distribution` - 获取预警类型分布
  - `GET /statistics/warnings/trends` - 获取预警趋势
- `/notifications` - 通知管理

### 预警类型说明
系统预定义的预警类型：
- `COURSE_GRADE` - 单科成绩预警
- `MULTIPLE_FAIL` - 多科不及格预警
- `SEMESTER_AVERAGE` - 学期平均分预警
- `SEVERE` - 严重学业危机预警

### 预警级别说明
系统中的预警级别：
- `1` - 一般预警
- `2` - 中度预警 
- `3` - 严重预警

### 预警状态说明
预警记录可能的状态：
- `NEW` - 新建预警
- `READ` - 已读预警
- `PROCESSED` - 处理中预警
- `RESOLVED` - 已解决预警

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
    └── DatabaseInitializer.java  # 测试数据初始化器
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
- [x] 预警规则管理
- [x] 预警记录管理
- [x] 统计报表功能
- [ ] 通知管理模块 

# 学业预警系统 - 修复说明（2025-03-27更新）

## 错误解决方案

### 问题描述
系统启动初始化数据时出现了以下问题：
1. 事务回滚问题：`Transaction silently rolled back because it has been marked as rollback-only`
2. 数据创建不完整：数据库中只有少量表的数据，大部分表为空
3. 教师创建错误：`detached entity passed to persist: com.academicmonitor.entity.User`

### 原因分析
1. 在 `DatabaseInitializer` 类中，使用了不完整的实体对象引用（只设置ID的简化对象），导致Hibernate无法正确处理关联关系
2. 整个数据初始化过程在单个大事务中执行，一旦某个步骤出错，所有操作都会回滚
3. 缺少适当的错误处理和日志记录，无法识别具体失败步骤
4. 缺少对依赖数据的检查，当前置数据创建失败时，后续步骤仍然尝试执行但无法成功
5. 教师创建过程中，尝试直接保存已经保存过的分离状态User实体，导致Hibernate抛出异常

### 修复步骤
1. 修复对象引用问题
   - 不使用简化的用户对象，改为使用完整的实体对象引用
   - 修复关联对象处理方法

2. 改进事务管理
   - 将大事务拆分为多个独立的小事务
   - 为每个数据初始化步骤创建单独的事务方法
   - 确保即使部分数据创建失败，其他数据仍能成功提交

3. 增强错误处理
   - 在每个关键方法中添加异常捕获和处理
   - 为每个保存操作添加详细的日志记录
   - 输出具体的错误信息帮助诊断问题

4. 添加依赖检查
   - 确保前置数据存在且有效后再执行后续步骤
   - 对教师、班级等关键对象进行空值检查
   - 当依赖数据缺失时跳过对应的创建步骤

5. 解决"detached entity"错误
   - 在创建教师前检查用户是否已存在
   - 如果用户已存在，使用merge操作而不是persist操作
   - 同样检查教师是否已存在，执行更新或创建

### 修复后特性
- 分步事务：每个主要步骤在独立事务中执行，避免全部回滚
- 容错能力：某个部分失败不会影响其他部分的数据创建
- 详细日志：提供每个步骤的执行状态和错误信息
- 健壮性：应用程序在初始化过程中出错后仍能正常启动和运行
- 增强诊断：输出具体到每个实体的创建状态和错误原因
- 实体检查：防止重复创建和分离实体问题

### 运行方式
1. 不初始化数据库运行：
```
mvn spring-boot:run
```

2. 初始化数据库运行（会显示详细日志）：
```
mvn spring-boot:run -Dspring-boot.run.arguments=--init-db
```

### 排错指南
如果在运行时仍然遇到初始化问题，请查看日志中的具体错误信息：
1. 若出现"创建教师过程中出错"的日志，可能是用户名重复或数据库约束问题
2. 若出现"警告：教师创建失败或为空，跳过创建班级"的日志，表示前置数据缺失
3. 检查数据库中已有数据，可能需要先清空数据库
4. 如果看到"detached entity passed to persist"错误，清空数据库后重试

如有持续问题，可尝试将初始化分两步执行：
1. 先使用常规启动创建表结构: `mvn spring-boot:run`
2. 停止后再使用初始化参数: `mvn spring-boot:run -Dspring-boot.run.arguments=--init-db` 