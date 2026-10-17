package com.yanleaf.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yanleaf.domain.Orders;
import com.yanleaf.domain.Product;
import com.yanleaf.domain.User;
import com.yanleaf.service.OrdersService;
import com.yanleaf.service.ProductService;
import com.yanleaf.service.UserService;
import com.yanleaf.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/api/admin/dashboard")
@CrossOrigin
@Tag(name = "AdminDashboardController", description = "后台数据看板接口")
public class AdminDashboardController {

    @Autowired private UserService userService;
    @Autowired private ProductService productService;
    @Autowired private OrdersService ordersService;

    @Operation(summary = "看板统计数据",
            description = "返回用户、商品、订单、销售额的汇总数据，以及最近 7 天的订单数和销售额趋势")
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {


        Date todayStart = Date.from(LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault()).toInstant());

        Map<String, Object> data = new HashMap<>();

        // 用户
        data.put("totalUsers", userService.count());
        data.put("todayUsers", userService.count(
                new LambdaQueryWrapper<User>().ge(User::getCreatedAt, todayStart)));

        // 商品
        data.put("totalProducts", productService.count());
        data.put("onSaleProducts", productService.count(
                new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1)));

        // 订单
        data.put("totalOrders", ordersService.count());
        data.put("todayOrders", ordersService.count(
                new LambdaQueryWrapper<Orders>().ge(Orders::getCreatedAt, todayStart)));
        data.put("pendingOrders", ordersService.count(
                new LambdaQueryWrapper<Orders>().eq(Orders::getStatus, 1)));

        // 销售额
        data.put("totalAmount", sumPaid(null));
        data.put("todayAmount", sumPaid(todayStart));

        // 趋势
        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            Date start = Date.from(day.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date end = Date.from(day.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

            int cnt = ordersService.count(new LambdaQueryWrapper<Orders>()
                    .ge(Orders::getCreatedAt, start)
                    .lt(Orders::getCreatedAt, end));

            BigDecimal amt = ordersService.list(new LambdaQueryWrapper<Orders>()
                            .ge(Orders::getCreatedAt, start)
                            .lt(Orders::getCreatedAt, end)
                            .in(Orders::getStatus, 1, 2, 3))
                    .stream()
                    .map(Orders::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> item = new HashMap<>();
            item.put("date", day.toString());
            item.put("orders", cnt);
            item.put("amount", amt);
            trend.add(item);
        }
        data.put("trend", trend);

        return Result.ok(data);
    }

    private BigDecimal sumPaid(Date from) {
        LambdaQueryWrapper<Orders> w = new LambdaQueryWrapper<Orders>()
                .in(Orders::getStatus, 1, 2, 3);
        if (from != null) w.ge(Orders::getCreatedAt, from);
        return ordersService.list(w).stream()
                .map(Orders::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}