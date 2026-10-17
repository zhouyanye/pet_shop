package com.yanleaf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanleaf.domain.User;
import com.yanleaf.mapper.UserMapper;
import com.yanleaf.service.UserService;
import com.yanleaf.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
* @author admin
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2026-10-10 20:09:06
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

	@Autowired
	private JwtUtil jwtUtil;

	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	@Override
	public void register(User user) {
		if (findByUsername(user.getUsername()) != null) {
			throw new RuntimeException("用户名已存在");
		}
		if (user.getPhone() != null && !user.getPhone().isEmpty()
				&& findByPhone(user.getPhone()) != null) {
			throw new RuntimeException("手机号已被注册");
		}
		if (user.getEmail() != null && !user.getEmail().isEmpty()
				&& findByEmail(user.getEmail()) != null) {
			throw new RuntimeException("邮箱已被注册");
		}

		user.setId(null);
		user.setPassword(encoder.encode(user.getPassword()));
		user.setStatus(1);
		user.setGender(user.getGender() == null ? 0 : user.getGender());
		user.setPoint(0);
		user.setCreatedAt(new Date());
		user.setUpdatedAt(new Date());
		this.save(user);
	}

	@Override
	public String login(String username, String password) {
		User user = findByUsername(username);
		if (user == null) {
			throw new RuntimeException("用户名或密码错误");
		}
		if (!encoder.matches(password, user.getPassword())) {
			throw new RuntimeException("用户名或密码错误");
		}
		if (user.getStatus() != null && user.getStatus() == 0) {
			throw new RuntimeException("账号已被禁用，请联系管理员");
		}
		return jwtUtil.generateToken(user.getId(), user.getUsername());
	}

	@Override
	public void changePassword(Long userId, String oldPassword, String newPassword) {
		User user = getById(userId);
		if (user == null) throw new RuntimeException("用户不存在");

		// 原密码校验
		if (!encoder.matches(oldPassword, user.getPassword())) {
			throw new RuntimeException("原密码错误");
		}
		// 新密码不能与原密码相同
		if (encoder.matches(newPassword, user.getPassword())) {
			throw new RuntimeException("新密码不能与原密码相同");
		}
		// 长度校验
		if (newPassword == null || newPassword.length() < 6) {
			throw new RuntimeException("新密码至少 6 位");
		}

		User update = new User();
		update.setId(userId);
		update.setPassword(encoder.encode(newPassword));
		user.setUpdatedAt(new Date());
		updateById(update);
	}

	@Override
	public void resetPassword(String phone, String email, String newPassword) {
		User user = null;
		if (phone != null && !phone.isEmpty()) {
			user = findByPhone(phone);
		} else if (email != null && !email.isEmpty()) {
			user = findByEmail(email);
		}
		if (user == null) {
			throw new RuntimeException("账号不存在");
		}
		user.setPassword(encoder.encode(newPassword));
		user.setUpdatedAt(new Date());
		this.updateById(user);
	}

	@Override
	public User findByUsername(String username) {
		return this.getOne(new LambdaQueryWrapper<User>()
				.eq(User::getUsername, username).last("LIMIT 1"));
	}

	@Override
	public User findByPhone(String phone) {
		return this.getOne(new LambdaQueryWrapper<User>()
				.eq(User::getPhone, phone).last("LIMIT 1"));
	}

	@Override
	public User findByEmail(String email) {
		return this.getOne(new LambdaQueryWrapper<User>()
				.eq(User::getEmail, email).last("LIMIT 1"));
	}
}




