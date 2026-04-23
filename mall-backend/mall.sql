-- Mall 毕设 - MySQL 8.0
-- 文件编码: UTF-8（请用 UTF-8 无 BOM 保存，避免乱码）
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS mall_dev DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mall_dev;

-- ========== ums 用户与权限 ==========
CREATE TABLE ums_member (
  id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username      VARCHAR(64)   NOT NULL COMMENT '登录名',
  password      VARCHAR(128)  NOT NULL,
  phone         VARCHAR(20)   DEFAULT NULL,
  email         VARCHAR(64)   DEFAULT NULL,
  avatar        VARCHAR(512)  DEFAULT NULL,
  status        TINYINT       NOT NULL DEFAULT 1 COMMENT '1=正常 0=禁用',
  create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT       NOT NULL DEFAULT 0,
  UNIQUE KEY uk_member_username (username),
  UNIQUE KEY uk_member_phone (phone),
  UNIQUE KEY uk_member_email (email)
) COMMENT='C端用户';

CREATE TABLE ums_member_address (
  id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  member_id     BIGINT        NOT NULL,
  name          VARCHAR(32)   NOT NULL,
  phone         VARCHAR(20)   NOT NULL,
  province      VARCHAR(32)   NOT NULL,
  city          VARCHAR(32)   NOT NULL,
  district      VARCHAR(32)   NOT NULL,
  detail        VARCHAR(256)  NOT NULL,
  is_default    TINYINT       NOT NULL DEFAULT 0,
  create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT       NOT NULL DEFAULT 0,
  KEY idx_member (member_id)
) COMMENT='收货地址';

CREATE TABLE ums_admin (
  id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username      VARCHAR(64)   NOT NULL,
  password      VARCHAR(128)  NOT NULL,
  status        TINYINT       NOT NULL DEFAULT 1,
  create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT       NOT NULL DEFAULT 0,
  UNIQUE KEY uk_admin_username (username)
) COMMENT='后台管理员';

CREATE TABLE ums_role (
  id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(64)   NOT NULL,
  code          VARCHAR(64)   NOT NULL,
  create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT       NOT NULL DEFAULT 0,
  UNIQUE KEY uk_role_code (code)
) COMMENT='角色';

CREATE TABLE ums_permission (
  id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(64)   NOT NULL,
  code          VARCHAR(64)   NOT NULL COMMENT '权限码 RBAC',
  type          TINYINT       NOT NULL DEFAULT 0 COMMENT '0=菜单 1=按钮 2=API',
  path          VARCHAR(128)  DEFAULT NULL,
  create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT       NOT NULL DEFAULT 0,
  UNIQUE KEY uk_perm_code (code)
) COMMENT='权限';

CREATE TABLE ums_admin_role (
  admin_id  BIGINT NOT NULL,
  role_id   BIGINT NOT NULL,
  PRIMARY KEY (admin_id, role_id)
) COMMENT='管理员-角色';

CREATE TABLE ums_role_permission (
  role_id       BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  PRIMARY KEY (role_id, permission_id)
) COMMENT='角色-权限';

-- ========== pms 商品 ==========
CREATE TABLE pms_product_category (
  id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  parent_id     BIGINT        NOT NULL DEFAULT 0,
  name          VARCHAR(64)   NOT NULL,
  level         TINYINT       NOT NULL DEFAULT 1,
  sort          INT           NOT NULL DEFAULT 0,
  create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT       NOT NULL DEFAULT 0,
  KEY idx_parent (parent_id)
) COMMENT='商品分类(三级)';

CREATE TABLE pms_brand (
  id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(64)   NOT NULL,
  logo          VARCHAR(512)  DEFAULT NULL,
  create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT       NOT NULL DEFAULT 0
) COMMENT='品牌';

-- SPU
CREATE TABLE pms_product (
  id              BIGINT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name            VARCHAR(128)   NOT NULL,
  sub_title       VARCHAR(256)   DEFAULT NULL,
  brand_id        BIGINT         DEFAULT NULL,
  category_id     BIGINT         NOT NULL,
  cover_img       VARCHAR(512)   DEFAULT NULL,
  min_price       DECIMAL(10,2)  NOT NULL DEFAULT 0,
  original_price  DECIMAL(10,2)  DEFAULT NULL,
  detail_html     LONGTEXT,
  publish_status  TINYINT        NOT NULL DEFAULT 0 COMMENT '0=下架 1=上架',
  verify_status   TINYINT        NOT NULL DEFAULT 0 COMMENT '0=待审 1=通过 2=拒绝',
  create_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted         TINYINT        NOT NULL DEFAULT 0,
  KEY idx_brand (brand_id),
  KEY idx_cat (category_id)
) COMMENT='商品 SPU';

