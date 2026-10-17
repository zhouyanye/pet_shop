package com.yanleaf.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanleaf.domain.Address;
import com.yanleaf.service.AddressService;
import com.yanleaf.mapper.AddressMapper;
import org.springframework.stereotype.Service;

/**
* @author admin
* @description 针对表【address(用户收货地址表)】的数据库操作Service实现
* @createDate 2026-10-10 20:09:06
*/
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address>
    implements AddressService{

}




