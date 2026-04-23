package com.mall.admin.pms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.api.Result;
import com.mall.mbg.entity.PmsProduct;
import com.mall.mbg.entity.PmsProductCategory;
import com.mall.mbg.entity.PmsSku;
import com.mall.mbg.mapper.PmsProductCategoryMapper;
import com.mall.mbg.mapper.PmsProductMapper;
import com.mall.mbg.mapper.PmsSkuMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import com.mall.mbg.entity.PmsBrand;
import com.mall.mbg.mapper.PmsBrandMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "商品(前台只读)")
@RestController
@RequestMapping("/api/pms")
@RequiredArgsConstructor
public class PmsPublicController {
    private final PmsProductCategoryMapper categoryMapper;
    private final PmsProductMapper productMapper;
    private final PmsSkuMapper skuMapper;
    private final PmsBrandMapper brandMapper;

    @GetMapping("/categories/tree")
    @Operation(summary = "分类树(三级以内)")
    public Result<List<PmsProductCategory>> tree() {
        return Result.ok(categoryMapper.selectList(new LambdaQueryWrapper<>()));
    }

    @GetMapping("/products")
    @Operation(summary = "商品分页(分类/价区/关键词/排序)")
    public Result<IPage<PmsProduct>> products(
        @RequestParam(defaultValue = "1") long p,
        @RequestParam(defaultValue = "10") long s,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @RequestParam(required = false) String q,
        @RequestParam(defaultValue = "create_time_desc") String sort) {
        var w = Wrappers.<PmsProduct>lambdaQuery()
            .eq(PmsProduct::getPublishStatus, 1).eq(PmsProduct::getVerifyStatus, 1)
            .eq(PmsProduct::getDeleted, 0);
        if (categoryId != null) {
            w.eq(PmsProduct::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(q)) {
            w.and(x -> x.like(PmsProduct::getName, q).or().like(PmsProduct::getSubTitle, q));
        }
        if (minPrice != null) {
            w.ge(PmsProduct::getMinPrice, minPrice);
        }
        if (maxPrice != null) {
            w.le(PmsProduct::getMinPrice, maxPrice);
        }
        if ("price_asc".equals(sort)) {
            w.orderByAsc(PmsProduct::getMinPrice);
        } else if ("price_desc".equals(sort)) {
            w.orderByDesc(PmsProduct::getMinPrice);
        } else {
            w.orderByDesc(PmsProduct::getCreateTime);
        }
        return Result.ok(productMapper.selectPage(new Page<>(p, s), w));
    }

    @GetMapping("/products/{id}")
    @Operation(summary = "商品+SKU")
    public Result<DetailVO> product(@PathVariable long id) {
        PmsProduct o = productMapper.selectById(id);
        if (o == null) {
            return Result.ok();
        }
        List<PmsSku> skus = skuMapper.selectList(Wrappers.<PmsSku>lambdaQuery().eq(PmsSku::getSpuId, id).eq(PmsSku::getDeleted, 0));
        DetailVO v = new DetailVO();
        v.setProduct(o);
        v.setSkus(skus);
        return Result.ok(v);
    }

    @GetMapping("/search")
    @Operation(summary = "MySQL 模糊搜 (预留ES见README)")
    public Result<List<PmsProduct>> search(@RequestParam String q) {
        return Result.ok(productMapper.searchByKeyword(q));
    }

    // === 品牌列表（只读，用于前台展示/论文接口完整性） ===
    @GetMapping("/brands")
    @Operation(summary = "品牌列表")
    public Result<List<PmsBrand>> brands() {
        return Result.ok(brandMapper.selectList(Wrappers.emptyWrapper()));
    }

    @Data
    public static class DetailVO {
        private PmsProduct product;
        private List<PmsSku> skus;
    }
}
