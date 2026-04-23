package com.mall.admin.oms.task;

import com.mall.common.constant.OrderStatus;
import com.mall.mbg.entity.OmsOrder;
import com.mall.mbg.mapper.OmsOrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.mall.admin.oms.service.OmsOrderService;

import java.time.LocalDateTime;

/**
 * 补偿关单(Quartz/定时任务: 对 MQ 的兜底, 论文中可二选一说明)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCompensateTask {
    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderService omsOrderService;

    @Scheduled(cron = "0 */2 * * * *")
    public void sweep() {
        var before = LocalDateTime.now().minusMinutes(32);
        var list = omsOrderMapper.selectList(new LambdaQueryWrapper<OmsOrder>()
            .eq(OmsOrder::getStatus, OrderStatus.PENDING_PAY.getCode())
            .lt(OmsOrder::getCreateTime, before));
        for (OmsOrder o : list) {
            try {
                omsOrderService.cancelUnpaid(o.getId());
            } catch (Exception e) {
                log.debug("sweep order {}", o.getId(), e);
            }
        }
    }
}
