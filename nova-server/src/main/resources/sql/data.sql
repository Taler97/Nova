-- =============================================================
-- 测试数据
-- =============================================================

-- 1) employee — 管理员账号 admin / 密码 123456 (MD5)
INSERT IGNORE INTO employee (id, name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user)
VALUES (1, '管理员', 'admin', 'e10adc3949ba59abbe56e057f20f883e', '13800000000', '男', '110101199001011234', 1, NOW(), NOW(), 1, 1);

-- 2) category — 分类
INSERT IGNORE INTO category (id, type, name, sort, status, create_time, update_time, create_user, update_user)
VALUES (1, 1, '川菜',      1, 1, NOW(), NOW(), 1, 1),
       (2, 1, '饮品',      2, 1, NOW(), NOW(), 1, 1),
       (3, 2, '超值套餐',  3, 1, NOW(), NOW(), 1, 1);

-- 3) dish — 菜品
INSERT IGNORE INTO dish (id, name, category_id, price, image, description, status, create_time, update_time, create_user, update_user)
VALUES (1, '鱼香肉丝', 1, 28.00, 'https://bucketcc0102.oss-cn-beijing.aliyuncs.com/dish/1.jpeg', '经典川菜', 1, NOW(), NOW(), 1, 1),
       (2, '宫保鸡丁', 1, 32.00, 'https://bucketcc0102.oss-cn-beijing.aliyuncs.com/dish/2.jpeg', '麻辣鲜香', 1, NOW(), NOW(), 1, 1),
       (3, '可乐',     2,  3.00, 'https://bucketcc0102.oss-cn-beijing.aliyuncs.com/dish/3.jpeg', '冰镇可乐', 1, NOW(), NOW(), 1, 1),
       (4, '雪碧',     2,  3.00, 'https://bucketcc0102.oss-cn-beijing.aliyuncs.com/dish/4.jpeg', '冰镇雪碧', 1, NOW(), NOW(), 1, 1);

-- 4) dish_flavor — 口味
INSERT IGNORE INTO dish_flavor (id, dish_id, name, value)
VALUES (1, 1, '辣度', '微辣,中辣,特辣'),
       (2, 2, '辣度', '微辣,中辣'),
       (3, 3, '规格', '大杯,小杯'),
       (4, 4, '规格', '大杯,小杯');

-- 5) setmeal — 套餐
INSERT IGNORE INTO setmeal (id, name, category_id, price, status, description, image, create_time, update_time, create_user, update_user)
VALUES (1, '超值午餐', 3, 39.00, 1, '鱼香肉丝 + 可乐', 'https://bucketcc0102.oss-cn-beijing.aliyuncs.com/dish/s1.jpeg', NOW(), NOW(), 1, 1);

-- 6) setmeal_dish — 套餐关联菜品
INSERT IGNORE INTO setmeal_dish (id, setmeal_id, dish_id, copies)
VALUES (1, 1, 1, 1),
       (2, 1, 3, 1);

-- 7) user — 微信用户（密码 123456）
-- 如果下面的 INSERT 因 password 列缺失报错，请先在 MySQL 执行:
-- ALTER TABLE `user` ADD COLUMN `password` VARCHAR(64) DEFAULT NULL COMMENT '密码(BCrypt加密)' AFTER `phone`;
INSERT IGNORE INTO `user` (id, openid, name, phone, password, sex, avatar, create_time)
VALUES (1, 'oI8fG6kG8lX0BdF6eQ1pR2sT3uV4wX5y', '测试用户', '13800138000', '$2a$10$EUfIKi5oAMD5LJ8yJjSeV./.BBrlapqnsQ7bsZcknHpxWd/m0oig2', '男', NULL, NOW());

-- 8) address_book — 测试地址
INSERT IGNORE INTO address_book (id, user_id, consignee, phone, detail, is_default, create_time)
VALUES (1, 1, '张三', '13800138000', '北京市朝阳区望京街道xxx号', 1, NOW());

-- 9) shopping_cart — 购物车
INSERT IGNORE INTO shopping_cart (id, user_id, dish_id, setmeal_id, dish_flavor, number, amount, image, create_time)
VALUES (1, 1, 1, NULL, '微辣', 1, 28.00, 'https://bucketcc0102.oss-cn-beijing.aliyuncs.com/dish/1.jpeg', NOW());

-- 10) orders — 订单（已完成状态）
INSERT IGNORE INTO `orders` (id, number, status, user_id, amount, order_time, pay_method, consignee, phone, address, remark, cancel_reason, rejection_reason, cancel_time, estimated_delivery_time)
VALUES (1, '20260624001', 5, 1, 31.00, '2026-06-24 12:00:00', 1, '张三', '13800138000', '北京市朝阳区望京街道xxx号', '少辣', NULL, NULL, NULL, '2026-06-24 12:30:00');

-- 11) order_detail — 订单明细
INSERT IGNORE INTO order_detail (id, order_id, dish_id, number, amount)
VALUES (1, 1, 1, 1, 28.00),
       (2, 1, 3, 1,  3.00);
