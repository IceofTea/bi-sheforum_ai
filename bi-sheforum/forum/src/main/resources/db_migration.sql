-- 校园论坛数据库迁移脚本
-- 添加用户经验等级积分系统所需字段
-- 执行方式: mysql -u root -p forum < db_migration.sql

USE forum;

-- 添加用户统计字段
ALTER TABLE user_info 
ADD COLUMN experience INT DEFAULT 0 COMMENT '经验值' AFTER status,
ADD COLUMN level INT DEFAULT 1 COMMENT '等级' AFTER experience,
ADD COLUMN points INT DEFAULT 0 COMMENT '积分' AFTER level,
ADD COLUMN thread_count INT DEFAULT 0 COMMENT '发帖数' AFTER points,
ADD COLUMN comment_count INT DEFAULT 0 COMMENT '评论数' AFTER thread_count,
ADD COLUMN login_count INT DEFAULT 0 COMMENT '登录次数' AFTER comment_count,
ADD COLUMN continuous_login_days INT DEFAULT 0 COMMENT '连续登录天数' AFTER login_count,
ADD COLUMN last_login_time VARCHAR(50) DEFAULT NULL COMMENT '最后登录时间' AFTER continuous_login_days,
ADD COLUMN register_time VARCHAR(50) DEFAULT NULL COMMENT '注册时间' AFTER last_login_time;

-- 初始化现有用户数据（如果需要）
-- UPDATE user_info SET register_time = NOW() WHERE register_time IS NULL;
