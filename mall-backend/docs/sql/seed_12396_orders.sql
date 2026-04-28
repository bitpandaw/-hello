-- 生成 12396 条订单测试数据（含订单行）
-- 用途：后台 dashboard 统计/图表联调
-- 执行前请先确保已执行 mall.sql（以及建议执行 mall_patch_v2.sql）

USE mall_dev;

SET NAMES utf8mb4;
SET @old_fk_checks := @@FOREIGN_KEY_CHECKS;
SET @old_sql_safe_updates := @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;
SET FOREIGN_KEY_CHECKS = 0;

-- MySQL 8 默认递归深度 1000，不足以生成 12396 条
SET SESSION cte_max_recursion_depth = 20000;

START TRANSACTION;

-- 0) 清空上次脚本生成的数据（只删 note=seed_12396_orders）
DELETE FROM oms_order_item
WHERE id > 0
  AND order_id IN (
    SELECT x.id
    FROM (
      SELECT id
      FROM oms_order
      WHERE id > 0
        AND note = 'seed_12396_orders'
    ) x
  );

DELETE FROM oms_order
WHERE id > 0
  AND note = 'seed_12396_orders';

-- 1) 确保至少有 1 个会员
INSERT INTO ums_member (username, password, phone, email, status, deleted)
SELECT 'seed_member_001', '123456', '13900000001', 'seed_member_001@mall.local', 1, 0
WHERE NOT EXISTS (SELECT 1 FROM ums_member WHERE deleted = 0);

-- 2) 确保至少有 1 个可售 SKU（若没有则补最小商品数据）
INSERT INTO pms_product_category (id, parent_id, name, level, sort, deleted)
SELECT 900001, 0, '测试分类', 1, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM pms_product_category WHERE deleted = 0);

INSERT INTO pms_product (id, name, sub_title, category_id, cover_img, min_price, original_price, publish_status, verify_status, deleted)
SELECT 900001, '测试商品SPU', '自动生成', 900001, 'https://picsum.photos/seed/test-spu/800/800', 99.00, 129.00, 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM pms_product WHERE deleted = 0 AND publish_status = 1);

INSERT INTO pms_sku (id, spu_id, sku_code, spec_json, price, deleted)
SELECT 900001, 900001, 'SEED-SKU-900001', '{"spec":"default"}', 99.00, 0
WHERE NOT EXISTS (SELECT 1 FROM pms_sku WHERE deleted = 0);

-- 3) 构造可复用的 SKU 池（最多 200 个）
DROP TEMPORARY TABLE IF EXISTS tmp_seed_sku;
CREATE TEMPORARY TABLE tmp_seed_sku (
  rn INT NOT NULL PRIMARY KEY,
  sku_id BIGINT NOT NULL,
  spu_id BIGINT NOT NULL,
  spu_name VARCHAR(256) NOT NULL,
  pic VARCHAR(512) NULL,
  sku_code VARCHAR(64) NOT NULL,
  spec_json VARCHAR(512) NULL,
  price DECIMAL(10,2) NOT NULL
);

INSERT INTO tmp_seed_sku (rn, sku_id, spu_id, spu_name, pic, sku_code, spec_json, price)
SELECT
  ROW_NUMBER() OVER (ORDER BY s.id) AS rn,
  s.id,
  s.spu_id,
  p.name,
  p.cover_img,
  s.sku_code,
  s.spec_json,
  s.price
FROM pms_sku s
JOIN pms_product p ON p.id = s.spu_id
WHERE s.deleted = 0
  AND p.deleted = 0
  AND p.publish_status = 1
LIMIT 200;

-- 兜底：若上面仍然为空，插入硬编码测试 SKU 到临时池
-- 注意：避免在同一条 INSERT 里再次读取目标临时表（MySQL 会报 Can't reopen table）
SET @tmp_sku_empty := (SELECT CASE WHEN COUNT(*) = 0 THEN 1 ELSE 0 END FROM tmp_seed_sku);
INSERT INTO tmp_seed_sku (rn, sku_id, spu_id, spu_name, pic, sku_code, spec_json, price)
SELECT 1, 900001, 900001, '测试商品SPU', 'https://picsum.photos/seed/test-spu/800/800', 'SEED-SKU-900001', '{"spec":"default"}', 99.00
WHERE @tmp_sku_empty = 1;

