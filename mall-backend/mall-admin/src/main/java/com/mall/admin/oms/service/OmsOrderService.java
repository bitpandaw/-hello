package com.mall.admin.oms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.admin.oms.mq.OrderMq;
import com.mall.admin.sms.util.CouponCalc;
import com.mall.common.api.ResultCode;
import com.mall.common.constant.OrderStatus;
import com.mall.common.exception.BusinessException;
import com.mall.mbg.entity.*;
import com.mall.mbg.mapper.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OmsOrderService {
    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderItemMapper omsOrderItemMapper;
    private final OmsOrderOperateHistoryMapper historyMapper;
    private final PmsSkuMapper pmsSkuMapper;
    private final PmsProductMapper pmsProductMapper;
    private final PmsSkuStockMapper pmsSkuStockMapper;
    private final OmsCartService omsCartService;
    private final UmsMemberAddressMapper addressMapper;
    private final OrderMq orderMq;
    private final SmsCouponMapper smsCouponMapper;
    private final SmsCouponHistoryMapper smsCouponHistoryMapper;

    @Data
    public static class PreviewVO {
        private List<Line> lines;
        private BigDecimal subtotal;
        private BigDecimal discount;
        private BigDecimal freight;
        private BigDecimal payAmount;
        private Long couponId;
    }

    @Data
    public static class Line {
        private long skuId;
        private long spuId;
        private String spuName;
        private int quantity;
        private BigDecimal lineTotal;
    }

    public PreviewVO preview(long memberId, long addressId, Long couponHistoryId) throws Exception {
        UmsMemberAddress addr = addressMapper.selectOne(new LambdaQueryWrapper<UmsMemberAddress>()
            .eq(UmsMemberAddress::getMemberId, memberId).eq(UmsMemberAddress::getId, addressId));
        if (addr == null) {
            throw new BusinessException(ResultCode.BUSINESS, "地址不存在");
        }
        List<OmsCartService.CartItem> list = listSelectedFromCart(memberId);
        if (list.isEmpty()) {
            throw new BusinessException(ResultCode.BUSINESS, "未勾选商品或购物车为空");
        }
        PreviewVO vo = new PreviewVO();
        vo.setLines(new ArrayList<>());
        BigDecimal sub = BigDecimal.ZERO;
        for (OmsCartService.CartItem c : list) {
            PmsSku sku = pmsSkuMapper.selectById(c.getSkuId());
            PmsProduct spu = pmsProductMapper.selectById(c.getSpuId());
            if (sku == null || spu == null) {
                continue;
            }
            Line l = new Line();
            l.setSkuId(sku.getId());
            l.setSpuId(spu.getId());
            l.setSpuName(spu.getName());
            l.setQuantity(c.getQuantity());
            BigDecimal lt = sku.getPrice().multiply(BigDecimal.valueOf(c.getQuantity()));
            l.setLineTotal(lt);
            vo.getLines().add(l);
            sub = sub.add(lt);
        }
        vo.setSubtotal(sub);
        vo.setFreight(BigDecimal.ZERO);
        SmsCoupon coupon = null;
        if (couponHistoryId != null) {
            SmsCouponHistory h = smsCouponHistoryMapper.selectById(couponHistoryId);
            if (h == null || !h.getMemberId().equals(memberId) || h.getUseStatus() != 0) {
                throw new BusinessException(ResultCode.BUSINESS, "优惠券不可用");
            }
            coupon = smsCouponMapper.selectById(h.getCouponId());
        }
        BigDecimal disc = coupon == null ? BigDecimal.ZERO : CouponCalc.discountOf(coupon, sub);
        vo.setDiscount(disc);
        vo.setPayAmount(sub.subtract(disc).add(vo.getFreight()).max(BigDecimal.ZERO));
        if (coupon != null) {
            vo.setCouponId(coupon.getId());
        }
        return vo;
    }

    private List<OmsCartService.CartItem> listSelectedFromCart(long memberId) throws Exception {
        Map<String, OmsCartService.CartItem> cart = omsCartService.getAll(memberId);
        List<OmsCartService.CartItem> list = new ArrayList<>();
        for (OmsCartService.CartItem c : cart.values()) {
            if (c.isSelected() && c.getQuantity() > 0) {
                list.add(c);
            }
        }
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public OmsOrder create(long memberId, long addressId, Long couponHistoryId) throws Exception {
        List<OmsCartService.CartItem> list = listSelectedFromCart(memberId);
        if (list.isEmpty()) {
            throw new BusinessException(ResultCode.BUSINESS, "未勾选商品或购物车为空");
        }
        UmsMemberAddress addr = addressMapper.selectOne(new LambdaQueryWrapper<UmsMemberAddress>()
            .eq(UmsMemberAddress::getMemberId, memberId).eq(UmsMemberAddress::getId, addressId));
        if (addr == null) {
            throw new BusinessException(ResultCode.BUSINESS, "地址不存在");
        }
        BigDecimal sub = BigDecimal.ZERO;
        for (OmsCartService.CartItem c : list) {
            PmsSku sku = pmsSkuMapper.selectById(c.getSkuId());
            if (sku == null) {
                throw new BusinessException(ResultCode.BUSINESS, "商品不存在");
            }
            sub = sub.add(sku.getPrice().multiply(BigDecimal.valueOf(c.getQuantity())));
        }
        SmsCoupon coupon = null;
        SmsCouponHistory hist = null;
        if (couponHistoryId != null) {
            hist = smsCouponHistoryMapper.selectById(couponHistoryId);
            if (hist == null || !hist.getMemberId().equals(memberId) || hist.getUseStatus() != 0) {
                throw new BusinessException(ResultCode.BUSINESS, "优惠券不可用");
            }
            coupon = smsCouponMapper.selectById(hist.getCouponId());
        }
        BigDecimal disc = coupon == null ? BigDecimal.ZERO : CouponCalc.discountOf(coupon, sub);
        BigDecimal pay = sub.subtract(disc).max(BigDecimal.ZERO);
        OmsOrder order = new OmsOrder();
        order.setOrderNo("M" + System.currentTimeMillis() + (int) (Math.random() * 1000));
        order.setMemberId(memberId);
        order.setStatus(OrderStatus.PENDING_PAY.getCode());
        order.setFreight(BigDecimal.ZERO);
        order.setTotalAmount(sub);
        order.setDiscountAmount(disc);
        order.setPayAmount(pay);
        order.setPayType(0);
        if (coupon != null) {
            order.setCouponId(coupon.getId());
        }
        if (hist != null) {
            order.setCouponHistoryId(hist.getId());
        }
        order.setReceiverName(addr.getName());
        order.setReceiverPhone(addr.getPhone());
        order.setFullAddress(addr.getProvince() + addr.getCity() + addr.getDistrict() + addr.getDetail());
        omsOrderMapper.insert(order);
        long oid = order.getId();
        for (OmsCartService.CartItem c : list) {
            decStock(c.getSkuId(), c.getQuantity());
            PmsSku sku = pmsSkuMapper.selectById(c.getSkuId());
            PmsProduct spu = pmsProductMapper.selectById(c.getSpuId());
            if (sku == null || spu == null) {
                throw new BusinessException(ResultCode.BUSINESS, "商品不存在");
            }
            OmsOrderItem oi = new OmsOrderItem();
            oi.setOrderId(oid);
            oi.setSkuId(sku.getId());
            oi.setSpuId(spu.getId());
            oi.setSpuName(spu.getName());
            oi.setPic(spu.getCoverImg());
            oi.setSkuCode(sku.getSkuCode());
            oi.setSpecJson(sku.getSpecJson());
            oi.setPrice(sku.getPrice());
            oi.setQuantity(c.getQuantity());
            oi.setTotalPrice(sku.getPrice().multiply(BigDecimal.valueOf(c.getQuantity())));
            omsOrderItemMapper.insert(oi);
        }
        for (OmsCartService.CartItem c : list) {
            omsCartService.remove(memberId, c.getSkuId());
        }
        if (hist != null) {
            hist.setUseStatus(1);
            hist.setOrderId(oid);
            smsCouponHistoryMapper.updateById(hist);
        }
        history(oid, String.valueOf(memberId), "创建订单");
        orderMq.sendTtl(oid, 30 * 60 * 1000L);
        return omsOrderMapper.selectById(oid);
    }

    private void decStock(long skuId, int n) {
        for (int i = 0; i < 5; i++) {
            PmsSkuStock st = pmsSkuStockMapper.selectOne(new LambdaQueryWrapper<PmsSkuStock>().eq(PmsSkuStock::getSkuId, skuId));
            if (st == null || st.getStock() < n) {
                throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
            }
            int u = pmsSkuStockMapper.decStock(skuId, n, st.getVersion());
            if (u > 0) {
                if (st.getStock() - n <= st.getLowStock()) {
                    org.slf4j.LoggerFactory.getLogger(OmsOrderService.class)
                        .warn("库存预警 skuId={} stock after={} low={}", skuId, st.getStock() - n, st.getLowStock());
                }
                return;
            }
        }
        throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
    }

    public void returnStockByOrder(long orderId) {
        List<OmsOrderItem> is = omsOrderItemMapper.selectList(new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, orderId));
        for (OmsOrderItem oi : is) {
            pmsSkuStockMapper.addStock(oi.getSkuId(), oi.getQuantity());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelUnpaid(long orderId) {
        OmsOrder o = omsOrderMapper.selectById(orderId);
        if (o == null || o.getStatus() == null) {
            return;
        }
        if (o.getStatus() != OrderStatus.PENDING_PAY.getCode()) {
            return;
        }
        o.setStatus(OrderStatus.CANCELLED.getCode());
        omsOrderMapper.updateById(o);
        returnStockByOrder(orderId);
        if (o.getCouponHistoryId() != null) {
            SmsCouponHistory h = smsCouponHistoryMapper.selectById(o.getCouponHistoryId());
            if (h != null) {
                h.setUseStatus(0);
                h.setOrderId(null);
                smsCouponHistoryMapper.updateById(h);
            }
        }
        history(orderId, "sys", "超时未支付，自动关单");
    }

    @Transactional(rollbackFor = Exception.class)
    public void payMock(long memberId, long orderId) {
        OmsOrder o = omsOrderMapper.selectById(orderId);
        if (o == null || !o.getMemberId().equals(memberId)) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (o.getStatus() != OrderStatus.PENDING_PAY.getCode()) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        o.setStatus(OrderStatus.TO_SHIP.getCode());
        omsOrderMapper.updateById(o);
        history(orderId, String.valueOf(memberId), "模拟支付成功(置为待发货,便于演示状态机)");
    }

    private void history(long orderId, String op, String note) {
        OmsOrderOperateHistory h = new OmsOrderOperateHistory();
        h.setOrderId(orderId);
        h.setOperator(op);
        h.setNote(note);
        h.setCreateTime(java.time.LocalDateTime.now());
        historyMapper.insert(h);
    }

    public IPage<OmsOrder> page(long memberId, Integer st, int p, int s) {
        return omsOrderMapper.selectPage(new Page<>(p, s),
            new LambdaQueryWrapper<OmsOrder>().eq(OmsOrder::getMemberId, memberId)
                .eq(st != null, OmsOrder::getStatus, st)
                .orderByDesc(OmsOrder::getCreateTime));
    }

    public OmsOrder detail(long memberId, long id) {
        OmsOrder o = omsOrderMapper.selectById(id);
        if (o == null || !o.getMemberId().equals(memberId)) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        return o;
    }
}
