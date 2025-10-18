package com.example.E_Ticket.controller.api;

import com.example.E_Ticket.dto.CheckinCreateReq;
import com.example.E_Ticket.dto.CheckinDto;
import com.example.E_Ticket.entity.CheckIn;
import com.example.E_Ticket.mapper.CheckinMapper;
import com.example.E_Ticket.service.CheckInService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/admin/v1/checkins")
@RequiredArgsConstructor
public class CheckinAdminApi {
    private final CheckInService checkInService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public CheckinDto check(@RequestBody @Valid CheckinCreateReq req){
        CheckIn ck = checkInService.verifyAndCheckin(req.code(), null);
        return CheckinMapper.toDto(ck);
    }
}
