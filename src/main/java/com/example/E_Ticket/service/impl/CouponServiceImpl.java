package com.example.E_Ticket.service.impl;

import com.example.E_Ticket.dto.CouponDto;
import com.example.E_Ticket.dto.CouponUpsertReq;
import com.example.E_Ticket.entity.Coupon;
import com.example.E_Ticket.exception.BusinessException;
import com.example.E_Ticket.exception.NotFoundException;
import com.example.E_Ticket.mapper.CouponMapper;
import com.example.E_Ticket.repository.CouponRepository;
import com.example.E_Ticket.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {
    private final CouponRepository couponRepository;

    @Override
    public List<CouponDto> list() {
        return couponRepository.findAll().stream().map(CouponMapper::toDto).toList();
    }

    private void validateWindow(CouponUpsertReq r){
        if (r.startAt()!=null && r.endAt()!=null && !r.endAt().isAfter(r.startAt()))
            throw new BusinessException("endAt must be after startAt");
    }

    @Override
    public CouponDto create(CouponUpsertReq r) {
        if ("PERCENT".equals(r.type()) && (r.value() <= 0 || r.value() > 100))
            throw new BusinessException("Percent must be 1..100");
        validateWindow(r);
        var saved = couponRepository.save(CouponMapper.toEntity(r));
        return CouponMapper.toDto(saved);
    }

    @Override
    public CouponDto update(Long id, CouponUpsertReq r) {
        if ("PERCENT".equals(r.type()) && (r.value() <= 0 || r.value() > 100))
            throw new BusinessException("Percent must be 1..100");
        validateWindow(r);
        var c = couponRepository.findById(id).orElseThrow(() -> new NotFoundException("Coupon Not Found"));
        CouponMapper.copyToExisting(c, r);
        return CouponMapper.toDto(couponRepository.save(c));
    }

    @Override
    public void delete(Long id) {
        couponRepository.deleteById(id);
    }

    @Override
    public CouponDto validate(String code) {
        Coupon c = couponRepository.findByCode(code).orElseThrow(() -> new NotFoundException("Invalid coupon"));
        Instant now = Instant.now();
        if (c.getStartAt()!=null && now.isBefore(c.getStartAt())) throw new BusinessException("Not started");
        if (c.getEndAt()!=null && now.isAfter(c.getEndAt()))   throw new BusinessException("Expired");
        if (c.getMaxUse()!=null && c.getUsed()>=c.getMaxUse()) throw new BusinessException("Out of quota");
        return CouponMapper.toDto(c);
    }
}
