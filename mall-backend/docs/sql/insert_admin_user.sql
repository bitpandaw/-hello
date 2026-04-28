-- 插入 C 端与后台同账号：用户名 admin，密码 123456（BCrypt，与 Spring BCryptPasswordEncoder 一致）
-- 适用：MySQL 8+，数据库 mall_dev
-- 可重复执行：会更新已存在行的密码与状态

SET NAMES utf8mb4;

-- BCrypt(123456)，strength=12（$2a/$2b 均可被 Spring 校验）
SET @pwd_bcrypt := '$2b$12$sOqZoddFE8Y/RsyucMazgelQqZiWPbgT/Cs5WPQBHsDr2fxG9ktAe';

-- C 端用户 ums_member
INSERT INTO ums_member (username, password, status, deleted)
VALUES ('admin', @pwd_bcrypt, 1, 0)
ON DUPLICATE KEY UPDATE
  password = VALUES(password),
  status   = 1,
  deleted  = 0;

-- 后台管理员 ums_admin
INSERT INTO ums_admin (username, password, status, deleted)
VALUES ('admin', @pwd_bcrypt, 1, 0)
ON DUPLICATE KEY UPDATE
  password = VALUES(password),
  status   = 1,
  deleted  = 0;

-- 关联超级管理员角色（id=1，见 mall.sql 中 ums_role 示例数据）
INSERT IGNORE INTO ums_admin_role (admin_id, role_id)
SELECT a.id, 1
FROM ums_admin a
WHERE a.username = 'admin'
LIMIT 1;
