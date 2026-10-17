package com.yanleaf.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanleaf.domain.*;
import com.yanleaf.exception.UnauthorizedException;
import com.yanleaf.service.*;
import com.yanleaf.utils.JwtUtil;
import com.yanleaf.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/comment")
@CrossOrigin
@Tag(name = "CommentController", description = "商品评价接口")

public class CommentController {

    @Autowired
    private CommentService commentService;
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    private Long parseUserId(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new UnauthorizedException("未登录");
        }
        String token = auth.substring(7);
        if (!jwtUtil.validate(token)) {
            throw new UnauthorizedException("登录已过期");
        }
        return jwtUtil.getUserId(token);
    }

    @Operation(summary = "提交评价", description = "仅已完成订单（status=3）可评价；需校验订单归属、商品是否在该订单中、是否重复评价；评价成功后用户积分 +10")
    @PostMapping
    public Result<Void> add(@RequestHeader("Authorization") String auth,
                            @RequestBody Map<String, Object> body) {
        Long userId = parseUserId(auth);

        Long orderId = Long.valueOf(body.get("orderId").toString());
        Long productId = Long.valueOf(body.get("productId").toString());
        Integer rating = Integer.valueOf(body.getOrDefault("rating", 5).toString());
        String content = (String) body.getOrDefault("content", "");
        String images = (String) body.getOrDefault("images", null);

        if (content == null || content.trim().isEmpty()) {
            return Result.fail("评价内容不能为空");
        }
        if (rating < 1 || rating > 5) {
            return Result.fail("评分必须是 1-5");
        }

        // 校验订单
        Orders order = ordersService.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.fail(404, "订单不存在");
        }
        if (order.getStatus() != 3) {
            return Result.fail("订单未完成，不能评价");
        }

        // 校验订单里有这个商品
        OrderItem oi = orderItemService.getOne(
                new LambdaQueryWrapper<OrderItem>()
                        .eq(OrderItem::getOrderId, orderId)
                        .eq(OrderItem::getProductId, productId)
                        .last("LIMIT 1")
        );
        if (oi == null) {
            return Result.fail("该订单不包含此商品");
        }

        // 是否已评价过
        Comment exist = commentService.getOne(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getOrderId, orderId)
                        .eq(Comment::getProductId, productId)
                        .eq(Comment::getUserId, userId)
                        .last("LIMIT 1")
        );
        if (exist != null) {
            return Result.fail("该商品已评价过");
        }

        Comment c = new Comment();
        c.setProductId(productId);
        c.setUserId(userId);
        c.setOrderId(orderId);
        c.setRating(rating);
        c.setContent(content);
        c.setImages(images);
        c.setLikes(0);
        c.setStatus(1);
        c.setCreatedAt(new Date());
        commentService.save(c);

        User user = userService.getById(userId);
        if (user != null) {
            user.setPoint((user.getPoint() == null ? 0 : user.getPoint()) + 10);
            userService.updateById(user);
        }
        return Result.ok();
    }

    @Operation(summary = "检查是否已评价", description = "查询当前用户对指定订单下指定商品是否已提交过评价，返回 true 表示已评价")
    @GetMapping("/check")
    public Result<Boolean> check(@RequestHeader("Authorization") String auth,
                                 @RequestParam Long orderId,
                                 @RequestParam Long productId) {
        Long userId = parseUserId(auth);
        Comment c = commentService.getOne(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getOrderId, orderId)
                        .eq(Comment::getProductId, productId)
                        .eq(Comment::getUserId, userId)
                        .last("LIMIT 1")
        );
        return Result.ok(c != null);
    }
    @Operation(summary = "我的评价列表", description = "分页返回当前用户的评价，按时间倒序，附带商品名称和封面图；商品不存在时显示商品已下架")

    @GetMapping("/my")
    public Result<Map<String, Object>> myComments(
            @RequestHeader("Authorization") String auth,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Long userId = parseUserId(auth);

        // 分页查询该用户的评价，按时间倒序
        Page<Comment> page = commentService.page(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getUserId, userId)
                        .orderByDesc(Comment::getCreatedAt)
        );

        // 补充商品名称和图片
        List<Map<String, Object>> records = new ArrayList<> ();
        for (Comment c : page.getRecords()) {
            Map<String, Object> m = new HashMap<> ();
            m.put("id", c.getId());
            m.put("rating", c.getRating());
            m.put("content", c.getContent());
            m.put("images", c.getImages());
            m.put("createdAt", c.getCreatedAt());

            // 查商品信息
            Product p = productService.getById(c.getProductId());
            if (p != null) {
                m.put("productName", p.getName());
                m.put("productImage", p.getCoverImage());
            } else {
                m.put("productName", "商品已下架");
                m.put("productImage", "");
            }
            records.add(m);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", page.getTotal());
        data.put("pageNum", page.getCurrent());
        data.put("pageSize", page.getSize());
        return Result.ok(data);
    }
}