CREATE TABLE pms_sku (
  id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  spu_id        BIGINT        NOT NULL,
  sku_code      VARCHAR(64)   NOT NULL,
  spec_json     VARCHAR(512)  NOT NULL DEFAULT '{}' COMMENT '规格 JSON',
  price         DECIMAL(10,2) NOT NULL,
  create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT       NOT NULL DEFAULT 0,
  UNIQUE KEY uk_sku_code (sku_code),
  KEY idx_spu (spu_id)
) COMMENT='商品 SKU';

CREATE TABLE pms_sku_stock (
  id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  sku_id        BIGINT        NOT NULL,
  stock         INT           NOT NULL DEFAULT 0,
  low_stock     INT           NOT NULL DEFAULT 0,
  version       INT           NOT NULL DEFAULT 0 COMMENT '乐观锁',
  create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_sku (sku_id)
) COMMENT='SKU 库存';

CREATE TABLE pms_product_attribute (
  id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  product_id    BIGINT        NOT NULL,
  name          VARCHAR(32)   NOT NULL,
  input_type    TINYINT       NOT NULL DEFAULT 0 COMMENT '0=选择 1=手输',
  value_list    VARCHAR(512)  DEFAULT NULL,
  create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT       NOT NULL DEFAULT 0,
  KEY idx_prod (product_id)
) COMMENT='SPU 销售属性';

-- ========== oms 订单 ==========
CREATE TABLE oms_order (
  id            BIGINT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
  order_no      VARCHAR(32)    NOT NULL,
  member_id     BIGINT         NOT NULL,
  total_amount  DECIMAL(12,2)  NOT NULL,
  pay_amount    DECIMAL(12,2)  NOT NULL,
  discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  freight       DECIMAL(10,2)  NOT NULL DEFAULT 0,
  status        TINYINT        NOT NULL COMMENT '订单状态 0-5',
  pay_type      TINYINT        DEFAULT 0,
  note          VARCHAR(500)   DEFAULT NULL,
  coupon_id     BIGINT         DEFAULT NULL,
  delivery_company VARCHAR(64)  DEFAULT NULL,
  delivery_sn   VARCHAR(64)   DEFAULT NULL,
  receiver_name VARCHAR(32)   NOT NULL,
  receiver_phone VARCHAR(20)  NOT NULL,
  full_address  VARCHAR(500)  NOT NULL,
  create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  pay_time      DATETIME      DEFAULT NULL,
  delivery_time DATETIME     DEFAULT NULL,
  receive_time  DATETIME     DEFAULT NULL,
  finish_time   DATETIME     DEFAULT NULL,
  cancel_time   DATETIME     DEFAULT NULL,
  update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT        NOT NULL DEFAULT 0,
  UNIQUE KEY uk_order_no (order_no),
  KEY idx_mem_status (member_id, status, create_time)
) COMMENT='订单';

CREATE TABLE oms_order_item (
  id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  order_id      BIGINT        NOT NULL,
  sku_id        BIGINT        NOT NULL,
  spu_id        BIGINT        NOT NULL,
  spu_name      VARCHAR(256)  NOT NULL,
  pic           VARCHAR(512)  DEFAULT NULL,
  sku_code      VARCHAR(64)   NOT NULL,
  spec_json     VARCHAR(512)  DEFAULT NULL,
  price         DECIMAL(10,2) NOT NULL,
  quantity      INT            NOT NULL,
  total_price   DECIMAL(12,2)  NOT NULL,
  KEY idx_order (order_id)
) COMMENT='订单行';

