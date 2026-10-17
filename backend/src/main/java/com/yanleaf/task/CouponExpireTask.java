package com.yanleaf.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yanleaf.domain.Coupon;
import com.yanleaf.domain.UserCoupon;
import com.yanleaf.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CouponExpireTask {
	@Autowired private CouponService couponService;
	@Autowired private UserCouponService userCouponService;

	@Scheduled(fixedRate = 60_000)
	public void expireUserCoupons() {
		// 查所有 status=0 的 user_coupon
		List<UserCoupon> list = userCouponService.list(
				new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getStatus, 0)
		);
		if (list.isEmpty()) return;

		// 批量查 coupon
		Set<Integer> couponIds = list.stream().map(UserCoupon::getCouponId).collect(Collectors.toSet());
		List<Coupon> coupons = couponService.listByIds(couponIds);
		if (coupons == null || coupons.isEmpty()) return;

		Map<Integer, Coupon> couponMap = coupons.stream()
				.collect(Collectors.toMap(Coupon::getId, c -> c));

		Date now = new Date();
		List<UserCoupon> toUpdate = new ArrayList<>();
		for (UserCoupon uc : list) {
			Coupon c = couponMap.get(uc.getCouponId());
			if (c == null) continue;
			// 如果优惠券已过结束时间，或者优惠券本身状态失效
			if (now.after(c.getEndTime()) || c.getStatus() == 0) {
				uc.setStatus(2); // 2代表已过期
				toUpdate.add(uc);
			}
		}
		if (!toUpdate.isEmpty()) {
			userCouponService.updateBatchById(toUpdate);
			System.out.println("【券过期】标记 " + toUpdate.size() + " 张券为已过期");
		}
	}
}