package com.mall.admin.oms.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.mbg.entity.PmsSku;
import com.mall.mbg.mapper.PmsSkuMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OmsCartService {
    private static final String C = "cart:";
    private final StringRedisTemplate redis;
    private final PmsSkuMapper skuMapper;
    private final ObjectMapper objectMapper;

    public String key(long userId) {
        return C + userId;
    }

    public Map<String, CartItem> getAll(long userId) throws JsonProcessingException {
        Map<Object, Object> m = redis.opsForHash().entries(key(userId));
        Map<String, CartItem> r = new HashMap<>();
        for (Map.Entry<Object, Object> e : m.entrySet()) {
            r.put(e.getKey().toString(), objectMapper.readValue(e.getValue().toString(), CartItem.class));
        }
        return r;
    }

    public void add(long userId, long skuId, int qty) throws JsonProcessingException {
        PmsSku s = skuMapper.selectById(skuId);
        if (s == null) {
            return;
        }
        CartItem c = new CartItem();
        c.setSkuId(skuId);
        c.setSpuId(s.getSpuId());
        c.setQuantity(qty);
        c.setSelected(true);
        c.setPrice(s.getPrice());
        c.setSpecJson(s.getSpecJson());
        redis.opsForHash().put(key(userId), String.valueOf(skuId), objectMapper.writeValueAsString(c));
    }

    public void setQty(long userId, long skuId, int qty) throws JsonProcessingException {
        Object o = redis.opsForHash().get(key(userId), String.valueOf(skuId));
        if (o == null) {
            return;
        }
        CartItem c = objectMapper.readValue(o.toString(), CartItem.class);
        c.setQuantity(qty);
        PmsSku s = skuMapper.selectById(skuId);
        if (s != null) {
            c.setPrice(s.getPrice());
        }
        redis.opsForHash().put(key(userId), String.valueOf(skuId), objectMapper.writeValueAsString(c));
    }

    public void select(long userId, long skuId, boolean on) throws JsonProcessingException {
        Object o = redis.opsForHash().get(key(userId), String.valueOf(skuId));
        if (o == null) {
            return;
        }
        CartItem c = objectMapper.readValue(o.toString(), CartItem.class);
        c.setSelected(on);
        redis.opsForHash().put(key(userId), String.valueOf(skuId), objectMapper.writeValueAsString(c));
    }

    public void remove(long userId, long skuId) {
        redis.opsForHash().delete(key(userId), String.valueOf(skuId));
    }

    public void clear(long userId) {
        redis.delete(key(userId));
    }

    @Data
    public static class CartItem {
        private long skuId;
        private long spuId;
        private int quantity;
        private boolean selected;
        private BigDecimal price;
        private String specJson;
    }
}
