package com.yanleaf.task;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yanleaf.domain.*;
import com.yanleaf.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class OrderTimeoutTask {

	@Autowired private OrdersService ordersService;
	@Autowired private OrderItemService orderItemService;
	@Autowired private ProductService productService;
	@Autowired private CouponService couponService;
	@Autowired private UserCouponService userCouponService;

	/** 每分钟扫一次：10 分钟未支付的订单自动取消 */
	@Scheduled(fixedRate = 60_000)
	@Transactional
	public void cancelTimeoutOrders() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -10);
		Date limit = cal.getTime();

		List<Orders> timeoutOrders = ordersService.list(
				new LambdaQueryWrapper<Orders>()
						.eq(Orders::getStatus, 0)
						.lt(Orders::getCreatedAt, limit)
		);

		for (Orders order : timeoutOrders) {
			// 回滚库存
			List<OrderItem> items = orderItemService.list(
					new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId())
			);
			for (OrderItem oi : items) {
				Product p = productService.getById(oi.getProductId());
				if (p != null) {
					p.setStock(p.getStock() + oi.getQuantity());
					p.setSales(Math.max(0, p.getSales() - oi.getQuantity()));
					productService.updateById(p);
				}
			}

			// 退还优惠券
			UserCoupon uc = userCouponService.getOne(
					new LambdaQueryWrapper<UserCoupon>()
							.eq(UserCoupon::getUsedOrderId, order.getId())
							.last("LIMIT 1")
			);
			if (uc != null) {
				Coupon coupon = couponService.getById(uc.getCouponId());
				boolean expired = coupon == null
						|| coupon.getStatus() == 0
						|| new Date().after(coupon.getEndTime());
				uc.setUsedOrderId(null);
				uc.setUsedAt(null);
				uc.setStatus(expired ? 2 : 0);
				userCouponService.updateById(uc);
			}

			// 取消订单
			order.setStatus(4);   // 已取消
			order.setUpdatedAt(new Date());
			ordersService.updateById(order);
		}

		if (!timeoutOrders.isEmpty()) {
			System.out.println("【超时取消】处理了 " + timeoutOrders.size() + " 个订单");
		}
	}
}
