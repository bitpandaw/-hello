package com.mall.admin.oms.service;

import com.mall.mbg.entity.PmsSku;
import com.mall.mbg.mapper.PmsSkuMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class OmsCartService {
    private final PmsSkuMapper skuMapper;
    private final JdbcTemplate jdbcTemplate;
    private static final Map<String, Boolean> SELECTED = new ConcurrentHashMap<>();

    private String selectedKey(long userId, long skuId) {
        return userId + ":" + skuId;
    }

    public Map<String, CartItem> getAll(long userId) {
        String sql = "SELECT sku_id, spu_id, quantity FROM oms_cart_item WHERE member_id=? AND deleted=0 ORDER BY id DESC";
        Map<String, CartItem> r = new HashMap<>();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, userId);
        for (Map<String, Object> row : rows) {
            long skuId = ((Number) row.get("sku_id")).longValue();
            long spuId = ((Number) row.get("spu_id")).longValue();
            int qty = ((Number) row.get("quantity")).intValue();
            PmsSku sku = skuMapper.selectById(skuId);
            if (sku == null) {
                continue;
            }
            CartItem c = new CartItem();
            c.setSkuId(skuId);
            c.setSpuId(spuId);
            c.setQuantity(qty);
            c.setPrice(sku.getPrice());
            c.setSpecJson(sku.getSpecJson());
            c.setSelected(SELECTED.getOrDefault(selectedKey(userId, skuId), true));
            r.put(String.valueOf(skuId), c);
        }
        return r;
    }

    public void add(long userId, long skuId, int qty) {
        PmsSku s = skuMapper.selectById(skuId);
        if (s == null) {
            return;
        }
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM oms_cart_item WHERE member_id=? AND sku_id=? AND deleted=0",
            Integer.class, userId, skuId
        );
        if (count != null && count > 0) {
            jdbcTemplate.update(
                "UPDATE oms_cart_item SET quantity = quantity + ?, update_time=NOW() WHERE member_id=? AND sku_id=? AND deleted=0",
                Math.max(1, qty), userId, skuId
            );
        } else {
            jdbcTemplate.update(
                "INSERT INTO oms_cart_item(member_id,spu_id,sku_id,quantity,deleted,create_time,update_time) VALUES(?,?,?,?,0,NOW(),NOW())",
                userId, s.getSpuId(), skuId, Math.max(1, qty)
            );
        }
        SELECTED.put(selectedKey(userId, skuId), true);
    }

    public void setQty(long userId, long skuId, int qty) {
        if (qty <= 0) {
            remove(userId, skuId);
            return;
        }
        jdbcTemplate.update(
            "UPDATE oms_cart_item SET quantity=?, update_time=NOW() WHERE member_id=? AND sku_id=? AND deleted=0",
            qty, userId, skuId
        );
    }

    public void select(long userId, long skuId, boolean on) {
        SELECTED.put(selectedKey(userId, skuId), on);
    }

    public void remove(long userId, long skuId) {
        jdbcTemplate.update(
            "UPDATE oms_cart_item SET deleted=1, update_time=NOW() WHERE member_id=? AND sku_id=? AND deleted=0",
            userId, skuId
        );
        SELECTED.remove(selectedKey(userId, skuId));
    }

    public void clear(long userId) {
        jdbcTemplate.update("UPDATE oms_cart_item SET deleted=1, update_time=NOW() WHERE member_id=? AND deleted=0", userId);
        SELECTED.keySet().removeIf(k -> k.startsWith(userId + ":"));
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
