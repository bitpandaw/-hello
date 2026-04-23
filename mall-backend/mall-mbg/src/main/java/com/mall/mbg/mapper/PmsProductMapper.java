package com.mall.mbg.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.mbg.entity.PmsProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PmsProductMapper extends BaseMapper<PmsProduct> {
    @Select("SELECT * FROM pms_product WHERE publish_status=1 AND verify_status=1 AND deleted=0 AND (name LIKE CONCAT('%',#{k},'%') OR sub_title LIKE CONCAT('%',#{k},'%'))")
    List<PmsProduct> searchByKeyword(@Param("k") String keyword);
}
