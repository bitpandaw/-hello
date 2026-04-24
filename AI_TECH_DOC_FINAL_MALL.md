# final mall 项目技术文档（面向 AI）

## 1. 项目定位

- 项目名：`final mall`
- 形态：前后端分离的电商毕业设计项目（后台管理端 + C 端门户 + Java 后端）
- 仓库根目录：
  - `mall-backend`：Spring Boot 3 多模块后端
  - `mall-admin-web`：Vue3 + Element Plus 后台前端
  - `mall-portal`：Vue3 + Element Plus C 端前端

## 2. 技术栈总览

### 2.1 后端

- JDK `17`
- Spring Boot `3.2.5`
- Spring MVC + Validation + AOP
- Spring Security + JWT（Access/Refresh 双 token）
- MyBatis-Plus `3.5.5`
- MySQL 8
- Redis（购物车、refresh token、限流）
- RabbitMQ（订单超时关单：TTL + DLX）
- 定时任务 `@Scheduled`（MQ 兜底补偿）
- Knife4j/OpenAPI3（在线文档）
- MapStruct + Lombok + Hutool

### 2.2 前端

- Vue `3.5.x`
- Vite `8.x`
- Element Plus `2.13.x`
- Pinia + 持久化插件
- Axios + 拦截器自动刷新 token
- Admin 端额外使用 ECharts、富文本编辑器（wangeditor）

## 3. 代码结构（关键目录）

### 3.1 后端多模块结构（Maven）

- `mall-backend/pom.xml`（父工程，聚合模块）
  - `mall-common`：通用能力（统一响应、异常、AOP、限流、Redis 配置等）
  - `mall-mbg`：实体 + Mapper（含库存扣减 SQL）
  - `mall-security`：JWT 服务、认证过滤器、安全配置
  - `mall-admin`：启动模块 + 全部业务 API + MQ + 定时任务

### 3.2 后端主入口

- `mall-backend/mall-admin/src/main/java/com/mall/admin/MallApplication.java`
  - `@EnableRabbit`：启用 RabbitMQ
  - `@EnableScheduling`：启用定时任务
  - `@SpringBootApplication(scanBasePackages = "com.mall")`

### 3.3 前端项目结构

- `mall-admin-web/src`
  - `views/login`、`views/dashboard`、`views/pms`、`views/oms`、`views/ums`
  - `api/admin.js`
  - `stores/admin.js`
  - `utils/request.js`（后台接口请求封装）
- `mall-portal/src`
  - `views/home/category/search/product/cart/order/member/auth`
  - `api/pms.js`、`api/ums.js`、`api/oms.js`、`api/pay.js`、`api/sms.js`
  - `stores/user.js`、`stores/cart.js`
  - `utils/request.js`（门户接口请求封装）

## 4. 后端核心配置

### 4.1 应用配置

- `mall-backend/mall-admin/src/main/resources/application.yml`
  - 启动 profile：`dev`
  - 端口：`8080`
  - JWT：
    - `mall.jwt.secret`
    - `access-expire-ms: 1800000`（30 分钟）
    - `refresh-expire-ms: 604800000`（7 天）
  - 文件上传目录：`${user.home}/mall-uploads`
  - OpenAPI：
    - `/doc.html`
    - `/v3/api-docs`

### 4.2 开发环境基础设施

- `application-dev.yml`
  - MySQL：`mall_dev`
  - Redis：`127.0.0.1:6379`
  - RabbitMQ：`127.0.0.1:5672`

## 5. 安全模型与鉴权机制

### 5.1 路由权限规则

- `mall-security/.../SecurityConfig.java`
  - 放行：
    - 文档与静态资源：`/v3/**`、`/doc.html`、`/uploads/**`
    - 登录注册刷新：
      - C 端：`/api/ums/captcha|register|login|refresh`
      - 管理端：`/admin/auth/login|refresh`
    - 商品读接口：`GET /api/pms/**`
  - 需要角色：
    - `/admin/**` -> `ROLE_ADMIN`
    - `/api/**` -> `ROLE_MEMBER`（除开放读接口）

### 5.2 JWT 设计

