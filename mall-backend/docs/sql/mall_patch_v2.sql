-- Run after mall.sql: RBAC 菜单树、订单券关联、评价图片
USE mall_dev;

ALTER TABLE ums_permission
  ADD COLUMN parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '0=根' AFTER code,
  ADD COLUMN sort INT NOT NULL DEFAULT 0,
  ADD COLUMN component VARCHAR(128) DEFAULT NULL COMMENT '前端路由 / 组件名';

UPDATE ums_permission SET type=0, parent_id=0, sort=1, path='/product', component='product/list' WHERE id=1;
UPDATE ums_permission SET type=0, parent_id=0, sort=2, path='/order', component='order/list' WHERE id=2;
UPDATE ums_permission SET type=0, parent_id=0, sort=3, path='/member', component='member/list' WHERE id=3;

ALTER TABLE oms_order
  ADD COLUMN coupon_history_id BIGINT NULL COMMENT '领券记录ID' AFTER coupon_id;

ALTER TABLE pms_comment
  ADD COLUMN image_urls TEXT NULL COMMENT 'JSON 数组图片URL' AFTER content;

-- 菜单样例（可扩展）
INSERT INTO ums_permission (id, name, code, type, path, parent_id, sort) VALUES
(10, '控制台', 'dashboard:view', 0, '/dashboard', 0, 0)
ON DUPLICATE KEY UPDATE name=VALUES(name);


-- 确保 admin(id=1) 绑定超级管理员角色
INSERT IGNORE INTO ums_admin_role (admin_id, role_id) VALUES
(1, 1);
INSERT INTO ums_permission (id, name, code, type, path, parent_id, sort, component) VALUES
(11, '角色权限', 'system:role', 0, '/system/role', 0, 4, 'system/role')
ON DUPLICATE KEY UPDATE name=VALUES(name), path=VALUES(path), component=VALUES(component), sort=VALUES(sort);

-- 给超级管理员角色授权菜单（商品/订单/用户/控制台）
INSERT IGNORE INTO ums_role_permission (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 10), (1, 11);
