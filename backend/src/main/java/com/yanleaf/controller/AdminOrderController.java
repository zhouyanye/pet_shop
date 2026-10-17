package com.yanleaf.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanleaf.domain.OrderItem;
import com.yanleaf.domain.Orders;
import com.yanleaf.domain.User;
import com.yanleaf.service.OrderItemService;
import com.yanleaf.service.OrdersService;
import com.yanleaf.service.UserService;
import com.yanleaf.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/orders")
@CrossOrigin
@Tag(name = "AdminOrderController", description = "后台订单管理接口")

public class AdminOrderController {

    @Autowired private OrdersService ordersService;
    @Autowired private OrderItemService orderItemService;
    @Autowired private UserService userService;

    @Operation(summary = "订单列表", description = "分页查询订单，支持按状态筛选，关键字可匹配订单号、用户名或昵称；返回结果附带用户名")
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<Orders> w = new LambdaQueryWrapper<>();

        if (status != null) w.eq(Orders::getStatus, status);

        // 关键词：订单号 or 用户名（先查 user）
        if (keyword != null && !keyword.isBlank()) {
            // 先按订单号模糊匹配
            List<Long> userIds = userService.list(
                    new LambdaQueryWrapper<User>()
                            .like(User::getUsername, keyword)
                            .or().like(User::getNickname, keyword)
            ).stream().map(User::getId).collect(Collectors.toList());

            w.and(q -> {
                q.like(Orders::getOrderNo, keyword);
                if (!userIds.isEmpty()) {
                    q.or().in(Orders::getUserId, userIds);
                }
            });
        }

        w.orderByAsc (Orders::getCreatedAt);

        Page<Orders> page = ordersService.page(new Page<>(pageNum, pageSize), w);

        // 补充用户名（关联查询）
        Set<Long> uids = page.getRecords().stream()
                .map(Orders::getUserId).collect(Collectors.toSet());
        Map<Long, String> userMap = new HashMap<>();
        if (!uids.isEmpty()) {
            userService.listByIds(uids).forEach(u ->
                    userMap.put(u.getId(), u.getNickname() != null ? u.getNickname() : u.getUsername()));
        }

        List<Map<String, Object>> records = new ArrayList<>();
        for (Orders o : page.getRecords()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", o.getId());
            m.put("orderNo", o.getOrderNo());
            m.put("userId", o.getUserId());
            m.put("username", userMap.getOrDefault(o.getUserId(), "-"));
            m.put("totalAmount", o.getTotalAmount());
            m.put("payMethod", o.getPayMethod());
            m.put("status", o.getStatus());
            m.put("createdAt", o.getCreatedAt());
            m.put("paidAt", o.getPaidAt());
            m.put("shippedAt", o.getShippedAt());
            records.add(m);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", page.getTotal());
        data.put("pageNum", page.getCurrent());
        data.put("pageSize", page.getSize());
        return Result.ok(data);
    }

    @Operation(summary = "订单详情", description = "根据订单 id 返回订单信息、下单用户和订单明细")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Orders order = ordersService.getById(id);
        if (order == null) return Result.fail(404, "订单不存在");

        User user = userService.getById(order.getUserId());

        List<OrderItem> items = orderItemService.list(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));

        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("user", user);
        data.put("items", items);
        return Result.ok(data);
    }

    @Operation(summary = "订单发货", description = "仅待发货（status=1）订单可发货，发货后状态变为待收货（status=2）并记录发货时间")
    @PutMapping("/{id}/ship")
    public Result<Void> ship(@PathVariable Long id) {
        Orders order = ordersService.getById(id);
        if (order == null) return Result.fail(404, "订单不存在");
        if (order.getStatus() != 1) return Result.fail(400, "只有待发货订单可以发货");

        Orders update = new Orders();
        update.setId(id);
        update.setStatus(2);              // 待收货
        update.setShippedAt(new Date());
        ordersService.updateById(update);
        return Result.ok();
    }

    @Operation(summary = "取消订单", description = "仅待付款（status=0）或待发货（status=1）订单可取消，取消后状态变为已取消（status=4）")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        Orders order = ordersService.getById(id);
        if (order == null) return Result.fail(404, "订单不存在");
        if (order.getStatus() != 0 && order.getStatus() != 1) {
            return Result.fail(400, "只有待付款/待发货订单可以取消");
        }

        Orders update = new Orders();
        update.setId(id);
        update.setStatus(4);              // 已取消
        ordersService.updateById(update);
        return Result.ok();
    }
}