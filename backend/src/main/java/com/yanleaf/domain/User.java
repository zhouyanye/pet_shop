package com.yanleaf.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yanleaf.validation.RegisterGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 用户表
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * 用户主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（登录账号）
     */
    @NotBlank(message = "用户名不能为空", groups = RegisterGroup.class)
    @Size(min = 3, max = 20, message = "用户名长度需在 3-20 位之间", groups = RegisterGroup.class)
    private String username;

    /**
     * 密码（加密存储，如bcrypt）
     */
//    @JsonIgnore
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需在 6-32 位之间")
    private String password;

    /**
     * 昵称（展示用，可改）
     */
    private String nickname;

    /**
     * 头像图片URL
     */
    private String avatar;

    /**
     * 手机号
     */
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 性别：0未知 1男 2女
     */
    private Integer gender;

    /**
     * 账号状态：0禁用 1正常
     */
    private Integer status;

    /**
     * 积分（消费/签到获取）
     */
    private Integer point;

    /**
     * 注册时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}