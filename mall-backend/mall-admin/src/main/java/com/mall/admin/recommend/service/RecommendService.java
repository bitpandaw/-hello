package com.mall.admin.recommend.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.admin.security.SecurityUser;
import com.mall.mbg.entity.PmsProduct;
import com.mall.mbg.mapper.PmsProductMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendService {
    private final JdbcTemplate jdbcTemplate;
    private final RestTemplate restTemplate;
    private final PmsProductMapper productMapper;
    private final ObjectMapper objectMapper;

    @Value("${recommend.sasrec.enabled:true}")
    private boolean enabled;
    @Value("${recommend.sasrec.base-url:http://127.0.0.1:8008}")
    private String baseUrl;
    @Value("${recommend.sasrec.timeout-ms:1200}")
    private int timeoutMs;
    @Value("${recommend.sasrec.max-retry:1}")
    private int maxRetry;
    @Value("${recommend.default-size:8}")
    private int defaultSize;
    @Value("${recommend.cache-ttl-minutes:120}")
    private int cacheTtlMinutes;

    public RecommendResponse guess(Long memberId, Integer size, String requestId) {
        int limit = normalizeSize(size);
        List<Long> ids = memberId == null ? List.of() : loadCached(memberId, "guess");
        if (ids.isEmpty() && memberId != null) {
            ids = requestPredict(memberId, "guess", limit);
            if (!ids.isEmpty()) {
                saveCache(memberId, "guess", ids);
            }
        }
        if (ids.isEmpty()) {
            ids = hotFallback(limit);
        }
        return buildResponse(ids, requestId, "guess");
    }

    public RecommendResponse similar(Long memberId, Long itemId, Integer size, String requestId) {
        int limit = normalizeSize(size);
        List<Long> ids = new ArrayList<>();
        if (memberId != null) {
            ids = requestPredict(memberId, "similar", limit);
        }
        if (ids.isEmpty() && itemId != null) {
            ids = sameCategoryFallback(itemId, limit);
        }
        if (ids.isEmpty()) {
            ids = hotFallback(limit);
        }
        return buildResponse(ids, requestId, "similar");
    }

    public void logEvent(Long memberId, EventReq req, String type) {
        Long uid = memberId == null ? 0L : memberId;
        jdbcTemplate.update(
            "INSERT INTO recommend_event_log(member_id, scene, request_id, item_id, position, event_type, create_time) VALUES(?,?,?,?,?,?,NOW())",
            uid, req.getScene(), req.getRequestId(), req.getItemId(), req.getPosition(), type
        );
    }

    public TrainResult triggerTrain(String operator) {
        SecurityUser.requireAdmin();
        String version = "sasrec-" + System.currentTimeMillis();
        jdbcTemplate.update(
            "INSERT INTO recommend_train_task(task_name, model_version, status, trigger_by, create_time, update_time) VALUES(?,?,?,?,NOW(),NOW())",
            "SASRec nightly train", version, "RUNNING", operator
        );
        Long taskId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        boolean ok = callTrain(version);
        String status = ok ? "SUCCESS" : "FAILED";
        jdbcTemplate.update(
            "UPDATE recommend_train_task SET status=?, finish_time=NOW(), update_time=NOW() WHERE id=?",
            status, taskId
        );
        TrainResult r = new TrainResult();
        r.setTaskId(taskId);
        r.setModelVersion(version);
        r.setStatus(status);
        return r;
    }

    public ModelStatus modelStatus() {
        SecurityUser.requireAdmin();
        ModelStatus v = new ModelStatus();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            "SELECT id, model_version, status, create_time, finish_time FROM recommend_train_task ORDER BY id DESC LIMIT 1"
        );
        if (!rows.isEmpty()) {
            Map<String, Object> row = rows.get(0);
            v.setLatestTaskId(((Number) row.get("id")).longValue());
            v.setModelVersion(Objects.toString(row.get("model_version"), ""));
            v.setStatus(Objects.toString(row.get("status"), ""));
            v.setCreateTime(row.get("create_time") == null ? "" : row.get("create_time").toString());
            v.setFinishTime(row.get("finish_time") == null ? "" : row.get("finish_time").toString());
        }
        Map<String, Object> m = jdbcTemplate.queryForMap(
            "SELECT " +
                "SUM(CASE WHEN event_type='EXPOSE' THEN 1 ELSE 0 END) exposeCnt, " +
                "SUM(CASE WHEN event_type='CLICK' THEN 1 ELSE 0 END) clickCnt " +
                "FROM recommend_event_log WHERE DATE(create_time)=CURDATE()"
        );
        long expose = m.get("exposeCnt") == null ? 0 : ((Number) m.get("exposeCnt")).longValue();
        long click = m.get("clickCnt") == null ? 0 : ((Number) m.get("clickCnt")).longValue();
        v.setTodayExpose(expose);
        v.setTodayClick(click);
        v.setTodayCtr(expose == 0 ? 0D : (double) click / expose);
        return v;
    }

    public MetricsVO metrics(int days) {
        SecurityUser.requireAdmin();
        MetricsVO vo = new MetricsVO();
        vo.setDaily(jdbcTemplate.query(
            "SELECT DATE_FORMAT(create_time,'%Y-%m-%d') day, " +
                "SUM(CASE WHEN event_type='EXPOSE' THEN 1 ELSE 0 END) exposeCnt, " +
                "SUM(CASE WHEN event_type='CLICK' THEN 1 ELSE 0 END) clickCnt " +
                "FROM recommend_event_log " +
                "WHERE create_time>=DATE_SUB(CURDATE(), INTERVAL ? DAY) " +
                "GROUP BY DATE_FORMAT(create_time,'%Y-%m-%d') ORDER BY day",
            (rs, i) -> {
                DailyMetrics d = new DailyMetrics();
                d.setDay(rs.getString("day"));
                d.setExpose(rs.getLong("exposeCnt"));
                d.setClick(rs.getLong("clickCnt"));
                d.setCtr(d.getExpose() == 0 ? 0D : (double) d.getClick() / d.getExpose());
                return d;
            },
            Math.max(1, days - 1)
        ));
        return vo;
    }

    private boolean callTrain(String modelVersion) {
        if (!enabled) {
            return false;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = Map.of("modelVersion", modelVersion, "timeoutMs", timeoutMs);
        try {
            ResponseEntity<Map> resp = restTemplate.postForEntity(baseUrl + "/train", new HttpEntity<>(body, headers), Map.class);
            return resp.getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            log.warn("train call failed: {}", ex.getMessage());
            return false;
        }
    }

    private RecommendResponse buildResponse(List<Long> productIds, String requestId, String scene) {
        List<PmsProduct> products = loadProductsByIds(productIds);
        RecommendResponse resp = new RecommendResponse();
        resp.setRequestId(requestId == null || requestId.isBlank() ? UUID.randomUUID().toString() : requestId);
        resp.setScene(scene);
        resp.setProducts(products);
        return resp;
    }

    private List<Long> requestPredict(long memberId, String scene, int limit) {
        if (!enabled) {
            return List.of();
        }
        List<Long> seq = buildSequence(memberId, 100);
        if (seq.isEmpty()) {
            return List.of();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> req = Map.of("userId", memberId, "scene", scene, "limit", limit, "sequence", seq, "timeoutMs", timeoutMs);
        for (int i = 0; i <= Math.max(0, maxRetry); i++) {
            try {
                ResponseEntity<Map> resp = restTemplate.postForEntity(baseUrl + "/predict", new HttpEntity<>(req, headers), Map.class);
                if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                    Object result = resp.getBody().get("itemIds");
                    if (result instanceof List<?> list) {
                        return list.stream().filter(Objects::nonNull).map(String::valueOf).map(Long::valueOf).collect(Collectors.toList());
                    }
                }
            } catch (Exception ex) {
                log.warn("predict try {} failed: {}", i + 1, ex.getMessage());
            }
        }
        return List.of();
    }

    private List<Long> buildSequence(long memberId, int maxLen) {
        List<Long> ids = jdbcTemplate.query(
            "SELECT oi.spu_id FROM oms_order_item oi " +
                "INNER JOIN oms_order o ON o.id=oi.order_id " +
                "WHERE o.member_id=? AND o.deleted=0 ORDER BY oi.id DESC LIMIT ?",
            (rs, i) -> rs.getLong(1),
            memberId, maxLen
        );
        List<Long> cart = jdbcTemplate.query(
            "SELECT spu_id FROM oms_cart_item WHERE member_id=? AND deleted=0 ORDER BY id DESC LIMIT ?",
            (rs, i) -> rs.getLong(1),
            memberId, maxLen
        );
        Set<Long> set = new LinkedHashSet<>();
        ids.forEach(set::add);
        cart.forEach(set::add);
        return new ArrayList<>(set);
    }

    private List<Long> hotFallback(int size) {
        List<Long> ids = jdbcTemplate.query(
            "SELECT oi.spu_id FROM oms_order_item oi " +
                "INNER JOIN oms_order o ON o.id=oi.order_id AND o.status>=1 AND o.deleted=0 " +
                "GROUP BY oi.spu_id ORDER BY SUM(oi.quantity) DESC LIMIT ?",
            (rs, i) -> rs.getLong(1), size
        );
        if (ids.isEmpty()) {
            ids = productMapper.selectList(Wrappers.<PmsProduct>lambdaQuery()
                    .eq(PmsProduct::getPublishStatus, 1)
                    .eq(PmsProduct::getVerifyStatus, 1)
                    .eq(PmsProduct::getDeleted, 0)
                    .orderByDesc(PmsProduct::getCreateTime)
                    .last("LIMIT " + size))
                .stream().map(PmsProduct::getId).collect(Collectors.toList());
        }
        return ids;
    }

    private List<Long> sameCategoryFallback(Long itemId, int size) {
        PmsProduct item = productMapper.selectById(itemId);
        if (item == null || item.getCategoryId() == null) {
            return List.of();
        }
        return productMapper.selectList(Wrappers.<PmsProduct>lambdaQuery()
                .eq(PmsProduct::getCategoryId, item.getCategoryId())
                .eq(PmsProduct::getPublishStatus, 1)
                .eq(PmsProduct::getVerifyStatus, 1)
                .eq(PmsProduct::getDeleted, 0)
                .ne(PmsProduct::getId, itemId)
                .orderByDesc(PmsProduct::getCreateTime)
                .last("LIMIT " + size))
            .stream().map(PmsProduct::getId).collect(Collectors.toList());
    }

    private List<PmsProduct> loadProductsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<PmsProduct> list = productMapper.selectList(Wrappers.<PmsProduct>lambdaQuery()
            .in(PmsProduct::getId, ids)
            .eq(PmsProduct::getPublishStatus, 1)
            .eq(PmsProduct::getVerifyStatus, 1)
            .eq(PmsProduct::getDeleted, 0));
        Map<Long, PmsProduct> byId = list.stream().collect(Collectors.toMap(PmsProduct::getId, x -> x));
        return ids.stream().map(byId::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private void saveCache(long memberId, String scene, List<Long> ids) {
        try {
            String json = objectMapper.writeValueAsString(ids);
            jdbcTemplate.update(
                "INSERT INTO recommend_user_result(member_id, scene, item_ids, score_json, expire_at, create_time, update_time) VALUES(?,?,?,?,?,?,?)",
                memberId, scene, json, "[]", LocalDateTime.now().plusMinutes(cacheTtlMinutes), LocalDateTime.now(), LocalDateTime.now()
            );
        } catch (Exception ex) {
            log.warn("save cache failed: {}", ex.getMessage());
        }
    }

    private List<Long> loadCached(long memberId, String scene) {
        List<String> rows = jdbcTemplate.query(
            "SELECT item_ids FROM recommend_user_result WHERE member_id=? AND scene=? AND expire_at>NOW() ORDER BY id DESC LIMIT 1",
            (rs, i) -> rs.getString(1), memberId, scene
        );
        if (rows.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(rows.get(0), new TypeReference<List<Long>>() {
            });
        } catch (Exception ex) {
            return List.of();
        }
    }

    private int normalizeSize(Integer size) {
        if (size == null || size <= 0) {
            return defaultSize;
        }
        return Math.min(30, size);
    }

    @Data
    public static class EventReq {
        private String requestId;
        private String scene;
        private Long itemId;
        private Integer position;
    }

    @Data
    public static class RecommendResponse {
        private String requestId;
        private String scene;
        private List<PmsProduct> products;
    }

    @Data
    public static class TrainResult {
        private Long taskId;
        private String modelVersion;
        private String status;
    }

    @Data
    public static class ModelStatus {
        private Long latestTaskId;
        private String modelVersion;
        private String status;
        private String createTime;
        private String finishTime;
        private long todayExpose;
        private long todayClick;
        private double todayCtr;
    }

    @Data
    public static class DailyMetrics {
        private String day;
        private long expose;
        private long click;
        private double ctr;
    }

    @Data
    public static class MetricsVO {
        private List<DailyMetrics> daily;
    }
}
