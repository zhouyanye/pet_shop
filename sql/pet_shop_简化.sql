-- =====================================================
-- 萌宠优选 宠物用品购物网站 数据库设计
-- MySQL 8.0+
-- =====================================================

CREATE DATABASE IF NOT EXISTS pet_shop DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE pet_shop;

-- -----------------------------------------------------
-- 用户表
-- -----------------------------------------------------
CREATE TABLE `user` (
                        `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户主键ID',
                        `username`      VARCHAR(50)  NOT NULL COMMENT '用户名（登录账号）',
                        `password`      VARCHAR(255) NOT NULL COMMENT '密码（加密存储，如bcrypt）',
                        `nickname`      VARCHAR(50)  DEFAULT NULL COMMENT '昵称（展示用，可改）',
                        `avatar`        VARCHAR(500) DEFAULT NULL COMMENT '头像图片URL',
                        `phone`         VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
                        `email`         VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
                        `gender`        TINYINT      DEFAULT 0 COMMENT '性别：0未知 1男 2女',
                        `status`        TINYINT      DEFAULT 1 COMMENT '账号状态：0禁用 1正常',
                        `point`         INT          DEFAULT 0 COMMENT '积分（消费/签到获取）',
                        `created_at`    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                        `updated_at`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_username` (`username`),
                        UNIQUE KEY `uk_phone` (`phone`),
                        UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- -----------------------------------------------------
-- 管理员表
-- -----------------------------------------------------
CREATE TABLE `admin` (
                         `id`         INT          NOT NULL AUTO_INCREMENT COMMENT '管理员主键ID',
                         `username`   VARCHAR(50)  NOT NULL COMMENT '管理员账号',
                         `password`   VARCHAR(255) NOT NULL COMMENT '密码（加密存储，如bcrypt）',
                         `nickname`   VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
                         `avatar`     VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
                         `role`       TINYINT      DEFAULT 1 COMMENT '角色：1超级管理员 2普通管理员',
                         `status`     TINYINT      DEFAULT 1 COMMENT '状态：0禁用 1正常',
                         `last_login` DATETIME     DEFAULT NULL COMMENT '最后登录时间',
                         `created_at` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `updated_at` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- -----------------------------------------------------
-- 支付流水表
-- -----------------------------------------------------
CREATE TABLE `payment` (
                           `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '支付流水主键ID',
                           `order_id`       BIGINT        NOT NULL COMMENT '订单ID（关联orders.id）',
                           `order_no`       VARCHAR(32)   NOT NULL COMMENT '订单编号',
                           `user_id`        BIGINT        NOT NULL COMMENT '用户ID（关联user.id）',
                           `pay_method`     TINYINT       NOT NULL COMMENT '支付方式：1微信 2支付宝 3货到付款',
                           `transaction_id` VARCHAR(64)   DEFAULT NULL COMMENT '第三方支付流水号',
                           `amount`         DECIMAL(10,2) NOT NULL COMMENT '支付金额',
                           `status`         TINYINT       DEFAULT 0 COMMENT '支付状态：0待支付 1成功 2失败 3已退款',
                           `paid_at`        DATETIME      DEFAULT NULL COMMENT '支付成功时间',
                           `refund_at`      DATETIME      DEFAULT NULL COMMENT '退款时间',
                           `remark`         VARCHAR(500)  DEFAULT NULL COMMENT '备注（失败原因/退款说明）',
                           `created_at`     DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `updated_at`     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `uk_transaction` (`transaction_id`),
                           KEY `idx_order` (`order_id`),
                           KEY `idx_user` (`user_id`),
                           KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付流水表';

-- -----------------------------------------------------
-- 商品分类表
-- -----------------------------------------------------
CREATE TABLE `category` (
                            `id`           INT          NOT NULL AUTO_INCREMENT COMMENT '分类主键ID',
                            `name`         VARCHAR(50)  NOT NULL COMMENT '分类名称（如：狗狗主粮）',
                            `icon`         VARCHAR(100) DEFAULT NULL COMMENT '分类图标（CSS类名或图片URL）',
                            `description`  VARCHAR(200) DEFAULT NULL COMMENT '分类描述',
#                             `sort_order`   INT          DEFAULT 0 COMMENT '排序（数值越小越靠前）',
                            `status`       TINYINT      DEFAULT 1 COMMENT '状态：0下架 1上架',
                            `created_at`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- -----------------------------------------------------
-- 商品表
-- -----------------------------------------------------
CREATE TABLE `product` (
                           `id`              BIGINT         NOT NULL AUTO_INCREMENT COMMENT '商品主键ID',
                           `category_id`     INT            NOT NULL COMMENT '所属分类ID（关联category.id）',
                           `name`            VARCHAR(200)   NOT NULL COMMENT '商品名称',
                           `subtitle`        VARCHAR(200)   DEFAULT NULL COMMENT '商品副标题/卖点',
                           `brand`           VARCHAR(50)    DEFAULT NULL COMMENT '品牌名称',
                           `price`           DECIMAL(10,2)  NOT NULL COMMENT '销售价格（当前售价）',
                           `original_price`  DECIMAL(10,2)  DEFAULT NULL COMMENT '原价/划线价',
                           `cover_image`     VARCHAR(500)   NOT NULL COMMENT '主图URL（列表展示用）',
                           `images`          TEXT           DEFAULT NULL COMMENT '商品图片URL集合（JSON数组）',
                           `description`     TEXT           DEFAULT NULL COMMENT '商品详情描述（富文本）',
                           `features`        VARCHAR(500)   DEFAULT NULL COMMENT '产品卖点标签（JSON数组，如"天然原料,均衡营养"）',
                           `specs`           VARCHAR(500)   DEFAULT NULL COMMENT '规格选项（JSON数组，如"15kg大包装,10kg中包装"）',
                           `stock`           INT            DEFAULT 0 COMMENT '库存数量',
                           `sales`           INT            DEFAULT 0 COMMENT '总销量',
                           `tag`             VARCHAR(20)    DEFAULT NULL COMMENT '商品标签（如：热销、新品、爆款）',
                           `status`          TINYINT        DEFAULT 1 COMMENT '上架状态：0下架 1上架',
#                            `sort_order`      INT            DEFAULT 0 COMMENT '排序权重',
                           `created_at`      DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `updated_at`      DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (`id`),
                           KEY `idx_category` (`category_id`),
                           KEY `idx_status` (`status`),
                           KEY `idx_tag` (`tag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- -----------------------------------------------------
-- 用户收货地址表
-- -----------------------------------------------------
CREATE TABLE `address` (
                           `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '地址主键ID',
                           `user_id`      BIGINT       NOT NULL COMMENT '所属用户ID（关联user.id）',
                           `receiver`     VARCHAR(50)  NOT NULL COMMENT '收货人姓名',
                           `phone`        VARCHAR(20)  NOT NULL COMMENT '收货人手机号',
                           `province`     VARCHAR(50)  NOT NULL COMMENT '省份',
                           `city`         VARCHAR(50)  NOT NULL COMMENT '城市',
                           `district`     VARCHAR(50)  NOT NULL COMMENT '区/县',
                           `detail`       VARCHAR(200) NOT NULL COMMENT '详细地址',
                           `is_default`   TINYINT      DEFAULT 0 COMMENT '是否默认地址：0否 1是',
                           `created_at`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `updated_at`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (`id`),
                           KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收货地址表';

-- -----------------------------------------------------
-- 购物车表
-- -----------------------------------------------------
CREATE TABLE `cart` (
                        `id`           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '购物车条目主键ID',
                        `user_id`      BIGINT   NOT NULL COMMENT '用户ID（关联user.id）',
                        `product_id`   BIGINT   NOT NULL COMMENT '商品ID（关联product.id）',
                        `spec`         VARCHAR(100) DEFAULT NULL COMMENT '所选规格（如：15kg 大包装）',
                        `quantity`     INT      DEFAULT 1 COMMENT '数量',
#                         `selected`     TINYINT  DEFAULT 1 COMMENT '是否勾选：0未勾选 1已勾选',
                        `created_at`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
                        `updated_at`   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`),
                        KEY `idx_user` (`user_id`),
                        KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- -----------------------------------------------------
-- 订单表
-- -----------------------------------------------------
CREATE TABLE `orders` (
                          `id`              BIGINT         NOT NULL AUTO_INCREMENT COMMENT '订单主键ID',
                          `order_no`        VARCHAR(32)    NOT NULL COMMENT '订单编号（唯一，如202610080001）',
                          `user_id`         BIGINT         NOT NULL COMMENT '下单用户ID（关联user.id）',
                          `address_id`      BIGINT         DEFAULT NULL COMMENT '收货地址ID（关联address.id）',
                          `total_amount`    DECIMAL(10,2)  NOT NULL COMMENT '订单总金额（商品金额+运费-优惠）',
                          `product_amount`  DECIMAL(10,2)  NOT NULL COMMENT '商品总金额（不含运费和优惠）',
                          `shipping_fee`    DECIMAL(10,2)  DEFAULT 0 COMMENT '运费',
                          `discount_amount` DECIMAL(10,2)  DEFAULT 0 COMMENT '优惠减免金额',
                          `pay_method`      TINYINT        DEFAULT NULL COMMENT '支付方式：1微信 2支付宝 3货到付款',
                          `status`          TINYINT        DEFAULT 0 COMMENT '订单状态：0待付款 1待发货 2待收货 3已完成 4已取消 5已退款',
                          `remark`          VARCHAR(500)   DEFAULT NULL COMMENT '订单备注（买家留言）',
                          `paid_at`         DATETIME       DEFAULT NULL COMMENT '支付时间',
                          `shipped_at`      DATETIME       DEFAULT NULL COMMENT '发货时间',
                          `received_at`     DATETIME       DEFAULT NULL COMMENT '收货时间',
                          `created_at`      DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
                          `updated_at`      DATETIME       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `uk_order_no` (`order_no`),
                          KEY `idx_user` (`user_id`),
                          KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- -----------------------------------------------------
-- 订单明细表（一个订单包含多个商品）
-- -----------------------------------------------------
CREATE TABLE `order_item` (
                              `id`             BIGINT         NOT NULL AUTO_INCREMENT COMMENT '明细主键ID',
                              `order_id`       BIGINT         NOT NULL COMMENT '所属订单ID（关联orders.id）',
                              `product_id`     BIGINT         NOT NULL COMMENT '商品ID（关联product.id）',
                              `product_name`   VARCHAR(200)   NOT NULL COMMENT '商品名称（下单时快照）',
                              `product_image`  VARCHAR(500)   DEFAULT NULL COMMENT '商品主图URL（下单时快照）',
                              `spec`           VARCHAR(100)   DEFAULT NULL COMMENT '所选规格',
                              `price`          DECIMAL(10,2)  NOT NULL COMMENT '下单时单价（快照）',
                              `quantity`       INT            NOT NULL COMMENT '购买数量',
                              `subtotal`       DECIMAL(10,2)  NOT NULL COMMENT '小计金额（price * quantity）',
                              PRIMARY KEY (`id`),
                              KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- -----------------------------------------------------
-- 商品评论表
-- -----------------------------------------------------
CREATE TABLE `comment` (
                           `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评论主键ID',
                           `product_id`   BIGINT       NOT NULL COMMENT '评论的商品ID（关联product.id）',
                           `user_id`      BIGINT       NOT NULL COMMENT '评论用户ID（关联user.id）',
                           `order_id`     BIGINT       DEFAULT NULL COMMENT '关联订单ID（关联orders.id，用于校验是否购买过）',
                           `rating`       TINYINT      NOT NULL DEFAULT 5 COMMENT '评分：1-5星',
                           `content`      TEXT         NOT NULL COMMENT '评论内容（可包含emoji）',
                           `images`       TEXT         DEFAULT NULL COMMENT '评价图片URL集合（JSON数组，可选）',
                           `status`       TINYINT      DEFAULT 1 COMMENT '状态：0隐藏（审核不通过/用户删除） 1正常显示',
                           `created_at`   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
                           PRIMARY KEY (`id`),
                           KEY `idx_product` (`product_id`),
                           KEY `idx_user` (`user_id`),
                           KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评论表';

-- -----------------------------------------------------
# -- 评论点赞记录表（防重复点赞）
# -- -----------------------------------------------------
# CREATE TABLE `comment_like` (
#                                 `id`           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '点赞记录主键ID',
#                                 `comment_id`   BIGINT   NOT NULL COMMENT '被点赞的评论ID（关联comment.id）',
#                                 `user_id`      BIGINT   NOT NULL COMMENT '点赞用户ID（关联user.id）',
#                                 `created_at`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
#                                 PRIMARY KEY (`id`),
#                                 UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`)
# ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞记录表';

-- -----------------------------------------------------
-- 优惠券表
-- -----------------------------------------------------
CREATE TABLE `coupon` (
                          `id`             INT            NOT NULL AUTO_INCREMENT COMMENT '优惠券主键ID',
                          `name`           VARCHAR(100)   NOT NULL COMMENT '优惠券名称（如：新人立减20）',
                          `type`           TINYINT        NOT NULL DEFAULT 1 COMMENT '类型：1满减 2折扣 3无门槛',
                          `discount_value` DECIMAL(10,2)  NOT NULL COMMENT '优惠金额（满减/无门槛时为减免金额，折扣时为折扣率如0.95）',
                          `min_amount`     DECIMAL(10,2)  DEFAULT 0 COMMENT '使用门槛（满多少可用，无门槛为0）',
                          `total_count`    INT            DEFAULT 0 COMMENT '发放总量（-1为不限量）',
                          `received_count` INT            DEFAULT 0 COMMENT '已领取数量',
                          `start_time`     DATETIME       NOT NULL COMMENT '生效时间',
                          `end_time`       DATETIME       NOT NULL COMMENT '失效时间',
                          `status`         TINYINT        DEFAULT 1 COMMENT '状态：0停用 1启用',
                          `created_at`     DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- -----------------------------------------------------
-- 用户优惠券表
-- -----------------------------------------------------
CREATE TABLE `user_coupon` (
                               `id`           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                               `user_id`      BIGINT   NOT NULL COMMENT '用户ID（关联user.id）',
                               `coupon_id`    INT      NOT NULL COMMENT '优惠券ID（关联coupon.id）',
                               `status`       TINYINT  DEFAULT 0 COMMENT '使用状态：0未使用 1已使用 2已过期',
                               `used_order_id` BIGINT  DEFAULT NULL COMMENT '使用的订单ID（关联orders.id）',
                               `received_at`  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
                               `used_at`      DATETIME DEFAULT NULL COMMENT '使用时间',
                               PRIMARY KEY (`id`),
                               KEY `idx_user` (`user_id`),
                               KEY `idx_coupon` (`coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';
#
# -- -----------------------------------------------------
# -- 商品收藏表
# -- -----------------------------------------------------
# CREATE TABLE `favorite` (
#                             `id`           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '收藏主键ID',
#                             `user_id`      BIGINT   NOT NULL COMMENT '用户ID（关联user.id）',
#                             `product_id`   BIGINT   NOT NULL COMMENT '商品ID（关联product.id）',
#                             `created_at`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
#                             PRIMARY KEY (`id`),
#                             UNIQUE KEY `uk_user_product` (`user_id`, `product_id`)
# ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品收藏表';

# -- -----------------------------------------------------
# -- 足迹/浏览历史表
# -- -----------------------------------------------------
# CREATE TABLE `browse_history` (
#                                   `id`           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
#                                   `user_id`      BIGINT   NOT NULL COMMENT '用户ID（关联user.id）',
#                                   `product_id`   BIGINT   NOT NULL COMMENT '商品ID（关联product.id）',
#                                   `created_at`   DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '浏览时间',
#                                   PRIMARY KEY (`id`),
#                                   KEY `idx_user` (`user_id`),
#                                   KEY `idx_product` (`product_id`)
# ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户浏览历史表';