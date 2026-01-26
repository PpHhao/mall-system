-- phpMyAdmin SQL Dump
-- version 4.5.5.1
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: 2026-01-26 07:50:35
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
  `id` bigint(20) UNSIGNED NOT NULL,
  `user_id` bigint(20) UNSIGNED NOT NULL,
  `receiver_name` varchar(50) NOT NULL,
  `receiver_phone` varchar(20) NOT NULL,
  `province` varchar(50) NOT NULL,
  `city` varchar(50) NOT NULL,
  `district` varchar(50) NOT NULL,
  `detail` varchar(255) NOT NULL,
  `postal_code` varchar(20) DEFAULT NULL,
  `is_default` tinyint(4) DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- 转存表中的数据 `addresses`
--

INSERT INTO `addresses` (`id`, `user_id`, `receiver_name`, `receiver_phone`, `province`, `city`, `district`, `detail`, `postal_code`, `is_default`) VALUES
(1, 2, '张三', '13000000000', '广东', '深圳', '南山', '科苑路', '518000', 1),
(3, 3, 'xiaopeng', '13800138000', '广东省', '深圳市', '南山区', '科苑南路xx号', '518000', 1);

-- --------------------------------------------------------

--
-- 表的结构 `cart_items`
--

CREATE TABLE `cart_items` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `user_id` bigint(20) UNSIGNED NOT NULL,
  `product_id` bigint(20) UNSIGNED NOT NULL,
  `quantity` int(11) NOT NULL,
  `checked` tinyint(4) DEFAULT NULL,
  `price_at_add` decimal(18,2) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- 表的结构 `categories`
--

CREATE TABLE `categories` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `parent_id` bigint(20) UNSIGNED DEFAULT '0',
  `name` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- 转存表中的数据 `categories`
--

INSERT INTO `categories` (`id`, `parent_id`, `name`) VALUES
(1, 0, '数码'),
(2, 1, '手机'),
(3, 0, '手机'),
(4, 3, '旗舰机');

-- --------------------------------------------------------

--
-- 表的结构 `orders`
--

CREATE TABLE `orders` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `order_no` varchar(100) NOT NULL,
  `user_id` bigint(20) UNSIGNED NOT NULL,
  `status` tinyint(4) DEFAULT NULL,
  `total_amount` decimal(18,2) DEFAULT NULL,
  `freight_amount` decimal(18,2) DEFAULT NULL,
  `pay_amount` decimal(18,2) DEFAULT NULL,
  `pay_method` tinyint(4) DEFAULT NULL,
  `paid_at` datetime DEFAULT NULL,
  `shipped_at` datetime DEFAULT NULL,
  `completed_at` datetime DEFAULT NULL,
  `canceled_at` datetime DEFAULT NULL,
  `cancel_reason` varchar(255) DEFAULT NULL,
  `receiver_name` varchar(50) DEFAULT NULL,
  `receiver_phone` varchar(20) DEFAULT NULL,
  `province` varchar(50) DEFAULT NULL,
  `city` varchar(50) DEFAULT NULL,
  `district` varchar(50) DEFAULT NULL,
  `detail` varchar(255) DEFAULT NULL,
  `postal_code` varchar(20) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `deleted` tinyint(4) DEFAULT '0',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- 转存表中的数据 `orders`
--

INSERT INTO `orders` (`id`, `order_no`, `user_id`, `status`, `total_amount`, `freight_amount`, `pay_amount`, `pay_method`, `paid_at`, `shipped_at`, `completed_at`, `canceled_at`, `cancel_reason`, `receiver_name`, `receiver_phone`, `province`, `city`, `district`, `detail`, `postal_code`, `remark`, `deleted`, `created_at`, `updated_at`) VALUES
(1, 'O20260126144814764795', 3, 4, '6888.00', '0.00', '6888.00', 1, NULL, '2026-01-26 14:52:48', '2026-01-26 14:53:52', NULL, NULL, 'xiaopeng', '13800138000', '广东省', '深圳市', '南山区', '科苑南路xx号', '518000', '请包装好一点', 0, '2026-01-26 14:48:15', '2026-01-26 14:53:52'),
(2, 'O20260126150634974900', 3, 4, '13776.00', '0.00', '13776.00', 1, NULL, '2026-01-26 15:07:33', '2026-01-26 15:07:45', NULL, NULL, 'xiaopeng', '13800138000', '广东省', '深圳市', '南山区', '科苑南路xx号', '518000', '请包装好一点', 0, '2026-01-26 15:06:34', '2026-01-26 15:07:45'),
(3, 'O20260126151944124921', 3, 4, '6888.00', '0.00', '6888.00', 1, NULL, '2026-01-26 15:20:22', '2026-01-26 15:20:30', NULL, NULL, 'xiaopeng', '13800138000', '广东省', '深圳市', '南山区', '科苑南路xx号', '518000', '请包装好一点', 0, '2026-01-26 15:19:44', '2026-01-26 15:20:30');

