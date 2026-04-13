-- 电商后台管理系统数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS `shop_system` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `shop_system`;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '加密密码',
    `email` VARCHAR(100) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `avatar` VARCHAR(255) COMMENT '头像',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `user_type` TINYINT DEFAULT 1 COMMENT '用户类型: 1-普通用户, 2-管理员',
    `login_fail_count` INT DEFAULT 0 COMMENT '登录失败次数',
    `lock_time` DATETIME COMMENT '锁定时间',
    `last_login_time` DATETIME COMMENT '最后登录时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除: 0-未删除, 1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    `update_by` VARCHAR(50) DEFAULT 'system' COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_status` (`status`),
    KEY `idx_user_type` (`user_type`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS `role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `description` VARCHAR(255) COMMENT '角色描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除: 0-未删除, 1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    `update_by` VARCHAR(50) DEFAULT 'system' COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS `permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    `name` VARCHAR(50) NOT NULL COMMENT '权限名称',
    `code` VARCHAR(100) NOT NULL COMMENT '权限编码',
    `type` TINYINT NOT NULL COMMENT '权限类型: 1-菜单, 2-操作',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父权限ID',
    `path` VARCHAR(255) COMMENT '菜单路径',
    `icon` VARCHAR(50) COMMENT '菜单图标',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除: 0-未删除, 1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    `update_by` VARCHAR(50) DEFAULT 'system' COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户角色关系表
CREATE TABLE IF NOT EXISTS `user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关系表';

-- 角色权限关系表
CREATE TABLE IF NOT EXISTS `role_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_permission_id` (`permission_id`),
    FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关系表';

-- 商品分类表
CREATE TABLE IF NOT EXISTS `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `description` VARCHAR(500) COMMENT '分类描述',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID',
    `level` INT DEFAULT 1 COMMENT '分类层级',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除: 0-未删除, 1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    `update_by` VARCHAR(50) DEFAULT 'system' COMMENT '更新人',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_status` (`status`),
    KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 商品表
CREATE TABLE IF NOT EXISTS `product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `description` TEXT COMMENT '商品描述',
    `category_id` BIGINT COMMENT '分类ID',
    `price` DECIMAL(10,2) NOT NULL COMMENT '商品价格',
    `original_price` DECIMAL(10,2) COMMENT '原价',
    `stock` INT DEFAULT 0 COMMENT '库存数量',
    `sales` INT DEFAULT 0 COMMENT '销量',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-下架, 1-上架',
    `is_hot` TINYINT DEFAULT 0 COMMENT '是否热销: 0-否, 1-是',
    `is_new` TINYINT DEFAULT 0 COMMENT '是否新品: 0-否, 1-是',
    `main_image` VARCHAR(255) COMMENT '主图',
    `images` TEXT COMMENT '商品图片(JSON数组)',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除: 0-未删除, 1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    `update_by` VARCHAR(50) DEFAULT 'system' COMMENT '更新人',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_price` (`price`),
    KEY `idx_create_time` (`create_time`),
    FULLTEXT KEY `idx_name` (`name`),
    FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 订单表（注意：order是SQL关键字，需要用反引号）
CREATE TABLE IF NOT EXISTS `order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    `discount_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '优惠金额',
    `shipping_fee` DECIMAL(10,2) DEFAULT 0 COMMENT '运费',
    `actual_amount` DECIMAL(10,2) NOT NULL COMMENT '实际支付金额',
    `status` TINYINT NOT NULL COMMENT '订单状态: 1-待付款, 2-待发货, 3-已发货, 4-已完成, 5-已取消',
    `payment_method` TINYINT COMMENT '支付方式: 1-支付宝, 2-微信, 3-银行卡',
    `payment_time` DATETIME COMMENT '支付时间',
    `shipping_time` DATETIME COMMENT '发货时间',
    `receive_time` DATETIME COMMENT '收货时间',
    `cancel_time` DATETIME COMMENT '取消时间',
    `cancel_reason` VARCHAR(255) COMMENT '取消原因',
    `shipping_address` TEXT COMMENT '收货地址(JSON)',
    `remark` VARCHAR(500) COMMENT '订单备注',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除: 0-未删除, 1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    `update_by` VARCHAR(50) DEFAULT 'system' COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单商品关系表
CREATE TABLE IF NOT EXISTS `order_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `product_image` VARCHAR(255) COMMENT '商品图片',
    `price` DECIMAL(10,2) NOT NULL COMMENT '商品价格',
    `quantity` INT NOT NULL COMMENT '购买数量',
    `total_price` DECIMAL(10,2) NOT NULL COMMENT '总价',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_product_id` (`product_id`),
    FOREIGN KEY (`order_id`) REFERENCES `order` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品关系表';

-- 商品属性表
CREATE TABLE IF NOT EXISTS `product_attribute` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '属性ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `attribute_name` VARCHAR(100) NOT NULL COMMENT '属性名称',
    `attribute_value` VARCHAR(500) NOT NULL COMMENT '属性值',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除: 0-未删除, 1-已删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `create_by` VARCHAR(50) DEFAULT 'system' COMMENT '创建人',
    `update_by` VARCHAR(50) DEFAULT 'system' COMMENT '更新人',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_sort_order` (`sort_order`),
    FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品属性表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS `system_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT NOT NULL COMMENT '配置值',
    `description` VARCHAR(500) COMMENT '配置描述',
    `is_system` TINYINT DEFAULT 0 COMMENT '是否系统配置: 0-否, 1-是',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 初始化角色数据
INSERT INTO `role` (`name`, `code`, `description`, `status`, `create_by`, `update_by`)
SELECT '系统管理员', 'ROLE_ADMIN', '系统全量管理权限', 1, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `role` WHERE `code` = 'ROLE_ADMIN');

INSERT INTO `role` (`name`, `code`, `description`, `status`, `create_by`, `update_by`)
SELECT '商家', 'ROLE_MERCHANT', '自己店铺商品与订单管理权限', 1, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `role` WHERE `code` = 'ROLE_MERCHANT');

INSERT INTO `role` (`name`, `code`, `description`, `status`, `create_by`, `update_by`)
SELECT '普通用户', 'ROLE_USER', '商品浏览与本人订单权限', 1, 'system', 'system'
WHERE NOT EXISTS (SELECT 1 FROM `role` WHERE `code` = 'ROLE_USER');
