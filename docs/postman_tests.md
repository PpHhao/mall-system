# Postman测试说明

## 1. 环境变量（Postman 推荐设置）
- `baseUrl` = `http://localhost:8080/api`
- `adminUser` = `admin`
- `adminPass` = `Admin@123`
- `demoUser` = `demo`
- `demoPass` = `Demo@123`

## 2. 初始化数据库（空表）
在 MySQL 里执行以下 SQL，基于 `docs/schema.sql` 创建好的空表插入最小可跑通全流程的基础数据（角色/权限/账号/分类/商品/地址）。密码使用 BCrypt。
```sql
-- 角色
INSERT INTO roles (code, name, remark, created_at, updated_at) VALUES
('ADMIN', '管理员', '系统管理员', NOW(), NOW()),
('USER', '普通用户', '默认角色', NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), remark=VALUES(remark), updated_at=NOW();

-- 权限（核心功能覆盖，可按需扩展）
INSERT INTO permissions (code, name, type, http_method, http_path, created_at, updated_at) VALUES
('auth:me','查看当前用户',1,'GET','/auth/me',NOW(),NOW()),
('user:list','用户列表',1,'GET','/users',NOW(),NOW()),
('user:detail','用户详情',1,'GET','/users/{id}',NOW(),NOW()),
('user:roles:update','分配用户角色',1,'PUT','/users/{id}/roles',NOW(),NOW()),
('user:profile:update','修改个人资料',1,'PUT','/users/me/profile',NOW(),NOW()),
('user:password:change','修改密码',1,'PUT','/users/me/password',NOW(),NOW()),
('role:crud','角色管理',1,'*','/roles*',NOW(),NOW()),
('permission:crud','权限管理',1,'*','/permissions*',NOW(),NOW()),
('product:crud','商品管理',1,'*','/products*',NOW(),NOW()),
('category:crud','分类管理',1,'*','/categories*',NOW(),NOW()),
('order:admin','订单管理',1,'*','/admin/orders*',NOW(),NOW()),
('payment:admin','支付管理',1,'*','/payments*',NOW(),NOW()),
('refund:admin','退款管理',1,'*','/refunds*',NOW(),NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), type=VALUES(type), http_method=VALUES(http_method), http_path=VALUES(http_path), updated_at=NOW();

-- 账号（BCrypt 密码，admin/Admin@123，demo/Demo@123）
INSERT INTO users (username,password_hash,email,phone,nickname,gender,status,deleted,created_at,updated_at) VALUES
('admin','$2b$12$8TRpJidJNZsDI3qcUHwSIOdoSgQtK8/FRmIQsPDU0.AC93hx36STS','admin@example.com',NULL,'管理员',0,1,0,NOW(),NOW()),
('demo','$2b$12$dBN2JOIa1ZsaLstac.hb2uu3dVcqFL5WoTPR6XeE2iG3kA9pSX3mG','demo@example.com',NULL,'演示用户',0,1,0,NOW(),NOW())
ON DUPLICATE KEY UPDATE password_hash=VALUES(password_hash), status=VALUES(status), nickname=VALUES(nickname), updated_at=NOW();

-- 用户角色
INSERT INTO user_roles (user_id, role_id, created_at)
SELECT u.id, r.id, NOW() FROM users u JOIN roles r ON r.code='ADMIN' WHERE u.username='admin'
  AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id=u.id AND ur.role_id=r.id);
INSERT INTO user_roles (user_id, role_id, created_at)
SELECT u.id, r.id, NOW() FROM users u JOIN roles r ON r.code='USER' WHERE u.username='demo'
  AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id=u.id AND ur.role_id=r.id);

-- 角色权限绑定：ADMIN 拥有全部，USER 拥有基础自助
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW() FROM roles r JOIN permissions p ON 1=1 WHERE r.code='ADMIN'
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW() FROM roles r JOIN permissions p ON p.code IN ('auth:me','user:profile:update','user:password:change')
WHERE r.code='USER'
  AND NOT EXISTS (SELECT 1 FROM role_permissions rp WHERE rp.role_id=r.id AND rp.permission_id=p.id);

-- 初始分类与商品（便于下单/评价链路）
INSERT INTO categories (parent_id, name) VALUES (0, '数码') ON DUPLICATE KEY UPDATE name=VALUES(name);
SET @cat_root := (SELECT id FROM categories WHERE name='数码' ORDER BY id LIMIT 1);
INSERT INTO categories (parent_id, name) VALUES (@cat_root, '手机') ON DUPLICATE KEY UPDATE name=VALUES(name);
SET @cat_phone := (SELECT id FROM categories WHERE name='手机' ORDER BY id LIMIT 1);

INSERT INTO products (category_id, name, subtitle, description, price, stock, status, created_at, updated_at)
VALUES (@cat_phone, 'iPhone 15', 'A17', '旗舰机', 6999.00, 100, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE subtitle=VALUES(subtitle), description=VALUES(description), price=VALUES(price), stock=VALUES(stock), status=VALUES(status), updated_at=NOW();
SET @prod_id := (SELECT id FROM products WHERE name='iPhone 15' ORDER BY id LIMIT 1);

INSERT INTO product_images (product_id, url, sort) VALUES
(@prod_id, 'https://img.example.com/iphone15.jpg', 0)
ON DUPLICATE KEY UPDATE url=VALUES(url), sort=VALUES(sort);

-- demo 用户默认地址（便于下单）
SET @demo_id := (SELECT id FROM users WHERE username='demo');
INSERT INTO addresses (user_id, receiver_name, receiver_phone, province, city, district, detail, postal_code, is_default)
VALUES (@demo_id, '张三', '13000000000', '广东', '深圳', '南山', '科苑路', '518000', 1)
ON DUPLICATE KEY UPDATE receiver_phone=VALUES(receiver_phone), detail=VALUES(detail), is_default=VALUES(is_default);
```

