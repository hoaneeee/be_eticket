package com.example.E_Ticket.service;

import com.example.E_Ticket.dto.CouponDto;
import com.example.E_Ticket.dto.CouponUpsertReq;

import java.util.List;

public interface CouponService {
    List<CouponDto> list();
    CouponDto create(CouponUpsertReq r);
    CouponDto update(Long id, CouponUpsertReq r);
    void delete(Long id);
    CouponDto validate(String code); //check han, chua vuot quota
}
