package com.yanleaf.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 优惠券表
 * @TableName coupon
 */
@TableName(value ="coupon")
@Data
public class Coupon implements Serializable {
    /**
     * 优惠券主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 优惠券名称（如：新人立减20）
     */
    private String name;

    /**
     * 类型：1满减 2折扣 3无门槛
     */
    private Integer type;

    /**
     * 优惠金额（满减/无门槛时为减免金额，折扣时为折扣率如0.95）
     */
    private BigDecimal discountValue;

    /**
     * 使用门槛（满多少可用，无门槛为0）
     */
    private BigDecimal minAmount;

    /**
     * 发放总量（-1为不限量）
     */
    private Integer totalCount;

    /**
     * 已领取数量
     */
    private Integer receivedCount;

    /**
     * 生效时间
     */
    private Date startTime;

    /**
     * 失效时间
     */
    private Date endTime;

    /**
     * 状态：0停用 1启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}