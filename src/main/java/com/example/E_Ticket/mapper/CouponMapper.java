package com.example.E_Ticket.mapper;

import com.example.E_Ticket.dto.CouponDto;
import com.example.E_Ticket.dto.CouponUpsertReq;
import com.example.E_Ticket.entity.Coupon;

public class CouponMapper {
    public static CouponDto toDto(Coupon coupon) {
        return new CouponDto(
                coupon.getId(),coupon.getCode(),coupon.getType().name(),coupon.getValue(),
                coupon.getStartAt(),coupon.getEndAt(),coupon.getMaxUse(),
                coupon.getPerUserLimit(), coupon.getUsed()
        );
    }
    public static Coupon toEntity(CouponUpsertReq couponUpsertReq) {
        return Coupon.builder()
                .code(couponUpsertReq.code())
                .type(Coupon.Type.valueOf(couponUpsertReq.type()))
                .value(couponUpsertReq.value())
                .startAt(couponUpsertReq.startAt())
                .endAt(couponUpsertReq.endAt())
                .maxUse(couponUpsertReq.maxUse())
                .perUserLimit(couponUpsertReq.perUserLimit())
                .used(0)
                .build();
    }
    public static void  copyToExisting (Coupon coupon, CouponUpsertReq  couponUpsertReq) {
        coupon.setType(Coupon.Type.valueOf(couponUpsertReq.type()));
        coupon.setValue(couponUpsertReq.value());
        coupon.setStartAt(couponUpsertReq.startAt());
        coupon.setEndAt(couponUpsertReq.endAt());
        coupon.setMaxUse(couponUpsertReq.maxUse());
        coupon.setPerUserLimit(couponUpsertReq.perUserLimit());
    }
}
