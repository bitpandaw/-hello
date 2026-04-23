package com.mall.mbg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mall.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("pms_comment")
public class PmsComment extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private Long memberId;
    private Long orderId;
    private String content;
    /** 多张图 JSON 数组 URL */
    private String imageUrls;
    private Integer score;
    private String memberIcon;
    private String memberNick;
    private Integer hasImages;
    private Integer showStatus;
}
