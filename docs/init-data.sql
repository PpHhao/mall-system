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

-- 6) 说明
-- 登录账号示例：
--   管理员：admin / Admin@123
--   演示用户：demo / User@123
-- 依据业务扩展时，请继续在 permissions、roles、role_permissions 中补充对应编码。
