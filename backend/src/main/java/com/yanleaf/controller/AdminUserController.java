package com.yanleaf.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanleaf.domain.Orders;
import com.yanleaf.domain.User;
import com.yanleaf.service.OrdersService;
import com.yanleaf.service.UserService;
import com.yanleaf.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin
@Tag(name = "AdminUserController", description = "后台用户管理接口")
public class AdminUserController {

    @Autowired private UserService userService;
    @Autowired private OrdersService ordersService;

    @Operation(summary = "用户列表", description = "分页查询用户，关键字可匹配用户名、昵称、手机号、邮箱；返回结果已脱敏密码")
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getPhone, keyword)
                    .or().like(User::getEmail, keyword));
        }
        if (status != null) w.eq(User::getStatus, status);
        w.orderByAsc(User::getId);

        Page<User> page = userService.page(new Page<>(pageNum, pageSize), w);
        page.getRecords().forEach(u -> u.setPassword(null));

        Map<String, Object> data = new HashMap<>();
        data.put("records", page.getRecords());
        data.put("total", page.getTotal());
        data.put("pageNum", page.getCurrent());
        data.put("pageSize", page.getSize());
        return Result.ok(data);
    }

    @Operation(summary = "用户详情", description = "返回用户基本信息、订单总数和已付款订单累计金额（status 1/2/3）")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) return Result.fail(404, "用户不存在");
        user.setPassword(null);

        // 该用户订单统计
        long orderCount = ordersService.count(
                new LambdaQueryWrapper<Orders>().eq(Orders::getUserId, id));

        // 该用户已付款订单总额（status 1/2/3）
        java.math.BigDecimal totalSpent = ordersService.list(
                        new LambdaQueryWrapper<Orders>()
                                .eq(Orders::getUserId, id)
                                .in(Orders::getStatus, 1, 2, 3))
                .stream()
                .map(Orders::getTotalAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("orderCount", orderCount);
        data.put("totalSpent", totalSpent);
        return Result.ok(data);
    }

    @Operation(summary = "封禁或解禁用户", description = "status 只能是 0 或 1；0 表示封禁，1 表示正常")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            return Result.fail(400, "status 只能是 0 或 1");
        }
        User u = userService.getById(id);
        if (u == null) return Result.fail(404, "用户不存在");

        User update = new User();
        update.setId(id);
        update.setStatus(status);
        userService.updateById(update);
        return Result.ok();
    }


}