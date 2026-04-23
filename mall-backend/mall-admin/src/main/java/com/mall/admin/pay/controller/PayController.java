package com.mall.admin.pay.controller;

import com.mall.admin.oms.service.OmsOrderService;
import com.mall.admin.security.SecurityUser;
import com.mall.common.api.Result;
import com.mall.mbg.entity.PayRecord;
import com.mall.mbg.mapper.PayRecordMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.mall.mbg.entity.OmsOrder;
import com.mall.mbg.mapper.OmsOrderMapper;

import java.util.UUID;
import com.mall.common.exception.BusinessException;
import com.mall.common.api.ResultCode;

@Tag(name = "支付(模拟+预留说明见README)")
@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PayController {
    private final OmsOrderService omsOrderService;
    private final PayRecordMapper payRecordMapper;
    private final OmsOrderMapper omsOrderMapper;

    @PostMapping("/mock/{orderId}")
    @Operation(summary = "模拟支付成功(更新订单+写流水,真实对接见文档)")
    @Transactional(rollbackFor = Exception.class)
    public Result<PayRecord> mock(@PathVariable long orderId) {
        long mid = SecurityUser.requireMember();
        omsOrderService.payMock(mid, orderId);
        OmsOrder o = omsOrderMapper.selectById(orderId);
        if (o == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        PayRecord p = new PayRecord();
        p.setOrderId(orderId);
        p.setOutTradeNo("MOCK-" + UUID.randomUUID().toString().replace("-", ""));
        p.setPayAmount(o.getPayAmount());
        p.setPayStatus(1);
        p.setCreateTime(java.time.LocalDateTime.now());
        p.setUpdateTime(p.getCreateTime());
        payRecordMapper.insert(p);
        return Result.ok(p);
    }
}