-- --------------------------------------------------------

--
-- 表的结构 `order_items`
--

CREATE TABLE `order_items` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `order_id` bigint(20) UNSIGNED NOT NULL,
  `product_id` bigint(20) UNSIGNED NOT NULL,
  `product_name` varchar(200) DEFAULT NULL,
  `product_image` varchar(500) DEFAULT NULL,
  `unit_price` decimal(18,2) DEFAULT NULL,
  `quantity` int(11) DEFAULT NULL,
  `total_price` decimal(18,2) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- 转存表中的数据 `order_items`
--

INSERT INTO `order_items` (`id`, `order_id`, `product_id`, `product_name`, `product_image`, `unit_price`, `quantity`, `total_price`, `created_at`) VALUES
(1, 1, 2, 'iPhone 15', 'https://img.example.com/iphone15.jpg', '6888.00', 1, '6888.00', '2026-01-26 14:48:15'),
(2, 2, 2, 'iPhone 15', 'https://img.example.com/iphone15.jpg', '6888.00', 2, '13776.00', '2026-01-26 15:06:34'),
(3, 3, 2, 'iPhone 15', 'https://img.example.com/iphone15.jpg', '6888.00', 1, '6888.00', '2026-01-26 15:19:44');

-- --------------------------------------------------------

--
-- 表的结构 `payments`
--

