package com.yanleaf.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 管理员表
 * @TableName admin
 */
@TableName(value ="admin")
@Data
public class Admin implements Serializable {
    /**
     * 管理员主键ID
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 管理员账号
     */
    private String username;

    /**
     * 密码（加密存储，如bcrypt）
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 角色：1超级管理员 2普通管理员
     */
    private Integer role;

    /**
     * 状态：0禁用 1正常
     */
    private Integer status;

    /**
     * 最后登录时间
     */
    private Date lastLogin;

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