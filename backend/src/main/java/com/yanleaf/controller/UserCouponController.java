package com.yanleaf.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yanleaf.domain.Coupon;
import com.yanleaf.domain.UserCoupon;
import com.yanleaf.exception.UnauthorizedException;
import com.yanleaf.service.CouponService;
import com.yanleaf.service.UserCouponService;
import com.yanleaf.utils.JwtUtil;
import com.yanleaf.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user-coupon")
@CrossOrigin
@Tag(name = "UserCouponController", description = "我的优惠券接口")

public class UserCouponController {

    @Autowired private UserCouponService userCouponService;
    @Autowired private CouponService couponService;
    @Autowired private JwtUtil jwtUtil;

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

    @Operation(summary = "可用优惠券", description = "返回当前用户未使用（status=0）的优惠券，并标记 usable（是否可用）和不可用原因 reason（已停用/未生效/已过期）")
    @GetMapping("/available")
    public Result<List<Map<String, Object>>> available(@RequestHeader("Authorization") String auth) {
        Long userId = parseUserId(auth);

        List<UserCoupon> list = userCouponService.list(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .eq(UserCoupon::getStatus, 0)
        );

        if (list.isEmpty()) return Result.ok(new ArrayList<>());

        Set<Integer> couponIds = list.stream().map(UserCoupon::getCouponId).collect(Collectors.toSet());
        Map<Integer, Coupon> couponMap = couponService.listByIds(couponIds).stream()
                .collect(Collectors.toMap(Coupon::getId, c -> c));

        List<Map<String, Object>> result = new ArrayList<>();
        Date now = new Date();
        for (UserCoupon uc : list) {
            Coupon c = couponMap.get(uc.getCouponId());
            if (c == null) continue;

            Map<String, Object> m = new HashMap<>();
            m.put("id", uc.getId());
            m.put("couponId", c.getId());
            m.put("couponName", c.getName());
            m.put("type", c.getType());
            m.put("discountValue", c.getDiscountValue());
            m.put("minAmount", c.getMinAmount());
            m.put ("startTime",c.getStartTime ());
            m.put("endTime", c.getEndTime());

            boolean usable = true;
            String reason = null;
            if (c.getStatus() == 0) { usable = false; reason = "已停用"; }
            else if (now.before(c.getStartTime())) { usable = false; reason = "未生效"; }
            else if (now.after(c.getEndTime())) { usable = false; reason = "已过期"; }

            m.put("usable", usable);
            m.put("reason", reason);
            result.add(m);
        }
        return Result.ok(result);
    }
    @Operation(summary = "我的卡券", description = "按状态返回当前用户的优惠券：0 未使用、1 已使用、2 已过期；未使用券附带动态提示 tip（即将过期/去使用）")
    @GetMapping("/my")
    public Result<List<Map<String, Object>>> my(@RequestHeader("Authorization") String auth,
                                                @RequestParam(required = false) Integer status) {
        Long userId = parseUserId(auth);

        LambdaQueryWrapper<UserCoupon> w = new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId)
                .orderByDesc(UserCoupon::getReceivedAt);
        if (status != null) w.eq(UserCoupon::getStatus, status);

        List<UserCoupon> list = userCouponService.list(w);
        if (list.isEmpty()) return Result.ok(new ArrayList<> ());

        Set<Integer> couponIds = list.stream().map(UserCoupon::getCouponId).collect(Collectors.toSet());
        Map<Integer, Coupon> couponMap = couponService.listByIds(couponIds).stream()
                .collect(Collectors.toMap(Coupon::getId, c -> c));

        Date now = new Date();
        List<Map<String, Object>> result = new ArrayList<>();
        for (UserCoupon uc : list) {
            Coupon c = couponMap.get(uc.getCouponId());
            if (c == null) continue;

            Map<String, Object> m = new HashMap<>();
            m.put("userCouponId", uc.getId());
            m.put("couponId", c.getId());
            m.put("name", c.getName());
            m.put("type", c.getType());
            m.put("discountValue", c.getDiscountValue());
            m.put("minAmount", c.getMinAmount());
            m.put("startTime", c.getStartTime());
            m.put("endTime", c.getEndTime());
            m.put("status", uc.getStatus());
            m.put("receivedAt", uc.getReceivedAt());
            m.put("usedAt", uc.getUsedAt());

            // 动态提示
            if (uc.getStatus() == 0) {
                long days = (c.getEndTime().getTime() - now.getTime()) / (24 * 3600 * 1000L);
                m.put("tip", days <= 3 ? "即将过期" : "去使用");
            } else if (uc.getStatus() == 1) {
                m.put("tip", "已使用");
            } else {
                m.put("tip", "已过期");
            }

            result.add(m);
        }
        return Result.ok(result);
    }
}