-- Mall System 初始数据（执行前确认已创建 mall_system 数据库并建好表）
SET NAMES utf8mb4;
USE mall_system;

-- 1) 角色
INSERT INTO roles (code, name, remark, created_at, updated_at)
VALUES
('ADMIN', '管理员', '系统管理员，拥有全部权限', NOW(), NOW()),
('USER', '普通用户', '默认角色', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  remark = VALUES(remark),
  updated_at = NOW();

-- 2) 权限（根据现有接口预置，可自行扩展）
INSERT INTO permissions (code, name, type, http_method, http_path, created_at, updated_at)
VALUES
('auth:me', '查看当前用户信息', 1, 'GET', '/auth/me', NOW(), NOW()),
('user:list', '用户列表', 1, 'GET', '/users', NOW(), NOW()),
('user:detail', '用户详情', 1, 'GET', '/users/{id}', NOW(), NOW()),
('user:roles:update', '为用户分配角色', 1, 'PUT', '/users/{id}/roles', NOW(), NOW()),
('user:profile:update', '更新个人资料', 1, 'PUT', '/users/me/profile', NOW(), NOW()),
('user:password:change', '修改个人密码', 1, 'PUT', '/users/me/password', NOW(), NOW())
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  type = VALUES(type),
  http_method = VALUES(http_method),
  http_path = VALUES(http_path),
  updated_at = NOW();

-- 3) 账户（默认管理员与演示用户）
INSERT INTO users (username, password_hash, email, phone, nickname, avatar_url, gender, status, last_login_at, deleted, created_at, updated_at)
VALUES
('admin', '$2b$12$8TRpJidJNZsDI3qcUHwSIOdoSgQtK8/FRmIQsPDU0.AC93hx36STS', 'admin@example.com', NULL, '管理员', NULL, 0, 1, NULL, 0, NOW(), NOW()),
('demo', '$2b$12$dBN2JOIa1ZsaLstac.hb2uu3dVcqFL5WoTPR6XeE2iG3kA9pSX3mG', 'demo@example.com', NULL, '演示用户', NULL, 0, 1, NULL, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  password_hash = VALUES(password_hash),
  status = VALUES(status),
  nickname = VALUES(nickname),
  updated_at = NOW();

-- 4) 用户角色绑定
INSERT INTO user_roles (user_id, role_id, created_at)
SELECT u.id, r.id, NOW()
FROM users u
JOIN roles r ON r.code = 'ADMIN'
WHERE u.username = 'admin'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

INSERT INTO user_roles (user_id, role_id, created_at)
SELECT u.id, r.id, NOW()
FROM users u
JOIN roles r ON r.code = 'USER'
WHERE u.username = 'demo'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id
  );

-- 5) 角色权限绑定（ADMIN 拥有全部）
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM roles r
JOIN permissions p ON 1=1
WHERE r.code = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- USER 角色仅拥有自助能力
INSERT INTO role_permissions (role_id, permission_id, created_at)
SELECT r.id, p.id, NOW()
FROM roles r
JOIN permissions p ON p.code IN ('auth:me', 'user:profile:update', 'user:password:change')
WHERE r.code = 'USER'
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- 6) 商品分类（多级分类）
INSERT INTO categories (id, parent_id, name) VALUES
-- 一级分类
(1, 0, '数码电子'),
(2, 0, '服装鞋包'),
(3, 0, '食品生鲜'),
(4, 0, '家居用品'),
-- 二级分类（数码电子）
(11, 1, '手机'),
(12, 1, '电脑'),
(13, 1, '摄影器材'),
-- 二级分类（服装鞋包）
(21, 2, '男装'),
(22, 2, '女装'),
(23, 2, '鞋靴'),
-- 二级分类（食品生鲜）
(31, 3, '零食'),
(32, 3, '饮料'),
(33, 3, '生鲜'),
-- 二级分类（家居用品）
(41, 4, '家具'),
(42, 4, '家纺'),
(43, 4, '厨具');

-- 7) 商品信息
INSERT INTO products (id, category_id, name, subtitle, description, price, stock, status, created_at, updated_at) VALUES
-- 手机分类商品
(1, 11, 'iPhone 15 Pro', '钛金属设计，A17 Pro芯片', 'iPhone 15 Pro采用全新钛金属设计，搭载A17 Pro芯片，支持USB-C接口，拍照能力全面提升。', 7999.00, 100, 1, NOW(), NOW()),
(2, 11, '华为 Mate 60 Pro', '卫星通话，昆仑玻璃', '华为Mate 60 Pro支持卫星通话功能，搭载第二代昆仑玻璃，HarmonyOS 4系统。', 6999.00, 80, 1, NOW(), NOW()),
(3, 11, '小米14 Ultra', '徕卡光学，一英寸主摄', '小米14 Ultra搭载徕卡专业光学镜头，一英寸大底主摄，骁龙8 Gen 3处理器。', 6499.00, 120, 1, NOW(), NOW()),

-- 电脑分类商品
(4, 12, 'MacBook Pro 14寸', 'M3 Pro芯片，强大性能', 'MacBook Pro 14搭载M3 Pro芯片，拥有18小时电池续航，Liquid Retina XDR显示屏。', 12999.00, 50, 1, NOW(), NOW()),
(5, 12, '联想拯救者 Y9000P', '游戏本首选，满血显卡', '联想拯救者Y9000P搭载i9处理器和RTX 4070显卡，240Hz高刷电竞屏。', 9999.00, 60, 1, NOW(), NOW()),
(6, 12, '戴尔 XPS 13', '超薄轻薄本，精致设计', '戴尔XPS 13采用InfinityEdge窄边框设计，仅重1.24kg，配备Intel EVO认证。', 8999.00, 40, 1, NOW(), NOW()),

