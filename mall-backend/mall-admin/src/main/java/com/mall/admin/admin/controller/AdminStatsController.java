package com.mall.admin.admin.controller;

import com.mall.admin.admin.service.AdminStatsService;
import com.mall.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "后台-看板")
@RestController
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {
    private final AdminStatsService adminStatsService;

    @GetMapping
    @Operation(summary = "统计首页")
    public Result<AdminStatsService.StatsVO> s() {
        return Result.ok(adminStatsService.home());
    }

    @GetMapping("/charts")
    @Operation(summary = "图表数据(近7日GMV+销量TOP10)")
    public Result<AdminStatsService.ChartVO> charts() {
        return Result.ok(adminStatsService.charts());
    }
}
