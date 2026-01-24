-- 支付模块数据库更新脚本
-- 日期: 2026-01-24

-- 1. 更新payments表结构，添加缺失的字段
ALTER TABLE `payments`
ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`,
ADD COLUMN `paid_at` DATETIME DEFAULT NULL COMMENT '支付时间' AFTER `updated_at`;

-- 2. 更新refunds表结构，添加缺失的字段
ALTER TABLE `refunds`
ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `created_at`,
ADD COLUMN `processed_at` DATETIME DEFAULT NULL COMMENT '处理时间' AFTER `updated_at`,
ADD COLUMN `processed_by` BIGINT(20) UNSIGNED DEFAULT NULL COMMENT '处理人ID' AFTER `processed_at`;

-- 3. 添加索引以提升查询性能
-- payments表索引
CREATE INDEX `idx_payments_created_at` ON `payments`(`created_at`);
CREATE INDEX `idx_payments_status_created` ON `payments`(`status`, `created_at`);

-- refunds表索引
CREATE INDEX `idx_refunds_created_at` ON `refunds`(`created_at`);
CREATE INDEX `idx_refunds_status_created` ON `refunds`(`status`, `created_at`);

-- 4. 插入支付相关的权限记录（如果需要）
INSERT INTO `permissions` (`code`, `name`, `type`, `http_method`, `http_path`, `created_at`, `updated_at`) VALUES
('payment:create', '创建支付', 1, 'POST', '/payments', NOW(), NOW()),
('payment:list', '支付列表', 1, 'GET', '/payments', NOW(), NOW()),
('payment:query', '查询支付', 1, 'GET', '/payments/*', NOW(), NOW()),
('payment:statistics', '支付统计', 1, 'GET', '/payments/statistics', NOW(), NOW()),
('payment:export', '导出支付记录', 1, 'GET', '/payments/export', NOW(), NOW()),
('refund:create', '申请退款', 1, 'POST', '/refunds', NOW(), NOW()),
('refund:list', '退款列表', 1, 'GET', '/refunds', NOW(), NOW()),
('refund:query', '查询退款', 1, 'GET', '/refunds/*', NOW(), NOW()),
('refund:process', '处理退款', 1, 'PUT', '/refunds/*/process', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();
