package com.yanleaf.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yanleaf.domain.*;
import com.yanleaf.exception.UnauthorizedException;
import com.yanleaf.service.*;
import com.yanleaf.utils.JwtUtil;
import com.yanleaf.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/order")
@CrossOrigin
@Tag(name = "OrderController", description = "订单接口")

public class OrderController {

    @Autowired
    private OrdersService ordersService;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private CartService cartService;
    @Autowired
    private ProductService productService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private UserCouponService userCouponService;
    @Autowired
    private CouponService couponService;
    @Autowired
    private UserService userService;
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

    @Operation(summary = "下单前预览金额", description = "根据当前勾选的购物车项和可选优惠券计算商品总额、运费（满 99 包邮，否则 10 元）、优惠金额和应付总额；不创建订单")
    @PostMapping("/preview")
    public Result<Map<String, Object>> preview(@RequestHeader("Authorization") String auth,
                                               @RequestBody Map<String, Object> body) {
        Long userId = parseUserId(auth);
        Long couponUserId = body.get("userCouponId") == null ? null
                : Long.valueOf(body.get("userCouponId").toString());

        // 1. 取勾选的购物车项
        List<Cart> selectedCarts = cartService.list(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
                        .eq(Cart::getSelected, 1)
        );
        if (selectedCarts.isEmpty()) {
            return Result.fail("请先勾选要结算的商品");
        }

        // 2. 拉商品快照
        Map<Long, Product> productMap = new HashMap<>();
        for (Cart c : selectedCarts) {
            Product p = productService.getById(c.getProductId());
            if (p == null) return Result.fail("商品 " + c.getProductId() + " 不存在");
            if (p.getStatus() == 0) return Result.fail("商品「" + p.getName() + "」已下架");
            if (p.getStock() < c.getQuantity()) {
                return Result.fail("商品「" + p.getName() + "」库存不足");
            }
            productMap.put(p.getId(), p);
        }

        // 3. 商品总额
        BigDecimal productAmount = BigDecimal.ZERO;
        List<Map<String, Object>> itemList = new ArrayList<>();
        for (Cart c : selectedCarts) {
            Product p = productMap.get(c.getProductId());
            BigDecimal subtotal = p.getPrice().multiply(BigDecimal.valueOf(c.getQuantity()));
            productAmount = productAmount.add(subtotal);

            Map<String, Object> m = new HashMap<>();
            m.put("cartId", c.getId());
            m.put("productId", p.getId());
            m.put("name", p.getName());
            m.put("coverImage", p.getCoverImage());
            m.put("spec", c.getSpec());
            m.put("price", p.getPrice());
            m.put("quantity", c.getQuantity());
            m.put("subtotal", subtotal);
            itemList.add(m);
        }

        // 4. 运费：满99包邮，否则10元
        BigDecimal shippingFee = productAmount.compareTo(new BigDecimal("99")) >= 0
                ? BigDecimal.ZERO : new BigDecimal("10");

        // 5. 优惠券
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (couponUserId != null) {
            UserCoupon uc = userCouponService.getById(couponUserId);
            if (uc == null || !uc.getUserId().equals(userId)) {
                return Result.fail("优惠券不存在");
            }
            if (uc.getStatus() != 0) {
                return Result.fail("优惠券已使用或已过期");
            }
            Coupon coupon = couponService.getById(uc.getCouponId());
            if (coupon == null || coupon.getStatus() == 0) {
                return Result.fail("优惠券无效");
            }
            Date now = new Date();
            if (now.before(coupon.getStartTime()) || now.after(coupon.getEndTime())) {
                return Result.fail("优惠券不在有效期内");
            }
            if (productAmount.compareTo(coupon.getMinAmount()) < 0) {
                return Result.fail("未达到使用门槛");
            }

            // 计算优惠
            if (coupon.getType() == 1 || coupon.getType() == 3) {
                // 满减 / 无门槛
                discountAmount = coupon.getDiscountValue();
            } else if (coupon.getType() == 2) {
                // 折扣：discount_value = 0.95 表示打95折，减5%
                BigDecimal off = productAmount.multiply(
                        BigDecimal.ONE.subtract(coupon.getDiscountValue())
                );
                discountAmount = off.setScale(2, RoundingMode.HALF_UP);
            }

            // 优惠不能超过商品总额
            if (discountAmount.compareTo(productAmount) > 0) {
                discountAmount = productAmount;
            }
        }

        BigDecimal totalAmount = productAmount.add(shippingFee).subtract(discountAmount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("items", itemList);
        result.put("productAmount", productAmount);
        result.put("shippingFee", shippingFee);
        result.put("discountAmount", discountAmount);
        result.put("totalAmount", totalAmount);
        return Result.ok(result);
    }

    @Operation(summary = "创建订单", description = "校验收货地址和勾选商品，扣库存、核销优惠券、清空已勾选购物车项并生成订单；整个过程在事务中执行")
    @PostMapping("/create")
    @Transactional
    public Result<Map<String, Object>> create(@RequestHeader("Authorization") String auth,
                                              @RequestBody Map<String, Object> body) {
        Long userId = parseUserId(auth);

        Long addressId = Long.valueOf(body.get("addressId").toString());
        Long couponUserId = body.get("userCouponId") == null ? null
                : Long.valueOf(body.get("userCouponId").toString());
        String remark = (String) body.getOrDefault("remark", null);

        // 校验收货地址
        Address address = addressService.getById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            return Result.fail("收货地址无效");
        }

        // 取勾选的购物车
        List<Cart> selectedCarts = cartService.list(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
                        .eq(Cart::getSelected, 1)
        );
        if (selectedCarts.isEmpty()) {
            return Result.fail("请先勾选要结算的商品");
        }

        // 校验商品 & 算钱
        BigDecimal productAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        List<Product> toUpdateStock = new ArrayList<>();

        for (Cart c : selectedCarts) {
            Product p = productService.getById(c.getProductId());
            if (p == null) return Result.fail("商品不存在");
            if (p.getStatus() == 0) return Result.fail("商品「" + p.getName() + "」已下架");
            if (p.getStock() < c.getQuantity()) {
                return Result.fail("商品「" + p.getName() + "」库存不足");
            }

            BigDecimal subtotal = p.getPrice().multiply(BigDecimal.valueOf(c.getQuantity()));
            productAmount = productAmount.add(subtotal);

            OrderItem oi = new OrderItem();
            oi.setProductId(p.getId());
            oi.setProductName(p.getName());
            oi.setProductImage(p.getCoverImage());
            oi.setSpec(c.getSpec());
            oi.setPrice(p.getPrice());
            oi.setQuantity(c.getQuantity());
            oi.setSubtotal(subtotal);
            orderItems.add(oi);

            // 扣库存
            p.setStock(p.getStock() - c.getQuantity());
            p.setSales(p.getSales() + c.getQuantity());
            toUpdateStock.add(p);
        }

        // 运费
        BigDecimal shippingFee = productAmount.compareTo(new BigDecimal("99")) >= 0
                ? BigDecimal.ZERO : new BigDecimal("10");

        // 优惠券
        BigDecimal discountAmount = BigDecimal.ZERO;
        UserCoupon userCoupon = null;
        if (couponUserId != null) {
            userCoupon = userCouponService.getById(couponUserId);
            if (userCoupon == null || !userCoupon.getUserId().equals(userId)
                    || userCoupon.getStatus() != 0) {
                return Result.fail("优惠券不可用");
            }
            Coupon coupon = couponService.getById(userCoupon.getCouponId());
            Date now = new Date();
            if (coupon == null || coupon.getStatus() == 0
                    || now.before(coupon.getStartTime()) || now.after(coupon.getEndTime())) {
                return Result.fail("优惠券失效");
            }
            if (productAmount.compareTo(coupon.getMinAmount()) < 0) {
                return Result.fail("未达到使用门槛");
            }

            if (coupon.getType() == 1 || coupon.getType() == 3) {
                discountAmount = coupon.getDiscountValue();
            } else if (coupon.getType() == 2) {
                discountAmount = productAmount.multiply(
                        BigDecimal.ONE.subtract(coupon.getDiscountValue())
                ).setScale(2, RoundingMode.HALF_UP);
            }
            if (discountAmount.compareTo(productAmount) > 0) {
                discountAmount = productAmount;
            }
        }

        BigDecimal totalAmount = productAmount.add(shippingFee).subtract(discountAmount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) totalAmount = BigDecimal.ZERO;

        // 生成订单号：yyyyMMddHHmmss + 4位随机
        String orderNo = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + String.format("%04d", new Random().nextInt(10000));

        // 创建订单
        Orders order = new Orders ();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setAddressId(addressId);
        order.setProductAmount(productAmount);
        order.setShippingFee(shippingFee);
        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(totalAmount);
        order.setStatus(0);   // 待付款
        order.setRemark(remark);
        order.setCreatedAt(new Date());
        order.setUpdatedAt(new Date());
        ordersService.save(order);

        // 创建订单明细
        for (OrderItem oi : orderItems) {
            oi.setOrderId(order.getId());
            orderItemService.save(oi);
        }

        // 更新库存
        for (Product p : toUpdateStock) {
            productService.updateById(p);
        }

        // 核销优惠券
        if (userCoupon != null) {
            userCoupon.setStatus(1);
            userCoupon.setUsedOrderId(order.getId());
            userCoupon.setUsedAt(new Date());
            userCouponService.updateById(userCoupon);
        }

        // 清空购物车勾选项
        cartService.remove(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getSelected, 1)
        );

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNo", orderNo);
        result.put("totalAmount", totalAmount);
        return Result.ok(result);
    }

    @Operation(summary = "订单详情", description = "根据订单 id 返回订单信息和订单明细，只能查看自己的订单")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@RequestHeader("Authorization") String auth,
                                              @PathVariable Long id) {
        Long userId = parseUserId(auth);
        Orders order = ordersService.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.fail(404, "订单不存在");
        }
        List<OrderItem> items = orderItemService.list(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id)
        );
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("items", items);
        return Result.ok(result);
    }
    @Operation(summary = "我的订单列表", description = "分页返回当前用户订单，可按状态筛选，每个订单附带订单明细")
    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestHeader("Authorization") String auth,
                                            @RequestParam(required = false) Integer status,
                                            @RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = parseUserId(auth);
        LambdaQueryWrapper<Orders> w = new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, userId)
                .orderByDesc(Orders::getCreatedAt);
        if (status != null) w.eq(Orders::getStatus, status);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Orders> page =
                ordersService.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize), w);

        // 每个订单带上明细
        List<Map<String, Object>> records = new ArrayList<>();
        for (Orders o : page.getRecords()) {
            List<OrderItem> items = orderItemService.list(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, o.getId())
            );
            Map<String, Object> m = new HashMap<>();
            m.put("order", o);
            m.put("items", items);
            records.add(m);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", page.getTotal());
        result.put("current", page.getCurrent());
        return Result.ok(result);
    }

    @Operation(summary = "取消订单", description = "仅待付款（status=0）订单可取消；取消时回滚库存、退还优惠券（过期的作废）、若已支付则扣回积分")
    @PutMapping("/{id}/cancel")
    @Transactional
    public Result<Void> cancel(@RequestHeader("Authorization") String auth,
                               @PathVariable Long id) {
        Long userId = parseUserId(auth);
        Orders order = ordersService.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.fail(404, "订单不存在");
        }
        if (order.getStatus() != 0) {
            return Result.fail("只有待付款订单才能取消");
        }

        // 回滚库存
        List<OrderItem> items = orderItemService.list(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id)
        );
        for (OrderItem oi : items) {
            Product p = productService.getById(oi.getProductId());
            if (p != null) {
                p.setStock(p.getStock() + oi.getQuantity());
                p.setSales(Math.max(0, p.getSales() - oi.getQuantity()));
                productService.updateById(p);
            }
        }

        // 退还优惠券
        UserCoupon uc = userCouponService.getOne(
                new LambdaQueryWrapper<UserCoupon>()
                        .eq(UserCoupon::getUsedOrderId, id)
                        .last("LIMIT 1")
        );
        if (uc != null) {
            // 判断券是否过期
            Coupon coupon = couponService.getById(uc.getCouponId());
            boolean expired = coupon == null
                    || coupon.getStatus() == 0
                    || new Date().after(coupon.getEndTime());
            uc.setUsedOrderId(null);
            uc.setUsedAt(null);
            uc.setStatus(expired ? 2 : 0);   // 过期作废，未过期退回
            userCouponService.updateById(uc);
        }
        // 如果订单已支付，扣回积分
        if (order.getPaidAt() != null) {
            User user = userService.getById(userId);
            if (user != null) {
                int points = order.getTotalAmount().intValue();
                user.setPoint(Math.max(0, (user.getPoint() == null ? 0 : user.getPoint()) - points));
                userService.updateById(user);
            }
        }

        order.setStatus(4);   // 已取消
        order.setUpdatedAt(new Date());
        ordersService.updateById(order);
        return Result.ok();
    }

    @Operation(summary = "确认收货", description = "仅待收货（status=2）订单可确认，确认后订单状态变为已完成（status=3）")
    @PutMapping("/{id}/receive")
    public Result<Void> receive(@RequestHeader("Authorization") String auth,
                                @PathVariable Long id) {
        Long userId = parseUserId(auth);
        Orders order = ordersService.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return Result.fail(404, "订单不存在");
        }
        if (order.getStatus() != 2) {
            return Result.fail("只有待收货订单才能确认收货");
        }
        order.setStatus(3);   // 已完成
        order.setReceivedAt(new Date());
        order.setUpdatedAt(new Date());
        ordersService.updateById(order);
        return Result.ok();
    }

}