package com.yanleaf.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yanleaf.domain.Category;
import com.yanleaf.service.CategoryService;
import com.yanleaf.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin
@Tag(name="CategoryController",description = "分类接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    @Operation(summary = "分类列表", description = "返回所有启用的分类，按排序字段升序排列")
    public Result<List<Category>>CategoryList() {
        LambdaQueryWrapper<Category> w = new LambdaQueryWrapper<>();
        w.eq(Category::getStatus, 1)
         .orderByAsc(Category::getSortOrder);
        return Result.ok(categoryService.list(w));
    }
}