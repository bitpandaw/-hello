package com.mall.admin.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mall.admin.admin.service.AdminPmsService;
import com.mall.common.api.Result;
import com.mall.mbg.entity.PmsBrand;
import com.mall.mbg.entity.PmsProduct;
import com.mall.mbg.entity.PmsProductCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台-商品/分类/品牌")
@RestController
@RequestMapping("/admin/pms")
@RequiredArgsConstructor
public class AdminPmsController {
    private final AdminPmsService adminPmsService;

    @GetMapping("/products")
    @Operation(summary = "SPU 分页")
    public Result<IPage<PmsProduct>> p(@RequestParam(defaultValue = "1") int p, @RequestParam(defaultValue = "10") int s, @RequestParam(required = false) String name) {
        return Result.ok(adminPmsService.pageProducts(p, s, name));
    }

    @PostMapping("/products")
    @Operation(summary = "新增/改 SPU")
    public Result<?> saveP(@RequestBody PmsProduct po) {
        adminPmsService.saveOrUpdate(po);
        return Result.ok();
    }

    @GetMapping("/categories")
    @Operation(summary = "分类分页(树可前端组)")
    public Result<IPage<PmsProductCategory>> c(@RequestParam(defaultValue = "1") int p, @RequestParam(defaultValue = "100") int s) {
        return Result.ok(adminPmsService.pageCat(p, s));
    }

    @PostMapping("/categories")
    @Operation(summary = "新增/改分类")
    public Result<?> saveC(@RequestBody PmsProductCategory c) {
        adminPmsService.saveCat(c);
        return Result.ok();
    }

    @GetMapping("/brands")
    @Operation(summary = "品牌分页")
    public Result<IPage<PmsBrand>> b(@RequestParam(defaultValue = "1") int p, @RequestParam(defaultValue = "20") int s) {
        return Result.ok(adminPmsService.pageBrand(p, s));
    }

    @PostMapping("/brands")
    @Operation(summary = "新增/改品牌")
    public Result<?> saveB(@RequestBody PmsBrand b) {
        adminPmsService.saveBrand(b);
        return Result.ok();
    }
}
