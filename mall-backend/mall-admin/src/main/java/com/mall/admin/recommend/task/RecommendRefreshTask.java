package com.mall.admin.recommend.task;

import com.mall.admin.recommend.service.RecommendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecommendRefreshTask {
    private final RecommendService recommendService;

    @Scheduled(cron = "0 30 2 * * *")
    public void nightlyTrain() {
        try {
            recommendService.triggerTrain("scheduler");
        } catch (Exception ex) {
            log.warn("nightly train failed: {}", ex.getMessage());
        }
    }
}
