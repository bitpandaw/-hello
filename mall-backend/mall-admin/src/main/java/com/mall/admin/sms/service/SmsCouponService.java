package com.mall.admin.sms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.mbg.entity.SmsCoupon;
import com.mall.mbg.entity.SmsCouponHistory;
import com.mall.mbg.mapper.SmsCouponHistoryMapper;
import com.mall.mbg.mapper.SmsCouponMapper;
import com.mall.common.api.ResultCode;
import com.mall.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class SmsCouponService {
    private final SmsCouponMapper smsCouponMapper;
    private final SmsCouponHistoryMapper smsCouponHistoryMapper;
    private static final Map<String, ReentrantLock> LOCKS = new ConcurrentHashMap<>();

    public List<SmsCoupon> list() {
        return smsCouponMapper.selectList(new LambdaQueryWrapper<SmsCoupon>().eq(SmsCoupon::getDeleted, 0));
    }

    @Transactional(rollbackFor = Exception.class)
    public void take(long memberId, long couponId) {
        SmsCoupon c = smsCouponMapper.selectById(couponId);
        if (c == null || c.getDeleted() == 1) {
            throw new BusinessException(ResultCode.BUSINESS, "优惠券无效");
        }
        String k = "coupon:take:lock:" + memberId + ":" + couponId;
        ReentrantLock lock = LOCKS.computeIfAbsent(k, x -> new ReentrantLock());
        if (!lock.tryLock()) {
            throw new BusinessException(ResultCode.BUSINESS, "请勿重复操作");
        }
        try {
            int got = smsCouponHistoryMapper.selectCount(new LambdaQueryWrapper<SmsCouponHistory>()
                .eq(SmsCouponHistory::getMemberId, memberId)
                .eq(SmsCouponHistory::getCouponId, couponId)).intValue();
            if (got >= c.getPerLimit()) {
                throw new BusinessException(ResultCode.BUSINESS, "已达领取上限");
            }
            SmsCouponHistory h = new SmsCouponHistory();
            h.setMemberId(memberId);
            h.setCouponId(couponId);
            h.setUseStatus(0);
            smsCouponHistoryMapper.insert(h);
        } finally {
            lock.unlock();
            LOCKS.remove(k, lock);
        }
    }
}
