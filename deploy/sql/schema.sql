CREATE DATABASE IF NOT EXISTS sky_take_out;
USE sky_take_out;

-- =============================================================
-- 1) employee — 员工表
-- =============================================================

CREATE TABLE IF NOT EXISTS employee (
    id          BIGINT        PRIMARY KEY AUTO_INCREMENT COMMENT '员工ID',
    name        VARCHAR(32)   NOT NULL       COMMENT '员工姓名',
    username    VARCHAR(32)   NOT NULL UNIQUE COMMENT '登录账号',
    password    VARCHAR(64)   NOT NULL DEFAULT 'e10adc3949ba59abbe56e057f20f883e' COMMENT '密码(MD5)',
    phone       VARCHAR(11)                  COMMENT '手机号',
    sex         VARCHAR(2)                   COMMENT '性别',
    id_number   VARCHAR(18)                  COMMENT '身份证号',
    status      INT           DEFAULT 1      COMMENT '状态 1启用 0禁用',
    create_time DATETIME                     COMMENT '创建时间',
    update_time DATETIME                     COMMENT '更新时间',
    create_user BIGINT                       COMMENT '创建人',
    update_user BIGINT                       COMMENT '修改人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

-- =============================================================
-- 2) category — 分类表
-- =============================================================

CREATE TABLE IF NOT EXISTS category (
    id          BIGINT        PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    type        INT           NOT NULL       COMMENT '类型 1菜品分类 2套餐分类',
    name        VARCHAR(32)   NOT NULL UNIQUE COMMENT '分类名称',
    sort        INT           NOT NULL DEFAULT 0 COMMENT '排序序号',
    status      INT           DEFAULT 1      COMMENT '状态 1启用 0禁用',
    create_time DATETIME                     COMMENT '创建时间',
    update_time DATETIME                     COMMENT '更新时间',
    create_user BIGINT                       COMMENT '创建人',
    update_user BIGINT                       COMMENT '修改人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- =============================================================
-- 3) dish — 菜品表
-- =============================================================

CREATE TABLE IF NOT EXISTS dish (
    id          BIGINT        PRIMARY KEY AUTO_INCREMENT COMMENT '菜品ID',
    name        VARCHAR(32)   NOT NULL UNIQUE COMMENT '菜品名称',
    category_id BIGINT        NOT NULL       COMMENT '所属分类ID',
    price       DECIMAL(10,2) NOT NULL       COMMENT '价格',
    image       VARCHAR(500)                 COMMENT '图片URL',
    description VARCHAR(500)                 COMMENT '描述',
    status      INT           DEFAULT 1      COMMENT '状态 1起售 0停售',
    create_time DATETIME                     COMMENT '创建时间',
    update_time DATETIME                     COMMENT '更新时间',
    create_user BIGINT                       COMMENT '创建人',
    update_user BIGINT                       COMMENT '修改人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品表';

-- =============================================================
-- 4) dish_flavor — 菜品口味表
-- =============================================================

CREATE TABLE IF NOT EXISTS dish_flavor (
    id      BIGINT        PRIMARY KEY AUTO_INCREMENT COMMENT '口味ID',
    dish_id BIGINT        NOT NULL       COMMENT '关联菜品ID',
    name    VARCHAR(32)                  COMMENT '口味名称',
    value   VARCHAR(255)                 COMMENT '口味值'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品口味表';

-- =============================================================
-- 5) setmeal — 套餐表
-- =============================================================

CREATE TABLE IF NOT EXISTS setmeal (
    id          BIGINT        PRIMARY KEY AUTO_INCREMENT COMMENT '套餐ID',
    name        VARCHAR(32)   NOT NULL UNIQUE COMMENT '套餐名称',
    category_id BIGINT        NOT NULL       COMMENT '所属分类',
    price       DECIMAL(10,2) NOT NULL       COMMENT '价格',
    status      INT           DEFAULT 1      COMMENT '状态 1起售 0停售',
    description VARCHAR(500)                 COMMENT '描述',
    image       VARCHAR(500)                 COMMENT '图片URL',
    create_time DATETIME                     COMMENT '创建时间',
    update_time DATETIME                     COMMENT '更新时间',
    create_user BIGINT                       COMMENT '创建人',
    update_user BIGINT                       COMMENT '修改人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐表';

-- =============================================================
-- 6) setmeal_dish — 套餐菜品关系表
-- =============================================================

