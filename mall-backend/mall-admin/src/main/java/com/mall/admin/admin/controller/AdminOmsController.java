package com.mall.admin.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mall.admin.admin.service.AdminOmsService;
import com.mall.common.api.Result;
import com.mall.mbg.entity.OmsOrder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台-订单")
@RestController
@RequestMapping("/admin/oms/orders")
@RequiredArgsConstructor
public class AdminOmsController {
    private final AdminOmsService adminOmsService;

    @GetMapping
    @Operation(summary = "订单分页")
    public Result<IPage<OmsOrder>> list(
        @RequestParam(defaultValue = "1") int p,
        @RequestParam(defaultValue = "10") int s,
        @RequestParam(required = false) Integer status,
        @RequestParam(required = false) String orderNo) {
        return Result.ok(adminOmsService.page(p, s, status, orderNo));
    }

    @PostMapping("/{id}/ship")
    @Operation(summary = "发货")
    public Result<?> ship(@PathVariable long id, @RequestBody @jakarta.validation.Valid ShipReq r) {
        adminOmsService.ship(id, r.getDeliveryCompany(), r.getDeliverySn());
        return Result.ok();
    }

    @Data
    public static class ShipReq {
        @NotBlank
        private String deliveryCompany;
        @NotBlank
        private String deliverySn;
    }
}
