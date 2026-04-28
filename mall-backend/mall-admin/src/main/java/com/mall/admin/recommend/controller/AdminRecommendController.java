package com.mall.admin.recommend.controller;

import com.mall.admin.recommend.service.RecommendService;
import com.mall.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台-推荐")
@RestController
@RequestMapping("/admin/recommend")
@RequiredArgsConstructor
public class AdminRecommendController {
    private final RecommendService recommendService;

    @PostMapping("/train")
    @Operation(summary = "触发训练")
    public Result<RecommendService.TrainResult> train() {
        return Result.ok(recommendService.triggerTrain("admin"));
    }

    @GetMapping("/model/status")
    @Operation(summary = "模型状态")
    public Result<RecommendService.ModelStatus> modelStatus() {
        return Result.ok(recommendService.modelStatus());
    }

    @GetMapping("/metrics")
    @Operation(summary = "推荐报表")
    public Result<RecommendService.MetricsVO> metrics(@RequestParam(defaultValue = "7") int days) {
        return Result.ok(recommendService.metrics(days));
    }
}
