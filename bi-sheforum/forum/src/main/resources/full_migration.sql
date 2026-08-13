-- ============================================================
-- 校园论坛 — 签到/经验/成就徽章 全量数据库迁移脚本
-- 执行: mysql -u root -p forum < full_migration.sql
-- ============================================================

USE forum;

-- ─── 1. user_info 补充字段 ──────────────────────────

ALTER TABLE user_info
  ADD COLUMN IF NOT EXISTS experience INT DEFAULT 0 COMMENT '经验值' AFTER status,
  ADD COLUMN IF NOT EXISTS level INT DEFAULT 1 COMMENT '等级' AFTER experience,
  ADD COLUMN IF NOT EXISTS points INT DEFAULT 0 COMMENT '积分' AFTER level,
  ADD COLUMN IF NOT EXISTS thread_count INT DEFAULT 0 COMMENT '发帖数' AFTER points,
  ADD COLUMN IF NOT EXISTS comment_count INT DEFAULT 0 COMMENT '评论数' AFTER thread_count,
  ADD COLUMN IF NOT EXISTS login_count INT DEFAULT 0 COMMENT '登录次数' AFTER comment_count,
  ADD COLUMN IF NOT EXISTS continuous_login_days INT DEFAULT 0 COMMENT '连续登录天数' AFTER login_count,
  ADD COLUMN IF NOT EXISTS last_login_time VARCHAR(50) DEFAULT NULL COMMENT '最后登录时间' AFTER continuous_login_days,
  ADD COLUMN IF NOT EXISTS register_time VARCHAR(50) DEFAULT NULL COMMENT '注册时间' AFTER last_login_time;

-- ─── 2. 签到表 ──────────────────────────────────────

CREATE TABLE IF NOT EXISTS user_sign (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_info_id INT NOT NULL,
  sign_date DATETIME NOT NULL,
  points INT DEFAULT 5,
  continuous_days INT DEFAULT 1,
  INDEX idx_user_date (user_info_id, sign_date)
);

-- ─── 3. 每日经验表 ──────────────────────────────────

CREATE TABLE IF NOT EXISTS user_daily_exp (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_info_id INT NOT NULL,
  exp_date DATE NOT NULL,
  exp_got INT DEFAULT 0,
  UNIQUE KEY uk_user_date (user_info_id, exp_date)
);

-- ─── 4. 徽章类型定义表 ─────────────────────────────

CREATE TABLE IF NOT EXISTS badge_type (
  id INT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE COMMENT '徽章代码',
  name VARCHAR(50) NOT NULL COMMENT '徽章名称',
  description VARCHAR(200) COMMENT '徽章描述',
  icon VARCHAR(255) COMMENT '徽章图标',
  category VARCHAR(50) COMMENT '分类: thread/comment/sign/login/collect/other',
  condition_type VARCHAR(50) COMMENT '条件类型: thread_count/comment_count/sign_days/login_days/level',
  condition_value INT NOT NULL COMMENT '条件值',
  points_reward INT DEFAULT 0 COMMENT '积分奖励',
  exp_reward INT DEFAULT 0 COMMENT '经验奖励',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ─── 5. 用户徽章关联表 ─────────────────────────────

CREATE TABLE IF NOT EXISTS user_badge (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_info_id INT NOT NULL,
  badge_type_id INT NOT NULL,
  earned_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  is_show TINYINT DEFAULT 1 COMMENT '是否展示',
  UNIQUE KEY uk_user_badge (user_info_id, badge_type_id),
  INDEX idx_user (user_info_id)
);

-- ─── 6. 初始化徽章类型数据 ─────────────────────────

INSERT IGNORE INTO badge_type (code, name, description, icon, category, condition_type, condition_value, points_reward, exp_reward) VALUES

-- 发帖类徽章
('thread_newbie',   '发帖新手',     '发布第一篇帖子',     'el-icon-document',      'thread',  'thread_count',   1,   5,   10),
('thread_active',   '活跃发帖者',   '发布10篇帖子',       'el-icon-document-add',  'thread',  'thread_count',   10,  20,  50),
('thread_master',   '帖子达人',     '发布50篇帖子',       'el-icon-reading',       'thread',  'thread_count',   50,  50,  150),
('thread_legend',   '传奇发帖者',   '发布100篇帖子',      'el-icon-notebook-2',    'thread',  'thread_count',   100, 100, 300),

-- 评论类徽章
('comment_newbie',  '评论新手',     '发布第一条评论',     'el-icon-chat-line-round',  'comment', 'comment_count', 1,   3,   5),
('comment_active',  '活跃评论者',   '发布10条评论',      'el-icon-chat-dot-square',  'comment', 'comment_count', 10,  15,  30),
('comment_master',  '评论达人',     '发布50条评论',      'el-icon-bubble',           'comment', 'comment_count', 50,  40,  100),
('comment_legend',  '传奇评论者',   '发布100条评论',     'el-icon-service',          'comment', 'comment_count', 100, 80,  200),

-- 签到类徽章
('sign_first',      '首次签到',     '完成第一次签到',     'el-icon-calendar', 'sign', 'sign_days', 1,   5,   10),
('sign_week',       '连续签到一周', '连续签到7天',       'el-icon-date',     'sign', 'sign_days', 7,   20,  50),
('sign_month',      '坚持不懈',     '连续签到30天',      'el-icon-clock',    'sign', 'sign_days', 30,  50,  150),
('sign_streak',     '签到达人',     '连续签到100天',     'el-icon-trophy',   'sign', 'sign_days', 100, 100, 300),

-- 登录类徽章
('login_first',     '首次登录',     '完成第一次登录',     'el-icon-user',        'login', 'login_count',  1,   2,   5),
('login_week',      '登录一周',     '累计登录7天',       'el-icon-user-filled', 'login', 'login_count',  7,   10,  20),
('login_month',     '登录一月',     '累计登录30天',      'el-icon-s-custom',    'login', 'login_count',  30,  30,  60),
('login_year',      '登录达人',     '累计登录100天',     'el-icon-medal',       'login', 'login_count',  100, 80,  150),

-- 等级徽章
('level_5',         'Lv.5',         '达到5级',           'el-icon-medal-1',   'other', 'level',  5,   20,  0),
('level_10',        'Lv.10',        '达到10级',          'el-icon-medal',     'other', 'level',  10,  50,  0),
('level_15',        'Lv.15',        '达到15级',          'el-icon-trophy-1',  'other', 'level',  15,  100, 0),
('level_max',       '满级用户',     '达到20级',          'el-icon-cpu',       'other', 'level',  20,  200, 0);
