-- 缺失表迁移脚本
USE forum;

-- 签到表
CREATE TABLE IF NOT EXISTS user_sign (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_info_id INT NOT NULL,
  sign_date DATETIME NOT NULL,
  points INT DEFAULT 5,
  continuous_days INT DEFAULT 1,
  INDEX idx_user_date (user_info_id, sign_date)
);

-- 帖子点赞表
CREATE TABLE IF NOT EXISTS thread_like (
  id INT AUTO_INCREMENT PRIMARY KEY,
  thread_info_id INT NOT NULL,
  user_info_id INT NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_thread_user (thread_info_id, user_info_id),
  INDEX idx_thread (thread_info_id)
);

-- 评论点赞表
CREATE TABLE IF NOT EXISTS comment_like (
  id INT AUTO_INCREMENT PRIMARY KEY,
  comment_id INT NOT NULL,
  user_info_id INT NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_comment_user (comment_id, user_info_id),
  INDEX idx_comment (comment_id)
);

-- 用户关注表
CREATE TABLE IF NOT EXISTS user_follow (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  follow_user_id INT NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_follow (user_id, follow_user_id),
  INDEX idx_user (user_id),
  INDEX idx_follow (follow_user_id)
);

-- 用户消息通知表
CREATE TABLE IF NOT EXISTS user_message (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  from_user_id INT,
  type VARCHAR(50) COMMENT '消息类型: comment/like/follow/system',
  related_id INT,
  content VARCHAR(500),
  is_read TINYINT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user (user_id),
  INDEX idx_unread (user_id, is_read)
);

-- 用户每日经验表
CREATE TABLE IF NOT EXISTS user_daily_exp (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_info_id INT NOT NULL,
  exp_date DATE NOT NULL,
  exp_got INT DEFAULT 0,
  UNIQUE KEY uk_user_date (user_info_id, exp_date)
);

-- 用户关注板块表
CREATE TABLE IF NOT EXISTS user_sort_follow (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  sort_id INT NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_sort_follow (user_id, sort_id),
  INDEX idx_user (user_id),
  INDEX idx_sort (sort_id)
);

-- 用户访问板块记录表
CREATE TABLE IF NOT EXISTS user_sort_visit (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  sort_id INT NOT NULL,
  visit_type INT DEFAULT 1 COMMENT '访问类型: 1-浏览帖子',
  visit_count INT DEFAULT 1 COMMENT '访问次数',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_sort (user_id, sort_id),
  INDEX idx_user (user_id)
);

-- 用户创建的板块表
CREATE TABLE IF NOT EXISTS user_sort_create (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  sort_id INT NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_sort_create (user_id, sort_id),
  INDEX idx_user (user_id),
  INDEX idx_sort (sort_id)
);

-- 为 user_info 表添加缺失字段（如果还没添加的话）
ALTER TABLE user_info ADD COLUMN last_login_time VARCHAR(50) DEFAULT NULL COMMENT '最后登录时间';
ALTER TABLE user_info ADD COLUMN register_time VARCHAR(50) DEFAULT NULL COMMENT '注册时间';