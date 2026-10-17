package com.yanleaf.controller;

import com.yanleaf.domain.User;
import com.yanleaf.exception.UnauthorizedException;
import com.yanleaf.service.CaptchaService;
import com.yanleaf.service.UserService;
import com.yanleaf.utils.JwtUtil;
import com.yanleaf.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
@Tag(name = "UserController", description = "用户接口")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private CaptchaService captchaService;


	@Operation(summary = "获取验证码", description = "生成图形验证码，返回 uuid 和 base64 图片；登录时需同时传 uuid 和用户输入的验证码")
	@GetMapping("/captcha")
	public Result<Map<String, String>> captcha() {
		return Result.ok(captchaService.generate());
	}

	@Operation(summary = "用户注册", description = "接收 User 实体，字段校验失败或注册异常时返回错误信息")
	@PostMapping("/register")
	public Result<Void> register(@Validated @RequestBody User user, BindingResult br) {
		if (br.hasErrors()) {
			return Result.fail(br.getFieldError().getDefaultMessage());
		}
		try {
			userService.register(user);
			return Result.ok();
		} catch (RuntimeException e) {
			return Result.fail(e.getMessage());
		}
	}

	@Operation(summary = "用户登录", description = "需先校验图形验证码（uuid + captcha），通过后校验账号密码，成功返回 JWT token 和用户信息（密码已脱敏）")
	@PostMapping("/login")
	public Result<Map<String, Object>> login(@RequestBody Map<String, String> req) {
		String uuid = req.get("uuid");
		String captcha = req.get("captcha");
		if (!captchaService.verify(uuid, captcha)) {
			return Result.fail("验证码错误或已过期");
		}
		try {
			String username = req.get("username");
			String password = req.get("password");
			String token = userService.login(username, password);
			User user = userService.findByUsername(username);
			user.setPassword(null);
			Map<String, Object> data = new HashMap<>();
			data.put("token", token);
			data.put("user", user);
			return Result.ok(data);
		} catch (RuntimeException e) {
			return Result.fail(e.getMessage());
		}
	}


	@Operation(summary = "获取当前用户资料", description = "根据 token 解析用户 id 并返回用户信息，密码已脱敏")
	@GetMapping("/profile")
	public Result<User> profile(@RequestHeader("Authorization") String auth) {
		Long userId = parseUserId(auth);
		User user = userService.getById(userId);
		if (user == null) return Result.fail(404, "用户不存在");
		user.setPassword(null);
		return Result.ok(user);
	}

	@Operation(summary = "修改个人资料", description = "可修改昵称、头像、手机号、邮箱、性别；用户名、密码、状态、积分不允许通过此接口修改")
	@PutMapping("/profile")
	public Result<Void> updateProfile(@RequestHeader("Authorization") String auth,
									  @RequestBody User req) {
		Long userId = parseUserId(auth);
		if (userId == null) return Result.fail(401, "未登录");

		User update = new User();
		update.setId(userId);
		update.setNickname(req.getNickname());
		update.setAvatar(req.getAvatar());
		update.setPhone(req.getPhone());
		update.setEmail(req.getEmail());
		update.setGender(req.getGender());
		// username / password / status / point 不允许通过此接口改
		userService.updateById(update);
		return Result.ok();
	}

	@Operation(summary = "修改密码", description = "需提供旧密码和新密码，旧密码校验失败时返回错误信息")
	@PutMapping("/password")
	public Result<Void> changePassword(@RequestHeader("Authorization") String auth,
									   @RequestBody Map<String, String> body) {
		Long userId = parseUserId(auth);
		if (userId == null) return Result.fail(401, "未登录");
		try {
			userService.changePassword(userId, body.get("oldPassword"), body.get("newPassword"));
			return Result.ok();
		} catch (RuntimeException e) {
			return Result.fail(e.getMessage());
		}
	}

	@Operation(summary = "找回密码", description = "通过手机号和邮箱验证身份后重置密码，不依赖登录状态")
	@PostMapping("/reset-password")
	public Result<Void> resetPassword(@RequestBody Map<String, String> body) {
		try {
			userService.resetPassword(body.get("phone"), body.get("email"), body.get("newPassword"));
			return Result.ok();
		} catch (RuntimeException e) {
			return Result.fail(e.getMessage());
		}
	}

	/*  工具：从 Authorization 解析 userId  */
	private Long parseUserId(String auth) {
		if (auth == null || !auth.startsWith("Bearer ")) {
			throw new UnauthorizedException ("未登录");
		}
		String token = auth.substring(7);
		if (!jwtUtil.validate(token)) {
			throw new UnauthorizedException("登录已过期，请重新登录");
		}
		return jwtUtil.getUserId(token);
	}

}