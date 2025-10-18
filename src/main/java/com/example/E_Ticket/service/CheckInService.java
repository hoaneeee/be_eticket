package com.example.E_Ticket.service;

import com.example.E_Ticket.entity.CheckIn;

public interface CheckInService {
    /** Trả về Checkin mới. Chỉ cho phép check-in khi Order đã PAID và chưa check-in code đó. */
    CheckIn verifyAndCheckin(String code, Long scannedBy);
}