-- 服装分类商品
(7, 21, '商务休闲夹克', '简约百搭，男士必备', '精选优质面料，立体剪裁设计，适合商务休闲场合穿着。', 399.00, 200, 1, NOW(), NOW()),
(8, 21, '纯棉衬衫', '舒适透气，经典款式', '100%纯棉材质，亲肤舒适，经典商务款式，多色可选。', 199.00, 300, 1, NOW(), NOW()),
(9, 22, '连衣裙', '优雅知性，时尚百搭', 'A字版型，优雅显瘦，适合各种场合穿着。', 299.00, 150, 1, NOW(), NOW()),
(10, 23, '运动休闲鞋', '轻便舒适，透气防滑', 'EVA中底，缓震回弹，网面设计，透气不闷脚。', 259.00, 250, 1, NOW(), NOW()),

-- 食品分类商品
(11, 31, '坚果礼盒', '精选坚果，营养丰富', '包含腰果、杏仁、核桃等多种坚果，独立小包装，健康美味。', 89.00, 500, 1, NOW(), NOW()),
(12, 32, '鲜榨果汁', '100%原汁，不添加糖', '新鲜水果现榨，保留原汁原味，无添加，更健康。', 15.00, 1000, 1, NOW(), NOW()),
(13, 33, '进口车厘子', '智利进口，新鲜直达', '智利空运车厘子，新鲜脆甜，果径26-28mm。', 68.00, 200, 1, NOW(), NOW()),

-- 家居分类商品
(14, 41, '北欧风实木茶几', '简约设计，环保材质', '北欧简约风格，进口白橡木，环保烤漆工艺。', 1299.00, 30, 1, NOW(), NOW()),
(15, 42, '纯棉四件套', '亲肤柔软，多种花色', '100%全棉材质，活性印染，手感柔软，不起球。', 299.00, 400, 1, NOW(), NOW()),
(16, 43, '不粘锅套装', '健康烹饪，易清洗', '麦饭石涂层，不粘耐用，少油烟，健康烹饪。', 399.00, 180, 1, NOW(), NOW()),

-- 摄影器材商品
(17, 13, 'Sony A7M4', '全画幅微单，3300万像素', '索尼A7M4搭载BIONZ XR处理器，AI对焦系统，4K60p视频录制。', 16999.00, 25, 1, NOW(), NOW()),
(18, 13, '大疆 DJI Air 3', '双摄航拍，智能跟随', '双镜头设计，46分钟超长续航，全向避障，智能跟随3.0。', 7888.00, 60, 1, NOW(), NOW()),

-- 家纺商品
(19, 42, '乳胶枕头', '天然乳胶，舒适护颈', '93%天然乳胶含量，透气孔设计，支撑颈部，缓解疲劳。', 199.00, 500, 1, NOW(), NOW()),

-- 厨具商品
(20, 43, '多功能料理机', '一机多用，省时省力', '搅拌、切碎、榨汁多功能合一，800W大功率，不锈钢刀头。', 459.00, 150, 1, NOW(), NOW());

-- 8) 商品图片
INSERT INTO product_images (id, product_id, url, sort) VALUES
-- iPhone 15 Pro 图片
(1, 1, 'https://example.com/images/iphone15pro-1.jpg', 1),
(2, 1, 'https://example.com/images/iphone15pro-2.jpg', 2),
(3, 1, 'https://example.com/images/iphone15pro-3.jpg', 3),
-- 华为 Mate 60 Pro 图片
(4, 2, 'https://example.com/images/mate60pro-1.jpg', 1),
(5, 2, 'https://example.com/images/mate60pro-2.jpg', 2),
-- 小米14 Ultra 图片
(6, 3, 'https://example.com/images/mi14ultra-1.jpg', 1),
(7, 3, 'https://example.com/images/mi14ultra-2.jpg', 2),
-- MacBook Pro 图片
(8, 4, 'https://example.com/images/macbookpro-1.jpg', 1),
(9, 4, 'https://example.com/images/macbookpro-2.jpg', 2),
-- 其他商品图片
(10, 5, 'https://example.com/images/lenovo-y9000p.jpg', 1),
(11, 6, 'https://example.com/images/dell-xps13.jpg', 1),
(12, 7, 'https://example.com/images/jacket.jpg', 1),
(13, 8, 'https://example.com/images/shirt.jpg', 1),
(14, 9, 'https://example.com/images/dress.jpg', 1),
(15, 10, 'https://example.com/images/sneakers.jpg', 1),
(16, 11, 'https://example.com/images/nuts.jpg', 1),
(17, 12, 'https://example.com/images/juice.jpg', 1),
(18, 13, 'https://example.com/images/cherry.jpg', 1),
(19, 14, 'https://example.com/images/coffee-table.jpg', 1),
(20, 15, 'https://example.com/images/bedding.jpg', 1),
(21, 16, 'https://example.com/images/pan-set.jpg', 1),
(22, 17, 'https://example.com/images/sony-a7m4.jpg', 1),
(23, 18, 'https://example.com/images/dji-air3.jpg', 1),
(24, 19, 'https://example.com/images/latex-pillow.jpg', 1),
(25, 20, 'https://example.com/images/blender.jpg', 1);

-- 9) 说明
-- 登录账号示例：
--   管理员：admin / Admin@123
--   演示用户：demo / User@123
--
-- 商品模块说明：
--   - 一级分类4个，二级分类12个
--   - 样例商品20个，涵盖各分类
--   - 每个商品至少配有1张图片
--   - 依据业务扩展时，请继续在 permissions、roles、role_permissions 中补充对应编码。
