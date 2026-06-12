USE mall_dev;
SET NAMES utf8mb4;

SET @old_fk_checks := @@FOREIGN_KEY_CHECKS;
SET @old_sql_safe_updates := @@SQL_SAFE_UPDATES;
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_SAFE_UPDATES = 0;
SET SESSION cte_max_recursion_depth = 2000;

START TRANSACTION;

DELETE FROM oms_order_item
WHERE order_id IN (
  SELECT x.id
  FROM (
    SELECT id
    FROM oms_order
    WHERE note = 'seed_real_30d'
  ) x
);

DELETE FROM oms_order
WHERE note = 'seed_real_30d';

DELETE FROM pms_sku_stock
WHERE sku_id IN (
  SELECT x.id
  FROM (
    SELECT id
    FROM pms_sku
    WHERE sku_code LIKE 'REAL30SKU%'
  ) x
);

DELETE FROM pms_sku
WHERE sku_code LIKE 'REAL30SKU%';

DELETE FROM pms_product
WHERE sub_title LIKE '[REAL30D]%';

DELETE FROM ums_member
WHERE email LIKE '%@seed.mall.local'
  AND username LIKE 'urban_buyer_%';

INSERT INTO ums_member (username, password, phone, email, avatar, status, create_time, deleted)
WITH RECURSIVE seq AS (
  SELECT 1 AS n
  UNION ALL
  SELECT n + 1 FROM seq WHERE n < 18
)
SELECT
  CONCAT('urban_buyer_', LPAD(n, 2, '0')),
  '123456',
  CONCAT('1397000', LPAD(100 + n, 4, '0')),
  CONCAT('urban_buyer_', LPAD(n, 2, '0'), '@seed.mall.local'),
  NULL,
  1,
  DATE_SUB(
    DATE_SUB(NOW(), INTERVAL ((n * 3 + 2) % 29) DAY),
    INTERVAL ((n * 47 + 11) % 1200) MINUTE
  ),
  0
FROM seq;

INSERT INTO pms_product (
  name, sub_title, brand_id, category_id, cover_img, min_price, original_price,
  detail_html, publish_status, verify_status, create_time, deleted
)
SELECT 'Aster X1 Phone Pro', '[REAL30D] premium phone line', 1, 1011, '/uploads/seed-real-30d/phone-01.jpg', 4299.00, 4699.00,
       '<p>Fast refresh display, balanced cameras, and all day battery life.</p>', 1, 1, DATE_SUB(NOW(), INTERVAL 25 DAY), 0
UNION ALL
SELECT 'Lumen Air Phone 256', '[REAL30D] slim phone line', 1, 1011, '/uploads/seed-real-30d/phone-02.jpg', 3599.00, 3999.00,
       '<p>Lightweight design for commuting, study, and social photography.</p>', 1, 1, DATE_SUB(NOW(), INTERVAL 23 DAY), 0
UNION ALL
SELECT 'Nova Tab 11 WiFi', '[REAL30D] entertainment tablet line', 1, 1011, '/uploads/seed-real-30d/tablet-01.jpg', 2799.00, 3099.00,
       '<p>Large display for streaming, note taking, and mobile office tasks.</p>', 1, 1, DATE_SUB(NOW(), INTERVAL 22 DAY), 0
UNION ALL
SELECT 'Summit Book 14', '[REAL30D] business notebook line', 1, 1011, '/uploads/seed-real-30d/laptop-01.jpg', 5899.00, 6399.00,
       '<p>Thin and quiet notebook tuned for office work and travel.</p>', 1, 1, DATE_SUB(NOW(), INTERVAL 21 DAY), 0
UNION ALL
SELECT 'Echo Buds ANC', '[REAL30D] wireless audio line', 1, 1012, '/uploads/seed-real-30d/earbuds-01.jpg', 499.00, 599.00,
       '<p>Noise canceling earbuds for calls, commuting, and focused work.</p>', 1, 1, DATE_SUB(NOW(), INTERVAL 20 DAY), 0