CREATE TABLE `payments` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `order_id` bigint(20) UNSIGNED NOT NULL,
  `order_no` varchar(100) NOT NULL,
  `user_id` bigint(20) UNSIGNED NOT NULL,
  `pay_no` varchar(100) NOT NULL,
  `pay_method` tinyint(4) DEFAULT NULL,
  `amount` decimal(18,2) DEFAULT NULL,
  `status` tinyint(4) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `paid_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- 转存表中的数据 `payments`
--

INSERT INTO `payments` (`id`, `order_id`, `order_no`, `user_id`, `pay_no`, `pay_method`, `amount`, `status`, `created_at`, `updated_at`, `paid_at`) VALUES
(1, 1, 'O20260126144814764795', 3, 'PAY1769410188936BA1E18FD', 1, '6888.00', 2, '2026-01-26 14:49:49', '2026-01-26 14:54:46', '2026-01-26 14:50:54'),
(2, 2, 'O20260126150634974900', 3, 'PAY176941121140444873D74', 1, '13776.00', 2, '2026-01-26 15:06:51', '2026-01-26 15:08:16', '2026-01-26 15:07:21'),
(3, 3, 'O20260126151944124921', 3, 'PAY17694119958059CC357DA', 1, '6888.00', 3, '2026-01-26 15:19:56', '2026-01-26 15:21:13', '2026-01-26 15:20:12');

-- --------------------------------------------------------

--
-- 表的结构 `permissions`
--

CREATE TABLE `permissions` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `code` varchar(100) NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `type` tinyint(4) DEFAULT NULL,
  `http_method` varchar(20) DEFAULT NULL,
  `http_path` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- 转存表中的数据 `permissions`
--

INSERT INTO `permissions` (`id`, `code`, `name`, `type`, `http_method`, `http_path`, `created_at`, `updated_at`) VALUES
(1, 'auth:me', '查看当前用户', 1, 'GET', '/auth/me', '2026-01-26 12:34:08', '2026-01-26 12:34:08'),
(2, 'user:list', '用户列表', 1, 'GET', '/users', '2026-01-26 12:34:08', '2026-01-26 12:34:08'),
(3, 'user:detail', '用户详情', 1, 'GET', '/users/{id}', '2026-01-26 12:34:08', '2026-01-26 12:34:08'),
(4, 'user:roles:update', '分配用户角色', 1, 'PUT', '/users/{id}/roles', '2026-01-26 12:34:08', '2026-01-26 12:34:08'),
(5, 'user:profile:update', '修改个人资料', 1, 'PUT', '/users/me/profile', '2026-01-26 12:34:08', '2026-01-26 12:34:08'),
(6, 'user:password:change', '修改密码', 1, 'PUT', '/users/me/password', '2026-01-26 12:34:08', '2026-01-26 12:34:08'),
(7, 'role:crud', '角色管理', 1, '*', '/roles*', '2026-01-26 12:34:08', '2026-01-26 12:34:08'),
(8, 'permission:crud', '权限管理', 1, '*', '/permissions*', '2026-01-26 12:34:08', '2026-01-26 12:34:08'),
(9, 'product:crud', '商品管理', 1, '*', '/products*', '2026-01-26 12:34:08', '2026-01-26 12:34:08'),
(10, 'category:crud', '分类管理', 1, '*', '/categories*', '2026-01-26 12:34:08', '2026-01-26 12:34:08'),
(11, 'order:admin', '订单管理', 1, '*', '/admin/orders*', '2026-01-26 12:34:08', '2026-01-26 12:34:08'),
(12, 'payment:admin', '支付管理', 1, '*', '/payments*', '2026-01-26 12:34:08', '2026-01-26 12:34:08'),
(13, 'refund:admin', '退款管理', 1, '*', '/refunds*', '2026-01-26 12:34:08', '2026-01-26 12:34:08'),
(14, 'log:view', '查看日志', 1, 'GET', '/logs', '2026-01-26 14:18:54', '2026-01-26 14:18:54');

-- --------------------------------------------------------

--
-- 表的结构 `products`
--

CREATE TABLE `products` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `category_id` bigint(20) UNSIGNED NOT NULL,
  `name` varchar(200) NOT NULL,
  `subtitle` varchar(255) DEFAULT NULL,
  `description` text,
  `price` decimal(18,2) NOT NULL,
  `stock` int(11) DEFAULT NULL,
  `status` tinyint(4) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- 转存表中的数据 `products`
--

INSERT INTO `products` (`id`, `category_id`, `name`, `subtitle`, `description`, `price`, `stock`, `status`, `created_at`, `updated_at`) VALUES
(1, 2, 'iPhone 15', 'A17', '旗舰机', '6999.00', 100, 1, '2026-01-26 12:34:08', '2026-01-26 12:34:08'),
(2, 4, 'iPhone 15', 'A17芯片', '2024年度旗舰手机', '6888.00', 196, 1, '2026-01-26 14:27:05', '2026-01-26 14:29:44');

-- --------------------------------------------------------

--
-- 表的结构 `product_images`
--

CREATE TABLE `product_images` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `product_id` bigint(20) UNSIGNED NOT NULL,
  `url` varchar(500) DEFAULT NULL,
  `sort` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- 转存表中的数据 `product_images`
--

INSERT INTO `product_images` (`id`, `product_id`, `url`, `sort`) VALUES
(1, 1, 'https://img.example.com/iphone15.jpg', 0),
(2, 2, 'https://img.example.com/iphone15.jpg', 0);

-- --------------------------------------------------------

--
-- 表的结构 `refunds`
--

CREATE TABLE `refunds` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `payment_id` bigint(20) UNSIGNED NOT NULL,
  `order_id` bigint(20) UNSIGNED NOT NULL,
  `refund_no` varchar(100) NOT NULL,
  `amount` decimal(18,2) DEFAULT NULL,
  `status` tinyint(4) DEFAULT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `processed_at` datetime DEFAULT NULL,
  `processed_by` bigint(20) UNSIGNED DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- 转存表中的数据 `refunds`
--

INSERT INTO `refunds` (`id`, `payment_id`, `order_id`, `refund_no`, `amount`, `status`, `reason`, `created_at`, `updated_at`, `processed_at`, `processed_by`) VALUES
(1, 1, 1, 'REF176941048635139F54CE2', '6888.00', 0, '手机发热严重', '2026-01-26 14:54:46', '2026-01-26 14:54:46', NULL, NULL),
(2, 2, 2, 'REF1769411296035AA9DA376', '13776.00', 0, '手机发热严重', '2026-01-26 15:08:16', '2026-01-26 15:08:16', NULL, NULL),
(3, 3, 3, 'REF1769412056765AA98E568', '6888.00', 3, '手机发热严重', '2026-01-26 15:20:57', '2026-01-26 15:21:13', '2026-01-26 15:21:13', 1);

-- --------------------------------------------------------

--
-- 表的结构 `reviews`
--

CREATE TABLE `reviews` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `order_id` bigint(20) UNSIGNED NOT NULL,
  `order_item_id` bigint(20) UNSIGNED DEFAULT NULL,
  `product_id` bigint(20) UNSIGNED NOT NULL,
  `user_id` bigint(20) UNSIGNED NOT NULL,
  `rating` tinyint(4) DEFAULT NULL,
  `content` text,
  `images` text,
  `status` tinyint(4) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- 转存表中的数据 `reviews`
--

INSERT INTO `reviews` (`id`, `order_id`, `order_item_id`, `product_id`, `user_id`, `rating`, `content`, `images`, `status`, `created_at`, `updated_at`) VALUES
(1, 3, NULL, 2, 3, 5, '手机非常棒，运行速度快！', '["http://img.example.com/rev1.png"]', 1, '2026-01-26 15:33:15', '2026-01-26 15:33:15');

-- --------------------------------------------------------

--
-- 表的结构 `review_likes`
--

CREATE TABLE `review_likes` (
  `review_id` bigint(20) UNSIGNED NOT NULL,
  `user_id` bigint(20) UNSIGNED NOT NULL,
  `created_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- 表的结构 `review_replies`
