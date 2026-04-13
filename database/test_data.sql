-- 电商后台管理系统测试数据
-- 注意：密码已使用BCrypt加密（明文密码均为：admin123）

SET FOREIGN_KEY_CHECKS = 0;
SET NAMES utf8mb4;
USE `shop_system`;

-- 清空旧数据（注意外键约束）
DELETE FROM `order_item`;
DELETE FROM `order`;
DELETE FROM `product`;
DELETE FROM `category`;
DELETE FROM `role_permission`;
DELETE FROM `user_role`;
DELETE FROM `permission`;
DELETE FROM `role`;
DELETE FROM `user`;

-- 插入管理员用户
-- 密码：admin123（明文存储）
INSERT INTO `user` (`username`, `password`, `email`, `phone`, `avatar`, `status`, `user_type`) VALUES
('admin', 'admin123', 'admin@shop.com', '13800138000', NULL, 1, 2),
('user1', 'admin123', 'user1@shop.com', '13800138001', NULL, 1, 1),
('user2', 'admin123', 'user2@shop.com', '13800138002', NULL, 1, 1);

-- 插入角色
INSERT INTO `role` (`name`, `code`, `description`, `status`) VALUES
('系统管理员', 'ROLE_ADMIN', '拥有系统所有权限', 1),
('运营管理员', 'ROLE_OPERATOR', '负责商品和订单管理', 1),
('客服人员', 'ROLE_CUSTOMER_SERVICE', '处理客户咨询和订单问题', 1);

-- 插入权限
INSERT INTO `permission` (`name`, `code`, `type`, `parent_id`, `path`, `icon`, `sort_order`, `status`) VALUES
-- 一级菜单
('仪表盘', 'dashboard', 1, 0, '/dashboard', 'el-icon-monitor', 1, 1),
('商品管理', 'product', 1, 0, '/product', 'el-icon-goods', 2, 1),
('订单管理', 'order', 1, 0, '/order', 'el-icon-tickets', 3, 1),
('用户管理', 'user', 1, 0, '/user', 'el-icon-user', 4, 1),
('权限管理', 'permission', 1, 0, '/permission', 'el-icon-lock', 5, 1),

-- 仪表盘子菜单
('首页', 'dashboard:home', 1, 1, '/dashboard/home', 'el-icon-house', 1, 1),
('数据分析', 'dashboard:analysis', 1, 1, '/dashboard/analysis', 'el-icon-data-analysis', 2, 1),

-- 商品管理子菜单
('商品列表', 'product:list', 1, 2, '/product/list', 'el-icon-list', 1, 1),
('添加商品', 'product:add', 1, 2, '/product/add', 'el-icon-plus', 2, 1),
('商品分类', 'product:category', 1, 2, '/product/category', 'el-icon-folder', 3, 1),

-- 订单管理子菜单
('订单列表', 'order:list', 1, 3, '/order/list', 'el-icon-list', 1, 1),
('订单统计', 'order:statistics', 1, 3, '/order/statistics', 'el-icon-data-line', 2, 1),

-- 用户管理子菜单
('用户列表', 'user:list', 1, 4, '/user/list', 'el-icon-user', 1, 1),
('用户分组', 'user:group', 1, 4, '/user/group', 'el-icon-user-group', 2, 1),

-- 权限管理子菜单
('角色管理', 'permission:role', 1, 5, '/permission/role', 'el-icon-key', 1, 1),
('权限列表', 'permission:list', 1, 5, '/permission/list', 'el-icon-set-up', 2, 1),

-- 操作权限
('查看商品', 'product:view', 2, 2, NULL, NULL, 10, 1),
('添加商品', 'product:create', 2, 2, NULL, NULL, 11, 1),
('编辑商品', 'product:edit', 2, 2, NULL, NULL, 12, 1),
('删除商品', 'product:delete', 2, 2, NULL, NULL, 13, 1),
('查看订单', 'order:view', 2, 3, NULL, NULL, 20, 1),
('处理订单', 'order:process', 2, 3, NULL, NULL, 21, 1),
('查看用户', 'user:view', 2, 4, NULL, NULL, 30, 1),
('编辑用户', 'user:edit', 2, 4, NULL, NULL, 31, 1);

-- 分配用户角色
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES
(1, 1),  -- admin -> 系统管理员
(2, 2),  -- user1 -> 运营管理员
(3, 3);  -- user2 -> 客服人员

-- 分配角色权限（系统管理员拥有所有权限）
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT 1, id FROM `permission`;

-- 运营管理员权限（商品和订单管理相关）
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES
(2, 1), (2, 2), (2, 3),  -- 仪表盘、商品管理、订单管理菜单
(2, 6), (2, 7),          -- 仪表盘子菜单
(2, 8), (2, 9), (2, 10), -- 商品管理子菜单
(2, 11), (2, 12),        -- 订单管理子菜单
(2, 15), (2, 16), (2, 17), (2, 18),  -- 商品操作权限
(2, 19), (2, 20);        -- 订单操作权限

-- 客服人员权限（订单查看和用户查看）
INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES
(3, 1), (3, 3), (3, 4),  -- 仪表盘、订单管理、用户管理菜单
(3, 6), (3, 11), (3, 13), -- 仪表盘首页、订单列表、用户列表
(3, 19), (3, 22);        -- 查看订单、查看用户

