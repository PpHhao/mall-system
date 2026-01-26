-- Mall System schema (MySQL 8+), empty tables only
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  email VARCHAR(100),
  phone VARCHAR(30),
  nickname VARCHAR(100),
  avatar_url VARCHAR(255),
  gender TINYINT,
  status TINYINT,
  last_login_at DATETIME,
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_username (username),
  UNIQUE KEY uk_users_email (email),
  UNIQUE KEY uk_users_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS roles (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(50) NOT NULL,
  name VARCHAR(100),
  remark VARCHAR(255),
  created_at DATETIME,
  updated_at DATETIME,
  PRIMARY KEY (id),
  UNIQUE KEY uk_roles_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS permissions (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  code VARCHAR(100) NOT NULL,
  name VARCHAR(100),
  type TINYINT,
  http_method VARCHAR(20),
  http_path VARCHAR(255),
  created_at DATETIME,
  updated_at DATETIME,
  PRIMARY KEY (id),
  UNIQUE KEY uk_permissions_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_roles (
  user_id BIGINT UNSIGNED NOT NULL,
  role_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME,
  PRIMARY KEY (user_id, role_id),
  KEY idx_user_roles_role (role_id),
  CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS role_permissions (
  role_id BIGINT UNSIGNED NOT NULL,
  permission_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME,
  PRIMARY KEY (role_id, permission_id),
  KEY idx_role_permissions_permission (permission_id),
  CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id),
  CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_tokens (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  jti VARCHAR(255) NOT NULL,
  token_type TINYINT,
  expired_at DATETIME,
  revoked TINYINT DEFAULT 0,
  created_at DATETIME,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_tokens_jti (jti),
  KEY idx_user_tokens_user (user_id),
  CONSTRAINT fk_user_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS categories (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  parent_id BIGINT UNSIGNED DEFAULT 0,
  name VARCHAR(100) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_categories_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS products (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  category_id BIGINT UNSIGNED NOT NULL,
  name VARCHAR(200) NOT NULL,
  subtitle VARCHAR(255),
  description TEXT,
  price DECIMAL(18,2) NOT NULL,
  stock INT,
  status TINYINT,
  created_at DATETIME,
  updated_at DATETIME,
  PRIMARY KEY (id),
  KEY idx_products_category (category_id),
  CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_images (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  product_id BIGINT UNSIGNED NOT NULL,
  url VARCHAR(500),
  sort INT,
  PRIMARY KEY (id),
  KEY idx_product_images_product (product_id),
  CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cart_items (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  product_id BIGINT UNSIGNED NOT NULL,
  quantity INT NOT NULL,
  checked TINYINT,
  price_at_add DECIMAL(18,2),
  created_at DATETIME,
  updated_at DATETIME,
  PRIMARY KEY (id),
  KEY idx_cart_items_user (user_id),
  KEY idx_cart_items_product (product_id),
  CONSTRAINT fk_cart_items_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS addresses (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  receiver_name VARCHAR(50) NOT NULL,
  receiver_phone VARCHAR(20) NOT NULL,
  province VARCHAR(50) NOT NULL,
  city VARCHAR(50) NOT NULL,
  district VARCHAR(50) NOT NULL,
  detail VARCHAR(255) NOT NULL,
  postal_code VARCHAR(20),
  is_default TINYINT DEFAULT 0,
  PRIMARY KEY (id),
  KEY idx_addresses_user (user_id),
  CONSTRAINT fk_addresses_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS orders (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  order_no VARCHAR(100) NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  status TINYINT,
  total_amount DECIMAL(18,2),
  freight_amount DECIMAL(18,2),
  pay_amount DECIMAL(18,2),
  pay_method TINYINT,
  paid_at DATETIME,
  shipped_at DATETIME,
  completed_at DATETIME,
  canceled_at DATETIME,
  cancel_reason VARCHAR(255),
  receiver_name VARCHAR(50),
  receiver_phone VARCHAR(20),
  province VARCHAR(50),
  city VARCHAR(50),
  district VARCHAR(50),
  detail VARCHAR(255),
  postal_code VARCHAR(20),
  remark VARCHAR(255),
  deleted TINYINT DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  PRIMARY KEY (id),
  UNIQUE KEY uk_orders_order_no (order_no),
  KEY idx_orders_user (user_id),
  KEY idx_orders_status (status),
  CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_items (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  order_id BIGINT UNSIGNED NOT NULL,
  product_id BIGINT UNSIGNED NOT NULL,
  product_name VARCHAR(200),
  product_image VARCHAR(500),
  unit_price DECIMAL(18,2),
  quantity INT,
  total_price DECIMAL(18,2),
  created_at DATETIME,
  PRIMARY KEY (id),
  KEY idx_order_items_order (order_id),
  KEY idx_order_items_product (product_id),
  CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id),
  CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS payments (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  order_id BIGINT UNSIGNED NOT NULL,
  order_no VARCHAR(100) NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  pay_no VARCHAR(100) NOT NULL,
  pay_method TINYINT,
  amount DECIMAL(18,2),
  status TINYINT,
  created_at DATETIME,
  updated_at DATETIME,
  paid_at DATETIME,
  PRIMARY KEY (id),
  UNIQUE KEY uk_payments_pay_no (pay_no),
  KEY idx_payments_order (order_id),
  KEY idx_payments_user (user_id),
  CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders (id),
  CONSTRAINT fk_payments_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS refunds (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  payment_id BIGINT UNSIGNED NOT NULL,
  order_id BIGINT UNSIGNED NOT NULL,
  refund_no VARCHAR(100) NOT NULL,
  amount DECIMAL(18,2),
  status TINYINT,
  reason VARCHAR(255),
  created_at DATETIME,
  updated_at DATETIME,
  processed_at DATETIME,
  processed_by BIGINT UNSIGNED,
  PRIMARY KEY (id),
  UNIQUE KEY uk_refunds_refund_no (refund_no),
  KEY idx_refunds_payment (payment_id),
  KEY idx_refunds_order (order_id),
  CONSTRAINT fk_refunds_payment FOREIGN KEY (payment_id) REFERENCES payments (id),
  CONSTRAINT fk_refunds_order FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reviews (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  order_id BIGINT UNSIGNED NOT NULL,
  order_item_id BIGINT UNSIGNED,
  product_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  rating TINYINT,
  content TEXT,
  images TEXT,
  status TINYINT,
  created_at DATETIME,
  updated_at DATETIME,
  PRIMARY KEY (id),
  KEY idx_reviews_product (product_id),
  KEY idx_reviews_user (user_id),
  CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products (id),
  CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS review_likes (
  review_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME,
  PRIMARY KEY (review_id, user_id),
  KEY idx_review_likes_user (user_id),
  CONSTRAINT fk_review_likes_review FOREIGN KEY (review_id) REFERENCES reviews (id),
  CONSTRAINT fk_review_likes_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS review_reports (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  review_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  reason VARCHAR(255),
  status TINYINT,
  created_at DATETIME,
  PRIMARY KEY (id),
  KEY idx_review_reports_review (review_id),
  KEY idx_review_reports_user (user_id),
  CONSTRAINT fk_review_reports_review FOREIGN KEY (review_id) REFERENCES reviews (id),
  CONSTRAINT fk_review_reports_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS review_replies (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  review_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  content TEXT,
  created_at DATETIME,
  PRIMARY KEY (id),
  KEY idx_review_replies_review (review_id),
  KEY idx_review_replies_user (user_id),
  CONSTRAINT fk_review_replies_review FOREIGN KEY (review_id) REFERENCES reviews (id),
  CONSTRAINT fk_review_replies_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET FOREIGN_KEY_CHECKS=1;