UNION ALL
SELECT 'Orbit Watch S2', '[REAL30D] wearable line', 1, 1012, '/uploads/seed-real-30d/watch-01.jpg', 1299.00, 1499.00,
       '<p>Daily health tracking with a clean design for office and gym.</p>', 1, 1, DATE_SUB(NOW(), INTERVAL 19 DAY), 0
UNION ALL
SELECT 'Swift Charge 20W Bank', '[REAL30D] charging accessory line', 1, 1011, '/uploads/seed-real-30d/powerbank-01.jpg', 159.00, 199.00,
       '<p>Compact power bank with dual output for phones and earbuds.</p>', 1, 1, DATE_SUB(NOW(), INTERVAL 18 DAY), 0
UNION ALL
SELECT 'Vista Mirrorless Lite', '[REAL30D] camera line', 1, 1011, '/uploads/seed-real-30d/camera-01.jpg', 4699.00, 5199.00,
       '<p>Travel friendly mirrorless camera with reliable color rendering.</p>', 1, 1, DATE_SUB(NOW(), INTERVAL 17 DAY), 0
UNION ALL
SELECT 'Aster Max Phone 512', '[REAL30D] power user phone line', 1, 1011, '/uploads/seed-real-30d/phone-01.jpg', 4999.00, 5399.00,
       '<p>Bigger battery, stronger cooling, and faster charging for heavy use.</p>', 1, 1, DATE_SUB(NOW(), INTERVAL 16 DAY), 0
UNION ALL
SELECT 'Nova Tab Air 128', '[REAL30D] light tablet line', 1, 1011, '/uploads/seed-real-30d/tablet-01.jpg', 2399.00, 2699.00,
       '<p>Light body and bright panel for reading, meetings, and drama nights.</p>', 1, 1, DATE_SUB(NOW(), INTERVAL 15 DAY), 0
UNION ALL
SELECT 'Summit Studio 14', '[REAL30D] creator notebook line', 1, 1011, '/uploads/seed-real-30d/laptop-01.jpg', 6999.00, 7599.00,
       '<p>Extra memory and storage for editing, design, and creator workflows.</p>', 1, 1, DATE_SUB(NOW(), INTERVAL 14 DAY), 0
UNION ALL
SELECT 'Echo Buds Pro', '[REAL30D] premium audio line', 1, 1012, '/uploads/seed-real-30d/earbuds-01.jpg', 699.00, 799.00,
       '<p>Comfortable fit with stronger call pickup for long daily use.</p>', 1, 1, DATE_SUB(NOW(), INTERVAL 13 DAY), 0;

INSERT INTO pms_sku (spu_id, sku_code, spec_json, price, create_time, deleted)
SELECT
  p.id,
  CONCAT('REAL30SKU', LPAD(p.id, 6, '0')),
  CASE p.name
    WHEN 'Aster X1 Phone Pro' THEN '{"color":"black","storage":"256GB"}'
    WHEN 'Lumen Air Phone 256' THEN '{"color":"silver","storage":"256GB"}'
    WHEN 'Nova Tab 11 WiFi' THEN '{"color":"blue","storage":"256GB"}'
    WHEN 'Summit Book 14' THEN '{"color":"silver","memory":"16GB"}'
    WHEN 'Echo Buds ANC' THEN '{"color":"graphite","type":"anc"}'
    WHEN 'Orbit Watch S2' THEN '{"color":"titanium","size":"46mm"}'
    WHEN 'Swift Charge 20W Bank' THEN '{"color":"gray","capacity":"10000mAh"}'
    WHEN 'Vista Mirrorless Lite' THEN '{"color":"silver","kit":"18-55mm"}'
    WHEN 'Aster Max Phone 512' THEN '{"color":"black","storage":"512GB"}'
    WHEN 'Nova Tab Air 128' THEN '{"color":"white","storage":"128GB"}'
    WHEN 'Summit Studio 14' THEN '{"color":"gray","memory":"32GB"}'
    ELSE '{"color":"black","type":"standard"}'
  END,
  p.min_price,
  p.create_time,
  0
FROM pms_product p
WHERE p.sub_title LIKE '[REAL30D]%';

