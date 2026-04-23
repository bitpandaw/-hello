package com.mall.mbg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@TableName("oms_order_item")
public class OmsOrderItem implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long skuId;
    private Long spuId;
    private String spuName;
    private String pic;
    private String skuCode;
    private String specJson;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;
}
