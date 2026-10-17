package com.yanleaf.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 购物车表
 * @TableName cart
 */
@TableName(value ="cart")
@Data
public class Cart implements Serializable {
    /**
     * 购物车条目主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（关联user.id）
     */
    private Long userId;

    /**
     * 商品ID（关联product.id）
     */
    private Long productId;

    /**
     * 所选规格（如：15kg 大包装）
     */
    private String spec;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 是否勾选：0未勾选 1已勾选
     */
    private Integer selected;

    /**
     * 加入时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}