CREATE TABLE IF NOT EXISTS setmeal_dish (
    id         BIGINT        PRIMARY KEY AUTO_INCREMENT COMMENT '关系ID',
    setmeal_id BIGINT        NOT NULL       COMMENT '套餐ID',
    dish_id    BIGINT        NOT NULL       COMMENT '菜品ID',
    copies     INT           DEFAULT 1      COMMENT '份数'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐菜品关系表';

-- =============================================================
-- 7) user — 用户表（C端微信用户）
-- =============================================================

CREATE TABLE IF NOT EXISTS `user` (
    id          BIGINT        PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    openid      VARCHAR(100)  NOT NULL UNIQUE COMMENT '微信OpenID',
    name        VARCHAR(50)                  COMMENT '用户昵称',
    phone       VARCHAR(11)                  COMMENT '手机号',
    sex         VARCHAR(2)                   COMMENT '性别',
    avatar      VARCHAR(500)                 COMMENT '头像URL',
    create_time DATETIME                     COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `password` VARCHAR(64) DEFAULT NULL COMMENT '密码(BCrypt加密)' AFTER `phone`;

-- =============================================================
-- 8) address_book — 地址表
-- =============================================================

CREATE TABLE IF NOT EXISTS address_book (
    id          BIGINT        PRIMARY KEY AUTO_INCREMENT COMMENT '地址ID',
    user_id     BIGINT        NOT NULL       COMMENT '用户ID',
    consignee   VARCHAR(50)                  COMMENT '收货人',
    phone       VARCHAR(11)                  COMMENT '手机号',
    detail      VARCHAR(200)                 COMMENT '详细地址',
    is_default  INT           DEFAULT 0      COMMENT '是否默认 1是 0否',
    create_time DATETIME                     COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地址表';

-- =============================================================
-- 9) shopping_cart — 购物车表
-- =============================================================

CREATE TABLE IF NOT EXISTS shopping_cart (
    id          BIGINT        PRIMARY KEY AUTO_INCREMENT COMMENT '购物车ID',
    user_id     BIGINT        NOT NULL       COMMENT '用户ID',
    dish_id     BIGINT                       COMMENT '菜品ID',
    setmeal_id  BIGINT                       COMMENT '套餐ID',
    dish_flavor VARCHAR(50)                  COMMENT '口味选择',
    number      INT           DEFAULT 1      COMMENT '数量',
    amount      DECIMAL(10,2)                COMMENT '金额',
    image       VARCHAR(500)                 COMMENT '图片',
    create_time DATETIME                     COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- =============================================================
-- 10) orders — 订单表
-- =============================================================

CREATE TABLE IF NOT EXISTS `orders` (
    id                     BIGINT        PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    number                 VARCHAR(50)   NOT NULL UNIQUE COMMENT '订单号',
    status                 INT           DEFAULT 1      COMMENT '状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消',
    user_id                BIGINT        NOT NULL       COMMENT '用户ID',
    amount                 DECIMAL(10,2)                COMMENT '订单金额',
    order_time             DATETIME                     COMMENT '下单时间',
    pay_method             INT                          COMMENT '支付方式 1微信支付',
    consignee              VARCHAR(50)                  COMMENT '收货人',
    phone                  VARCHAR(11)                  COMMENT '联系电话',
    address                VARCHAR(255)                 COMMENT '收货地址',
    remark                 VARCHAR(500)                 COMMENT '备注',
    cancel_reason          VARCHAR(255)                 COMMENT '取消原因',
    rejection_reason       VARCHAR(255)                 COMMENT '拒单原因',
    cancel_time            DATETIME                     COMMENT '取消时间',
    estimated_delivery_time DATETIME                    COMMENT '预计送达时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- =============================================================
-- 11) order_detail — 订单明细表
-- =============================================================

CREATE TABLE IF NOT EXISTS order_detail (
    id         BIGINT        PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
    order_id   BIGINT        NOT NULL       COMMENT '订单ID',
    dish_id    BIGINT        NOT NULL       COMMENT '菜品ID',
    number     INT                          COMMENT '数量',
    amount     DECIMAL(10,2)                COMMENT '明细金额',
    name       VARCHAR(32)                  COMMENT '菜品名称',
    image      VARCHAR(500)                 COMMENT '菜品图片',
    dish_flavor VARCHAR(50)                 COMMENT '菜品口味'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';
