package com.mall.admin.pms.convert;

import com.mall.mbg.entity.PmsProduct;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PmsProductConvert {
    PmsProduct copy(PmsProduct p);
}
