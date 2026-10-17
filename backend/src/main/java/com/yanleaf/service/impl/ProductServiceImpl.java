package com.yanleaf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yanleaf.common.PageQuery;
import com.yanleaf.domain.Product;
import com.yanleaf.mapper.ProductMapper;
import com.yanleaf.service.ProductService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
* @author admin
* @description 针对表【product(商品表)】的数据库操作Service实现
* @createDate 2026-10-10 20:09:06
*/
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product>
		implements ProductService {

	@Override
	public Page<Product> pageProducts(PageQuery query) {
		LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

		if (StringUtils.isNotBlank(query.getKeyword())) {
			wrapper.and(w -> w.like(Product::getName, query.getKeyword())
					.or().like(Product::getSubtitle, query.getKeyword())
					.or().like(Product::getBrand, query.getKeyword()));
		}
		if (query.getCategoryId() != null) {
			wrapper.eq(Product::getCategoryId, query.getCategoryId());
		}
		if (query.getStatus() != null) {
			wrapper.eq(Product::getStatus, query.getStatus());
		}
		if (query.getMinPrice() != null) {
			wrapper.ge(Product::getPrice, query.getMinPrice());
		}
		if (query.getMaxPrice() != null) {
			wrapper.le(Product::getPrice, query.getMaxPrice());
		}
		if (StringUtils.isNotBlank(query.getTag())) {
			wrapper.eq(Product::getTag, query.getTag());
		}

		// 动态排序（白名单）
		boolean isAsc = "asc".equalsIgnoreCase(query.getOrder());
		String sortBy = query.getSortBy();

		if (StringUtils.isNotBlank(sortBy)) {
			switch (sortBy) {
				case "sales":
					wrapper.orderBy(true, isAsc, Product::getSales);
					break;
				case "created_at":
				case "createdAt":
					wrapper.orderBy(true, isAsc, Product::getCreatedAt);
					break;
				case "price":
					wrapper.orderBy(true, isAsc, Product::getPrice);
					break;
				case "sort_order":
				case "sortOrder":
					wrapper.orderBy(true, isAsc, Product::getSortOrder);
					break;
				default:
					wrapper.orderByAsc(Product::getSortOrder).orderByDesc(Product::getId);
			}
		} else {
			wrapper.orderByAsc(Product::getSortOrder).orderByDesc(Product::getId);
		}

		return this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
	}
}