SET @sku_cnt := (SELECT COUNT(*) FROM tmp_seed_sku);
SET @member_cnt := (SELECT COUNT(*) FROM ums_member WHERE deleted = 0);
SET @batch_tag := DATE_FORMAT(NOW(), '%Y%m%d%H%i%s');

-- 4) 构造可复用的会员池（避免在子查询中使用动态 LIMIT 偏移）
DROP TEMPORARY TABLE IF EXISTS tmp_seed_member;
CREATE TEMPORARY TABLE tmp_seed_member (
  rn INT NOT NULL PRIMARY KEY,
  member_id BIGINT NOT NULL
);

INSERT INTO tmp_seed_member (rn, member_id)
SELECT
  ROW_NUMBER() OVER (ORDER BY m.id) AS rn,
  m.id
FROM ums_member m
WHERE m.deleted = 0;

-- 4) 先生成订单头（12396 条）
DROP TEMPORARY TABLE IF EXISTS tmp_seed_orders;
CREATE TEMPORARY TABLE tmp_seed_orders (
  n INT NOT NULL PRIMARY KEY,
  member_id BIGINT NOT NULL,
  sku_rn INT NOT NULL,
  quantity INT NOT NULL,
  create_time DATETIME NOT NULL,
  status TINYINT NOT NULL
);

INSERT INTO tmp_seed_orders (n, member_id, sku_rn, quantity, create_time, status)
WITH RECURSIVE seq AS (
  SELECT 1 AS n
  UNION ALL
  SELECT n + 1 FROM seq WHERE n < 12396
)
SELECT
  seq.n,
  tm.member_id,
  (
    MOD(CRC32(CONCAT('sku-', @batch_tag, '-', seq.n)), @sku_cnt) + 1
  ) AS sku_rn,
  (
    CASE
      WHEN MOD(CRC32(CONCAT('qty-', @batch_tag, '-', seq.n)), 100) < 55 THEN 1
      WHEN MOD(CRC32(CONCAT('qty-', @batch_tag, '-', seq.n)), 100) < 85 THEN 2
      WHEN MOD(CRC32(CONCAT('qty-', @batch_tag, '-', seq.n)), 100) < 95 THEN 3
      ELSE 4
    END
  ) AS quantity,
  (
    DATE_SUB(
      NOW(),
      INTERVAL MOD(CRC32(CONCAT('day-', @batch_tag, '-', seq.n)), 7) DAY
    )
    - INTERVAL MOD(CRC32(CONCAT('sec-', @batch_tag, '-', seq.n)), 86400) SECOND
  ) AS create_time,
  CASE
    WHEN MOD(CRC32(CONCAT('st-', @batch_tag, '-', seq.n)), 100) < 8 THEN 0   -- 待支付
    WHEN MOD(CRC32(CONCAT('st-', @batch_tag, '-', seq.n)), 100) < 18 THEN 4  -- 已完成
    WHEN MOD(CRC32(CONCAT('st-', @batch_tag, '-', seq.n)), 100) < 38 THEN 3  -- 已收货
    WHEN MOD(CRC32(CONCAT('st-', @batch_tag, '-', seq.n)), 100) < 72 THEN 2  -- 已发货
    ELSE 1                                                                     -- 已支付
  END AS status
FROM seq
JOIN tmp_seed_member tm
  ON tm.rn = MOD(CRC32(CONCAT('mem-', @batch_tag, '-', seq.n)), @member_cnt) + 1;

