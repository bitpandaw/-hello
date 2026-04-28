-- final mall: 批量测试数据（含图片）
-- 适用：MySQL 8+
-- 用途：生成电商前台可见的大量商品数据（品牌、分类、商品、SKU、库存、评价）

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

START TRANSACTION;

-- 1) 基础分类与品牌（若已存在则忽略）
INSERT IGNORE INTO pms_brand (id, name, logo, deleted)
VALUES
  (1001, 'PixelWave',   'https://picsum.photos/seed/brand-1001/240/240', 0),
  (1002, 'NovaHome',    'https://picsum.photos/seed/brand-1002/240/240', 0),
  (1003, 'UrbanWalk',   'https://picsum.photos/seed/brand-1003/240/240', 0),
  (1004, 'FreshLife',   'https://picsum.photos/seed/brand-1004/240/240', 0),
  (1005, 'SmartGear',   'https://picsum.photos/seed/brand-1005/240/240', 0),
  (1006, 'CloudTaste',  'https://picsum.photos/seed/brand-1006/240/240', 0);

INSERT IGNORE INTO pms_product_category (id, parent_id, name, level, sort, deleted)
VALUES
  (100, 0, '精选好物', 1, 1, 0),
  (101, 100, '3C数码', 2, 1, 0),
  (102, 100, '家电家居', 2, 2, 0),
  (103, 100, '服饰箱包', 2, 3, 0),
  (104, 100, '食品生鲜', 2, 4, 0),
  (1011, 101, '手机通讯', 3, 1, 0),
  (1012, 101, '智能穿戴', 3, 2, 0),
  (1021, 102, '厨房电器', 3, 1, 0),
  (1022, 102, '生活家电', 3, 2, 0),
  (1031, 103, '潮流服饰', 3, 1, 0),
  (1032, 103, '旅行箱包', 3, 2, 0),
  (1041, 104, '休闲零食', 3, 1, 0),
  (1042, 104, '新鲜水果', 3, 2, 0);

-- 2) 批量插入 SPU（60条，含封面图与详情图）
SET @product_base := (SELECT IFNULL(MAX(id), 0) FROM pms_product);

INSERT INTO pms_product (
  id, name, sub_title, brand_id, category_id, cover_img, min_price, original_price,
  detail_html, publish_status, verify_status, deleted
)
WITH RECURSIVE seq AS (
  SELECT 1 AS n
  UNION ALL
  SELECT n + 1 FROM seq WHERE n < 60
)
SELECT
  @product_base + n AS id,
  CONCAT(
    CASE
      WHEN n % 6 = 1 THEN '轻旗舰手机 '
      WHEN n % 6 = 2 THEN '智能手表 '
      WHEN n % 6 = 3 THEN '便携咖啡机 '
      WHEN n % 6 = 4 THEN '极简卫衣 '
      WHEN n % 6 = 5 THEN '真空行李箱 '
      ELSE '混合坚果礼盒 '
    END,
    LPAD(n, 3, '0')
  ) AS name,
  CASE
    WHEN n % 6 = 1 THEN '120Hz高刷 / 轻薄长续航'
    WHEN n % 6 = 2 THEN '全天候健康监测 / 蓝牙通话'
    WHEN n % 6 = 3 THEN '15Bar萃取 / 一键奶泡'
    WHEN n % 6 = 4 THEN '宽松剪裁 / 四季可穿'
    WHEN n % 6 = 5 THEN '轻量防刮 / 万向静音轮'
    ELSE '低温烘焙 / 独立小袋'
  END AS sub_title,
  CASE
    WHEN n % 6 = 1 THEN 1001
    WHEN n % 6 = 2 THEN 1005
    WHEN n % 6 = 3 THEN 1002
    WHEN n % 6 = 4 THEN 1003
    WHEN n % 6 = 5 THEN 1003
    ELSE 1006
  END AS brand_id,
  CASE
    WHEN n % 6 = 1 THEN 1011
    WHEN n % 6 = 2 THEN 1012
    WHEN n % 6 = 3 THEN 1021
    WHEN n % 6 = 4 THEN 1031
    WHEN n % 6 = 5 THEN 1032
    ELSE 1041
  END AS category_id,
  CONCAT('https://picsum.photos/seed/mall-spu-', @product_base + n, '/800/800') AS cover_img,
  ROUND(59 + (n * 12.8), 2) AS min_price,
  ROUND(109 + (n * 13.4), 2) AS original_price,
  CONCAT(
    '<h3>商品详情</h3><p>测试商品 #', @product_base + n, '，用于页面联调与回归。</p>',
    '<p><img src="https://picsum.photos/seed/mall-detail-', @product_base + n, '/1200/700" style="max-width:100%;" /></p>',
    '<p><img src="https://picsum.photos/seed/mall-detail2-', @product_base + n, '/1200/700" style="max-width:100%;" /></p>'
  ) AS detail_html,
  1 AS publish_status,
  1 AS verify_status,
  0 AS deleted
