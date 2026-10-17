package com.yanleaf.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanleaf.domain.OrderItem;
import com.yanleaf.service.OrderItemService;
import com.yanleaf.mapper.OrderItemMapper;
import org.springframework.stereotype.Service;

/**
* @author admin
* @description 针对表【order_item(订单明细表)】的数据库操作Service实现
* @createDate 2026-10-10 20:09:06
*/
@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem>
    implements OrderItemService{

}