## 3. 全量测试用例（覆盖所有核心功能）
以下所有接口除注册/登录/刷新外均需 `Authorization: Bearer <token>`。

### 3.1 认证/用户
1) 登录（管理员）  
`POST {{baseUrl}}/auth/login` → 保存 `adminToken`
```json
{ "username": "{{adminUser}}", "password": "{{adminPass}}" }
```
2) 刷新令牌  
`POST {{baseUrl}}/auth/refresh` 使用 `refreshToken`
3) 注册新用户  
`POST {{baseUrl}}/auth/register` （无须 token）
```json
{ "username":"u1","password":"Pass@123","email":"u1@example.com","phone":"13000000000","nickname":"U1" }
```
4) 登录新用户 → `userToken`  
5) 当前用户信息  
`GET {{baseUrl}}/auth/me`
6) 修改个人资料  
`PUT {{baseUrl}}/users/me/profile`
```json
{ "email":"u1_new@example.com","nickname":"U1-new","avatarUrl":"http://img/u1.png","gender":1 }
```
7) 修改密码  
`PUT {{baseUrl}}/users/me/password`
```json
{ "oldPassword":"Pass@123","newPassword":"Pass@456" }
```
8) 登出  
`POST {{baseUrl}}/auth/logout`
9) 管理员：用户列表、详情  
`GET {{baseUrl}}/users` / `GET {{baseUrl}}/users/{id}`
10) 管理员：分配角色  
`PUT {{baseUrl}}/users/{userId}/roles` → `{ "roleCodes":["USER"] }`

### 3.2 角色 / 权限 CRUD（管理员）
- 列表角色：`GET /roles`
- 创建角色：`POST /roles` → `{ "code":"OPS","name":"运维","remark":"运维角色" }`
- 更新角色：`PUT /roles/{roleId}` → `{ "name":"运维-更新" }`
- 绑定权限：`PUT /roles/{roleId}/permissions` → `{ "permissionCodes":["auth:me","user:list"] }`
- 删除角色：`DELETE /roles/{roleId}`
- 权限 CRUD 同理：`/permissions` POST/PUT/DELETE；校验权限编码冲突。

### 3.3 商品与分类
管理员：
- 创建分类：`POST /categories` → `{ "name":"手机", "parentId":0 }`
- 创建子分类：`POST /categories` → `{ "name":"旗舰", "parentId":<上级分类ID> }`
- 更新分类：`PUT /categories/{id}` → `{ "name":"旗舰机" }`
- 删除分类（需无子类/商品）：`DELETE /categories/{id}`
- 创建商品：`POST /products`
```json
{
  "categoryId": <分类ID>,
  "name": "iPhone 15",
  "subtitle": "A17",
  "description": "旗舰",
  "price": 6999,
  "stock": 100,
  "imageUrls": ["https://img.example.com/iphone15.jpg"]
}
```
- 更新商品：`PUT /products/{id}` → 修改名称/库存等
- 上下架：`PUT /products/{id}/status` → `{ "status":2 }`（2=下架，再改回1=上架）
- 删除商品：`DELETE /products/{id}`

公开/已登录：
- 分类树：`GET /categories/tree`
- 商品搜索：`GET /products/search?keyword=iphone&page=1&pageSize=10`
- 商品详情：`GET /products/{id}`

### 3.4 购物车与地址（用户）
地址：
- 新增地址：`POST /addresses`
```json
{ "receiverName":"张三","receiverPhone":"13000000000","province":"广东","city":"深圳","district":"南山","detail":"科苑路","postalCode":"518000","isDefault":1 }
```
- 更新地址：`PUT /addresses/{id}`（修改电话/是否默认等）
- 删除地址：`DELETE /addresses/{id}`
- 列表地址：`GET /addresses`

购物车：
- 加入购物车：`POST /cart/items` → `{ "productId": <商品ID>, "quantity": 2 }`
- 修改数量：`PUT /cart/items/{itemId}` → `{ "quantity": 1 }`
- 删除条目：`DELETE /cart/items/{itemId}`
- 查看购物车：`GET /cart`