- `mall-security/.../JwtService.java`
  - token 类型 claim：`typ`（`acc` / `ref`）
  - 账号类型 claim：`knd`（`M` 会员 / `A` 管理员）
  - Refresh token 含 `jti`，并落 Redis：
    - key 形态：`ref:{kind}:{id}:{jti}`
  - 刷新逻辑：验证 refresh token + 校验 Redis key 存在 + 旋转签发新对
  - 登出：删除对应 refresh key

### 5.3 认证过滤器

- `mall-security/.../JwtAuthFilter.java`
  - 对公共路径和 `GET /api/pms/**` 直接放行
  - 非公共接口读取 `Authorization: Bearer ...`
  - 校验 access token 后写入 `SecurityContext`
  - 根据路径前缀校验 token 中账号类型是否匹配（admin/member）

## 6. 统一响应与异常约定

### 6.1 统一响应体

- `mall-common/.../Result.java`
  - 固定字段：`code`、`message`、`data`、`time`
  - 成功码使用 `ResultCode.SUCCESS`（`200`）

### 6.2 错误码

- `mall-common/.../ResultCode.java`
  - 认证类：`401/403/1005`
  - 业务类：`1000`（通用）、`1001~1004`（验证码/登录/注册）
  - 订单类：`2001~2003`（库存/订单不存在/状态错误）

### 6.3 全局异常处理

- `mall-common/.../GlobalExceptionHandler.java`
  - `BusinessException` -> 业务错误响应
  - 参数校验异常 -> `VALIDATE_FAILED`
  - 未捕获异常 -> `FAILED` + `"系统繁忙"`

## 7. 业务域与主要 API

> 说明：以下按控制器聚类，便于 AI 做路由定位和变更影响分析。

### 7.1 用户与会员（UMS）

- `UmsController`（`/api/ums`）
  - 验证码、注册、登录、刷新
  - 会员信息 `me`
  - 收货地址 CRUD（列表/新增/删除）
- `AdminUmsController`（`/admin/ums/members`）
  - 会员状态管理

### 7.2 商品与分类品牌（PMS）

- `PmsPublicController`（`/api/pms`）
  - 类目树、商品列表/详情、搜索、品牌
- `AdminPmsController`（`/admin/pms`）
  - 商品、分类、品牌管理
- `PmsCommentController`（`/api/pms/comment`）
  - 评论列表与相关能力

### 7.3 购物车与订单（OMS）

- `OmsCartController`（`/api/oms/cart`）
  - 加入购物车、改数量、勾选、删除
- `OmsOrderController`（`/api/oms/orders`）
  - 预览、创建、分页、详情
- `AdminOmsController`（`/admin/oms/orders`）
  - 发货等后台订单动作

### 7.4 支付与优惠券（PAY/SMS）

- `PayController`（`/api/pay`）
  - 模拟支付：`POST /mock/{orderId}`
- `SmsCouponController`（`/api/sms/coupons`）
  - 领券动作

### 7.5 管理端辅助

- `AdminAuthController`（`/admin/auth`）登录与刷新
- `AdminMenuController`（`/admin/menus`）
- `AdminStatsController`（`/admin/stats/charts`）看板统计
- `FileUploadController`
  - `/api/ums/avatar`
  - `/admin/pms/file`
  - `/api/pms/comment/images`

## 8. 核心业务流程（重点）

### 8.1 下单主链路

1. 用户在 Redis 购物车勾选商品（`cart:{memberId}` hash）
2. 调用预览接口计算：
   - 小计
   - 优惠券折扣（`CouponCalc`）
   - 应付金额
3. 创建订单时：
   - 校验地址、券状态、商品状态
   - 生成订单主表 + 子项
   - 乐观锁扣减库存（最多重试 5 次）
   - 清理已下单购物车项
   - 标记优惠券已使用（若有）
   - 写入订单操作历史
   - 发送 TTL 延迟关单消息（30 分钟）

### 8.2 订单超时关单

- MQ 配置见 `OrderRabbitConfig`：
  - TTL 队列：`mall.order.ttl.hold`
  - 死信交换机：`mall.order.dlx`
  - 取消队列：`mall.order.cancel`
- 消费者 `OrderCancelListener`：
  - 接收订单 ID，执行 `cancelUnpaid(orderId)`
- 兜底补偿 `OrderCompensateTask`：
  - 每 2 分钟扫描创建超 32 分钟、仍待支付订单
  - 执行同一关单逻辑

## 9. 数据模型（领域划分）

数据库初始化 SQL：`mall-backend/mall.sql`