FROM seq;

-- 3) 每个 SPU 生成 1 条 SKU
SET @sku_base := (SELECT IFNULL(MAX(id), 0) FROM pms_sku);

INSERT INTO pms_sku (id, spu_id, sku_code, spec_json, price, deleted)
WITH RECURSIVE seq AS (
  SELECT 1 AS n
  UNION ALL
  SELECT n + 1 FROM seq WHERE n < 60
)
SELECT
  @sku_base + n AS id,
  @product_base + n AS spu_id,
  CONCAT('TSKU', LPAD(@sku_base + n, 8, '0')) AS sku_code,
  CONCAT(
    '{"color":"',
    CASE
      WHEN n % 4 = 0 THEN '曜石黑'
      WHEN n % 4 = 1 THEN '月光白'
      WHEN n % 4 = 2 THEN '星际蓝'
      ELSE '晨曦粉'
    END,
    '","edition":"',
    CASE
      WHEN n % 3 = 0 THEN '标准版'
      WHEN n % 3 = 1 THEN '进阶版'
      ELSE '旗舰版'
    END,
    '"}'
  ) AS spec_json,
  ROUND(59 + (n * 12.8), 2) AS price,
  0 AS deleted
FROM seq;

-- 4) SKU 库存
INSERT INTO pms_sku_stock (sku_id, stock, low_stock, version)
WITH RECURSIVE seq AS (
  SELECT 1 AS n
  UNION ALL
  SELECT n + 1 FROM seq WHERE n < 60
)
SELECT
  @sku_base + n AS sku_id,
  80 + (n % 40) AS stock,
  10 AS low_stock,
  0 AS version
FROM seq
ON DUPLICATE KEY UPDATE
  stock = VALUES(stock),
  low_stock = VALUES(low_stock),
  version = VALUES(version);

-- 5) 商品属性（每个 SPU 两条）
INSERT INTO pms_product_attribute (product_id, name, input_type, value_list, deleted)
WITH RECURSIVE seq AS (
  SELECT 1 AS n
  UNION ALL
  SELECT n + 1 FROM seq WHERE n < 60
)
SELECT @product_base + n, '颜色', 0, '曜石黑,月光白,星际蓝,晨曦粉', 0 FROM seq
UNION ALL
SELECT @product_base + n, '版本', 0, '标准版,进阶版,旗舰版', 0 FROM seq;

-- 6) 评价（含头像图片），若存在会员则写入
SET @member_id := (SELECT id FROM ums_member ORDER BY id LIMIT 1);
SET @order_id_base := (SELECT IFNULL(MAX(id), 0) FROM oms_order);

INSERT INTO pms_comment (
  product_id, member_id, order_id, content, score, member_icon, member_nick,
  has_images, show_status, deleted
)
WITH RECURSIVE seq AS (
  SELECT 1 AS n
  UNION ALL
  SELECT n + 1 FROM seq WHERE n < 30
)
SELECT
  @product_base + n AS product_id,
  @member_id AS member_id,
  @order_id_base + 900000 + n AS order_id,
  CONCAT('这是一条测试评价 #', n, '，商品体验良好，性价比不错。') AS content,
  4 + (n % 2) AS score,
  CONCAT('https://picsum.photos/seed/mall-avatar-', n, '/120/120') AS member_icon,
  CONCAT('测试用户', LPAD(n, 2, '0')) AS member_nick,
  1 AS has_images,
  1 AS show_status,
  0 AS deleted
FROM seq
WHERE @member_id IS NOT NULL;

COMMIT;
SET FOREIGN_KEY_CHECKS = 1;

-- 完成后可执行：
-- USE mall_dev;
-- SOURCE D:/code/final/mall/mall-backend/docs/sql/seed_test_data_with_images.sql;
