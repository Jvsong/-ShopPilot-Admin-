package com.shop.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shop.dao.entity.Order;
import com.shop.dao.entity.OrderItem;
import com.shop.dao.entity.Product;
import com.shop.dao.entity.Role;
import com.shop.dao.entity.User;
import com.shop.dao.entity.UserRole;
import com.shop.dao.mapper.OrderItemMapper;
import com.shop.dao.mapper.OrderMapper;
import com.shop.dao.mapper.ProductMapper;
import com.shop.dao.mapper.RoleMapper;
import com.shop.dao.mapper.UserMapper;
import com.shop.dao.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        ensureOrderSchema();

        ensureRole("系统管理员", "ROLE_ADMIN", 1);
        ensureRole("商家", "ROLE_MERCHANT", 1);
        ensureRole("运营", "ROLE_OPERATOR", 1);
        ensureRole("客服", "ROLE_CUSTOMER_SERVICE", 1);
        ensureRole("普通用户", "ROLE_USER", 1);

        User admin = ensureUser("admin", "admin123", "admin@shop.com", "13800138000", 2);
        User merchant = ensureUser("merchant1", "admin123", "merchant1@shop.com", "13800138010", 3);
        User operator = ensureUser("operator1", "admin123", "operator1@shop.com", "13800138020", 1);
        User service = ensureUser("service1", "admin123", "service1@shop.com", "13800138030", 1);
        User customer1 = ensureUser("customer1", "admin123", "customer1@shop.com", "13800138001", 1);
        User customer2 = ensureUser("customer2", "admin123", "customer2@shop.com", "13800138002", 1);

        ensureUserRole(admin.getId(), "ROLE_ADMIN");
        ensureUserRole(merchant.getId(), "ROLE_MERCHANT");
        ensureUserRole(operator.getId(), "ROLE_OPERATOR");
        ensureUserRole(service.getId(), "ROLE_CUSTOMER_SERVICE");
        ensureUserRole(customer1.getId(), "ROLE_USER");
        ensureUserRole(customer2.getId(), "ROLE_USER");

        Product phone = ensureProduct("Demo Phone X", new BigDecimal("3999.00"), 200, merchant.getUsername());
        Product laptop = ensureProduct("Demo Laptop Pro", new BigDecimal("6999.00"), 80, merchant.getUsername());

        ensureOrder("ORDER-20240404", customer1.getId(), phone, 1, new BigDecimal("3999.00"), "{\"name\":\"customer1\",\"phone\":\"13800138001\",\"address\":\"Shanghai Road 1\"}");
        ensureOrder("ORDER-20240405", customer1.getId(), laptop, 2, new BigDecimal("6999.00"), "{\"name\":\"customer1\",\"phone\":\"13800138001\",\"address\":\"Shanghai Road 1\"}");
        ensureOrder("ORDER-20240406", customer2.getId(), phone, 3, new BigDecimal("3999.00"), "{\"name\":\"customer2\",\"phone\":\"13800138002\",\"address\":\"Beijing Road 8\"}");
        ensureOrder("ORDER-20240407", customer2.getId(), laptop, 4, new BigDecimal("6999.00"), "{\"name\":\"customer2\",\"phone\":\"13800138002\",\"address\":\"Beijing Road 8\"}");

        log.info("Dev demo data checked");
    }

    private void ensureOrderSchema() {
        ensureColumn("order", "shipping_company", "ALTER TABLE `order` ADD COLUMN `shipping_company` VARCHAR(100) NULL COMMENT '物流公司' AFTER `shipping_time`");
        ensureColumn("order", "tracking_number", "ALTER TABLE `order` ADD COLUMN `tracking_number` VARCHAR(100) NULL COMMENT '物流单号' AFTER `shipping_company`");
        ensureColumn("order_item", "create_time", "ALTER TABLE `order_item` ADD COLUMN `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间' AFTER `total_price`");
        ensureColumn("order_item", "update_time", "ALTER TABLE `order_item` ADD COLUMN `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER `create_time`");
    }

    private void ensureColumn(String tableName, String columnName, String ddl) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                Integer.class,
                tableName,
                columnName
        );
        if (count != null && count > 0) {
            return;
        }
        log.warn("Column {}.{} missing, applying dev schema patch", tableName, columnName);
        jdbcTemplate.execute(ddl);
    }

    private Role ensureRole(String name, String code, Integer status) {
        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getCode, code).eq(Role::getIsDeleted, 0));
        if (role != null) {
            return role;
        }
        role = new Role();
        role.setName(name);
        role.setCode(code);
        role.setDescription(name);
        role.setStatus(status);
        role.setIsDeleted(0);
        roleMapper.insert(role);
        return role;
    }

    private User ensureUser(String username, String password, String email, String phone, Integer userType) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username).eq(User::getIsDeleted, 0));
        if (user != null) {
            return user;
        }
        user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setPhone(phone);
        user.setStatus(1);
        user.setUserType(userType);
        user.setIsDeleted(0);
        user.setLoginFailCount(0);
        userMapper.insert(user);
        return user;
    }

    private void ensureUserRole(Long userId, String roleCode) {
        Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getCode, roleCode).eq(Role::getIsDeleted, 0));
        if (role == null) {
            return;
        }
        long count = userRoleMapper.selectCount(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId).eq(UserRole::getRoleId, role.getId()));
        if (count > 0) {
            return;
        }
        UserRole relation = new UserRole();
        relation.setUserId(userId);
        relation.setRoleId(role.getId());
        userRoleMapper.insert(relation);
    }

    private Product ensureProduct(String name, BigDecimal price, Integer stock, String createBy) {
        Product product = productMapper.selectOne(new LambdaQueryWrapper<Product>().eq(Product::getName, name).eq(Product::getIsDeleted, 0));
        if (product != null) {
            return product;
        }
        product = new Product();
        product.setName(name);
        product.setDescription(name);
        product.setCategoryId(null);
        product.setPrice(price);
        product.setOriginalPrice(price);
        product.setStock(stock);
        product.setSales(0);
        product.setStatus(1);
        product.setIsHot(0);
        product.setIsNew(1);
        product.setMainImage("https://example.com/demo.png");
        product.setIsDeleted(0);
        product.setCreateBy(createBy);
        product.setUpdateBy(createBy);
        productMapper.insert(product);
        return product;
    }

    private void ensureOrder(String orderNo, Long userId, Product product, Integer status, BigDecimal amount, String addressJson) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo).eq(Order::getIsDeleted, 0));
        if (order != null) {
            return;
        }

        order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(amount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setShippingFee(BigDecimal.ZERO);
        order.setActualAmount(amount);
        order.setStatus(status);
        order.setPaymentMethod(status >= 2 ? 1 : null);
        order.setShippingAddress(addressJson);
        order.setRemark("demo");
        order.setIsDeleted(0);
        orderMapper.insert(order);

        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setProductImage(product.getMainImage());
        item.setPrice(amount);
        item.setQuantity(1);
        item.setTotalPrice(amount);
        orderItemMapper.insert(item);
    }
}
