package com.mall.mbg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("pms_sku_stock")
public class PmsSkuStock implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long skuId;
    private Integer stock;
    private Integer lowStock;
    private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
