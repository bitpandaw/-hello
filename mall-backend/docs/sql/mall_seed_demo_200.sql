-- 演示：200 个样例 SPU，并生成 1:1 的 SKU + 库存
-- 前置：已建库并执行 mall.sql；存在品牌 id=1、末级分类 id=3
-- 要求：MySQL 8+（递归 CTE）。若报 recursion 错误，先执行：SET GLOBAL cte_max_recursion_depth = 1000;
-- 使用：整段执行本文件（不要只选 DELETE 单句）。Workbench：Query → Execute（全选后执行）
-- 或命令行：mysql -u... mall_dev < mall_seed_demo_200.sql

USE mall_dev;
SET NAMES utf8mb4;

-- 可重复跑：只删本脚本生成的行
-- MySQL Workbench 开启 “Safe Updates” 时，凡带子查询的 DELETE 也常报 1175，需在本会话先关闭安全更新
SET SQL_SAFE_UPDATES = 0;

DELETE FROM pms_sku_stock
WHERE sku_id IN (SELECT id FROM pms_sku WHERE sku_code LIKE 'DEMOSKU%');
DELETE FROM pms_sku
WHERE id IN (SELECT id FROM (SELECT id FROM pms_sku WHERE sku_code LIKE 'DEMOSKU%') t);
DELETE FROM pms_product
WHERE id IN (SELECT id FROM (SELECT id FROM pms_product WHERE `name` LIKE '样例SPU%') t);

SET SESSION cte_max_recursion_depth = 1000;

INSERT INTO pms_product (
  `name`, sub_title, brand_id, category_id, min_price, original_price,
  publish_status, verify_status, deleted
)
WITH RECURSIVE seq AS (
  SELECT 1 AS n
  UNION ALL
  SELECT n + 1 FROM seq WHERE n < 200
)
SELECT
  CONCAT('样例SPU', LPAD(n, 3, '0')),
  CONCAT('演示用副标题#', n),
  1,
  3,
  ROUND( 29.90 + (n * 37) % 9980 / 10.0, 2),
  ROUND( 39.90 + (n * 41) % 9980 / 10.0, 2),
  1,
  1,
  0
FROM seq;

INSERT INTO pms_sku (spu_id, sku_code, spec_json, price, deleted)
SELECT
  p.id,
  CONCAT('DEMOSKU', p.id),
  CONCAT('{"i":', p.id, ',"c":"样例色"}'),
  p.min_price,
  0
FROM pms_product p
WHERE p.`name` LIKE '样例SPU%';

INSERT INTO pms_sku_stock (sku_id, stock, low_stock, version)
SELECT
  s.id,
  30 + (s.id % 800),
  3,
  0
FROM pms_sku s
WHERE s.sku_code LIKE 'DEMOSKU%';

SET SQL_SAFE_UPDATES = 1;
