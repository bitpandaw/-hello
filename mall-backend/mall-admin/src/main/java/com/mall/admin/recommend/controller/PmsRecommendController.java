package com.mall.admin.recommend.controller;

import com.mall.admin.recommend.service.RecommendService;
import com.mall.admin.security.SecurityUser;
import com.mall.common.api.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "前台-推荐")
@RestController
@RequestMapping("/api/pms/recommend")
@RequiredArgsConstructor
public class PmsRecommendController {
    private final RecommendService recommendService;

    @GetMapping("/guess")
    @Operation(summary = "猜你喜欢")
    public Result<RecommendService.RecommendResponse> guess(
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String requestId
    ) {
        return Result.ok(recommendService.guess(SecurityUser.optionalMember(), size, requestId));
    }

    @GetMapping("/similar")
    @Operation(summary = "相似商品推荐")
    public Result<RecommendService.RecommendResponse> similar(
        @RequestParam(required = false) Long itemId,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String requestId
    ) {
        return Result.ok(recommendService.similar(SecurityUser.optionalMember(), itemId, size, requestId));
    }

    @PostMapping("/expose")
    @Operation(summary = "推荐曝光埋点")
    public Result<?> expose(@RequestBody RecommendService.EventReq req) {
        recommendService.logEvent(SecurityUser.optionalMember(), req, "EXPOSE");
        return Result.ok();
    }

    @PostMapping("/click")
    @Operation(summary = "推荐点击埋点")
    public Result<?> click(@RequestBody RecommendService.EventReq req) {
        recommendService.logEvent(SecurityUser.optionalMember(), req, "CLICK");
        return Result.ok();
    }
}
