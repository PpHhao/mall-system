# Mall System 开发记录

## 项目背景
- 目标是基于 Spring Boot + Spring Cloud 的分布式商城系统，本仓库先搭建了用户与认证子系统，为后续商品、订单等业务模块打基础。
- 当前代码聚焦“账号体系 + 鉴权”的底座，实现登录态管理、角色/权限绑定和统一异常返回，方便同学继续迭代业务模块。

## 技术栈与约定
- Spring Boot 3.5、Spring Web、Validation
- Spring Security + JWT（自定义过滤器、无状态会话）
- MyBatis-Plus（MySQL 5.7.11），Lombok
- 统一响应模型 `Result{code,message,data}`，业务异常通过 `BusinessException` + `GlobalExceptionHandler` 处理
- 服务上下文路径：`/api`（见 `application.yml`）

## 当前已完成功能
- **认证与令牌**：注册、登录、刷新、注销、获取当前用户信息（`/auth/*`）。使用 JWT 生成访问/刷新令牌（带角色、权限、jti），并把 jti 持久化到 `user_tokens` 表以支持注销与过期校验。
- **安全鉴权**：SecurityFilterChain 开启方法级注解，除注册/登录/刷新外均需认证；自定义未认证/无权限返回；密码使用 BCrypt 存储；`JwtAuthenticationFilter` 负责解析 Bearer Token、校验 jti 是否被吊销并注入认证上下文。
- **用户管理**：  
  - 自助：更新个人资料（邮箱/手机号/昵称/头像/性别）、修改密码、查看自己信息。  
  - 管理：管理员可分页（当前为全量列表）查看用户列表与详情、为用户分配角色。  
  - 登录成功更新 `last_login_at`。创建用户会自动绑定默认角色 `USER`（需预置）。
- **数据模型（实体与 Mapper 已就绪）**：`users`、`roles`、`permissions`、`user_roles`、`role_permissions`、`user_tokens`。`RoleMapper/PermissionMapper` 提供按用户查角色编码与权限编码的查询。
- **配置**：`application-dev.yml`（本地默认 8080，数据库 `mall_system`）；`application-prod.yml`（示例云库）。JWT 相关参数在 `security.jwt.*`。

## 目录速览
```
src/main/java/com/szu/mallsystem
├─common            # 统一返回、错误码、异常处理
├─config            # 安全配置、JWT 配置
├─controller        # Auth、User 控制器
├─dto               # 接口入参对象（auth/user）
├─entity            # MyBatis-Plus 实体
├─mapper            # Mapper 接口及自定义查询
├─security          # JWT 生成/解析、过滤器、自定义 UserDetails
├─service           # 业务接口
└─service/impl      # 业务实现（Auth/User/Role/Permission）
resources
├─application.yml
├─application-dev.yml
└─application-prod.yml
```

## 启动与配置提示
1. 环境：JDK 17、Maven、MySQL 5.7.11。  
2. 数据库：创建库（默认 `mall_system`），按实体字段建表，并在 `roles` 中预置 `USER`（必需）与 `ADMIN` 等角色，在 `permissions` 中填充权限编码（可选）。  
3. 可先手动创建管理员账号并绑定 `ADMIN` 角色，以便调用管理端接口。  
4. 调整 `application-dev.yml` 的数据库账号密码后运行：  
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```  
5. 访问路径均以 `/api` 开头。

## API 快速索引
- `POST /api/auth/register` 注册并返回访问/刷新令牌
- `POST /api/auth/login` 登录并返回令牌
- `POST /api/auth/refresh` 用刷新令牌换取新访问令牌
- `POST /api/auth/logout` 注销（需要 Authorization Bearer 头）
- `GET /api/auth/me` 获取当前登录用户信息
- `GET /api/users` 管理员列出用户
- `GET /api/users/{id}` 管理员查看用户详情
- `PUT /api/users/me/profile` 更新个人资料
- `PUT /api/users/me/password` 修改密码
- `PUT /api/users/{id}/roles` 管理员为用户分配角色

## 后续待办建议
- 补充数据建表脚本与初始化数据（角色/权限/管理员）。
- 按任务书继续实现商品、分类、购物车、订单、支付、评论等业务模块，并为管理端提供 CRUD 接口。
- 接入 API 文档（Swagger/OpenAPI）与接口鉴权规则的权限码映射。
- 增强异常与审计：接口操作日志、登录失败告警、令牌黑名单清理任务等。



## Swagger API Documentation
- UI: `http://localhost:8080/api/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/api/v3/api-docs`
- OpenAPI YAML: `http://localhost:8080/api/v3/api-docs.yaml`
- Export JSON to file:
  ```bash
  curl http://localhost:8080/api/v3/api-docs -o docs/openapi.json
  ```
- Export YAML to file:
  ```bash
  curl http://localhost:8080/api/v3/api-docs.yaml -o docs/openapi.yaml
  ```

## 订单模块接口（Swagger 为准）
- 购物车：
  - `GET /api/cart`
  - `POST /api/cart/items`
  - `PUT /api/cart/items/{itemId}`
  - `DELETE /api/cart/items/{itemId}`
- 地址：
  - `GET /api/addresses`
  - `POST /api/addresses`
  - `PUT /api/addresses/{addressId}`
  - `DELETE /api/addresses/{addressId}`
- 订单（用户端）：
  - `POST /api/orders`
  - `GET /api/orders` (status/page/pageSize)
  - `GET /api/orders/{orderId}`
  - `PUT /api/orders/{orderId}/pay`
  - `PUT /api/orders/{orderId}/cancel`
  - `PUT /api/orders/{orderId}/complete`
- 订单（管理端）：
  - `GET /api/admin/orders` (userId/status/page/pageSize)
  - `PUT /api/admin/orders/{orderId}/ship`
  - `GET /api/admin/orders/stats`

## API 自测脚本
1. 启动服务：
   ```bash
   .\mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```
2. 运行脚本（默认使用 admin/Admin@123）：
   ```powershell
   .\scripts\api_smoke_test.ps1
   ```
3. 可选环境变量：
   - `BASE_URL` (default `http://localhost:8080/api`)
   - `API_USER` / `API_PASS`
   - `PRODUCT_ID` (default `1`)
