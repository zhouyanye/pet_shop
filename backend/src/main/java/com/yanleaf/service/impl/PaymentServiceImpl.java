package com.yanleaf.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanleaf.domain.Payment;
import com.yanleaf.service.PaymentService;
import com.yanleaf.mapper.PaymentMapper;
import org.springframework.stereotype.Service;

/**
* @author admin
* @description 针对表【payment(支付流水表)】的数据库操作Service实现
* @createDate 2026-10-10 20:09:06
*/
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment>
    implements PaymentService{

}




