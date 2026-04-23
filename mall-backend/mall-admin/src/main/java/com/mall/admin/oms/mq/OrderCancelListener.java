package com.mall.admin.oms.mq;

import com.mall.admin.oms.service.OmsOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCancelListener {
    private final OmsOrderService omsOrderService;

    @RabbitListener(queues = OrderRabbitConfig.Q_CANCEL)
    public void onMessage(String id) {
        try {
            omsOrderService.cancelUnpaid(Long.parseLong(id.trim()));
        } catch (Exception e) {
            log.error("order cancel handle {}", id, e);
        }
    }
}
