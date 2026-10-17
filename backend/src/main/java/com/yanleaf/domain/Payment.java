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
 * 支付流水表
 * @TableName payment
 */
@TableName(value ="payment")
@Data
public class Payment implements Serializable {
    /**
     * 支付流水主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID（关联orders.id）
     */
    private Long orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 用户ID（关联user.id）
     */
    private Long userId;

    /**
     * 支付方式：1微信 2支付宝 3货到付款
     */
    private Integer payMethod;

    /**
     * 第三方支付流水号
     */
    private String transactionId;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付状态：0待支付 1成功 2失败 3已退款
     */
    private Integer status;

    /**
     * 支付成功时间
     */
    private Date paidAt;

    /**
     * 退款时间
     */
    private Date refundAt;

    /**
     * 备注（失败原因/退款说明）
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}