package com.mall.mbg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.mbg.entity.PmsSkuStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PmsSkuStockMapper extends BaseMapper<PmsSkuStock> {
    @Update("UPDATE pms_sku_stock SET stock = stock - #{n}, version = version + 1, update_time=NOW() WHERE sku_id = #{skuId} AND version = #{v} AND stock >= #{n}")
    int decStock(@Param("skuId") long skuId, @Param("n") int n, @Param("v") int v);

    @Update("UPDATE pms_sku_stock SET stock = stock + #{n}, version = version + 1, update_time=NOW() WHERE sku_id = #{skuId}")
    int addStock(@Param("skuId") long skuId, @Param("n") int n);
}
