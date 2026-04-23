package com.mall.mbg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("pms_product")
public class PmsProduct extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String subTitle;
    private Long brandId;
    private Long categoryId;
    private String coverImg;
    private BigDecimal minPrice;
    private BigDecimal originalPrice;
    private String detailHtml;
    private Integer publishStatus;
    private Integer verifyStatus;
}
