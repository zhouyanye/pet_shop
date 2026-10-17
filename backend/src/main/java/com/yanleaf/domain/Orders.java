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
 * 订单表
 * @TableName orders
 */
@TableName(value ="orders")
@Data
public class Orders implements Serializable {
    /**
     * 订单主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号（唯一，如202610080001）
     */
    private String orderNo;

    /**
     * 下单用户ID（关联user.id）
     */
    private Long userId;

    /**
     * 收货地址ID（关联address.id）
     */
    private Long addressId;

    /**
     * 订单总金额（商品金额+运费-优惠）
     */
    private BigDecimal totalAmount;

    /**
     * 商品总金额（不含运费和优惠）
     */
    private BigDecimal productAmount;

    /**
     * 运费
     */
    private BigDecimal shippingFee;

    /**
     * 优惠减免金额
     */
    private BigDecimal discountAmount;

    /**
     * 支付方式：1微信 2支付宝 3货到付款
     */
    private Integer payMethod;

    /**
     * 订单状态：0待付款 1待发货 2待收货 3已完成 4已取消 5已退款
     */
    private Integer status;

    /**
     * 订单备注（买家留言）
     */
    private String remark;

    /**
     * 支付时间
     */
    private Date paidAt;

    /**
     * 发货时间
     */
    private Date shippedAt;

    /**
     * 收货时间
     */
    private Date receivedAt;

    /**
     * 下单时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}