CREATE TABLE oms_order_operate_history (
  id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  order_id      BIGINT        NOT NULL,
  operator      VARCHAR(64)  NOT NULL,
  note          VARCHAR(512)  DEFAULT NULL,
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='订单操作历史';

CREATE TABLE oms_cart_item (
  id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  member_id     BIGINT        NOT NULL,
  spu_id        BIGINT        NOT NULL,
  sku_id        BIGINT        NOT NULL,
  quantity      INT            NOT NULL DEFAULT 1,
  create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT       NOT NULL DEFAULT 0,
  KEY idx_mem (member_id)
) COMMENT='DB 购物车(演示/可选)';

-- ========== sms 营销券 ==========
CREATE TABLE sms_coupon (
  id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(128)  NOT NULL,
  type          TINYINT       NOT NULL COMMENT '1=满减 2=折扣',
  amount        DECIMAL(10,2) DEFAULT NULL COMMENT '面额/减免金额',
  discount      DECIMAL(5,2)  DEFAULT NULL COMMENT '折扣 0-1',
  min_point     DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '使用门槛(满额)',
  start_time    DATETIME      NOT NULL,
  end_time      DATETIME      NOT NULL,
  per_limit     INT            NOT NULL DEFAULT 1,
  use_type      TINYINT        NOT NULL DEFAULT 0 COMMENT '0=全场 1=部分商品',
  create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT        NOT NULL DEFAULT 0
) COMMENT='优惠券';

CREATE TABLE sms_coupon_history (
  id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  coupon_id     BIGINT        NOT NULL,
  member_id     BIGINT        NOT NULL,
  use_status    TINYINT       NOT NULL DEFAULT 0 COMMENT '0=未用 1=已用 2=过期',
  get_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  order_id      BIGINT         DEFAULT NULL,
  KEY idx_mem_cou (member_id, coupon_id)
) COMMENT='领券记录';

CREATE TABLE sms_coupon_product_relation (
  id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  coupon_id     BIGINT        NOT NULL,
  spu_id        BIGINT        NOT NULL
) COMMENT='券适用 SPU';

-- ========== 评价与支付 ==========
CREATE TABLE pms_comment (
  id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  product_id    BIGINT        NOT NULL,
  member_id     BIGINT        NOT NULL,
  order_id      BIGINT        NOT NULL,
  content       TEXT          NOT NULL,
  score         TINYINT       NOT NULL,
  member_icon   VARCHAR(512)  DEFAULT NULL,
  member_nick   VARCHAR(64)  DEFAULT NULL,
  has_images    TINYINT       NOT NULL DEFAULT 0,
  show_status   TINYINT       NOT NULL DEFAULT 0,
  create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted       TINYINT       NOT NULL DEFAULT 0,
  UNIQUE KEY uk_order_comment (order_id)
) COMMENT='商品评价';

CREATE TABLE pms_comment_replay (
  id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  comment_id    BIGINT        NOT NULL,
  content       TEXT          NOT NULL,
  type          TINYINT       NOT NULL DEFAULT 0 COMMENT '0=官方 1=用户',
  create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted       TINYINT       NOT NULL DEFAULT 0,
  KEY idx_cmt (comment_id)
) COMMENT='评价回复';

CREATE TABLE pay_record (
  id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  order_id      BIGINT        NOT NULL,
  out_trade_no  VARCHAR(64)  NOT NULL,
  pay_amount    DECIMAL(12,2) NOT NULL,
  pay_status    TINYINT        NOT NULL COMMENT '0=待付 1=成功 2=失败',
  create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_order (order_id)
) COMMENT='支付流水';

-- Quartz 等由 Spring 创建时可省略此处 DDL

-- =============== 示例数据 ===============
-- 管理员测试账号的 BCrypt(123456) 在应用 InitData 首次启动时写入，见 mall-admin
INSERT INTO ums_role (id, name, code) VALUES
(1, '超级管理', 'ROOT');
INSERT INTO ums_permission (id, name, code, type) VALUES
(1, '商品管理', 'product:manage', 2),
(2, '订单管理', 'order:manage', 2),
(3, '用户管理', 'user:manage', 2);

INSERT INTO pms_brand (id, name) VALUES (1, 'Mall 品牌');
INSERT INTO pms_product_category (id, parent_id, name, level, sort) VALUES
(1, 0, '数码', 1, 1),
(2, 1, '手机', 2, 1),
(3, 2, '智能手机', 3, 1);
INSERT INTO pms_product (id, name, sub_title, brand_id, category_id, min_price, publish_status, verify_status) VALUES
(1, '示例手机A', '性价比', 1, 3, 1999.00, 1, 1);
INSERT INTO pms_sku (id, spu_id, sku_code, spec_json, price) VALUES
(1, 1, 'SKU001', '{"color":"黑色","rom":"256G"}', 1999.00);
INSERT INTO pms_sku_stock (id, sku_id, stock, low_stock, version) VALUES
(1, 1, 100, 5, 0);
INSERT INTO pms_product_attribute (id, product_id, name, input_type, value_list) VALUES
(1, 1, '颜色', 0, '黑,白'),
(2, 1, '内存', 0, '128G,256G');

INSERT INTO sms_coupon (id, name, type, amount, discount, min_point, start_time, end_time, per_limit, use_type) VALUES
(1, '新用户满100减20', 1, 20.00, NULL, 100, NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 30 DAY, 1, 0);
INSERT INTO sms_coupon (id, name, type, amount, discount, min_point, start_time, end_time, per_limit, use_type) VALUES
(2, '9折券', 2, NULL, 0.9, 50, NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 30 DAY, 1, 0);

SET FOREIGN_KEY_CHECKS = 1;
