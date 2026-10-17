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
 * 商品表
 * @TableName product
 */
@TableName(value ="product")
@Data
public class Product implements Serializable {
    /**
     * 商品主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属分类ID（关联category.id）
     */
    private Integer categoryId;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品副标题/卖点
     */
    private String subtitle;

    /**
     * 品牌名称
     */
    private String brand;

    /**
     * 销售价格（当前售价）
     */
    private BigDecimal price;

    /**
     * 原价/划线价
     */
    private BigDecimal originalPrice;

    /**
     * 主图URL（列表展示用）
     */
    private String coverImage;

    /**
     * 商品图片URL集合（JSON数组）
     */
    private String images;

    /**
     * 商品详情描述（富文本）
     */
    private String description;

    /**
     * 产品卖点标签（JSON数组，如"天然原料,均衡营养"）
     */
    private String features;

    /**
     * 规格选项（JSON数组，如"15kg大包装,10kg中包装"）
     */
    private String specs;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 总销量
     */
    private Integer sales;

    /**
     * 商品标签（如：热销、新品、爆款）
     */
    private String tag;

    /**
     * 上架状态：0下架 1上架
     */
    private Integer status;

    /**
     * 排序权重
     */
    private Integer sortOrder;

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