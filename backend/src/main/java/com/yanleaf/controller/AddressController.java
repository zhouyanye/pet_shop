package com.yanleaf.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yanleaf.domain.Address;
import com.yanleaf.exception.UnauthorizedException;
import com.yanleaf.service.AddressService;
import com.yanleaf.utils.JwtUtil;
import com.yanleaf.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/address")
@CrossOrigin
@Tag(name = "AddressController", description = "收货地址管理接口")
public class AddressController {

    @Autowired
    private AddressService addressService;

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

    @Operation(summary = "地址列表", description = "获取当前登录用户的所有收货地址，默认地址排在最前")
    @GetMapping
    public Result<List<Address>> list(@RequestHeader("Authorization") String auth) {
        Long userId = parseUserId(auth);
        List<Address> list = addressService.list(
                new LambdaQueryWrapper<Address>()
                        .eq(Address::getUserId, userId)
                        .orderByDesc(Address::getIsDefault)
                        .orderByDesc(Address::getCreatedAt)
        );
        return Result.ok(list);
    }

    @Operation(summary = "默认地址", description = "获取当前登录用户的默认收货地址")
    @GetMapping("/default")
    public Result<Address> getDefault(@RequestHeader("Authorization") String auth) {
        Long userId = parseUserId(auth);
        Address addr = addressService.getOne(
                new LambdaQueryWrapper<Address>()
                        .eq(Address::getUserId, userId)
                        .eq(Address::getIsDefault, 1)
                        .last("LIMIT 1")
        );
        return Result.ok(addr);
    }

    @Operation(summary = "新增地址", description = "新增收货地址，若设为默认则会自动取消其他默认地址")
    @PostMapping
    public Result<Void> add(@RequestHeader("Authorization") String auth,
                            @RequestBody Address req) {
        Long userId = parseUserId(auth);

        if (req.getReceiver() == null || req.getPhone() == null
                || req.getProvince() == null || req.getCity() == null
                || req.getDistrict() == null || req.getDetail() == null) {
            return Result.fail("请填写完整地址信息");
        }

        req.setId(null);
        req.setUserId(userId);
        req.setCreatedAt(new Date());
        req.setUpdatedAt(new Date());

        // 如果设为默认，先把其他默认取消
        if (req.getIsDefault() != null && req.getIsDefault() == 1) {
            clearDefault(userId);
        } else {
            req.setIsDefault(0);
        }

        addressService.save(req);
        return Result.ok();
    }

    /** 修改 PUT /api/address/{id} */
    @PutMapping("/{id}")
    public Result<Void> update(@RequestHeader("Authorization") String auth,
                               @PathVariable Long id,
                               @RequestBody Address req) {
        Long userId = parseUserId(auth);

        Address exist = addressService.getById(id);
        if (exist == null || !exist.getUserId().equals(userId)) {
            return Result.fail(404, "地址不存在");
        }

        req.setId(id);
        req.setUserId(userId);
        req.setUpdatedAt(new Date());

        if (req.getIsDefault() != null && req.getIsDefault() == 1) {
            clearDefault(userId);
        }

        addressService.updateById(req);
        return Result.ok();
    }

    /** 删除 DELETE /api/address/{id} */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@RequestHeader("Authorization") String auth,
                               @PathVariable Long id) {
        Long userId = parseUserId(auth);
        Address exist = addressService.getById(id);
        if (exist == null || !exist.getUserId().equals(userId)) {
            return Result.fail(404, "地址不存在");
        }
        addressService.removeById(id);
        return Result.ok();
    }

    /** 设为默认 PUT /api/address/{id}/default */
    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@RequestHeader("Authorization") String auth,
                                   @PathVariable Long id) {
        Long userId = parseUserId(auth);
        Address exist = addressService.getById(id);
        if (exist == null || !exist.getUserId().equals(userId)) {
            return Result.fail(404, "地址不存在");
        }
        clearDefault(userId);

        Address update = new Address();
        update.setId(id);
        update.setIsDefault(1);
        update.setUpdatedAt(new Date());
        addressService.updateById(update);
        return Result.ok();
    }

    /** 工具：清掉该用户所有默认地址 */
    private void clearDefault(Long userId) {
        Address clear = new Address();
        clear.setIsDefault(0);
        addressService.update(clear,
                new LambdaQueryWrapper<Address>()
                        .eq(Address::getUserId, userId)
                        .eq(Address::getIsDefault, 1)
        );
    }
}