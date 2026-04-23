package com.mall.admin.pms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.admin.security.SecurityUser;
import com.mall.common.api.Result;
import com.mall.common.api.ResultCode;
import com.mall.common.constant.OrderStatus;
import com.mall.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.mbg.entity.OmsOrder;
import com.mall.mbg.entity.PmsComment;
import com.mall.mbg.mapper.OmsOrderMapper;
import com.mall.mbg.mapper.PmsCommentMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Tag(name = "评价(一单一评)")
@RestController
@RequestMapping("/api/pms/comment")
@RequiredArgsConstructor
public class PmsCommentController {
    private final PmsCommentMapper pmsCommentMapper;
    private final OmsOrderMapper omsOrderMapper;

    @PostMapping
    @Operation(summary = "根据订单发布评价(订单已付款后可评)")
    public Result<?> add(@RequestBody @jakarta.validation.Valid C r) {
        long m = SecurityUser.requireMember();
        OmsOrder o = omsOrderMapper.selectById(r.getOrderId());
        if (o == null || !o.getMemberId().equals(m)) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (o.getStatus() == null || o.getStatus() < OrderStatus.TO_SHIP.getCode()) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR, "当前订单不可评价");
        }
        if (pmsCommentMapper.selectCount(new LambdaQueryWrapper<PmsComment>().eq(PmsComment::getOrderId, r.getOrderId())) > 0) {
            throw new BusinessException(ResultCode.BUSINESS, "该订单已评价");
        }
        PmsComment c = new PmsComment();
        c.setOrderId(r.getOrderId());
        c.setProductId(r.getProductId());
        c.setMemberId(m);
        c.setContent(r.getContent());
        c.setImageUrls(r.getImageUrls());
        c.setScore(r.getScore());
        c.setShowStatus(1);
        c.setHasImages(StringUtils.hasText(r.getImageUrls()) ? 1 : 0);
        c.setMemberNick("会员");
        pmsCommentMapper.insert(c);
        return Result.ok();
    }

    @GetMapping("/list")
    @Operation(summary = "商品评价列表(公开)")
    public Result<IPage<PmsComment>> list(
        @RequestParam long productId,
        @RequestParam(required = false) Integer minScore,
        @RequestParam(defaultValue = "1") int p,
        @RequestParam(defaultValue = "10") int s) {
        var w = new LambdaQueryWrapper<PmsComment>().eq(PmsComment::getProductId, productId).eq(PmsComment::getShowStatus, 1).orderByDesc(PmsComment::getCreateTime);
        if (minScore != null) {
            w.ge(PmsComment::getScore, minScore);
        }
        return Result.ok(pmsCommentMapper.selectPage(new Page<>(p, s), w));
    }

    @Data
    public static class C {
        @NotNull
        private Long orderId;
        @NotNull
        private Long productId;
        @NotBlank
        private String content;
        private String imageUrls;
        @NotNull
        @Min(1) @Max(5)
        private Integer score;
    }
}
