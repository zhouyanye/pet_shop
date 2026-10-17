package com.yanleaf.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 订单明细表
 * @TableName order_item
 */
@TableName(value ="order_item")
@Data
public class OrderItem implements Serializable {
    /**
     * 明细主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属订单ID（关联orders.id）
     */
    private Long orderId;

    /**
     * 商品ID（关联product.id）
     */
    private Long productId;

    /**
     * 商品名称（下单时快照）
     */
    private String productName;

    /**
     * 商品主图URL（下单时快照）
     */
    private String productImage;

    /**
     * 所选规格
     */
    private String spec;

    /**
     * 下单时单价（快照）
     */
    private BigDecimal price;

    /**
     * 购买数量
     */
    private Integer quantity;

    /**
     * 小计金额（price * quantity）
     */
    private BigDecimal subtotal;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}