package com.yanleaf.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yanleaf.domain.Category;
import com.yanleaf.domain.Product;
import com.yanleaf.service.CategoryService;
import com.yanleaf.service.ProductService;
import com.yanleaf.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@CrossOrigin
@Tag(name = "AdminCategoryController", description = "后台分类管理接口")
public class AdminCategoryController {

    @Autowired private CategoryService categoryService;
    @Autowired private ProductService productService;

    @Operation(summary = "查询所有分类", description = "按 id 倒序返回全部分类，供后台列表展示")    @GetMapping
    public Result<List<Category>> list() {
        LambdaQueryWrapper<Category> w = new LambdaQueryWrapper<>();
        w.orderByDesc(Category::getId);
        return Result.ok(categoryService.list(w));
    }

    @Operation(summary = "新增分类", description = "分类名称不能为空；id 由后端生成，创建时间自动填充")
    @PostMapping
    public Result<Void> add(@RequestBody Category category) {
        if (category.getName() == null || category.getName().isBlank()) {
            return Result.fail(400, "分类名称不能为空");
        }
        category.setId(null);
        category.setCreatedAt(new Date());
        categoryService.save(category);
        return Result.ok();
    }

    @Operation(summary = "编辑分类", description = "根据 id 更新分类信息，分类不存在时返回 404")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id, @RequestBody Category category) {
        if (categoryService.getById(id) == null) return Result.fail(404, "分类不存在");
        category.setId(id);
        categoryService.updateById(category);
        return Result.ok();
    }

    @Operation(summary = "删除分类", description = "分类下仍有商品时禁止删除，需先删除或移动商品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        long count = productService.count(
                new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, id));
        if (count > 0) {
            return Result.fail(400, "该分类下还有商品，请先删除或移动商品！");
        }
        categoryService.removeById(id);
        return Result.ok();
    }
}