--

CREATE TABLE `review_replies` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `review_id` bigint(20) UNSIGNED NOT NULL,
  `user_id` bigint(20) UNSIGNED NOT NULL,
  `content` text,
  `created_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- 转存表中的数据 `review_replies`
--

INSERT INTO `review_replies` (`id`, `review_id`, `user_id`, `content`, `created_at`) VALUES
(1, 1, 1, '感谢您的支持，我们将继续努力！', '2026-01-26 15:38:32');

-- --------------------------------------------------------

--
-- 表的结构 `review_reports`
--

CREATE TABLE `review_reports` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `review_id` bigint(20) UNSIGNED NOT NULL,
  `user_id` bigint(20) UNSIGNED NOT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `status` tinyint(4) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- 转存表中的数据 `review_reports`
--

INSERT INTO `review_reports` (`id`, `review_id`, `user_id`, `reason`, `status`, `created_at`) VALUES
(1, 1, 3, '涉嫌广告', 0, '2026-01-26 15:37:34');

-- --------------------------------------------------------

--
-- 表的结构 `roles`
--

CREATE TABLE `roles` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `code` varchar(50) NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- 转存表中的数据 `roles`
--

INSERT INTO `roles` (`id`, `code`, `name`, `remark`, `created_at`, `updated_at`) VALUES
(1, 'ADMIN', '管理员', '系统管理员', '2026-01-26 12:34:08', '2026-01-26 12:34:08'),
(2, 'USER', '普通用户', '默认角色', '2026-01-26 12:34:08', '2026-01-26 12:34:08');

-- --------------------------------------------------------

--
-- 表的结构 `role_permissions`
--

CREATE TABLE `role_permissions` (
  `role_id` bigint(20) UNSIGNED NOT NULL,
  `permission_id` bigint(20) UNSIGNED NOT NULL,
  `created_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- 转存表中的数据 `role_permissions`
--

INSERT INTO `role_permissions` (`role_id`, `permission_id`, `created_at`) VALUES
(1, 1, '2026-01-26 12:34:08'),
(1, 2, '2026-01-26 12:34:08'),
(1, 3, '2026-01-26 12:34:08'),
(1, 4, '2026-01-26 12:34:08'),
(1, 5, '2026-01-26 12:34:08'),
(1, 6, '2026-01-26 12:34:08'),
(1, 7, '2026-01-26 12:34:08'),
(1, 8, '2026-01-26 12:34:08'),
(1, 9, '2026-01-26 12:34:08'),
(1, 10, '2026-01-26 12:34:08'),
(1, 11, '2026-01-26 12:34:08'),
(1, 12, '2026-01-26 12:34:08'),
(1, 13, '2026-01-26 12:34:08'),
(2, 1, '2026-01-26 12:34:08'),
(2, 5, '2026-01-26 12:34:08'),
(2, 6, '2026-01-26 12:34:08');

-- --------------------------------------------------------

--
-- 表的结构 `users`
--

CREATE TABLE `users` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `phone` varchar(30) DEFAULT NULL,
  `nickname` varchar(100) DEFAULT NULL,
  `avatar_url` varchar(255) DEFAULT NULL,
  `gender` tinyint(4) DEFAULT NULL,
  `status` tinyint(4) DEFAULT NULL,
  `last_login_at` datetime DEFAULT NULL,
  `deleted` tinyint(4) DEFAULT '0',
  `created_at` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- 转存表中的数据 `users`
--

