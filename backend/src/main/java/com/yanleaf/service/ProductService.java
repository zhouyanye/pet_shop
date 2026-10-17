package com.yanleaf.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanleaf.common.PageQuery;
import com.yanleaf.domain.Product;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author admin
* @description 针对表【product(商品表)】的数据库操作Service
* @createDate 2026-10-10 20:09:06
*/
public interface ProductService extends IService<Product> {
	Page<Product> pageProducts(PageQuery query);

}
