-- 数据库表结构创建脚本
-- 提供手动方式创建学业监测预警系统所需的所有表结构

-- 首先禁用外键约束检查，以便顺序创建表
SET FOREIGN_KEY_CHECKS = 0;

-- 角色表
CREATE TABLE IF NOT EXISTS roles (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created_at DATETIME,
  updated_at DATETIME,
  name VARCHAR(50) NOT NULL,
  description VARCHAR(255),
  PRIMARY KEY (id),
  UNIQUE KEY UK_roles_name (name)
);

-- 用户表
CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created_at DATETIME,
  updated_at DATETIME,
  username VARCHAR(50) NOT NULL,
  password VARCHAR(255) NOT NULL,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(100),
  phone VARCHAR(20),
  enabled BIT(1) DEFAULT 1,
  PRIMARY KEY (id),
  UNIQUE KEY UK_users_username (username),
  UNIQUE KEY UK_users_email (email)
);

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT FK_user_roles_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT FK_user_roles_role_id FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 教师表
CREATE TABLE IF NOT EXISTS teachers (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created_at DATETIME,
  updated_at DATETIME,
  teacher_number VARCHAR(50) NOT NULL,
  title VARCHAR(50),
  specialty VARCHAR(100),
  user_id BIGINT NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UK_teachers_teacher_number (teacher_number),
  UNIQUE KEY UK_teachers_user_id (user_id),
  CONSTRAINT FK_teachers_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 班级表
CREATE TABLE IF NOT EXISTS class_groups (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created_at DATETIME,
  updated_at DATETIME,
  name VARCHAR(100) NOT NULL,
  year VARCHAR(10),
  description VARCHAR(255),
  head_teacher_id BIGINT,
  PRIMARY KEY (id),
  UNIQUE KEY UK_class_groups_name (name),
  CONSTRAINT FK_class_groups_head_teacher_id FOREIGN KEY (head_teacher_id) REFERENCES teachers(id)
);

-- 学生表
CREATE TABLE IF NOT EXISTS students (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created_at DATETIME,
  updated_at DATETIME,
  student_number VARCHAR(50) NOT NULL,
  date_of_birth DATE,
  gender VARCHAR(10),
  address VARCHAR(255),
  emergency_contact VARCHAR(100),
  emergency_phone VARCHAR(20),
  has_warning BIT(1) DEFAULT 0,
  user_id BIGINT NOT NULL,
  class_group_id BIGINT,
  PRIMARY KEY (id),
  UNIQUE KEY UK_students_student_number (student_number),
  UNIQUE KEY UK_students_user_id (user_id),
  CONSTRAINT FK_students_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT FK_students_class_group_id FOREIGN KEY (class_group_id) REFERENCES class_groups(id)
);

-- 课程表
CREATE TABLE IF NOT EXISTS courses (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created_at DATETIME,
  updated_at DATETIME,
  course_code VARCHAR(50) NOT NULL,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(255),
  credit INT,
  semester VARCHAR(50),
  teacher_id BIGINT,
  PRIMARY KEY (id),
  UNIQUE KEY UK_courses_course_code (course_code),
  CONSTRAINT FK_courses_teacher_id FOREIGN KEY (teacher_id) REFERENCES teachers(id)
);

-- 选课记录表
CREATE TABLE IF NOT EXISTS course_enrollments (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created_at DATETIME,
  updated_at DATETIME,
  student_id BIGINT NOT NULL,
  course_id BIGINT NOT NULL,
  semester VARCHAR(50),
  is_active BIT(1) DEFAULT 1,
  PRIMARY KEY (id),
  UNIQUE KEY UK_enrollments_student_course (student_id, course_id),
  CONSTRAINT FK_enrollments_student_id FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
  CONSTRAINT FK_enrollments_course_id FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
);

-- 成绩表
CREATE TABLE IF NOT EXISTS grades (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created_at DATETIME,
  updated_at DATETIME,
  enrollment_id BIGINT NOT NULL,
  grade_type VARCHAR(50) NOT NULL,
  score DOUBLE NOT NULL,
  max_score DOUBLE DEFAULT 100.0,
  weight INT,
  exam_date DATE,
  recorded_by_id BIGINT,
  PRIMARY KEY (id),
  CONSTRAINT FK_grades_enrollment_id FOREIGN KEY (enrollment_id) REFERENCES course_enrollments(id) ON DELETE CASCADE,
  CONSTRAINT FK_grades_recorded_by_id FOREIGN KEY (recorded_by_id) REFERENCES users(id)
);

-- 预警规则表
CREATE TABLE IF NOT EXISTS warning_rules (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created_at DATETIME,
  updated_at DATETIME,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(255),
  type VARCHAR(50) NOT NULL,
  `condition` VARCHAR(255) NOT NULL,
  level INT NOT NULL,
  color VARCHAR(50),
  is_active BIT(1) DEFAULT 1,
  PRIMARY KEY (id)
);

-- 预警记录表
CREATE TABLE IF NOT EXISTS warning_records (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created_at DATETIME,
  updated_at DATETIME,
  student_id BIGINT NOT NULL,
  rule_id BIGINT NOT NULL,
  warning_content TEXT,
  warning_time DATETIME,
  status VARCHAR(20) DEFAULT 'NEW',
  processed_time DATETIME,
  processed_by_id BIGINT,
  process_notes TEXT,
  PRIMARY KEY (id),
  CONSTRAINT FK_warning_records_student_id FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
  CONSTRAINT FK_warning_records_rule_id FOREIGN KEY (rule_id) REFERENCES warning_rules(id) ON DELETE CASCADE,
  CONSTRAINT FK_warning_records_processed_by_id FOREIGN KEY (processed_by_id) REFERENCES users(id)
);

-- 通知表
CREATE TABLE IF NOT EXISTS notifications (
  id BIGINT NOT NULL AUTO_INCREMENT,
  created_at DATETIME,
  updated_at DATETIME,
  user_id BIGINT NOT NULL,
  title VARCHAR(100) NOT NULL,
  content TEXT,
  type VARCHAR(20),
  related_object VARCHAR(50),
  is_read BIT(1) DEFAULT 0,
  sender_id BIGINT,
  send_time DATETIME,
  read_time DATETIME,
  PRIMARY KEY (id),
  CONSTRAINT FK_notifications_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT FK_notifications_sender_id FOREIGN KEY (sender_id) REFERENCES users(id)
);

-- 最后重新启用外键约束检查
SET FOREIGN_KEY_CHECKS = 1;

-- 脚本执行完成信息
SELECT 'All tables have been created successfully!' AS result; 