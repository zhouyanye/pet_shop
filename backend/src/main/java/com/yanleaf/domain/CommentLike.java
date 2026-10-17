package com.yanleaf.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 评论点赞记录表
 * @TableName comment_like
 */
@TableName(value ="comment_like")
@Data
public class CommentLike implements Serializable {
    /**
     * 点赞记录主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 被点赞的评论ID（关联comment.id）
     */
    private Long commentId;

    /**
     * 点赞用户ID（关联user.id）
     */
    private Long userId;

    /**
     * 点赞时间
     */
    private Date createdAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}