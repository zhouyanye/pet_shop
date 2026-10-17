package com.yanleaf.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 商品分类表
 * @TableName category
 */
@TableName(value ="category")
@Data
public class Category implements Serializable {
    /**
     * 分类主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 分类名称（如：狗狗主粮）
     */
    private String name;

    /**
     * 分类图标（CSS类名或图片URL）
     */
    private String icon;

    /**
     * 分类描述
     */
    private String description;

    /**
     * 排序（数值越小越靠前）
     */
    private Integer sortOrder;

    /**
     * 状态：0下架 1上架
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}