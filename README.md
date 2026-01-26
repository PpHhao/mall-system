# Mall System 项目说明

## 项目定位
- 单体 Spring Boot 服务（不拆分微服务），覆盖商城基础业务，统一鉴权。
- 统一返回结构 `Result{code,message,data}`，业务异常通过 `BusinessException` + `GlobalExceptionHandler` 处理。

## 技术栈
- Spring Boot 3.5 / Spring Web / Validation
- Spring Security + JWT（自定义过滤器、无状态会话）
- MyBatis-Plus（MySQL 8+），Lombok
- Springdoc OpenAPI（`/api/swagger-ui/index.html`），Redisson 预留（未强制使用）

## 鉴权策略
- 除登录、注册、刷新令牌外，其余接口均需携带 `Authorization: Bearer <token>`。
- 方法级角色控制：管理端接口使用 `@PreAuthorize("hasRole('ADMIN')")`。

## 已完成功能
- 认证：注册 / 登录 / 刷新 / 注销 / 获取当前用户。
- 用户：个人资料修改、密码修改；管理员查看用户、分配角色。
- 角色/权限：角色、权限 CRUD，角色绑定权限。
- 商品与分类：分类树、商品搜索/详情开放；商品、分类的新增/修改/删除需管理员。
- 购物车与地址：购物车增删改查；地址增删改查+默认地址处理。
- 订单：支持直接下单或基于购物车下单（未传 items 且 useCart=true 自动取购物车）；创建、查询、支付、取消、确认收货；管理员发货、统计。
- 支付：模拟支付创建/回调、查询、导出、统计。
- 退款：用户申请退款；管理员审批/处理（含失败回滚）。
- 评价：发布/查询评价，点赞/取消，举报，管理员回复。

## 快速启动
1. 准备环境：JDK 17、Maven、MySQL 8+；创建数据库并导入 `docs/init-data.sql`（包含基础角色/权限/账号）。
2. 配置数据库连接：`src/main/resources/application-dev.yml`。
3. 启动服务：`mvn spring-boot:run -Dspring-boot.run.profiles=dev`，上下文路径 `/api`。
4. API 文档：`http://localhost:8080/api/swagger-ui/index.html`。
5. Postman 集合（包含所有测试样例docs/postman_tests.md内）：<https://interstellar-rocket-6151851.postman.co/workspace/hao-peng's-Workspace~5dc8bbc3-987a-4d4f-81b2-df244590fce6/collection/51082774-873b7516-bd60-4dc9-b812-679892a5d1b9?action=share&source=copy-link&creator=51082774> 同时也在docs/Mall System.postman_collection.json和docs/Mall-Dev.postman_environment里提供。
