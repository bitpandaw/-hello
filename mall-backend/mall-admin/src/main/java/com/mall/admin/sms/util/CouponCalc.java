package com.mall.admin.sms.util;

import com.mall.mbg.entity.SmsCoupon;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

public final class CouponCalc {
    private CouponCalc() {
    }

    /** 根据券规则计算可减免金额(不超过 subtotal) */
    public static BigDecimal discountOf(SmsCoupon c, BigDecimal subtotal) {
        if (c == null || subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        LocalDateTime n = LocalDateTime.now();
        if (c.getStartTime() != null && n.isBefore(c.getStartTime())) {
            return BigDecimal.ZERO;
        }
        if (c.getEndTime() != null && n.isAfter(c.getEndTime())) {
            return BigDecimal.ZERO;
        }
        if (c.getMinPoint() != null && subtotal.compareTo(c.getMinPoint()) < 0) {
            return BigDecimal.ZERO;
        }
        if (c.getType() != null && c.getType() == 1 && c.getAmount() != null) {
            return c.getAmount().min(subtotal).setScale(2, RoundingMode.HALF_UP);
        }
        if (c.getType() != null && c.getType() == 2 && c.getDiscount() != null) {
            BigDecimal after = subtotal.multiply(c.getDiscount()).setScale(2, RoundingMode.HALF_UP);
            return subtotal.subtract(after).max(BigDecimal.ZERO);
        }
        return BigDecimal.ZERO;
    }
}
