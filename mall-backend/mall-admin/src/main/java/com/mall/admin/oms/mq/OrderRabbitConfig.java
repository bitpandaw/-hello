package com.mall.admin.oms.mq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class OrderRabbitConfig {
    public static final String DLX = "mall.order.dlx";
    public static final String Q_TTL = "mall.order.ttl.hold";
    public static final String Q_CANCEL = "mall.order.cancel";

    @Bean
    public DirectExchange dlx() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    public Queue cancelQueue() {
        return new Queue(Q_CANCEL, true);
    }

    @Bean
    public Binding cancelBind(Queue cancelQueue, DirectExchange dlx) {
        return BindingBuilder.bind(cancelQueue).to(dlx).with("order.cancel");
    }

    @Bean
    public Queue ttlQueue() {
        Map<String, Object> a = new HashMap<>();
        a.put("x-dead-letter-exchange", DLX);
        a.put("x-dead-letter-routing-key", "order.cancel");
        return new Queue(Q_TTL, true, false, false, a);
    }
}
