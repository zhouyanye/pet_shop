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

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/coupon")
@CrossOrigin
@Tag(name = "CouponController", description = "领券中心接口")

public class CouponController {

    @Autowired private CouponService couponService;
    @Autowired private UserCouponService userCouponService;
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

    @Operation(summary = "领券中心列表", description = "返回当前可领取（已上架且在有效期内）的优惠券；未登录也可访问，登录后会标记 received（是否已领）和 canReceive（是否还能领）")
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list(@RequestHeader(value = "Authorization", required = false) String auth) {
        // 当前用户（未登录也能看，只是 received=false）
        Long userId = null;
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            if (jwtUtil.validate(token)) {
                userId = jwtUtil.getUserId(token);
            }
        }

        Date now = new Date();
        List<Coupon> coupons = couponService.list(
                new LambdaQueryWrapper<Coupon>()
                        .eq(Coupon::getStatus, 1)
                        .le(Coupon::getStartTime, now)
                        .ge(Coupon::getEndTime, now)
                        .orderByDesc(Coupon::getCreatedAt)
        );

        // 已领取的 couponId 集合
        Set<Integer> receivedIds = new HashSet<>();
        if (userId != null) {
            List<UserCoupon> list = userCouponService.list(
                    new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getUserId, userId)
            );
            receivedIds = list.stream().map(UserCoupon::getCouponId).collect(Collectors.toSet());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Coupon c : coupons) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("name", c.getName());
            m.put("type", c.getType());
            m.put("discountValue", c.getDiscountValue());
            m.put("minAmount", c.getMinAmount());
            m.put("startTime", c.getStartTime());
            m.put("endTime", c.getEndTime());

            boolean received = receivedIds.contains(c.getId());
            m.put("received", received);

            // 是否还能领：总量 -1 表示不限量
            boolean stockEnough = c.getTotalCount() == -1
                    || c.getReceivedCount() < c.getTotalCount();
            m.put("canReceive", !received && stockEnough);

            // 剩余百分比
            if (c.getTotalCount() > 0) {
                int pct = (int) ((1.0 - (double) c.getReceivedCount() / c.getTotalCount()) * 100);
                m.put("remainPercent", Math.max(0, pct));
            } else {
                m.put("remainPercent", 100);
            }

            result.add(m);
        }
        return Result.ok(result);
    }

    @Operation(summary = "领取优惠券", description = "校验优惠券状态、活动时间、是否重复领取、库存是否充足；领取成功后已领数量 +1")
    @PostMapping("/receive/{id}")
    public Result<Void> receive(@RequestHeader("Authorization") String auth,
                                @PathVariable Integer id) {
        Long userId = parseUserId(auth);

        Coupon coupon = couponService.getById(id);
        if (coupon == null || coupon.getStatus() == 0) {
            return Result.fail("优惠券不存在或已停用");
        }
        Date now = new Date();
        if (now.before(coupon.getStartTime())) return Result.fail("活动未开始");
        if (now.after(coupon.getEndTime())) return Result.fail("活动已结束");

        // 是否已领过
        UserCoupon exist = userCouponService.getOne(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUserId, userId)
                        .eq(UserCoupon::getCouponId, id)
                        .last("LIMIT 1")
        );
        if (exist != null) {
            return Result.fail("您已领取过该优惠券");
        }

        // 库存
        if (coupon.getTotalCount() != -1 && coupon.getReceivedCount() >= coupon.getTotalCount()) {
            return Result.fail("优惠券已被抢完");
        }

        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(id);
        uc.setStatus(0);
        uc.setReceivedAt(new Date());
        userCouponService.save(uc);

        // 已领数量 +1
        coupon.setReceivedCount(coupon.getReceivedCount() + 1);
        couponService.updateById(coupon);

        return Result.ok();
    }
}