- UMS（用户权限）
  - `ums_member`、`ums_member_address`
  - `ums_admin`、`ums_role`、`ums_permission`
  - `ums_admin_role`、`ums_role_permission`
- PMS（商品）
  - `pms_product_category`、`pms_brand`
  - `pms_product`、`pms_sku`、`pms_sku_stock`
  - `pms_product_attribute`
  - `pms_comment`、`pms_comment_replay`
- OMS（订单）
  - `oms_order`、`oms_order_item`
  - `oms_order_operate_history`
  - `oms_cart_item`（当前核心购物车实现使用 Redis；该表可用于扩展/迁移）
- SMS（营销）
  - `sms_coupon`、`sms_coupon_history`、`sms_coupon_product_relation`
- PAY（支付）
  - `pay_record`

## 10. 前端交互模式

### 10.1 API 网关与代理

- 两个前端均支持：
  - `VITE_API_BASE_URL` 配置直连
  - 为空时使用 Vite 代理转发到 `http://localhost:8080`

### 10.2 token 自动续期

- `mall-admin-web/src/utils/request.js`
  - 401 时尝试 `/admin/auth/refresh`
  - 并发刷新队列防重入
- `mall-portal/src/utils/request.js`
  - 401 时尝试 `/api/ums/refresh`
  - 刷新失败跳转登录页，支持 redirect

## 11. 运行与开发指令

### 11.1 后端

- 在 `mall-backend`：
  - `mvn -pl mall-admin -am spring-boot:run`
- 启动前确保：
  - JDK17
  - MySQL/Redis/RabbitMQ 在线
  - 已导入 `mall-backend/mall.sql`

### 11.2 前端

- 后台前端：
  - `cd mall-admin-web && npm install && npm run dev`（默认 `5174`）
- 门户前端：
  - `cd mall-portal && npm install && npm run dev`（默认 Vite 端口）

## 12. AI 接手开发的高价值入口

### 12.1 最常改文件

- 后端：
  - `mall-admin/.../controller/*`
  - `mall-admin/.../service/*`
  - `mall-mbg/.../mapper/*`
  - `mall-admin/src/main/resources/application-*.yml`
- 前端：
  - `src/api/*`
  - `src/views/**`
  - `src/stores/*`
  - `src/utils/request.js`

### 12.2 新需求落地建议

- 新增业务接口建议沿用分层：
  - Controller（参数 + 返回）-> Service（事务 + 业务）-> Mapper（SQL）
- 统一返回 `Result`
- 业务错误抛 `BusinessException`
- 鉴权接口遵循 `/admin/**`（管理员）与 `/api/**`（会员）域隔离
- 涉及订单状态变更时同步：
  - 主表状态
  - 操作历史
  - 库存回滚或扣减
  - 优惠券使用状态

## 13. 已知实现细节与风险提示（AI 修改前必读）

- `OmsOrderService.cancelUnpaid()` 中存在早退逻辑：
  - 执行 `returnStockByOrder(orderId);` 后立即 `return;`
  - 导致后续“优惠券回退 + 操作历史记录”代码不可达
  - AI 在做订单/关单相关改造时需先确认这是否是有意行为
- Redis 购物车与数据库 `oms_cart_item` 并存：
  - 当前主路径使用 Redis
  - 若扩展“跨端同步购物车/持久化购物车”需设计一致性策略
- 支付当前为 `mock` 流程：
  - 生产化需对接真实支付网关、异步回调验签、幂等处理

## 14. AI 协作检查清单

- 修改接口前先定位控制器与 service 是否已有同类逻辑
- 涉及鉴权时同步检查：
  - `SecurityConfig`
  - `JwtAuthFilter`
  - 前端 request 拦截器
- 涉及状态机时检查：
  - 订单状态枚举与流转合法性
  - MQ 与定时补偿是否重复触发
- 交付前最少验证：
  - 关键接口 smoke test（登录、下单、支付、后台查询）
  - 编译通过（后端 Maven、前端 Vite build）

---

如需把本文件升级为“可执行的 AI 任务手册”，下一步可追加：

1. 全量 API 清单（参数/响应示例）
2. 订单状态机图（Mermaid）
3. 常见变更模板（新增接口、新增页面、新增表结构）
4. 回归测试脚本清单（Postman/Newman 或 curl）
