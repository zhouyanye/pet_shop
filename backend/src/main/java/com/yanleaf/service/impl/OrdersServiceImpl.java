package com.yanleaf.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanleaf.domain.Orders;
import com.yanleaf.service.OrdersService;
import com.yanleaf.mapper.OrdersMapper;
import org.springframework.stereotype.Service;

/**
* @author admin
* @description 针对表【orders(订单表)】的数据库操作Service实现
* @createDate 2026-10-10 20:09:06
*/
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders>
    implements OrdersService{

}




