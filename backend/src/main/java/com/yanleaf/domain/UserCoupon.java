package com.yanleaf.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户优惠券表
 * @TableName user_coupon
 */
@TableName(value ="user_coupon")
@Data
public class UserCoupon implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（关联user.id）
     */
    private Long userId;

    /**
     * 优惠券ID（关联coupon.id）
     */
    private Integer couponId;

    /**
     * 使用状态：0未使用 1已使用 2已过期
     */
    private Integer status;

    /**
     * 使用的订单ID（关联orders.id）
     */
    private Long usedOrderId;

    /**
     * 领取时间
     */
    private Date receivedAt;

    /**
     * 使用时间
     */
    private Date usedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}