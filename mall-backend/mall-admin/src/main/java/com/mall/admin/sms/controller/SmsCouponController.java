package com.mall.admin.sms.controller;

import com.mall.admin.security.SecurityUser;
import com.mall.admin.sms.service.SmsCouponService;
import com.mall.common.api.Result;
import com.mall.mbg.entity.SmsCoupon;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "优惠券")
@RestController
@RequestMapping("/api/sms/coupons")
@RequiredArgsConstructor
public class SmsCouponController {
    private final SmsCouponService svc;

    @GetMapping
    @Operation(summary = "券列表")
    public Result<List<SmsCoupon>> all() {
        return Result.ok(svc.list());
    }

    @PostMapping("/{id}/take")
    @Operation(summary = "领券(限领+Redis防并发)")
    public Result<?> take(@PathVariable("id") long id) {
        svc.take(SecurityUser.requireMember(), id);
        return Result.ok();
    }
}
