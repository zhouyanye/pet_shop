package com.yanleaf.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanleaf.common.PageQuery;
import com.yanleaf.domain.Comment;
import com.yanleaf.domain.Product;
import com.yanleaf.domain.User;
import com.yanleaf.service.CommentService;
import com.yanleaf.service.ProductService;
import com.yanleaf.service.UserService;
import com.yanleaf.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@CrossOrigin
@Tag(name = "ProductController", description = "商品接口")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CommentService commentService;
    @Autowired
    private UserService userService;

    @Operation(summary = "商品分页列表", description = "分页返回商品，支持关键字搜索")
    @GetMapping
    public Result<Page<Product>> ProductList(PageQuery query) {
        return Result.ok(productService.pageProducts(query));
    }

    @Operation(summary = "根据 id 获取商品", description = "返回指定 id 的商品详情，商品不存在时返回 404")
    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            return Result.fail(404, "商品不存在");
        }
        return Result.ok(product);
    }
    @Operation(summary = "商品评价列表", description = "分页返回指定商品的评价（仅正常状态），按时间倒序，附带评价用户昵称和头像")
    @GetMapping("/{id}/comments")
    public Result<Map<String, Object>> comments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "5") Integer pageSize) {

        LambdaQueryWrapper<Comment> w = new LambdaQueryWrapper<>();
        w.eq(Comment::getProductId, id)
                .eq(Comment::getStatus, 1)
                .orderByDesc(Comment::getCreatedAt);

//        return Result.ok(commentService.page(new Page<>(pageNum, pageSize), w));
        Page<Comment> page = commentService.page(new Page<>(pageNum, pageSize), w);

        // 拼昵称
        List<Map<String, Object>> records = page.getRecords().stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("rating", c.getRating());
            m.put("content", c.getContent());
            m.put("images", c.getImages());
            m.put("likes", c.getLikes());
            m.put("createdAt", c.getCreatedAt());

            User u = userService.getById(c.getUserId());
            if (u != null) {
                m.put("nickname", u.getNickname() != null ? u.getNickname() : u.getUsername());
                m.put("avatar", u.getAvatar());
            }
            return m;
        }).collect(Collectors.toList());
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", page.getTotal());
        data.put("current", page.getCurrent());
        data.put("size", page.getSize());

        return Result.ok(data);

    }

    @Operation(summary = "新增商品", description = "创建商品，销量/库存默认 0、状态默认 1（上架），创建和更新时间自动填充")
    @PostMapping
    public Result<Void> create(@RequestBody Product product) {
        product.setId(null);
        product.setCreatedAt(new Date());
        product.setUpdatedAt(new Date());
        if (product.getSales() == null) product.setSales(0);
        if (product.getStock() == null) product.setStock(0);
        if (product.getStatus() == null) product.setStatus(1);
        productService.save(product);
        return Result.ok();
    }

    @Operation(summary = "修改商品", description = "根据 id 更新商品，商品不存在时返回 404")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Product product) {
        if (productService.getById(id) == null) {
            return Result.fail(404, "商品不存在");
        }
        product.setId(id);
        product.setUpdatedAt(new Date());
        productService.updateById(product);
        return Result.ok();
    }

    @Operation(summary = "删除商品", description = "根据 id 删除商品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "图书id",required = true) @PathVariable Long id) {
        productService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "商品上下架", description = "status 只能是 0 或 1；0 表示下架，1 表示上架")
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Product p = new Product();
        p.setId(id);
        p.setStatus(status);
        p.setUpdatedAt(new Date());
        productService.updateById(p);
        return Result.ok();
    }
}