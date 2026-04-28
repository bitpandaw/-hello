-- Run after mall.sql and mall_patch_v2.sql
USE mall_dev;

CREATE TABLE IF NOT EXISTS recommend_train_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_name VARCHAR(128) NOT NULL,
  model_version VARCHAR(64) NOT NULL,
  status VARCHAR(32) NOT NULL COMMENT 'RUNNING/SUCCESS/FAILED',
  trigger_by VARCHAR(32) NOT NULL DEFAULT 'scheduler',
  create_time DATETIME NOT NULL,
  finish_time DATETIME NULL,
  update_time DATETIME NOT NULL,
  deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='推荐训练任务';

CREATE TABLE IF NOT EXISTS recommend_user_result (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  member_id BIGINT NOT NULL,
  scene VARCHAR(32) NOT NULL DEFAULT 'guess',
  item_ids JSON NOT NULL,
  score_json JSON NULL,
  expire_at DATETIME NOT NULL,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_member_scene_expire (member_id, scene, expire_at)
) COMMENT='推荐结果缓存';

CREATE TABLE IF NOT EXISTS recommend_event_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  member_id BIGINT NOT NULL DEFAULT 0,
  scene VARCHAR(32) NOT NULL,
  request_id VARCHAR(64) NOT NULL,
  item_id BIGINT NOT NULL,
  position INT NOT NULL DEFAULT 0,
  event_type VARCHAR(16) NOT NULL COMMENT 'EXPOSE/CLICK',
  create_time DATETIME NOT NULL,
  KEY idx_scene_time (scene, create_time),
  KEY idx_req_event (request_id, event_type)
) COMMENT='推荐曝光点击日志';

INSERT INTO ums_permission(id, name, code, type, path, parent_id, sort, component)
VALUES
  (20, '推荐管理', 'recommend:manage', 0, '/recommend/manage', 0, 5, 'recommend/manage'),
  (21, '推荐报表', 'recommend:report', 0, '/recommend/report', 0, 6, 'recommend/report')
ON DUPLICATE KEY UPDATE
  name=VALUES(name), code=VALUES(code), path=VALUES(path), parent_id=VALUES(parent_id), sort=VALUES(sort), component=VALUES(component);

INSERT IGNORE INTO ums_role_permission(role_id, permission_id) VALUES
  (1, 20), (1, 21);