INSERT INTO oms_order (
  order_no, member_id, total_amount, pay_amount, discount_amount, freight,
  status, pay_type, note, coupon_id, delivery_company, delivery_sn,
  receiver_name, receiver_phone, full_address,
  create_time, pay_time, delivery_time, receive_time, finish_time, cancel_time, deleted
)
SELECT
  CONCAT('T', @batch_tag, LPAD(t.n, 5, '0')) AS order_no,
  t.member_id,
  ROUND((s.price * t.quantity) + (MOD(CRC32(CONCAT('fee-', @batch_tag, '-', t.n)), 2) * 6.00), 2) AS total_amount,
  ROUND(
    ((s.price * t.quantity) + (MOD(CRC32(CONCAT('fee-', @batch_tag, '-', t.n)), 2) * 6.00))
    - (
      CASE
        WHEN MOD(CRC32(CONCAT('disc-', @batch_tag, '-', t.n)), 100) < 22
          THEN ROUND((s.price * t.quantity) * 0.05, 2)
        ELSE 0
      END
    ),
    2
  ) AS pay_amount,
  CASE
    WHEN MOD(CRC32(CONCAT('disc-', @batch_tag, '-', t.n)), 100) < 22
      THEN ROUND((s.price * t.quantity) * 0.05, 2)
    ELSE 0
  END AS discount_amount,
  (MOD(CRC32(CONCAT('fee-', @batch_tag, '-', t.n)), 2) * 6.00) AS freight,
  t.status,
  1 AS pay_type,
  'seed_12396_orders' AS note,
  NULL AS coupon_id,
  CASE WHEN t.status >= 2 THEN '顺丰' ELSE NULL END AS delivery_company,
  CASE WHEN t.status >= 2 THEN CONCAT('SF', @batch_tag, LPAD(t.n, 7, '0')) ELSE NULL END AS delivery_sn,
  '测试收货人' AS receiver_name,
  '13900000000' AS receiver_phone,
  '广东省深圳市南山区科技园测试路 1 号' AS full_address,
  t.create_time,
  CASE WHEN t.status >= 1 THEN DATE_ADD(t.create_time, INTERVAL 5 MINUTE) ELSE NULL END AS pay_time,
  CASE WHEN t.status >= 2 THEN DATE_ADD(t.create_time, INTERVAL 1 DAY) ELSE NULL END AS delivery_time,
  CASE WHEN t.status >= 3 THEN DATE_ADD(t.create_time, INTERVAL 3 DAY) ELSE NULL END AS receive_time,
  CASE WHEN t.status >= 4 THEN DATE_ADD(t.create_time, INTERVAL 4 DAY) ELSE NULL END AS finish_time,
  NULL AS cancel_time,
  0 AS deleted
FROM tmp_seed_orders t
JOIN tmp_seed_sku s ON s.rn = t.sku_rn;

-- 5) 生成订单行（与上面 12396 条订单一一对应，每单 1 行）
INSERT INTO oms_order_item (
  order_id, sku_id, spu_id, spu_name, pic, sku_code, spec_json, price, quantity, total_price
)
SELECT
  o.id,
  s.sku_id,
  s.spu_id,
  s.spu_name,
  s.pic,
  s.sku_code,
  s.spec_json,
  s.price,
  t.quantity,
  ROUND(s.price * t.quantity, 2) AS total_price
FROM tmp_seed_orders t
JOIN oms_order o ON o.order_no COLLATE utf8mb4_unicode_ci =
  CONCAT('T', @batch_tag, LPAD(t.n, 5, '0')) COLLATE utf8mb4_unicode_ci
JOIN tmp_seed_sku s ON s.rn = t.sku_rn;

COMMIT;

SET FOREIGN_KEY_CHECKS = @old_fk_checks;
SET SQL_SAFE_UPDATES = @old_sql_safe_updates;

-- 6) 校验
SELECT 'seed_12396_orders inserted' AS msg, COUNT(*) AS cnt
FROM oms_order
WHERE note = 'seed_12396_orders'
  AND order_no COLLATE utf8mb4_unicode_ci LIKE
      CONCAT('T', @batch_tag, '%') COLLATE utf8mb4_unicode_ci;

