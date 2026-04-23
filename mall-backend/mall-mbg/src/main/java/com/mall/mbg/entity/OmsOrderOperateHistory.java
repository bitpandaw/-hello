package com.mall.mbg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("oms_order_operate_history")
public class OmsOrderOperateHistory implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String operator;
    private String note;
    private LocalDateTime createTime;
}
