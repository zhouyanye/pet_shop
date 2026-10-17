package com.yanleaf.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanleaf.domain.Cart;
import com.yanleaf.service.CartService;
import com.yanleaf.mapper.CartMapper;
import org.springframework.stereotype.Service;

/**
* @author admin
* @description 针对表【cart(购物车表)】的数据库操作Service实现
* @createDate 2026-10-10 20:09:06
*/
@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart>
    implements CartService{

}




