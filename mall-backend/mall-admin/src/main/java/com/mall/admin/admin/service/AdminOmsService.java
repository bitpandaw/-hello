package com.mall.admin.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.admin.security.SecurityUser;
import com.mall.common.constant.OrderStatus;
import com.mall.common.api.ResultCode;
import com.mall.common.exception.BusinessException;
import com.mall.mbg.entity.OmsOrder;
import com.mall.mbg.entity.OmsOrderOperateHistory;
import com.mall.mbg.mapper.OmsOrderMapper;
import com.mall.mbg.mapper.OmsOrderOperateHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminOmsService {
    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderOperateHistoryMapper historyMapper;

    public IPage<OmsOrder> page(int p, int s, Integer status, String orderNo) {
        SecurityUser.requireAdmin();
        return omsOrderMapper.selectPage(new Page<>(p, s),
            new LambdaQueryWrapper<OmsOrder>()
                .eq(status != null, OmsOrder::getStatus, status)
                .like(orderNo != null && !orderNo.isBlank(), OmsOrder::getOrderNo, orderNo)
                .orderByDesc(OmsOrder::getCreateTime));
    }

    @Transactional(rollbackFor = Exception.class)
    public void ship(long orderId, String company, String sn) {
        SecurityUser.requireAdmin();
        OmsOrder o = omsOrderMapper.selectById(orderId);
        if (o == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (o.getStatus() == null || o.getStatus() != OrderStatus.TO_SHIP.getCode()) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "仅待发货可发货");
        }
        o.setStatus(OrderStatus.SHIPPED.getCode());
        o.setDeliveryCompany(company);
        o.setDeliverySn(sn);
        o.setDeliveryTime(LocalDateTime.now());
        omsOrderMapper.updateById(o);
        OmsOrderOperateHistory h = new OmsOrderOperateHistory();
        h.setOrderId(orderId);
        h.setOperator("admin");
        h.setNote("发货 " + company + " " + sn);
        h.setCreateTime(LocalDateTime.now());
        historyMapper.insert(h);
    }
}
