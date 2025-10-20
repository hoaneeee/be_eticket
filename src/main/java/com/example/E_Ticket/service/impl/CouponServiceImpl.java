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

        String code = r.normalizedCode();

        if (couponRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("Code đã tồn tại");
        }

        var saved = couponRepository.save(
                Coupon.builder()
                        .code(code)
                        .type(Coupon.Type.valueOf(r.type()))
                        .value(r.value())
                        .startAt(r.startAt())
                        .endAt(r.endAt())
                        .maxUse(r.maxUse())
                        .perUserLimit(r.perUserLimit())
                        .used(0)
                        .build()
        );
        return CouponMapper.toDto(saved);
    }

    @Override
    public CouponDto update(Long id, CouponUpsertReq r) {
        // validate logic cho PERCENT
        if ("PERCENT".equals(r.type()) && (r.value() <= 0 || r.value() > 100)) {
            throw new BusinessException("Percent must be 1..100");
        }
        validateWindow(r);

        var c = couponRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Coupon Not Found"));

        String newCode = r.code() == null ? null : r.code().trim().toUpperCase();
        if (newCode != null && !newCode.equalsIgnoreCase(c.getCode())) {
            if (couponRepository.existsByCodeIgnoreCase(newCode)) {
                throw new BusinessException("Code đã tồn tại");
            }
            c.setCode(newCode);
        }

        CouponMapper.copyToExisting(c, r);

        return CouponMapper.toDto(couponRepository.save(c));
    }
    @Override
    public void delete(Long id) {
        couponRepository.deleteById(id);
    }
    @Override
    public CouponDto validate(String code) {
        if (code == null || code.isBlank()) throw new BusinessException("Code trống");
        String normalized = code.trim().toUpperCase();

        Coupon c = couponRepository.findByCodeIgnoreCase(normalized)
                .orElseThrow(() -> new NotFoundException("Invalid coupon"));

        Instant now = Instant.now();
        if (c.getStartAt()!=null && now.isBefore(c.getStartAt())) throw new BusinessException("Not started");
        if (c.getEndAt()!=null && now.isAfter(c.getEndAt()))   throw new BusinessException("Expired");
        if (c.getMaxUse()!=null && c.getUsed()>=c.getMaxUse()) throw new BusinessException("Out of quota");

        return CouponMapper.toDto(c);
    }
}