INSERT INTO `users` (`id`, `username`, `password_hash`, `email`, `phone`, `nickname`, `avatar_url`, `gender`, `status`, `last_login_at`, `deleted`, `created_at`, `updated_at`) VALUES
(1, 'admin', '$2b$12$8TRpJidJNZsDI3qcUHwSIOdoSgQtK8/FRmIQsPDU0.AC93hx36STS', 'admin@example.com', NULL, '管理员', NULL, 0, 1, '2026-01-26 15:38:28', 0, '2026-01-26 12:34:08', '2026-01-26 12:34:08'),
(2, 'demo', '$2b$12$dBN2JOIa1ZsaLstac.hb2uu3dVcqFL5WoTPR6XeE2iG3kA9pSX3mG', 'demo@example.com', NULL, '演示用户', NULL, 0, 1, NULL, 0, '2026-01-26 12:34:08', '2026-01-26 12:34:08'),
(3, 'xiaopeng', '$2a$10$5BePvEUwT5TzRjAC2Igy0uIuApreFq2MJ6OK7jNLpISfFx/cEkjRK', 'u1_new@example.com', '13000000000', 'U1-new-name', 'http://img/u1.png', 1, 1, '2026-01-26 15:33:12', 0, '2026-01-26 13:38:24', '2026-01-26 13:56:06');

-- --------------------------------------------------------

--
-- 表的结构 `user_roles`
--

