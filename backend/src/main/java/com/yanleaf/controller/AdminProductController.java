package com.yanleaf.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanleaf.domain.Product;
import com.yanleaf.service.ProductService;
import com.yanleaf.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@CrossOrigin
@Tag(name = "AdminProductController", description = "后台商品管理接口")

public class AdminProductController {

    @Autowired private ProductService productService;

    @Operation(summary = "商品列表", description = "分页查询商品，支持按名称/品牌关键字、状态、分类 id 筛选，按 id 倒序排列")
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer categoryId) {

        LambdaQueryWrapper<Product> w = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isBlank()) {
            w.and(q -> q.like(Product::getName, keyword)
                    .or().like(Product::getBrand, keyword));
        }
        if (status != null) w.eq(Product::getStatus, status);
        if (categoryId != null) w.eq(Product::getCategoryId, categoryId);
        
        // 后台最新加的在前面
        w.orderByDesc(Product::getId);

        Page<Product> page = productService.page(new Page<>(pageNum, pageSize), w);

        Map<String, Object> data = new HashMap<>();
        data.put("records", page.getRecords());
        data.put("total", page.getTotal());
        data.put("pageNum", page.getCurrent());
        data.put("pageSize", page.getSize());
        return Result.ok(data);
    }

    @Operation(summary = "新增商品", description = "商品名称不能为空；销量默认 0、状态默认 1（上架），id 由后端生成")
    @PostMapping
    public Result<Void> add(@RequestBody Product product) {
        if (product.getName() == null || product.getName().isBlank()) {
            return Result.fail(400, "商品名称不能为空");
        }
        product.setId(null);
        if (product.getSales() == null) product.setSales(0);
        if (product.getStatus() == null) product.setStatus(1);
        
        productService.save(product);
        return Result.ok();
    }

    @Operation(summary = "编辑商品", description = "根据 id 更新商品；销量和创建时间不允许通过编辑修改")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Product product) {
        Product exist = productService.getById(id);
        if (exist == null) return Result.fail(404, "商品不存在");

        product.setId(id);
        // 销量不允许后台直接改，由订单系统扣减
        product.setSales(null);
        // 创建时间不变
        product.setCreatedAt(null);

        productService.updateById(product);
        return Result.ok();
    }

    @Operation(summary = "商品上下架", description = "status 只能是 0 或 1；0 表示下架，1 表示上架")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            return Result.fail(400, "status 只能是 0 或 1");
        }
        Product p = productService.getById(id);
        if (p == null) return Result.fail(404, "商品不存在");

        Product update = new Product();
        update.setId(id);
        update.setStatus(status);
        productService.updateById(update);
        return Result.ok();
    }

    @Operation(summary = "删除商品", description = "根据 id 删除商品，商品不存在时返回 404")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Product p = productService.getById(id);
        if (p == null) return Result.fail(404, "商品不存在");

        productService.removeById(id);
        return Result.ok();
    }
}