INSERT INTO pms_sku_stock (sku_id, stock, low_stock, version)
SELECT
  s.id,
  120 + (ROW_NUMBER() OVER (ORDER BY s.id) * 7),
  12,
  0
FROM pms_sku s
JOIN pms_product p ON p.id = s.spu_id
WHERE p.sub_title LIKE '[REAL30D]%';

DROP TEMPORARY TABLE IF EXISTS tmp_real30_members;
CREATE TEMPORARY TABLE tmp_real30_members (
  rn INT NOT NULL PRIMARY KEY,
  member_id BIGINT NOT NULL
);

INSERT INTO tmp_real30_members (rn, member_id)
SELECT
  ROW_NUMBER() OVER (ORDER BY id) AS rn,
  id
FROM ums_member
WHERE deleted = 0
  AND (username = 'user01' OR email LIKE '%@seed.mall.local');

DROP TEMPORARY TABLE IF EXISTS tmp_real30_sku;
CREATE TEMPORARY TABLE tmp_real30_sku (
  rn INT NOT NULL PRIMARY KEY,
  sku_id BIGINT NOT NULL,
  spu_id BIGINT NOT NULL,
  spu_name VARCHAR(256) NOT NULL,
  pic VARCHAR(512) DEFAULT NULL,
  sku_code VARCHAR(64) NOT NULL,
  spec_json VARCHAR(512) DEFAULT NULL,
  price DECIMAL(10,2) NOT NULL
);

INSERT INTO tmp_real30_sku (rn, sku_id, spu_id, spu_name, pic, sku_code, spec_json, price)
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
  AND p.sub_title LIKE '[REAL30D]%';

SET @member_cnt := (SELECT COUNT(*) FROM tmp_real30_members);
SET @sku_cnt := (SELECT COUNT(*) FROM tmp_real30_sku);

DROP TEMPORARY TABLE IF EXISTS tmp_real30_orders;
CREATE TEMPORARY TABLE tmp_real30_orders (
  n INT NOT NULL PRIMARY KEY,
  order_no VARCHAR(32) NOT NULL,
  member_id BIGINT NOT NULL,
  days_ago INT NOT NULL,
  create_time DATETIME NOT NULL,
  status TINYINT NOT NULL
);

INSERT INTO tmp_real30_orders (n, order_no, member_id, days_ago, create_time, status)
WITH RECURSIVE seq AS (
  SELECT 1 AS n
  UNION ALL
  SELECT n + 1 FROM seq WHERE n < 100
)
SELECT
  n,
  CONCAT('R30', DATE_FORMAT(NOW(), '%m%d%H%i'), LPAD(n, 4, '0')),
  (SELECT member_id FROM tmp_real30_members WHERE rn = ((n * 5 + 2) % @member_cnt) + 1),
  ((n * 7 + 3) % 30),
  TIMESTAMP(
    DATE_SUB(CURDATE(), INTERVAL ((n * 7 + 3) % 30) DAY),
    MAKETIME(9 + ((n * 11) % 11), (n * 17) % 60, (n * 29) % 60)
  ),
  CASE
    WHEN ((n * 7 + 3) % 30) >= 20 THEN 4
    WHEN ((n * 7 + 3) % 30) >= 10 THEN 3
    WHEN ((n * 7 + 3) % 30) >= 3 THEN 2
    ELSE 1
  END
FROM seq;