CREATE TABLE `user_roles` (
  `user_id` bigint(20) UNSIGNED NOT NULL,
  `role_id` bigint(20) UNSIGNED NOT NULL,
  `created_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- 转存表中的数据 `user_roles`
--

INSERT INTO `user_roles` (`user_id`, `role_id`, `created_at`) VALUES
(1, 1, '2026-01-26 12:34:08'),
(2, 2, '2026-01-26 12:34:08'),
(3, 2, '2026-01-26 14:01:42');

-- --------------------------------------------------------

--
-- 表的结构 `user_tokens`
--

CREATE TABLE `user_tokens` (
  `id` bigint(20) UNSIGNED NOT NULL,
  `user_id` bigint(20) UNSIGNED NOT NULL,
  `jti` varchar(255) NOT NULL,
  `token_type` tinyint(4) DEFAULT NULL,
  `expired_at` datetime DEFAULT NULL,
  `revoked` tinyint(4) DEFAULT '0',
  `created_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- 转存表中的数据 `user_tokens`
--

INSERT INTO `user_tokens` (`id`, `user_id`, `jti`, `token_type`, `expired_at`, `revoked`, `created_at`) VALUES
(1, 1, '2d99fabe3b6f489c95817649d06932f5', 1, '2026-01-26 13:38:28', 0, '2026-01-26 13:08:28'),
(2, 1, '586976ee53da41aebb816976abb28e51', 2, '2026-02-02 13:08:28', 0, '2026-01-26 13:08:28'),
(3, 1, '93df56392a094eaab196a3828b5b3834', 1, '2026-01-26 13:49:45', 0, '2026-01-26 13:19:45'),
(4, 1, 'c2e4a9f23a3d4e05bdd393c5129676fe', 2, '2026-02-02 13:19:45', 1, '2026-01-26 13:19:45'),
(5, 1, 'dc6a18681c7f45d484771786bafa1e79', 1, '2026-01-26 13:54:35', 0, '2026-01-26 13:24:35'),
(6, 1, 'caae21d946eb4c7180428a0180e72fed', 2, '2026-02-02 13:24:35', 0, '2026-01-26 13:24:35'),
(7, 3, '79c2c7ebebb64ba191f08fddc3c3224b', 1, '2026-01-26 14:08:24', 0, '2026-01-26 13:38:24'),
(8, 3, '04e7e758af18469bbbeeb15a0b87aefc', 2, '2026-02-02 13:38:24', 0, '2026-01-26 13:38:24'),
(9, 3, 'ccc1119c3f86453692a0b585feac8ec5', 1, '2026-01-26 14:17:35', 1, '2026-01-26 13:47:35'),
(10, 3, '99df9b6bc1d442cf89b3bb4ee0dda7ec', 2, '2026-02-02 13:47:35', 0, '2026-01-26 13:47:35'),
(11, 1, 'c3c5f1fa0b124ebaa9686dc9b7d6baee', 1, '2026-01-26 14:27:32', 0, '2026-01-26 13:57:32'),
(12, 1, 'e1071ad065be4fe2b01905e94368c5fd', 2, '2026-02-02 13:57:32', 1, '2026-01-26 13:57:32'),
(13, 1, '671a47977bdc460291e49c490e5e1ae1', 1, '2026-01-26 14:45:04', 1, '2026-01-26 14:15:04'),
(14, 1, 'b8df464879e04c47a884d155ba401e4c', 2, '2026-02-02 14:15:04', 0, '2026-01-26 14:15:04'),
(15, 1, '18fb0c469317469f998b082a4acaf057', 1, '2026-01-26 14:49:32', 1, '2026-01-26 14:19:32'),
(16, 1, '62a410eb96c04cf4880d54282a2136b7', 2, '2026-02-02 14:19:32', 0, '2026-01-26 14:19:32'),
(17, 3, '81fc3ecd86e244238761c3941b41a58f', 1, '2026-01-26 15:01:54', 1, '2026-01-26 14:31:54'),
(18, 3, '97aefa15eeab4edba3f48cd135dd7bb3', 2, '2026-02-02 14:31:54', 0, '2026-01-26 14:31:54'),
(19, 3, 'd8b3a8bcc69e473b9ac2bd8c3d7fc200', 1, '2026-01-26 15:13:56', 0, '2026-01-26 14:43:56'),
(20, 3, '52ce967847f948f68f5bb435a3033842', 2, '2026-02-02 14:43:56', 0, '2026-01-26 14:43:56'),
(21, 1, '69065e1c584847809b6c4a0d633e11e5', 1, '2026-01-26 15:21:45', 0, '2026-01-26 14:51:45'),
(22, 1, '0269ca1c2ad9403a993bf7d680b95bb9', 2, '2026-02-02 14:51:45', 0, '2026-01-26 14:51:45'),
(23, 3, '88f66ed159f1441495e6213ec2cbf604', 1, '2026-01-26 15:23:34', 0, '2026-01-26 14:53:34'),
(24, 3, 'c8d07c94f45e4e20a04163007ff4afaf', 2, '2026-02-02 14:53:34', 0, '2026-01-26 14:53:34'),
(25, 1, 'c7076deb75814b00890b9e4cec4ce211', 1, '2026-01-26 15:26:04', 0, '2026-01-26 14:56:04'),
(26, 1, 'e87a5586211b4fdd96c1eb819efea8df', 2, '2026-02-02 14:56:04', 0, '2026-01-26 14:56:04'),
(27, 3, '874ffa8299274e72a331ad168af39849', 1, '2026-01-26 15:27:52', 0, '2026-01-26 14:57:52'),
(28, 3, '75cace203e1447259ccd7b1eaf85f20f', 2, '2026-02-02 14:57:52', 0, '2026-01-26 14:57:52'),
(29, 1, 'cc944e5acc9c4c1299a5650f6bdb22d4', 1, '2026-01-26 15:28:22', 0, '2026-01-26 14:58:22'),
(30, 1, 'c1559cc66cca45d490c25ae00af143c9', 2, '2026-02-02 14:58:22', 0, '2026-01-26 14:58:22'),
(31, 1, 'cffc7eacf8c24c1bbc7aa5aacddbeb82', 1, '2026-01-26 15:34:03', 0, '2026-01-26 15:04:03'),
(32, 1, 'f2371757eb1b4e5e921e35cbdc8ac9ab', 2, '2026-02-02 15:04:03', 0, '2026-01-26 15:04:03'),
(33, 1, 'a531cbe03b5744889560d7bf541f6862', 1, '2026-01-26 15:35:26', 0, '2026-01-26 15:05:26'),
(34, 1, '79fcedf43abb4a07813bf75e6836eb0a', 2, '2026-02-02 15:05:26', 0, '2026-01-26 15:05:26'),
(35, 3, '9e43bcb68af94d35a3e08d3e5286cc11', 1, '2026-01-26 15:36:25', 0, '2026-01-26 15:06:25'),
(36, 3, '8b5103f9f4e84f7da23467e50d05a25e', 2, '2026-02-02 15:06:25', 0, '2026-01-26 15:06:25'),
(37, 1, 'fc0f2c715a9b4049bc6437142c482d3d', 1, '2026-01-26 15:37:26', 0, '2026-01-26 15:07:26'),
(38, 1, 'dff1d145281e4f428ee47afc9fc0b9ec', 2, '2026-02-02 15:07:26', 0, '2026-01-26 15:07:26'),
(39, 3, '6e5dcf5866d1425a89851bfedb11a6ed', 1, '2026-01-26 15:37:39', 0, '2026-01-26 15:07:39'),
(40, 3, 'aa1a7448431e45a38bd865bf26b8ed0e', 2, '2026-02-02 15:07:39', 0, '2026-01-26 15:07:39'),
(41, 1, '0674433802cb4da1a5eb3078a8cdd5fb', 1, '2026-01-26 15:38:22', 0, '2026-01-26 15:08:22'),
(42, 1, '1980ac0104f54cc48ad274ef85cc0829', 2, '2026-02-02 15:08:22', 0, '2026-01-26 15:08:22'),
(43, 1, '7a4c1a66782740349cd441d48419bc7a', 1, '2026-01-26 15:39:24', 0, '2026-01-26 15:09:24'),
(44, 1, '8859c7d6687e40d9bcf09441ea6e895e', 2, '2026-02-02 15:09:24', 0, '2026-01-26 15:09:24'),
(45, 3, '0150a53637a747cfbbfd4c5d652d1071', 1, '2026-01-26 15:49:38', 0, '2026-01-26 15:19:38'),
(46, 3, 'ee18fae13a324ff790d0eda94eb51f29', 2, '2026-02-02 15:19:38', 0, '2026-01-26 15:19:38'),
(47, 1, 'f498209801764ea59254deb1ac92250e', 1, '2026-01-26 15:50:18', 0, '2026-01-26 15:20:18'),
(48, 1, 'ed085895704f4a3884bc9565aaa9851d', 2, '2026-02-02 15:20:18', 0, '2026-01-26 15:20:18'),
(49, 3, '52bfe81c247240cbacc54c09e02902a7', 1, '2026-01-26 15:50:27', 0, '2026-01-26 15:20:27'),
(50, 3, '7cffc97ca19441ae87bbde2c5204af3e', 2, '2026-02-02 15:20:27', 0, '2026-01-26 15:20:27'),
(51, 1, 'e5d236d2ce4c48858afcb1017eafb572', 1, '2026-01-26 15:51:01', 0, '2026-01-26 15:21:01'),
(52, 1, '81d4f564b45c42e7852865895c8ff232', 2, '2026-02-02 15:21:01', 0, '2026-01-26 15:21:01'),
(53, 3, '571075af08534d49b8dc802cd4d6294a', 1, '2026-01-26 16:03:12', 0, '2026-01-26 15:33:12'),
(54, 3, 'a936cc7749e943e8b70c23b7552fb72e', 2, '2026-02-02 15:33:12', 0, '2026-01-26 15:33:12'),
(55, 1, '53b205823b804bf796eddfb4ea1fb3bd', 1, '2026-01-26 16:08:28', 0, '2026-01-26 15:38:28'),
(56, 1, 'c75987f775f840c8b8d3acef0faf371b', 2, '2026-02-02 15:38:28', 0, '2026-01-26 15:38:28');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `addresses`
--
ALTER TABLE `addresses`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_addresses_user` (`user_id`);

--
-- Indexes for table `cart_items`
--
ALTER TABLE `cart_items`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_cart_items_user` (`user_id`),
  ADD KEY `idx_cart_items_product` (`product_id`);

--
-- Indexes for table `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_categories_parent` (`parent_id`);

--
-- Indexes for table `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_orders_order_no` (`order_no`),
  ADD KEY `idx_orders_user` (`user_id`),
  ADD KEY `idx_orders_status` (`status`);

--
-- Indexes for table `order_items`
--
ALTER TABLE `order_items`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_order_items_order` (`order_id`),
  ADD KEY `idx_order_items_product` (`product_id`);

--
-- Indexes for table `payments`
--
ALTER TABLE `payments`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_payments_pay_no` (`pay_no`),
  ADD KEY `idx_payments_order` (`order_id`),
  ADD KEY `idx_payments_user` (`user_id`);

--
-- Indexes for table `permissions`
--
ALTER TABLE `permissions`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_permissions_code` (`code`);

--
-- Indexes for table `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_products_category` (`category_id`);

--
-- Indexes for table `product_images`
--
ALTER TABLE `product_images`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_product_images_product` (`product_id`);

--
-- Indexes for table `refunds`
--
ALTER TABLE `refunds`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_refunds_refund_no` (`refund_no`),
  ADD KEY `idx_refunds_payment` (`payment_id`),
  ADD KEY `idx_refunds_order` (`order_id`);

--
-- Indexes for table `reviews`
--
ALTER TABLE `reviews`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_reviews_product` (`product_id`),
  ADD KEY `idx_reviews_user` (`user_id`);

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
  ADD KEY `idx_review_replies_user` (`user_id`);

--
-- Indexes for table `review_reports`
--
ALTER TABLE `review_reports`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_review_reports_review` (`review_id`),
  ADD KEY `idx_review_reports_user` (`user_id`);

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
  ADD KEY `idx_role_permissions_permission` (`permission_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_users_username` (`username`),
  ADD UNIQUE KEY `uk_users_email` (`email`),
  ADD UNIQUE KEY `uk_users_phone` (`phone`);

--
-- Indexes for table `user_roles`
--
ALTER TABLE `user_roles`
  ADD PRIMARY KEY (`user_id`,`role_id`),
  ADD KEY `idx_user_roles_role` (`role_id`);

--
-- Indexes for table `user_tokens`
--
ALTER TABLE `user_tokens`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_user_tokens_jti` (`jti`),
  ADD KEY `idx_user_tokens_user` (`user_id`);

--
-- 在导出的表使用AUTO_INCREMENT
--

--
-- 使用表AUTO_INCREMENT `addresses`
--
ALTER TABLE `addresses`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;
--
-- 使用表AUTO_INCREMENT `cart_items`
--
ALTER TABLE `cart_items`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;
--
-- 使用表AUTO_INCREMENT `categories`
--
ALTER TABLE `categories`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;
--
-- 使用表AUTO_INCREMENT `orders`
--
ALTER TABLE `orders`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;
--
-- 使用表AUTO_INCREMENT `order_items`
--
ALTER TABLE `order_items`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;
--
-- 使用表AUTO_INCREMENT `payments`
--
ALTER TABLE `payments`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;
--
-- 使用表AUTO_INCREMENT `permissions`
--
ALTER TABLE `permissions`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;
--
-- 使用表AUTO_INCREMENT `products`
--
ALTER TABLE `products`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;
--
-- 使用表AUTO_INCREMENT `product_images`
--
ALTER TABLE `product_images`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;
--
-- 使用表AUTO_INCREMENT `refunds`
--
ALTER TABLE `refunds`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;
--
-- 使用表AUTO_INCREMENT `reviews`
--
ALTER TABLE `reviews`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;
--
-- 使用表AUTO_INCREMENT `review_replies`
--
ALTER TABLE `review_replies`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;
--
-- 使用表AUTO_INCREMENT `review_reports`
--
ALTER TABLE `review_reports`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;
--
-- 使用表AUTO_INCREMENT `roles`
--
ALTER TABLE `roles`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;
--
-- 使用表AUTO_INCREMENT `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;
--
-- 使用表AUTO_INCREMENT `user_tokens`
--
ALTER TABLE `user_tokens`
  MODIFY `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=57;
--
-- 限制导出的表
--

--
-- 限制表 `addresses`
--
ALTER TABLE `addresses`
  ADD CONSTRAINT `fk_addresses_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- 限制表 `cart_items`
--
ALTER TABLE `cart_items`
  ADD CONSTRAINT `fk_cart_items_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  ADD CONSTRAINT `fk_cart_items_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- 限制表 `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `fk_orders_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- 限制表 `order_items`
--
ALTER TABLE `order_items`
  ADD CONSTRAINT `fk_order_items_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  ADD CONSTRAINT `fk_order_items_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`);