### 3.5 订单
1) 创建订单  
`POST /orders`
 ```json
 {
   "addressId": <地址ID>,
   "items":[{"productId":<商品ID>,"quantity":2}],  // 也可省略 items 且 useCart=true 基于购物车下单
   "useCart": false,
   "payMethod":1,
   "remark":"尽快发货"
 }
 ```
   - 基于购物车下单示例：先往购物车加商品，再调用 `POST /orders` 传 `{"addressId":..., "useCart": true}`（items 可空），系统会自动取当前用户购物车中选中的商品。
2) 用户订单列表：`GET /orders?status=2&page=1&pageSize=10`
3) 订单详情：`GET /orders/{orderId}`
4) 支付（标记为已支付）：`PUT /orders/{orderId}/pay` → `{ "payMethod":1 }`
5) 取消订单（待支付）：`PUT /orders/{orderId}/cancel` → `{ "reason":"下单错误" }`
6) 完成订单（已发货后用户确认收货）：`PUT /orders/{orderId}/complete`
7) 状态流转验证：创建(PENDING) → pay(PAID) → 管理员发货(SHIPPED) → 完成(COMPLETED) 或取消(CANCELED)
8) 管理员发货：`PUT /admin/orders/{orderId}/ship`
9) 管理员订单查询/按状态/用户过滤：`GET /admin/orders?status=2&userId=<uid>&page=1&pageSize=10`
10) 管理员订单统计：`GET /admin/orders/stats`

### 3.6 支付与退款（模拟）
1) 创建支付记录：`POST /payments` → `{ "orderId": <已创建订单ID>, "payMethod":1 }`
2) 查询我的支付：`GET /payments/my?page=1&size=10&status=0`
3) 按订单号查支付：`GET /payments/order/{orderNo}`
4) 模拟支付成功回调：`POST /payments/test/{payNo}/success`  
   模拟失败：`POST /payments/test/{payNo}/fail`
5) 支付记录查询（管理员）：`GET /payments?page=1&size=10&status=1`
6) 支付统计（管理员）：`GET /payments/statistics?startDate=2026-01-01T00:00:00&endDate=2026-12-31T23:59:59`
7) 导出支付记录（管理员）：`GET /payments/export`（下载 xlsx）
8) 申请退款（用户，需支付成功）：`POST /refunds` → `{ "paymentId": <支付ID>, "amount": 100, "reason": "不想要了" }`
9) 查看我的退款：`GET /refunds/my?page=1&size=10`
10) 管理员审批退款：`PUT /refunds/{refundId}/process`
```json
{ "approved": true, "rejectReason": null }
```
11) 管理员退款列表：`GET /refunds?page=1&size=10&userId=<申请人可选>`
12) 退款详情：`GET /refunds/{refundId}`

### 3.7 评价（商品评价/查询/点赞/举报/回复）
前置：订单已完成，且包含该商品。
- 发表评价：`POST /products/{productId}/reviews`
```json
{ "orderId": <订单ID>, "rating": 5, "content": "很好", "imageUrls": ["http://img/rev1.png"] }
```
- 查询评价（支持筛选/排序）：  
`GET /products/{productId}/reviews?rating=5&sortBy=time&sortOrder=desc&page=1&pageSize=10`
- 点赞：`POST /reviews/{reviewId}/like`
- 取消点赞：`DELETE /reviews/{reviewId}/like`
- 举报：`POST /reviews/{reviewId}/reports` → `{ "reason":"辱骂" }`
- 管理员回复：`POST /reviews/{reviewId}/replies` → `{ "content":"感谢反馈" }`
- 查看回复：`GET /reviews/{reviewId}/replies`

### 3.8 其他校验
- Swagger UI 需登录：`GET /swagger-ui/index.html`（未登录应 401）
- 未带 Token 访问任一业务接口应返回 401；权限不足接口返回 403。
- 商品上下架：下架后搜索/详情仍可见但购买逻辑应校验状态（下单/加购应被拒绝）。

## 4. Postman 测试小贴士

### 1. 角色登录策略
* **管理员操作**：建议先使用 `admin` 账号登录获取 `adminToken`。使用管理员权限创建基础业务数据（如分类、上架商品、发货等）。
* **普通用户操作**：使用 `demo` 账号或注册新账号登录，用于测试面向用户的接口（如加入购物车、下单、评价等）。

### 2. 自动提取 Token 脚本
在登录接口（如 `/auth/login` 或 `/auth/register`）的 **Scripts** -> **Post-response** 标签页中粘贴以下代码。这将自动将 Token 保存到环境变量中，无需手动复制。

```javascript
pm.test("Login Successful", function () {
    pm.response.to.have.status(200);
});

var jsonData = pm.response.json();

if (jsonData.data) {
    // 提取并设置 Access Token
    if (jsonData.data.accessToken) {
        pm.environment.set("token", jsonData.data.accessToken);
        console.log("Access Token 已更新");
    }

    // 提取并设置 Refresh Token
    if (jsonData.data.refreshToken) {
        pm.environment.set("refreshToken", jsonData.data.refreshToken); 
        console.log("Refresh Token 已保存");
    }
} else {
    console.error("Token提取失败");
}
