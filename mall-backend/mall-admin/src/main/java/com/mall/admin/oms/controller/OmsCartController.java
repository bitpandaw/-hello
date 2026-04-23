package com.mall.admin.oms.controller;

import com.mall.admin.oms.service.OmsCartService;
import com.mall.admin.security.SecurityUser;
import com.mall.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "购物车")
@RestController
@RequestMapping("/api/oms/cart")
@RequiredArgsConstructor
public class OmsCartController {
    private final OmsCartService cartService;

    @GetMapping
    @Operation(summary = "当前购物车(redis)")
    public Result<Map<String, OmsCartService.CartItem>> list() throws Exception {
        return Result.ok(cartService.getAll(SecurityUser.requireMember()));
    }

    @PostMapping("/{skuId}")
    @Operation(summary = "加车")
    public Result<?> add(@PathVariable long skuId, @RequestParam(defaultValue = "1") int quantity) throws Exception {
        cartService.add(SecurityUser.requireMember(), skuId, quantity);
        return Result.ok();
    }

    @PutMapping("/{skuId}")
    @Operation(summary = "改数量(同步价)")
    public Result<?> setQty(@PathVariable long skuId, @RequestParam int quantity) throws Exception {
        cartService.setQty(SecurityUser.requireMember(), skuId, quantity);
        return Result.ok();
    }

    @PostMapping("/{skuId}/select")
    @Operation(summary = "勾选/取消")
    public Result<?> sel(@PathVariable long skuId, @RequestParam boolean on) throws Exception {
        cartService.select(SecurityUser.requireMember(), skuId, on);
        return Result.ok();
    }

    @DeleteMapping("/{skuId}")
    @Operation(summary = "删项")
    public Result<?> del(@PathVariable long skuId) {
        cartService.remove(SecurityUser.requireMember(), skuId);
        return Result.ok();
    }

    @DeleteMapping
    @Operation(summary = "清空")
    public Result<?> clear() {
        cartService.clear(SecurityUser.requireMember());
        return Result.ok();
    }
}
