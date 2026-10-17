package com.yanleaf.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanleaf.domain.Category;
import com.yanleaf.service.CategoryService;
import com.yanleaf.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

/**
* @author admin
* @description 针对表【category(商品分类表)】的数据库操作Service实现
* @createDate 2026-10-10 20:09:06
*/
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
    implements CategoryService{

}




