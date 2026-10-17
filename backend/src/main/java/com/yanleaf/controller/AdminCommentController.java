package com.yanleaf.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanleaf.domain.Comment;
import com.yanleaf.domain.Product;
import com.yanleaf.domain.User;
import com.yanleaf.service.CommentService;
import com.yanleaf.service.ProductService;
import com.yanleaf.service.UserService;
import com.yanleaf.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/comments")
@CrossOrigin
@Tag(name = "AdminCommentController", description = "后台评论管理接口")
public class AdminCommentController {

    @Autowired private CommentService commentService;
    @Autowired private ProductService productService;
    @Autowired private UserService userService;

    @Operation(summary = "评论列表", description = "分页查询评论，支持按状态、关键字（评论内容）、商品 id 筛选；返回结果附带商品名和用户名")
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long productId) {

        LambdaQueryWrapper<Comment> w = new LambdaQueryWrapper<>();

        if (status != null) w.eq(Comment::getStatus, status);
        if (productId != null) w.eq(Comment::getProductId, productId);
        if (keyword != null && !keyword.isBlank()) {
            w.like(Comment::getContent, keyword);
        }
        w.orderByDesc(Comment::getId);

        Page<Comment> page = commentService.page(new Page<>(pageNum, pageSize), w);
        List<Comment> records = page.getRecords();

        // 补充商品名、用户名
        Set<Long> pids = records.stream().map(Comment::getProductId).collect(Collectors.toSet());
        Set<Long> uids = records.stream().map(Comment::getUserId).collect(Collectors.toSet());

        Map<Long, String> productMap = new HashMap<>();
        if (!pids.isEmpty()) {
            productService.listByIds(pids).forEach(p ->
                    productMap.put(p.getId(), p.getName()));
        }
        Map<Long, String> userMap = new HashMap<>();
        if (!uids.isEmpty()) {
            userService.listByIds(uids).forEach(u ->
                    userMap.put(u.getId(),
                            u.getNickname() != null ? u.getNickname() : u.getUsername()));
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Comment c : records) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("productId", c.getProductId());
            m.put("productName", productMap.getOrDefault(c.getProductId(), "-"));
            m.put("userId", c.getUserId());
            m.put("username", userMap.getOrDefault(c.getUserId(), "-"));
            m.put("rating", c.getRating());
            m.put("content", c.getContent());
            m.put("images", c.getImages());
            m.put("likes", c.getLikes());
            m.put("status", c.getStatus());
            m.put("createdAt", c.getCreatedAt());
            list.add(m);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("records", list);
        data.put("total", page.getTotal());
        data.put("pageNum", page.getCurrent());
        data.put("pageSize", page.getSize());
        return Result.ok(data);
    }

    @Operation(summary = "隐藏或恢复评论", description = "status 只能是 0 或 1；0 表示隐藏，1 表示恢复显示")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            return Result.fail(400, "status 只能是 0 或 1");
        }
        Comment c = commentService.getById(id);
        if (c == null) return Result.fail(404, "评论不存在");

        Comment update = new Comment();
        update.setId(id);
        update.setStatus(status);
        commentService.updateById(update);
        return Result.ok();
    }

    @Operation(summary = "删除评论", description = "根据 id 物理删除评论，删除后不可恢复")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Comment c = commentService.getById(id);
        if (c == null) return Result.fail(404, "评论不存在");
        commentService.removeById(id);
        return Result.ok();
    }
}