package com.yanleaf.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yanleaf.domain.Orders;
import com.yanleaf.domain.Payment;
import com.yanleaf.domain.User;
import com.yanleaf.exception.UnauthorizedException;
import com.yanleaf.service.OrdersService;
import com.yanleaf.service.PaymentService;
import com.yanleaf.service.UserService;
import com.yanleaf.utils.JwtUtil;
import com.yanleaf.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin
@Tag(name = "PaymentController", description = "支付接口")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;


    private Long parseUserId(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new UnauthorizedException("未登录");
        }
        String token = auth.substring(7);
        if (!jwtUtil.validate(token)) {
            throw new UnauthorizedException("登录已过期");
        }
        return jwtUtil.getUserId(token);
    }

    @Operation(summary = "模拟支付", description = "仅待付款（status=0）订单可支付，且下单后 10 分钟内有效；支付成功写支付流水、订单变为待发货（status=1），并按金额 1:1 赠送积分。payMethod：1 微信、2 支付宝、3 货到付款")
    @PostMapping("/pay")
    @Transactional
    public Result<Map<String, Object>> pay(@RequestHeader("Authorization") String auth,
                                           @RequestBody Map<String, Object> body) {
        Long userId = parseUserId(auth);
        Long orderId = Long.valueOf(body.get("orderId").toString());
        Integer payMethod = Integer.valueOf(body.get("payMethod").toString());  // 1微信 2支付宝 3货到付款

        Orders order = ordersService.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.fail(404, "订单不存在");
        }
        if (order.getStatus() != 0) {
            return Result.fail("订单状态不可支付");
        }

        // 检查是否超时
        long diff = System.currentTimeMillis() - order.getCreatedAt().getTime();
        if (diff > 10 * 60 * 1000L) {
            return Result.fail("订单已超时，请重新下单");
        }

        // 写支付流水
        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setOrderNo(order.getOrderNo());
        payment.setUserId(userId);
        payment.setPayMethod(payMethod);
        payment.setTransactionId("MOCK_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(1);   // 成功
        payment.setPaidAt(new Date());
        payment.setRemark("模拟支付");
        payment.setCreatedAt(new Date());
        payment.setUpdatedAt(new Date());
        paymentService.save(payment);

        // 更新订单
        order.setStatus(1);   // 待发货
        order.setPayMethod(payMethod);
        order.setPaidAt(new Date());
        order.setUpdatedAt(new Date());
        ordersService.updateById(order);
        // 添加积分
        User user = userService.getById(userId);
        if (user != null) {
            int points = order.getTotalAmount().intValue();   // 1元=1积分
            user.setPoint((user.getPoint() == null ? 0 : user.getPoint()) + points);
            userService.updateById(user);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("transactionId", payment.getTransactionId());
        result.put("paidAt", payment.getPaidAt());
        return Result.ok(result);
    }
}