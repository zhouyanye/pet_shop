package com.yanleaf.service;

import com.yanleaf.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author admin
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2026-10-10 20:09:06
*/
public interface UserService extends IService<User> {

	/** 注册 */
	void register(User user);

	/** 登录，返回 token */
	String login(String username, String password);

	/** 修改密码 */
	void changePassword(Long userId, String oldPassword, String newPassword);

	/** 找回密码（简化版：凭手机号或邮箱直接重置） */
	void resetPassword(String phone, String email, String newPassword);

	/** 根据用户名查 */
	User findByUsername(String username);

	/** 根据手机号查 */
	User findByPhone(String phone);

	/** 根据邮箱查 */
	User findByEmail(String email);

}