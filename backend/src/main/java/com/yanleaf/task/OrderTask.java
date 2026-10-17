package com.yanleaf.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yanleaf.domain.Orders;
import com.yanleaf.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class OrderTask {

    @Autowired
    private OrdersService ordersService;

    /** 支付后 1 分钟，自动发货 */
    @Scheduled(fixedRate = 60_000)
    public void autoShipOrders() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, -1);   // 1 分钟前支付的
        Date limit = cal.getTime();

        List<Orders> toShip = ordersService.list(
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getStatus, 1)               // 待发货
                        .lt(Orders::getPaidAt, limit)           // 支付超过 1 分钟
        );

        for (Orders o : toShip) {
            o.setStatus(2);            // 待收货
            o.setShippedAt(new Date());
            o.setUpdatedAt(new Date());
            ordersService.updateById(o);
        }

        if (!toShip.isEmpty()) {
            System.out.println("【自动发货】处理了 " + toShip.size() + " 个订单");
        }
    }

    /** 可选：发货后 7 天自动确认收货 */
    @Scheduled(fixedRate = 3_600_000)   // 每小时扫一次
    public void autoReceiveOrders() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        Date limit = cal.getTime();

        List<Orders> toReceive = ordersService.list(
                new LambdaQueryWrapper<Orders>()
                        .eq(Orders::getStatus, 2)               // 待收货
                        .lt(Orders::getShippedAt, limit)        // 发货超过 7 天
        );

        for (Orders o : toReceive) {
            o.setStatus(3);            // 已完成
            o.setReceivedAt(new Date());
            o.setUpdatedAt(new Date());
            ordersService.updateById(o);
        }

        if (!toReceive.isEmpty()) {
            System.out.println("【自动收货】处理了 " + toReceive.size() + " 个订单");
        }
    }
}