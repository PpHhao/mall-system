-- phpMyAdmin SQL Dump
-- version 4.5.5.1
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: 2026-01-23 15:39:58
-- 服务器版本： 5.7.11
-- PHP Version: 5.6.19

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `mall_system`
--

-- --------------------------------------------------------

--
-- 表的结构 `addresses`
--

CREATE TABLE `addresses` (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '地址ID',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '用户ID',
  `receiver_name` varchar(50) NOT NULL COMMENT '收货人',
  `receiver_phone` varchar(20) NOT NULL COMMENT '收货电话',
  `province` varchar(50) NOT NULL COMMENT '省',
  `city` varchar(50) NOT NULL COMMENT '市',
  `district` varchar(50) NOT NULL COMMENT '区',
  `detail` varchar(255) NOT NULL COMMENT '详细地址',
  `postal_code` varchar(20) DEFAULT NULL COMMENT AS `邮编`,
  `is_default` tinyint(4) NOT NULL DEFAULT '0'COMMENT
) ;

-- --------------------------------------------------------

--
-- 表的结构 `cart_items`
--

CREATE TABLE `cart_items` (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '购物车项ID',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '用户ID',
  `product_id` bigint(20) UNSIGNED NOT NULL COMMENT '商品ID',
  `quantity` int(11) NOT NULL DEFAULT '1' COMMENT '数量',
  `checked` tinyint(4) NOT NULL DEFAULT '1' COMMENT '是否勾选：1是 0否',
  `price_at_add` decimal(10,2) DEFAULT NULL COMMENT AS `加入时价格（可选）`,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- --------------------------------------------------------

--
-- 表的结构 `categories`
--

CREATE TABLE `categories` (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '分类ID',
  `parent_id` bigint(20) UNSIGNED NOT NULL DEFAULT '0'COMMENT
) ;

-- --------------------------------------------------------

--
-- 表的结构 `files`
--

CREATE TABLE `files` (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '文件ID',
  `user_id` bigint(20) UNSIGNED DEFAULT NULL COMMENT AS `上传者用户ID（可选）`,
  `biz_type` varchar(32) NOT NULL COMMENT '业务类型：product/user/review等',
  `biz_id` bigint(20) UNSIGNED NOT NULL COMMENT '业务ID',
  `url` varchar(255) NOT NULL COMMENT '文件URL',
  `original_name` varchar(255) DEFAULT NULL COMMENT AS `原始文件名`,
  `mime_type` varchar(100) DEFAULT NULL COMMENT AS `MIME类型`,
  `size_bytes` int(10) UNSIGNED DEFAULT NULL COMMENT AS `文件大小（字节）`,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件表（可选）';

-- --------------------------------------------------------

--
-- 表的结构 `orders`
--

CREATE TABLE `orders` (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '订单ID',
  `order_no` varchar(32) NOT NULL COMMENT '订单号（业务唯一）',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '用户ID',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '订单状态：1待支付 2已支付 3已发货 4已完成 5已取消',
  `total_amount` decimal(10,2) NOT NULL COMMENT '商品总金额',
  `freight_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '运费（可选）',
  `pay_amount` decimal(10,2) NOT NULL COMMENT '应付金额',
  `pay_method` tinyint(4) DEFAULT NULL COMMENT AS `支付方式：1模拟 2支付宝(模拟) 3微信(模拟)`,
  `paid_at` datetime DEFAULT NULL COMMENT AS `支付时间`,
  `shipped_at` datetime DEFAULT NULL COMMENT AS `发货时间`,
  `completed_at` datetime DEFAULT NULL COMMENT AS `完成时间`,
  `canceled_at` datetime DEFAULT NULL COMMENT AS `取消时间`,
  `cancel_reason` varchar(255) DEFAULT NULL COMMENT AS `取消原因`,
  `receiver_name` varchar(50) NOT NULL COMMENT '收货人',
  `receiver_phone` varchar(20) NOT NULL COMMENT '收货电话',
  `province` varchar(50) NOT NULL COMMENT '省',
  `city` varchar(50) NOT NULL COMMENT '市',
  `district` varchar(50) NOT NULL COMMENT '区',
  `detail` varchar(255) NOT NULL COMMENT '详细地址',
  `postal_code` varchar(20) DEFAULT NULL COMMENT AS `邮编`,
  `remark` varchar(255) DEFAULT NULL COMMENT AS `买家备注`,
  `deleted` tinyint(4) NOT NULL DEFAULT '0'COMMENT
) ;

-- --------------------------------------------------------

--
-- 表的结构 `order_items`
--

CREATE TABLE `order_items` (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '订单明细ID',
  `order_id` bigint(20) UNSIGNED NOT NULL COMMENT '订单ID',
  `product_id` bigint(20) UNSIGNED NOT NULL COMMENT '商品ID',
  `product_name` varchar(200) NOT NULL COMMENT '商品名称快照',
  `product_image` varchar(255) DEFAULT NULL COMMENT AS `商品图片快照`,
  `unit_price` decimal(10,2) NOT NULL COMMENT '成交单价',
  `quantity` int(11) NOT NULL COMMENT '购买数量',
  `total_price` decimal(10,2) NOT NULL COMMENT '明细小计',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品明细表';

-- --------------------------------------------------------

--
-- 表的结构 `payments`
--

CREATE TABLE `payments` (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '支付记录ID',
  `order_id` bigint(20) UNSIGNED NOT NULL COMMENT '订单ID',
  `order_no` varchar(32) NOT NULL COMMENT '订单号（冗余）',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '用户ID',
  `pay_no` varchar(64) NOT NULL COMMENT '支付单号（业务唯一）',
  `pay_method` tinyint(4) NOT NULL DEFAULT '1' COMMENT '支付方式：1模拟 2支付宝(模拟) 3微信(模拟)',
  `amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `status` tinyint(4) NOT NULL DEFAULT '0'COMMENT
) ;

-- --------------------------------------------------------

--
-- 表的结构 `permissions`
--

CREATE TABLE `permissions` (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '权限ID',
  `code` varchar(100) NOT NULL COMMENT '权限编码（如 product:read）',
  `name` varchar(100) NOT NULL COMMENT '权限名称',
  `type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '类型：1API 2菜单/页面（可选）',
  `http_method` varchar(10) DEFAULT NULL COMMENT AS `HTTP方法（可选）`,
  `http_path` varchar(255) DEFAULT NULL COMMENT AS `接口路径（可选）`,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

--
-- 转存表中的数据 `permissions`
--

INSERT INTO `permissions` (`id`, `code`, `name`, `type`, `http_method`, `http_path`, `created_at`, `updated_at`) VALUES
(1, 'auth:me', '查看当前用户信息', 1, 'GET', '/auth/me', '2026-01-23 23:37:37', '2026-01-23 23:37:37'),
(2, 'user:list', '用户列表', 1, 'GET', '/users', '2026-01-23 23:37:37', '2026-01-23 23:37:37'),
(3, 'user:detail', '用户详情', 1, 'GET', '/users/{id}', '2026-01-23 23:37:37', '2026-01-23 23:37:37'),
(4, 'user:roles:update', '为用户分配角色', 1, 'PUT', '/users/{id}/roles', '2026-01-23 23:37:37', '2026-01-23 23:37:37'),
(5, 'user:profile:update', '更新个人资料', 1, 'PUT', '/users/me/profile', '2026-01-23 23:37:37', '2026-01-23 23:37:37'),
(6, 'user:password:change', '修改个人密码', 1, 'PUT', '/users/me/password', '2026-01-23 23:37:37', '2026-01-23 23:37:37');

-- --------------------------------------------------------

--
-- 表的结构 `products`
--

CREATE TABLE `products` (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '商品ID',
  `category_id` bigint(20) UNSIGNED NOT NULL COMMENT '分类ID',
  `name` varchar(200) NOT NULL COMMENT '商品名称',
  `subtitle` varchar(200) DEFAULT NULL COMMENT AS `副标题/卖点`,
  `description` text COMMENT '商品详情描述',
  `price` decimal(10,2) NOT NULL COMMENT '售价',
  `stock` int(11) NOT NULL DEFAULT '0'COMMENT
) ;

-- --------------------------------------------------------

--
-- 表的结构 `product_images`
--

CREATE TABLE `product_images` (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '图片ID',
  `product_id` bigint(20) UNSIGNED NOT NULL COMMENT '商品ID',
  `url` varchar(255) NOT NULL COMMENT '图片URL',
  `sort` int(11) NOT NULL DEFAULT '0'COMMENT
) ;

-- --------------------------------------------------------

--
-- 表的结构 `refunds`
--

CREATE TABLE `refunds` (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '退款记录ID',
  `payment_id` bigint(20) UNSIGNED NOT NULL COMMENT '支付记录ID',
  `order_id` bigint(20) UNSIGNED NOT NULL COMMENT '订单ID',
  `refund_no` varchar(64) NOT NULL COMMENT '退款单号（业务唯一）',
  `amount` decimal(10,2) NOT NULL COMMENT '退款金额',
  `status` tinyint(4) NOT NULL DEFAULT '0'COMMENT
) ;

-- --------------------------------------------------------

--
-- 表的结构 `reviews`
--

CREATE TABLE `reviews` (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '评价ID',
  `order_id` bigint(20) UNSIGNED DEFAULT NULL COMMENT AS `订单ID（可选）`,
  `order_item_id` bigint(20) UNSIGNED DEFAULT NULL COMMENT AS `订单明细ID（可选）`,
  `product_id` bigint(20) UNSIGNED NOT NULL COMMENT '商品ID',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '评价人ID',
  `rating` tinyint(4) NOT NULL COMMENT '评分：1~5',
  `content` varchar(1000) DEFAULT NULL COMMENT AS `评价内容`,
  `images` text COMMENT '图片列表（JSON字符串）',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：1正常 2隐藏 3删除(逻辑)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表';

-- --------------------------------------------------------

--
-- 表的结构 `review_likes`
--

CREATE TABLE `review_likes` (
  `review_id` bigint(20) UNSIGNED NOT NULL COMMENT '评价ID',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '点赞用户ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价点赞表';

-- --------------------------------------------------------

--
-- 表的结构 `review_replies`
--

CREATE TABLE `review_replies` (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '回复ID',
  `review_id` bigint(20) UNSIGNED NOT NULL COMMENT '评价ID',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '回复人ID',
  `content` varchar(1000) NOT NULL COMMENT '回复内容',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价回复表';

-- --------------------------------------------------------

--
-- 表的结构 `review_reports`
--

CREATE TABLE `review_reports` (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '举报ID',
  `review_id` bigint(20) UNSIGNED NOT NULL COMMENT '评价ID',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '举报人ID',
  `reason` varchar(255) NOT NULL COMMENT '举报原因',
  `status` tinyint(4) NOT NULL DEFAULT '0'COMMENT
) ;

-- --------------------------------------------------------

--
-- 表的结构 `roles`
--

CREATE TABLE `roles` (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '角色ID',
  `code` varchar(50) NOT NULL COMMENT '角色编码（如 ADMIN/USER）',
  `name` varchar(50) NOT NULL COMMENT '角色名称',
  `remark` varchar(255) DEFAULT NULL COMMENT AS `备注`,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

--
-- 转存表中的数据 `roles`
--

INSERT INTO `roles` (`id`, `code`, `name`, `remark`, `created_at`, `updated_at`) VALUES
(1, 'ADMIN', '管理员', '系统管理员，拥有全部权限', '2026-01-23 23:37:37', '2026-01-23 23:37:37'),
(2, 'USER', '普通用户', '默认角色', '2026-01-23 23:37:37', '2026-01-23 23:37:37');

-- --------------------------------------------------------

--
-- 表的结构 `role_permissions`
--

CREATE TABLE `role_permissions` (
  `role_id` bigint(20) UNSIGNED NOT NULL COMMENT '角色ID',
  `permission_id` bigint(20) UNSIGNED NOT NULL COMMENT '权限ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限关系表';

--
-- 转存表中的数据 `role_permissions`
--

INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES
(1, 1, '2026-01-23 23:37:37'),
(1, 2, '2026-01-23 23:37:37'),
(1, 3, '2026-01-23 23:37:37'),
(1, 4, '2026-01-23 23:37:37'),
(1, 5, '2026-01-23 23:37:37'),
(1, 6, '2026-01-23 23:37:37'),
(2, 1, '2026-01-23 23:37:37'),
(2, 5, '2026-01-23 23:37:37'),
(2, 6, '2026-01-23 23:37:37');

-- --------------------------------------------------------

--
-- 表的结构 `users`
--

CREATE TABLE `users` (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名（登录用）',
  `password_hash` varchar(100) NOT NULL COMMENT '密码哈希（BCrypt等）',
  `email` varchar(100) DEFAULT NULL COMMENT AS `邮箱`,
  `phone` varchar(20) DEFAULT NULL COMMENT AS `手机号`,
  `nickname` varchar(50) DEFAULT NULL COMMENT AS `昵称`,
  `avatar_url` varchar(255) DEFAULT NULL COMMENT AS `头像URL`,
  `gender` tinyint(4) NOT NULL DEFAULT '0'COMMENT
) ;

--
-- 转存表中的数据 `users`
--

INSERT INTO `users` (`id`, `username`, `password_hash`, `email`, `phone`, `nickname`, `avatar_url`, `gender`, `status`, `last_login_at`, `deleted`, `created_at`, `updated_at`) VALUES
(1, 'admin', '$2b$12$8TRpJidJNZsDI3qcUHwSIOdoSgQtK8/FRmIQsPDU0.AC93hx36STS', 'admin@example.com', NULL, '管理员', NULL, 0, 1, NULL, 0, '2026-01-23 23:37:37', '2026-01-23 23:37:37'),
(2, 'demo', '$2b$12$dBN2JOIa1ZsaLstac.hb2uu3dVcqFL5WoTPR6XeE2iG3kA9pSX3mG', 'demo@example.com', NULL, '演示用户', NULL, 0, 1, NULL, 0, '2026-01-23 23:37:37', '2026-01-23 23:37:37');

-- --------------------------------------------------------

--
-- 表的结构 `user_roles`
--

CREATE TABLE `user_roles` (
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) UNSIGNED NOT NULL COMMENT '角色ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关系表';

--
-- 转存表中的数据 `user_roles`
--

INSERT INTO `user_roles` (`user_id`, `role_id`, `created_at`) VALUES
(1, 1, '2026-01-23 23:37:37'),
(2, 2, '2026-01-23 23:37:37');

-- --------------------------------------------------------

--
-- 表的结构 `user_tokens`
--

CREATE TABLE `user_tokens` (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT 'Token记录ID',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '用户ID',
  `jti` varchar(64) NOT NULL COMMENT 'JWT ID（唯一标识）',
  `token_type` tinyint(4) NOT NULL DEFAULT '2' COMMENT '类型：1access 2refresh',
  `expired_at` datetime NOT NULL COMMENT '过期时间',
  `revoked` tinyint(4) NOT NULL DEFAULT '0'COMMENT
) ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `cart_items`
--
ALTER TABLE `cart_items`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_cart_user_product` (`user_id`,`product_id`),
  ADD KEY `idx_cart_user` (`user_id`),
  ADD KEY `fk_cart_product` (`product_id`);

--
-- Indexes for table `files`
--
ALTER TABLE `files`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_files_biz` (`biz_type`,`biz_id`),
  ADD KEY `idx_files_user` (`user_id`);

--
-- Indexes for table `order_items`
--
ALTER TABLE `order_items`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_order_items_order` (`order_id`),
  ADD KEY `idx_order_items_product` (`product_id`);

--
-- Indexes for table `permissions`
--
ALTER TABLE `permissions`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_permissions_code` (`code`),
  ADD KEY `idx_permissions_path` (`http_path`);

--
-- Indexes for table `reviews`
--
ALTER TABLE `reviews`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_reviews_product` (`product_id`),
  ADD KEY `idx_reviews_user` (`user_id`),
  ADD KEY `idx_reviews_rating` (`rating`),
  ADD KEY `idx_reviews_created` (`created_at`),
  ADD KEY `fk_reviews_order` (`order_id`),
  ADD KEY `fk_reviews_order_item` (`order_item_id`);

--
-- Indexes for table `review_likes`
--
ALTER TABLE `review_likes`
  ADD PRIMARY KEY (`review_id`,`user_id`),
  ADD KEY `idx_review_likes_user` (`user_id`);

--
-- Indexes for table `review_replies`
--
ALTER TABLE `review_replies`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_review_replies_review` (`review_id`),
  ADD KEY `fk_review_replies_user` (`user_id`);

--
-- Indexes for table `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_roles_code` (`code`);

--
-- Indexes for table `role_permissions`
--
ALTER TABLE `role_permissions`
  ADD PRIMARY KEY (`role_id`,`permission_id`),
  ADD KEY `fk_role_permissions_perm` (`permission_id`);

--
-- Indexes for table `user_roles`
--
ALTER TABLE `user_roles`
  ADD PRIMARY KEY (`user_id`,`role_id`),
  ADD KEY `fk_user_roles_role` (`role_id`);

--
-- 在导出的表使用AUTO_INCREMENT
--

--
-- 使用表AUTO_INCREMENT `addresses`
--
ALTER TABLE `addresses`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '地址ID';
--
-- 使用表AUTO_INCREMENT `cart_items`
--
ALTER TABLE `cart_items`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '购物车项ID';
--
-- 使用表AUTO_INCREMENT `categories`
--
ALTER TABLE `categories`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID';
--
-- 使用表AUTO_INCREMENT `files`
--
ALTER TABLE `files`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '文件ID';
--
-- 使用表AUTO_INCREMENT `orders`
--
ALTER TABLE `orders`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID';
--
-- 使用表AUTO_INCREMENT `order_items`
--
ALTER TABLE `order_items`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单明细ID';
--
-- 使用表AUTO_INCREMENT `payments`
--
ALTER TABLE `payments`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '支付记录ID';
--
-- 使用表AUTO_INCREMENT `permissions`
--
ALTER TABLE `permissions`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '权限ID', AUTO_INCREMENT=7;
--
-- 使用表AUTO_INCREMENT `products`
--
ALTER TABLE `products`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商品ID';
--
-- 使用表AUTO_INCREMENT `product_images`
--
ALTER TABLE `product_images`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '图片ID';
--
-- 使用表AUTO_INCREMENT `refunds`
--
ALTER TABLE `refunds`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '退款记录ID';
--
-- 使用表AUTO_INCREMENT `reviews`
--
ALTER TABLE `reviews`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评价ID';
--
-- 使用表AUTO_INCREMENT `review_replies`
--
ALTER TABLE `review_replies`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '回复ID';
--
-- 使用表AUTO_INCREMENT `review_reports`
--
ALTER TABLE `review_reports`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '举报ID';
--
-- 使用表AUTO_INCREMENT `roles`
--
ALTER TABLE `roles`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色ID', AUTO_INCREMENT=3;
--
-- 使用表AUTO_INCREMENT `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID';
--
-- 使用表AUTO_INCREMENT `user_tokens`
--
ALTER TABLE `user_tokens`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Token记录ID';
--
-- 限制导出的表
--

--
-- 限制表 `cart_items`
--
ALTER TABLE `cart_items`
  ADD CONSTRAINT `fk_cart_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- 限制表 `files`
--
ALTER TABLE `files`
  ADD CONSTRAINT `fk_files_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- 限制表 `order_items`
--
ALTER TABLE `order_items`
  ADD CONSTRAINT `fk_order_items_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_order_items_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON UPDATE CASCADE;

--
-- 限制表 `reviews`
--
ALTER TABLE `reviews`
  ADD CONSTRAINT `fk_reviews_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_reviews_order_item` FOREIGN KEY (`order_item_id`) REFERENCES `order_items` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_reviews_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_reviews_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON UPDATE CASCADE;

--
-- 限制表 `review_likes`
--
ALTER TABLE `review_likes`
  ADD CONSTRAINT `fk_review_likes_review` FOREIGN KEY (`review_id`) REFERENCES `reviews` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_review_likes_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- 限制表 `review_replies`
--
ALTER TABLE `review_replies`
  ADD CONSTRAINT `fk_review_replies_review` FOREIGN KEY (`review_id`) REFERENCES `reviews` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_review_replies_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON UPDATE CASCADE;

--
-- 限制表 `role_permissions`
--
ALTER TABLE `role_permissions`
  ADD CONSTRAINT `fk_role_permissions_perm` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_role_permissions_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- 限制表 `user_roles`
--
ALTER TABLE `user_roles`
  ADD CONSTRAINT `fk_user_roles_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_user_roles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
