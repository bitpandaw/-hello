package com.mall.admin.oms.mq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderMq {
    private final RabbitTemplate rabbitTemplate;

    public void sendTtl(long orderId, long delayMs) {
        String body = String.valueOf(orderId);
        rabbitTemplate.convertAndSend("", OrderRabbitConfig.Q_TTL, body, m -> {
            m.getMessageProperties().setExpiration(String.valueOf(delayMs));
            return m;
        });
    }
}