--
-- 限制表 `payments`
--
ALTER TABLE `payments`
  ADD CONSTRAINT `fk_payments_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  ADD CONSTRAINT `fk_payments_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- 限制表 `products`
--
ALTER TABLE `products`
  ADD CONSTRAINT `fk_products_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`);

--
-- 限制表 `product_images`
--
ALTER TABLE `product_images`
  ADD CONSTRAINT `fk_product_images_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`);

--
-- 限制表 `refunds`
--
ALTER TABLE `refunds`
  ADD CONSTRAINT `fk_refunds_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  ADD CONSTRAINT `fk_refunds_payment` FOREIGN KEY (`payment_id`) REFERENCES `payments` (`id`);

--
-- 限制表 `reviews`
--
ALTER TABLE `reviews`
  ADD CONSTRAINT `fk_reviews_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  ADD CONSTRAINT `fk_reviews_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- 限制表 `review_likes`
--
ALTER TABLE `review_likes`
  ADD CONSTRAINT `fk_review_likes_review` FOREIGN KEY (`review_id`) REFERENCES `reviews` (`id`),
  ADD CONSTRAINT `fk_review_likes_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- 限制表 `review_replies`
--
ALTER TABLE `review_replies`
  ADD CONSTRAINT `fk_review_replies_review` FOREIGN KEY (`review_id`) REFERENCES `reviews` (`id`),
  ADD CONSTRAINT `fk_review_replies_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- 限制表 `review_reports`
--
ALTER TABLE `review_reports`
  ADD CONSTRAINT `fk_review_reports_review` FOREIGN KEY (`review_id`) REFERENCES `reviews` (`id`),
  ADD CONSTRAINT `fk_review_reports_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- 限制表 `role_permissions`
--
ALTER TABLE `role_permissions`
  ADD CONSTRAINT `fk_role_permissions_permission` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`),
  ADD CONSTRAINT `fk_role_permissions_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`);

--
-- 限制表 `user_roles`
--
ALTER TABLE `user_roles`
  ADD CONSTRAINT `fk_user_roles_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
  ADD CONSTRAINT `fk_user_roles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- 限制表 `user_tokens`
--
ALTER TABLE `user_tokens`
  ADD CONSTRAINT `fk_user_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
