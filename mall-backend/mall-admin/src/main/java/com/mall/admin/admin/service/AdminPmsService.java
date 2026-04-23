package com.mall.admin.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.admin.security.SecurityUser;
import com.mall.mbg.entity.PmsProduct;
import com.mall.mbg.entity.PmsProductCategory;
import com.mall.mbg.entity.PmsBrand;
import com.mall.mbg.mapper.PmsProductMapper;
import com.mall.mbg.mapper.PmsProductCategoryMapper;
import com.mall.mbg.mapper.PmsBrandMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminPmsService {
    private final PmsProductMapper pmsProductMapper;
    private final PmsProductCategoryMapper categoryMapper;
    private final PmsBrandMapper brandMapper;

    public IPage<PmsProduct> pageProducts(int p, int s, String name) {
        SecurityUser.requireAdmin();
        return pmsProductMapper.selectPage(new Page<>(p, s), new LambdaQueryWrapper<PmsProduct>()
            .like(name != null && !name.isBlank(), PmsProduct::getName, name)
            .orderByDesc(PmsProduct::getCreateTime));
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(PmsProduct po) {
        SecurityUser.requireAdmin();
        if (po.getId() == null) {
            pmsProductMapper.insert(po);
        } else {
            pmsProductMapper.updateById(po);
        }
    }

    public IPage<PmsProductCategory> pageCat(int p, int s) {
        SecurityUser.requireAdmin();
        return categoryMapper.selectPage(new Page<>(p, s), new LambdaQueryWrapper<PmsProductCategory>().orderByAsc(PmsProductCategory::getParentId, PmsProductCategory::getSort));
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveCat(PmsProductCategory c) {
        SecurityUser.requireAdmin();
        if (c.getId() == null) {
            categoryMapper.insert(c);
        } else {
            categoryMapper.updateById(c);
        }
    }

    public IPage<PmsBrand> pageBrand(int p, int s) {
        SecurityUser.requireAdmin();
        return brandMapper.selectPage(new Page<>(p, s), new LambdaQueryWrapper<>());
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveBrand(PmsBrand b) {
        SecurityUser.requireAdmin();
        if (b.getId() == null) {
            brandMapper.insert(b);
        } else {
            brandMapper.updateById(b);
        }
    }
}
