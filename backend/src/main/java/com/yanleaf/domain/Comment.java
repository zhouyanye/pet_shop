package com.yanleaf.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 商品评论表
 * @TableName comment
 */
@TableName(value ="comment")
@Data
public class Comment implements Serializable {
    /**
     * 评论主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 评论的商品ID（关联product.id）
     */
    private Long productId;

    /**
     * 评论用户ID（关联user.id）
     */
    private Long userId;

    /**
     * 关联订单ID（关联orders.id，用于校验是否购买过）
     */
    private Long orderId;

    /**
     * 评分：1-5星
     */
    private Integer rating;

    /**
     * 评论内容（可包含emoji）
     */
    private String content;

    /**
     * 评价图片URL集合（JSON数组，可选）
     */
    private String images;

    /**
     * 点赞数
     */
    private Integer likes;

    /**
     * 状态：0隐藏（审核不通过/用户删除） 1正常显示
     */
    private Integer status;

    /**
     * 评论时间
     */
    private Date createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}