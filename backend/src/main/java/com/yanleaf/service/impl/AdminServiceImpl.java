package com.yanleaf.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanleaf.domain.Admin;
import com.yanleaf.service.AdminService;
import com.yanleaf.mapper.AdminMapper;
import org.springframework.stereotype.Service;

/**
* @author admin
* @description 针对表【admin(管理员表)】的数据库操作Service实现
* @createDate 2026-10-10 20:09:06
*/
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin>
    implements AdminService{

}




