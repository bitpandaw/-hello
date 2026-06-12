-- Remove recommendation module data from an existing mall_dev database.
-- Run this only if mall_patch_v3_recommend.sql was applied before.
USE mall_dev;

DELETE FROM ums_role_permission
WHERE permission_id IN (
  SELECT id FROM ums_permission
  WHERE code IN ('recommend:manage', 'recommend:report')
);

DELETE FROM ums_permission
WHERE code IN ('recommend:manage', 'recommend:report');

DROP TABLE IF EXISTS recommend_event_log;
DROP TABLE IF EXISTS recommend_user_result;
DROP TABLE IF EXISTS recommend_train_task;
