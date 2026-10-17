package com.yanleaf.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanleaf.domain.Coupon;
import com.yanleaf.domain.UserCoupon;
import com.yanleaf.service.CouponService;
import com.yanleaf.service.UserCouponService;
import com.yanleaf.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/coupons")
@CrossOrigin
@Tag(name = "AdminCouponController", description = "后台优惠券管理接口")

public class AdminCouponController {

    @Autowired private CouponService couponService;
    @Autowired private UserCouponService userCouponService;

    @Operation(summary = "优惠券列表", description = "分页查询优惠券，支持按状态和名称关键字筛选")
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<Coupon> w = new LambdaQueryWrapper<>();
        if (status != null) w.eq(Coupon::getStatus, status);
        if (keyword != null && !keyword.isBlank()) {
            w.like(Coupon::getName, keyword);
        }
        w.orderByDesc(Coupon::getId);

        Page<Coupon> page = couponService.page(new Page<>(pageNum, pageSize), w);

        Map<String, Object> data = new HashMap<>();
        data.put("records", page.getRecords());
        data.put("total", page.getTotal());
        data.put("pageNum", page.getCurrent());
        data.put("pageSize", page.getSize());
        return Result.ok(data);
    }

    @Operation(summary = "新增优惠券", description = "校验名称、类型、优惠值、有效期；已领取数和创建时间由后端填充，状态默认 1")
    @PostMapping
    public Result<Void> add(@RequestBody Coupon coupon) {
        String err = validate(coupon);
        if (err != null) return Result.fail(400, err);

        coupon.setId(null);
        coupon.setReceivedCount(0);
        coupon.setCreatedAt(new Date());
        if (coupon.getStatus() == null) coupon.setStatus(1);
        couponService.save(coupon);
        return Result.ok();
    }

    @Operation(summary = "编辑优惠券", description = "根据 id 更新优惠券，已领取数量不允许通过编辑修改")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Integer id, @RequestBody Coupon coupon) {
        Coupon exist = couponService.getById(id);
        if (exist == null) return Result.fail(404, "优惠券不存在");

        String err = validate(coupon);
        if (err != null) return Result.fail(400, err);

        coupon.setId(id);
        // 发放量、已领取不能通过编辑改
        coupon.setReceivedCount(null);
        couponService.updateById(coupon);
        return Result.ok();
    }

    @Operation(summary = "优惠券上下架", description = "status 只能是 0 或 1；0 表示下架，1 表示上架")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Integer id,
                                     @RequestParam Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            return Result.fail(400, "status 只能是 0 或 1");
        }
        Coupon c = couponService.getById(id);
        if (c == null) return Result.fail(404, "优惠券不存在");

        Coupon update = new Coupon();
        update.setId(id);
        update.setStatus(status);
        couponService.updateById(update);
        return Result.ok();
    }

    @Operation(summary = "删除优惠券", description = "已被用户领取的优惠券不允许删除，需改为停用")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        Coupon c = couponService.getById(id);
        if (c == null) return Result.fail(404, "优惠券不存在");

        // 有人领过就不让删
        long used = userCouponService.count(
                new LambdaQueryWrapper<UserCoupon>().eq(UserCoupon::getCouponId, id));
        if (used > 0) {
            return Result.fail(400, "该优惠券已被领取，无法删除，请改为停用");
        }

        couponService.removeById(id);
        return Result.ok();
    }

    /** 校验 */
    private String validate(Coupon c) {
        if (c.getName() == null || c.getName().isBlank()) return "优惠券名称不能为空";
        if (c.getType() == null) return "请选择优惠券类型";
        if (c.getDiscountValue() == null || c.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            return "优惠金额/折扣必须大于 0";
        }
        if (c.getType() == 2) {
            // 折扣类型：0 < 值 < 1
            if (c.getDiscountValue().compareTo(BigDecimal.ONE) >= 0) {
                return "折扣率必须小于 1（如 0.95 表示 95 折）";
            }
        }
        if (c.getStartTime() == null || c.getEndTime() == null) return "请选择有效期";
        if (!c.getEndTime().after(c.getStartTime())) return "结束时间必须晚于开始时间";
        return null;
    }
}