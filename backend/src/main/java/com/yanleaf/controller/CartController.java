package com.yanleaf.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yanleaf.domain.Cart;
import com.yanleaf.domain.Product;
import com.yanleaf.exception.UnauthorizedException;
import com.yanleaf.service.CartService;
import com.yanleaf.service.ProductService;
import com.yanleaf.utils.JwtUtil;
import com.yanleaf.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin
@Tag(name = "CartController", description = "购物车接口")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @Autowired
    private JwtUtil jwtUtil;

    private Long parseUserId(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new UnauthorizedException("未登录");
        }
        String token = auth.substring(7);
        if (!jwtUtil.validate(token)) {
            throw new UnauthorizedException("登录已过期，请重新登录");
        }
        return jwtUtil.getUserId(token);
    }

    @Operation(summary = "购物车列表", description = "返回当前用户购物车项，附带商品名称、图片、价格、库存、上下架状态；已下架或无库存的商品 offline 为 true")
    @GetMapping
    public Result<List<Map<String, Object>>> list(@RequestHeader("Authorization") String auth) {
        Long userId = parseUserId(auth);

        List<Cart> items = cartService.list(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
                        .orderByDesc(Cart::getCreatedAt)
        );

        if (items.isEmpty()) return Result.ok(new ArrayList<>());

        // 批量查商品
        Set<Long> pids = items.stream().map(Cart::getProductId).collect(Collectors.toSet());
        Map<Long, Product> productMap = productService.listByIds(pids).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Cart c : items) {
            Product p = productMap.get(c.getProductId());
            if (p == null) continue;

            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("productId", p.getId());
            m.put("name", p.getName());
            m.put("coverImage", p.getCoverImage());
            m.put("price", p.getPrice());
            m.put("spec", c.getSpec());
            m.put("quantity", c.getQuantity());
            m.put("selected", c.getSelected());
            m.put("stock", p.getStock());
            m.put("status", p.getStatus());
            m.put("offline", p.getStatus() == 0 || p.getStock() <= 0);
            result.add(m);
        }
        return Result.ok(result);
    }

    @Operation(summary = "加入购物车", description = "同一商品同规格会累加数量；校验商品是否下架、库存是否充足，累计数量不能超过库存")
    @PostMapping
    public Result<Void> add(@RequestHeader("Authorization") String auth,
                            @RequestBody Map<String, Object> body) {
        Long userId = parseUserId(auth);

        Long productId = Long.valueOf(body.get("productId").toString());
        String spec = (String) body.getOrDefault("spec", null);
        Integer quantity = Integer.valueOf(body.getOrDefault("quantity", 1).toString());

        if (quantity <= 0) return Result.fail("数量必须大于 0");

        Product product = productService.getById(productId);
        if (product == null) return Result.fail(404, "商品不存在");
        if (product.getStatus() == 0) return Result.fail("商品已下架");
        if (product.getStock() <= 0) return Result.fail("商品库存不足");

        // 查是否已有同商品 + 同规格
        LambdaQueryWrapper<Cart> w = new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, productId)
                .eq(Cart::getSpec, spec);
        Cart exist = cartService.getOne(w);

        int finalQty = (exist == null ? 0 : exist.getQuantity()) + quantity;
        if (finalQty > product.getStock()) {
            return Result.fail("库存不足，当前库存 " + product.getStock());
        }

        if (exist != null) {
            exist.setQuantity(finalQty);
            exist.setUpdatedAt(new Date());
            cartService.updateById(exist);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setSpec(spec);
            cart.setQuantity(quantity);
            cart.setSelected(1);
            cart.setCreatedAt(new Date());
            cart.setUpdatedAt(new Date());
            cartService.save(cart);
        }
        return Result.ok();
    }

    @Operation(summary = "修改购物车项", description = "可修改数量（需大于 0 且不超过库存）或勾选状态 selected；只能操作自己的购物车项")
    @PutMapping("/{id}")
    public Result<Void> update(@RequestHeader("Authorization") String auth,
                               @PathVariable Long id,
                               @RequestBody Map<String, Object> body) {
        Long userId = parseUserId(auth);

        Cart cart = cartService.getById(id);
        if (cart == null || !cart.getUserId().equals(userId)) {
            return Result.fail(404, "购物车项不存在");
        }

        if (body.containsKey("quantity")) {
            Integer q = Integer.valueOf(body.get("quantity").toString());
            if (q <= 0) return Result.fail("数量必须大于 0");

            Product p = productService.getById(cart.getProductId());
            if (p == null || p.getStatus() == 0) return Result.fail("商品已下架");
            if (q > p.getStock()) return Result.fail("库存不足，当前库存 " + p.getStock());

            cart.setQuantity(q);
        }
        if (body.containsKey("selected")) {
            cart.setSelected(Integer.valueOf(body.get("selected").toString()));
        }
        cart.setUpdatedAt(new Date());
        cartService.updateById(cart);
        return Result.ok();
    }

    @Operation(summary = "删除购物车项", description = "根据 id 删除购物车项，只能删除自己的购物车项")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@RequestHeader("Authorization") String auth,
                               @PathVariable Long id) {
        Long userId = parseUserId(auth);
        Cart cart = cartService.getById(id);
        if (cart == null || !cart.getUserId().equals(userId)) {
            return Result.fail(404, "购物车项不存在");
        }
        cartService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "购物车数量", description = "返回当前用户购物车项总数，用于前端角标显示")
    @GetMapping("/count")
    public Result<Integer> count(@RequestHeader("Authorization") String auth) {
        Long userId = parseUserId(auth);
        long n = cartService.count(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
        return Result.ok((int) n);
    }
}