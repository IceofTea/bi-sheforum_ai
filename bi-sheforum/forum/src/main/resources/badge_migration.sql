-- 成就徽章系统数据库脚本

USE forum;

-- 徽章类型定义表
CREATE TABLE IF NOT EXISTS badge_type (
  id INT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE COMMENT '徽章代码',
  name VARCHAR(50) NOT NULL COMMENT '徽章名称',
  description VARCHAR(200) COMMENT '徽章描述',
  icon VARCHAR(255) COMMENT '徽章图标',
  category VARCHAR(50) COMMENT '分类: thread/comment/sign/login/collect/other',
  condition_type VARCHAR(50) COMMENT '条件类型: thread_count/comment_count/sign_days/login_days/follower_count',
  condition_value INT NOT NULL COMMENT '条件值',
  points_reward INT DEFAULT 0 COMMENT '积分奖励',
  exp_reward INT DEFAULT 0 COMMENT '经验奖励',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 用户徽章关联表
CREATE TABLE IF NOT EXISTS user_badge (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_info_id INT NOT NULL,
  badge_type_id INT NOT NULL,
  earned_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  is_show TINYINT DEFAULT 1 COMMENT '是否展示',
  UNIQUE KEY uk_user_badge (user_info_id, badge_type_id),
  INDEX idx_user (user_info_id)
);

-- 初始化徽章类型数据
INSERT INTO badge_type (code, name, description, icon, category, condition_type, condition_value, points_reward, exp_reward) VALUES
-- 发帖类徽章
('thread_newbie', '发帖新手', '发布第一篇帖子', 'el-icon-document', 'thread', 'thread_count', 1, 5, 10),
('thread_active', '活跃发帖者', '发布10篇帖子', 'el-icon-document-add', 'thread', 'thread_count', 10, 20, 50),
('thread_master', '帖子达人', '发布50篇帖子', 'el-icon-reading', 'thread', 'thread_count', 50, 50, 150),
('thread_legend', '传奇发帖者', '发布100篇帖子', 'el-icon-notebook-2', 'thread', 'thread_count', 100, 100, 300),

-- 评论类徽章
('comment_newbie', '评论新手', '发布第一条评论', 'el-icon-chat-line-round', 'comment', 'comment_count', 1, 3, 5),
('comment_active', '活跃评论者', '发布10条评论', 'el-icon-chat-dot-square', 'comment', 'comment_count', 10, 15, 30),
('comment_master', '评论达人', '发布50条评论', 'el-icon-bubble', 'comment', 'comment_count', 50, 40, 100),
('comment_legend', '传奇评论者', '发布100条评论', 'el-icon-service', 'comment', 'comment_count', 100, 80, 200),

-- 签到类徽章
('sign_first', '首次签到', '完成第一次签到', 'el-icon-calendar', 'sign', 'sign_days', 1, 5, 10),
('sign_week', '连续签到一周', '连续签到7天', 'el-icon-date', 'sign', 'sign_days', 7, 20, 50),
('sign_month', '坚持不懈', '连续签到30天', 'el-icon-clock', 'sign', 'sign_days', 30, 50, 150),
('sign_streak', '签到达人', '连续签到100天', 'el-icon-trophy', 'sign', 'sign_days', 100, 100, 300),

-- 登录类徽章
('login_first', '首次登录', '完成第一次登录', 'el-icon-user', 'login', 'login_days', 1, 2, 5),
('login_week', '登录一周', '累计登录7天', 'el-icon-user-filled', 'login', 'login_count', 7, 10, 20),
('login_month', '登录一月', '累计登录30天', 'el-icon-s-custom', 'login', 'login_count', 30, 30, 60),
('login_year', '登录达人', '累计登录100天', 'el-icon-medal', 'login', 'login_count', 100, 80, 150),

-- 收藏类徽章
('collect_first', '首次收藏', '收藏第一篇帖子', 'el-icon-star-off', 'collect', 'collect_count', 1, 3, 5),
('collect_active', '收藏达人', '收藏10篇帖子', 'el-icon-star-on', 'collect', 'collect_count', 10, 15, 30),
('collect_master', '收藏专家', '收藏50篇帖子', 'el-icon-star', 'collect', 'collect_count', 50, 40, 100),

-- 经验等级徽章
('level_5', 'Lv.5', '达到5级', 'el-icon-medal-1', 'other', 'level', 5, 20, 0),
('level_10', 'Lv.10', '达到10级', 'el-icon-medal', 'other', 'level', 10, 50, 0),
('level_15', 'Lv.15', '达到15级', 'el-icon-trophy-1', 'other', 'level', 15, 100, 0),
('level_max', '满级用户', '达到20级', 'el-icon-cpu', 'other', 'level', 20, 200, 0);

-- 添加收藏数字段（如果 user_info 表没有的话）
-- ALTER TABLE user_info ADD COLUMN collect_count INT DEFAULT 0 COMMENT '收藏数' AFTER comment_count;