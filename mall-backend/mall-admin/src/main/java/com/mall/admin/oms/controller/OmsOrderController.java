package com.mall.admin.oms.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mall.admin.oms.service.OmsOrderService;
import com.mall.admin.security.SecurityUser;
import com.mall.common.api.Result;
import com.mall.mbg.entity.OmsOrder;
import com.mall.mbg.entity.OmsOrderItem;
import com.mall.mbg.mapper.OmsOrderItemMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.mall.common.annotation.RateLimit;

@Tag(name = "订单")
@RestController
@RequestMapping("/api/oms/orders")
@RequiredArgsConstructor
public class OmsOrderController {
    private final OmsOrderService omsOrderService;
    private final OmsOrderItemMapper omsOrderItemMapper;

    @PostMapping("/preview")
    @Operation(summary = "预订单/试算(地址+可选用户券)")
    public Result<OmsOrderService.PreviewVO> preview(@RequestBody @jakarta.validation.Valid PreviewReq r) throws Exception {
        return Result.ok(omsOrderService.preview(SecurityUser.requireMember(), r.getAddressId(), r.getCouponHistoryId()));
    }

    @PostMapping
    @Operation(summary = "从购物车创建订单(地址+可选领券记录ID)")
    @RateLimit(key = "order", max = 10, seconds = 60)
    public Result<?> create(@RequestBody @jakarta.validation.Valid CreateReq r) throws Exception {
        OmsOrder o = omsOrderService.create(SecurityUser.requireMember(), r.getAddressId(), r.getCouponHistoryId());
        return Result.ok(o);
    }

    @GetMapping
    @Operation(summary = "我的订单")
    public Result<IPage<OmsOrder>> my(@RequestParam(defaultValue = "1") int p, @RequestParam(defaultValue = "10") int s, @RequestParam(required = false) Integer status) {
        return Result.ok(omsOrderService.page(SecurityUser.requireMember(), status, p, s));
    }

    @GetMapping("/{id}")
    @Operation(summary = "订单详情")
    public Result<Detail> detail(@PathVariable long id) {
        OmsOrder o = omsOrderService.detail(SecurityUser.requireMember(), id);
        java.util.List<OmsOrderItem> is = omsOrderItemMapper.selectList(new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, id));
        Detail d = new Detail();
        d.setOrder(o);
        d.setItems(is);
        return Result.ok(d);
    }

    @Data
    public static class PreviewReq {
        @jakarta.validation.constraints.NotNull
        private Long addressId;
        private Long couponHistoryId;
    }

    @Data
    public static class CreateReq {
        @jakarta.validation.constraints.NotNull
        private Long addressId;
        private Long couponHistoryId;
    }

    @Data
    public static class Detail {
        private OmsOrder order;
        private java.util.List<OmsOrderItem> items;
    }
}