-- 插入商品分类
INSERT INTO `category` (`name`, `description`, `parent_id`, `level`, `sort_order`, `status`) VALUES
('电子产品', '手机、电脑、平板等电子设备', 0, 1, 1, 1),
('服装鞋帽', '男女服装、鞋帽配饰', 0, 1, 2, 1),
('图书音像', '图书、音像制品、学习资料', 0, 1, 3, 1),
('手机', '智能手机、功能手机', 1, 2, 1, 1),
('笔记本电脑', '轻薄本、游戏本、商务本', 1, 2, 2, 1),
('男装', '男士服装', 2, 2, 1, 1),
('女装', '女士服装', 2, 2, 2, 1);

-- 插入测试商品
INSERT INTO `product` (`name`, `description`, `category_id`, `price`, `original_price`, `stock`, `sales`, `status`, `is_hot`, `is_new`, `main_image`) VALUES
('iPhone 15 Pro', '苹果最新旗舰手机，A17 Pro芯片，钛金属边框', 4, 8999.00, 9999.00, 100, 50, 1, 1, 1, 'https://example.com/iphone15.jpg'),
('华为 Mate 60', '华为旗舰手机，麒麟9000S芯片，卫星通话', 4, 6999.00, 7999.00, 150, 80, 1, 1, 1, 'https://example.com/mate60.jpg'),
('小米 14 Pro', '小米旗舰手机，骁龙8 Gen3，徕卡影像', 4, 4999.00, 5499.00, 200, 120, 1, 1, 0, 'https://example.com/mi14.jpg'),
('MacBook Pro 16英寸', '苹果专业笔记本电脑，M3 Max芯片，120Hz ProMotion', 5, 19999.00, 21999.00, 50, 20, 1, 1, 1, 'https://example.com/macbook.jpg'),
('ThinkPad X1 Carbon', '联想商务笔记本电脑，轻薄便携，长续航', 5, 12999.00, 13999.00, 80, 40, 1, 0, 0, 'https://example.com/thinkpad.jpg'),
('男士休闲衬衫', '纯棉材质，舒适透气，多色可选', 6, 199.00, 299.00, 500, 200, 1, 1, 0, 'https://example.com/shirt.jpg'),
('女士连衣裙', '春夏新款，雪纺面料，优雅大方', 7, 299.00, 399.00, 300, 150, 1, 1, 1, 'https://example.com/dress.jpg'),
('Java编程思想', 'Java编程经典书籍，第5版', 3, 128.00, 158.00, 1000, 500, 1, 1, 0, 'https://example.com/java.jpg'),
('Python机器学习', 'Python机器学习实战指南', 3, 89.00, 99.00, 800, 300, 1, 0, 1, 'https://example.com/python.jpg');

-- 插入测试订单
INSERT INTO `order` (`order_no`, `user_id`, `total_amount`, `discount_amount`, `shipping_fee`, `actual_amount`, `status`, `payment_method`, `shipping_address`, `remark`) VALUES
('202304040001', 2, 8999.00, 0, 0, 8999.00, 4, 1, '{"name":"张三","phone":"13800138001","address":"北京市海淀区中关村大街1号"}', '尽快发货'),
('202304040002', 2, 12999.00, 1000, 20, 12019.00, 3, 2, '{"name":"张三","phone":"13800138001","address":"北京市海淀区中关村大街1号"}', '发票需要'),
('202304040003', 3, 199.00, 0, 10, 209.00, 2, 1, '{"name":"李四","phone":"13800138002","address":"上海市浦东新区张江高科"}', ''),
('202304040004', 3, 299.00, 20, 10, 289.00, 1, NULL, '{"name":"李四","phone":"13800138002","address":"上海市浦东新区张江高科"}', '等待付款'),
('202304040005', 2, 128.00, 0, 5, 133.00, 5, NULL, '{"name":"张三","phone":"13800138001","address":"北京市海淀区中关村大街1号"}', '用户取消');

-- 插入订单商品
INSERT INTO `order_item` (`order_id`, `product_id`, `product_name`, `product_image`, `price`, `quantity`, `total_price`) VALUES
(1, 1, 'iPhone 15 Pro', 'https://example.com/iphone15.jpg', 8999.00, 1, 8999.00),
(2, 5, 'ThinkPad X1 Carbon', 'https://example.com/thinkpad.jpg', 12999.00, 1, 12999.00),
(3, 6, '男士休闲衬衫', 'https://example.com/shirt.jpg', 199.00, 1, 199.00),
(4, 7, '女士连衣裙', 'https://example.com/dress.jpg', 299.00, 1, 299.00),
(5, 8, 'Java编程思想', 'https://example.com/java.jpg', 128.00, 1, 128.00);

-- 插入系统配置
INSERT INTO `system_config` (`config_key`, `config_value`, `description`, `is_system`, `status`) VALUES
('site_name', '电商后台管理系统', '网站名称', 1, 1),
('site_logo', '', '网站Logo', 1, 1),
('site_copyright', '© 2026 电商后台管理系统', '版权信息', 1, 1),
('order_timeout', '1800', '订单超时时间（秒）', 1, 1),
('product_review', '1', '是否开启商品评价', 1, 1);

-- 输出初始化完成信息
SELECT '数据库初始化完成！' AS '消息';
SET FOREIGN_KEY_CHECKS = 1;