INSERT INTO oms_order (
  order_no, member_id, total_amount, pay_amount, discount_amount, freight, status, pay_type, note,
  delivery_company, delivery_sn, receiver_name, receiver_phone, full_address, create_time, pay_time,
  delivery_time, receive_time, finish_time, cancel_time, deleted
)
SELECT
  t.order_no,
  t.member_id,
  0,
  0,
  0,
  0,
  t.status,
  0,
  'seed_real_30d',
  CASE WHEN t.status >= 3 THEN ELT((t.n % 3) + 1, 'SF Express', 'JD Logistics', 'ZTO Express') ELSE NULL END,
  CASE WHEN t.status >= 3 THEN CONCAT('SF', DATE_FORMAT(t.create_time, '%m%d'), LPAD(t.n, 6, '0')) ELSE NULL END,
  ELT((t.n % 6) + 1, 'Lena Lin', 'Mia Zhou', 'Aiden Chen', 'Ethan Gu', 'Nora Shen', 'Ryan Xu'),
  CONCAT('13988', LPAD(10000 + t.n, 6, '0')),
  ELT((t.n % 5) + 1,
      'No. 88 Zhangjiang Rd, Pudong, Shanghai',
      'No. 218 Wensan Rd, Xihu, Hangzhou',
      'No. 66 Nanshan Science Park, Shenzhen',
      'No. 588 Tianfu Ave, Chengdu',
      'No. 77 Optics Valley Ave, Wuhan'),
  t.create_time,
  DATE_ADD(t.create_time, INTERVAL 12 + (t.n % 70) MINUTE),
  CASE
    WHEN t.status >= 3 THEN DATE_ADD(t.create_time, INTERVAL 2 + (t.n % 4) DAY)
    ELSE NULL
  END,
  CASE
    WHEN t.status = 4 THEN DATE_ADD(t.create_time, INTERVAL 5 + (t.n % 3) DAY)
    ELSE NULL
  END,
  CASE
    WHEN t.status = 4 THEN DATE_ADD(t.create_time, INTERVAL 6 + (t.n % 3) DAY)
    ELSE NULL
  END,
  NULL,
  0
FROM tmp_real30_orders t;

DROP TEMPORARY TABLE IF EXISTS tmp_real30_lines;
CREATE TEMPORARY TABLE tmp_real30_lines (
  order_n INT NOT NULL,
  line_no INT NOT NULL,
  sku_rn INT NOT NULL,
  quantity INT NOT NULL,
  PRIMARY KEY (order_n, line_no)
);

INSERT INTO tmp_real30_lines (order_n, line_no, sku_rn, quantity)
SELECT n, 1, ((n * 7 + 1) % @sku_cnt) + 1, (n % 3) + 1
FROM tmp_real30_orders;

INSERT INTO tmp_real30_lines (order_n, line_no, sku_rn, quantity)
SELECT n, 2, ((n * 11 + 4) % @sku_cnt) + 1, ((n + 1) % 2) + 1
FROM tmp_real30_orders
WHERE MOD(n, 3) <> 0;

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
  l.quantity,
  ROUND(s.price * l.quantity, 2)
FROM tmp_real30_lines l
JOIN tmp_real30_orders t ON t.n = l.order_n
JOIN oms_order o ON o.order_no = t.order_no
JOIN tmp_real30_sku s ON s.rn = l.sku_rn;

UPDATE oms_order o
JOIN (
  SELECT
    oi.order_id,
    SUM(oi.total_price) AS subtotal
  FROM oms_order_item oi
  JOIN oms_order so ON so.id = oi.order_id
  WHERE so.note = 'seed_real_30d'
  GROUP BY oi.order_id
) x ON x.order_id = o.id
SET
  o.total_amount = x.subtotal,
  o.discount_amount = CASE
    WHEN MOD(o.id, 8) = 0 THEN ROUND(x.subtotal * 0.08, 2)
    WHEN MOD(o.id, 5) = 0 THEN ROUND(x.subtotal * 0.05, 2)
    ELSE 0.00
  END,
  o.freight = CASE WHEN x.subtotal < 299 THEN 12.00 ELSE 0.00 END,
  o.pay_amount = x.subtotal
    - CASE
        WHEN MOD(o.id, 8) = 0 THEN ROUND(x.subtotal * 0.08, 2)
        WHEN MOD(o.id, 5) = 0 THEN ROUND(x.subtotal * 0.05, 2)
        ELSE 0.00
      END
    + CASE WHEN x.subtotal < 299 THEN 12.00 ELSE 0.00 END
WHERE o.note = 'seed_real_30d';

COMMIT;

SET FOREIGN_KEY_CHECKS = @old_fk_checks;
SET SQL_SAFE_UPDATES = @old_sql_safe_updates;
