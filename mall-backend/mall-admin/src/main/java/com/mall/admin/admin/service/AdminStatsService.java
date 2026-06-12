package com.mall.admin.admin.service;

import com.mall.admin.security.SecurityUser;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminStatsService {
    private static final int STATS_DAYS = 30;
    private final JdbcTemplate jdbc;

    @Data
    public static class StatsVO {
        private long last30DayOrderCount;
        private BigDecimal last30DayGmv;
        private long last30DayNewUser;
    }

    @Data
    public static class GmvPoint {
        private String day;
        private BigDecimal gmv;
    }

    @Data
    public static class TopSku {
        private String name;
        private Long qty;
    }

    @Data
    public static class ChartVO {
        private List<GmvPoint> gmvLast30Days;
        private List<TopSku> top10Sku;
    }

    public StatsVO home() {
        SecurityUser.requireAdmin();
        StatsVO v = new StatsVO();
        Map<String, Object> a = jdbc.queryForMap(
            "SELECT COUNT(*) c, COALESCE(SUM(pay_amount),0) s " +
                "FROM oms_order " +
                "WHERE status>=1 AND create_time >= DATE_SUB(CURDATE(), INTERVAL ? DAY) AND deleted=0",
            STATS_DAYS - 1
        );
        v.setLast30DayOrderCount(((Number) a.get("c")).longValue());
        v.setLast30DayGmv(a.get("s") == null ? BigDecimal.ZERO : new BigDecimal(a.get("s").toString()));
        v.setLast30DayNewUser(jdbc.queryForObject(
            "SELECT COUNT(*) FROM ums_member WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL ? DAY) AND deleted=0",
            Long.class,
            STATS_DAYS - 1
        ));
        return v;
    }

    public ChartVO charts() {
        SecurityUser.requireAdmin();
        ChartVO c = new ChartVO();
        c.setGmvLast30Days(jdbc.query(
            "SELECT DATE_FORMAT(create_time,'%Y-%m-%d') AS d, COALESCE(SUM(pay_amount),0) AS gmv " +
                "FROM oms_order " +
                "WHERE status>=1 AND create_time >= DATE_SUB(CURDATE(),INTERVAL ? DAY) AND deleted=0 " +
                "GROUP BY DATE_FORMAT(create_time,'%Y-%m-%d') ORDER BY d",
            (rs, i) -> {
                GmvPoint p = new GmvPoint();
                p.setDay(rs.getString("d"));
                p.setGmv(rs.getBigDecimal("gmv"));
                return p;
            },
            STATS_DAYS - 1
        ));
        c.setTop10Sku(jdbc.query(
            "SELECT oi.spu_name n, SUM(oi.quantity) q FROM oms_order_item oi INNER JOIN oms_order o ON o.id=oi.order_id AND o.deleted=0 AND o.status>=1 GROUP BY oi.spu_id, oi.spu_name ORDER BY q DESC LIMIT 10",
            (rs, i) -> {
                TopSku t = new TopSku();
                t.setName(rs.getString("n"));
                t.setQty(rs.getLong("q"));
                return t;
            }));
        return c;
    }
}
