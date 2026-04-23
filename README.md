# 全栈电商（毕设）— `mall/`

在 `d:\code\final\mall` 下 monorepo：**Spring Boot 多模块后端** + **用户端 mall-portal** + **管理端 mall-admin-web**。

## 目录

| 路径 | 说明 |
|------|------|
| `mall-backend/` | Maven 父 POM + `mall-common` / `mall-mbg` / `mall-security` / `mall-admin` 可运行应用 |
| `mall-portal/` | Vue3 + Vite + Element Plus + Pinia（C 端） |
| `mall-admin-web/` | Vue3 + Vite + Element Plus + ECharts + WangEditor（管理端） |
| `mall-backend/docs/sql/` | `mall.sql` 全量 + `mall_patch_v2.sql` 增量（RBAC 菜单、订单券、评价图等） |

## 环境

- **JDK 17+**、**Maven 3.8+**
- **MySQL 8**、**Redis**（验证码、购物车、限流等依赖）
- **RabbitMQ**（若你启用了消息相关配置）
- **Node.js 20+**（两前端）

## 数据库

1. 建库后执行 `mall-backend/docs/sql/mall.sql`。
2. 再执行 **`mall-backend/docs/sql/mall_patch_v2.sql`**（菜单 component/path、订单 `coupon_history_id`、评价 `image_urls` 等）。

## 启动后端

```bash
cd mall-backend
# 使用 JDK 17+：设置 JAVA_HOME 后
mvn -DskipTests -pl mall-admin -am package
mvn -pl mall-admin -am spring-boot:run
```

- 默认端口 **8080**（与下方前端代理一致）。
- API 文档：Knife4j / SpringDoc 按你项目里实际路径（常见为 `/v3/api-docs` 或 `doc.html`）。

## 统一响应码

`mall-common` 中成功码为 **200**（`ResultCode.SUCCESS`），前端 `request` 拦截器按 `code === 200` 处理。

## 用户端 `mall-portal`

```bash
cd mall-portal
npm install
npm run dev
```

- 开发默认 **<http://localhost:5173>**，通过 Vite 将 `/api`、`/admin`、`/v3`、`/uploads` 代理到 `http://localhost:8080`。
- 环境变量见 `mall-portal/.env.development`、`.env.production`（`VITE_API_BASE_URL` 可为空，走代理即可）。

## 管理端 `mall-admin-web`

```bash
cd mall-admin-web
npm install
npm run dev
```

- 开发默认 **<http://localhost:5174>**，代理 `/api`、`/admin`、`/v3`、`/uploads` 到 `http://localhost:8080`。
- 使用 **管理员账号** 登录 `POST /admin/auth/login` 获取 `access` / `refresh`；侧栏菜单来自 **`GET /admin/menus`**（RBAC），动态 `addRoute`；控制台图表数据来自 `GET /admin/stats` 与 `GET /admin/stats/charts`。
- 管理端 `VITE_API_BASE_URL`：见 `mall-admin-web/.env.*`。

## 文件上传

- 管理端图片：`POST /admin/pms/file`（`multipart` 字段 `file`），返回可写进 `pms_product.cover_img` 等字段的 **相对路径**（以 `/uploads/...` 形式访问）。
- 本地静态资源映射由后端 `WebMvcFileConfig` 提供 `/uploads/**`。

## 说明与增量 SQL

- 主要增量在 **`docs/sql/mall_patch_v2.sql`**；论文中可写：预订单/试算、用券、统计图、评论图、管理端菜单与 RBAC 等。
- 更详细的后端说明见 `mall-backend/README-BACKEND.md`（若存在）。

## 工程化

两前端均提供 **ESLint + Prettier** 配置，脚本：

- `npm run lint`（在各自目录执行，见各 `package.json`）。
