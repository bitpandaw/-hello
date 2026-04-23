# Mall 电商毕设后端

基于 **Spring Boot 3.2** 的 Maven 多模块项目：`mall-common`、`mall-mbg`（MyBatis-Plus 实体/Mapper）、`mall-security`（Spring Security + JWT 双 Token）、`mall-admin`（业务与启动）。

## 技术栈

- Spring Boot 3.2 + Spring MVC + Validation + AOP
- MyBatis-Plus 3.5、MySQL 8、Redis、RabbitMQ
- Spring Security + JWT（Access/Refresh，Refresh 存 Redis 可登出）
- Knife4j（OpenAPI3）
- Hutool（图形验证码等）、Lombok
- 定时任务：`@Scheduled` 订单关单补偿（对 MQ 的兜底；论文中可对比 Quartz）

## 系统结构（简要）

- **C 端 API**：`/api/ums` 用户/地址；`/api/pms` 商品只读；`/api/oms` 购物车（Redis Hash）与订单；`/api/pay` 模拟支付与流水；`/api/sms/coupons` 优惠券；`/api/pms/comment` 评价
- **后台 API**：`/admin/auth` 登录与刷新；`/admin/stats` 统计看板
- **订单关单**：创建订单后向 Rabbit **TTL+死信** 队列发送消息（`mall.order.ttl.hold` → 到期进入 `mall.order.dlx` → `mall.order.cancel` 消费关单+还库存）；另设 **2 分钟** 定时任务扫描超 32 分钟仍待付订单作补偿

## 环境要求

- **JDK 17+**（Spring Boot 3 必需；若本机 `mvn -version` 为 Java 11，请设置 `JAVA_HOME` 为 JDK 17+ 后再执行 Maven）
- MySQL 8、Redis、RabbitMQ 已启动，库名与账号见 `application-dev.yml`

## 初始化数据库

```bash
mysql -u root -p < mall.sql
```

或执行 `docs/sql/mall.sql`（与根目录 `mall.sql` 相同）。首次启动会插入默认 **管理员 `admin` / 123456** 与 **用户 `user01` / 123456**（若不存在则插入）。

## 运行

```bash
cd d:\code\final
# Windows PowerShell 示例：指定 JDK 17
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"
mvn -pl mall-admin -am spring-boot:run
```

- 接口文档：http://localhost:8080/doc.html  
- OpenAPI：http://localhost:8080/v3/api-docs  

## 主要配置

- `mall-admin/src/main/resources/application.yml`：JWT、上传目录等
- `application-dev.yml` / `application-prod.yml`：数据源、Redis、RabbitMQ 分环境

## 说明与扩展

- **支付宝/微信**：`PayController` 为模拟成功；生产应对接官方 SDK，异步通知 URL 验签、幂等、与订单状态机配合（见代码注释与论文「支付安全」节）。
- **ES 搜索**：`PmsProductMapper#searchByKeyword` 为 MySQL `LIKE`；可抽象 `ProductSearchService` 接口，增加 `EsProductSearchServiceImpl` 与索引同步任务。
- **OSS 对象存储**：可新增 `FileStorage` 接口，当前默认本地目录 `mall.file.upload-dir`；实现 `OssFileStorage` 后通过配置切换。
- **MyBatis 代码生成**：`mall-mbg` 中实体与 Mapper 已手写可运行；需要批量生成时可在本模块增加 MyBatis-Plus Generator 的 `CodeGenerator` 主类，输出包名 `com.mall.mbg`。

## 模块说明

| 模块 | 说明 |
|------|------|
| mall-common | 统一 `Result`、业务异常、AOP 接口日志、`@RateLimit`（Redis+Lua INCR+EXPIRE）、脱敏注解、Redis 基础配置 |
| mall-mbg | 实体、Mapper、乐观锁减库存 SQL |
| mall-security | `JwtService`、JWT 过滤器、`SecurityFilterChain` |
| mall-admin | 业务、Rabbit 配置、定时补偿、全局异常与跨域（如 `CorsConfig`） |
