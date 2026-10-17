package com.yanleaf.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yanleaf.domain.Admin;
import com.yanleaf.service.AdminService;
import com.yanleaf.service.UserService;
import com.yanleaf.utils.JwtUtil;
import com.yanleaf.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
@Tag(name = "AdminController", description = "管理员登录接口")
public class AdminController {

    @Autowired private AdminService adminService;
    @Autowired private UserService userService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    @Autowired private JwtUtil jwtUtil;

    @Operation(summary = "管理员登录", description = "账号密码校验通过后返回 JWT token 和管理员信息；账号被禁用时拒绝登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> req) {

        String username = req.get("username");
        String password = req.get("password");


        Admin admin = adminService.getOne(
                new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username)
        );
        if (admin == null) return Result.fail("账号或密码错误");
        if (admin.getStatus() == 0) return Result.fail("账号已被禁用");
        if (!encoder.matches (password,admin.getPassword())) return Result.fail("账号或密码错误");

        // 更新最后登录时间
        admin.setLastLogin(new Date());
        adminService.updateById(admin);

        // 生成 token
        String token = jwtUtil.generateToken(
                admin.getId().longValue(),
                admin.getUsername(),
                "ADMIN"
        );

        admin.setPassword(null);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("admin", admin);
        return Result.ok(data);
    }


}