/*
package com.example.E_Ticket.controller.api;

import com.example.E_Ticket.dto.RefundCreateReq;
import com.example.E_Ticket.dto.RefundDto;
import com.example.E_Ticket.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/v1/refunds")
@RequiredArgsConstructor
public class RefundAdminApi {
    private final RefundService refundService;

    private String adminEmail(Authentication a){
        return (a!=null ? a.getName() : "admin");
    }

    @PostMapping
    public RefundDto create(@Valid @RequestBody RefundCreateReq req, Authentication auth){
        return refundService.create(req, adminEmail(auth));
    }

    @GetMapping("/by-order/{orderId}")
    public List<RefundDto> listByOrder(@PathVariable Long orderId){
        return refundService.listByOrder(orderId);
    }

    @PostMapping("/{id}/approve")
    public RefundDto approve(@PathVariable Long id, @RequestBody(required=false) Map<String,String> body, Authentication auth){
        return refundService.approve(id, adminEmail(auth), body!=null? body.get("note"):null);
    }

    @PostMapping("/{id}/reject")
    public RefundDto reject(@PathVariable Long id, @RequestBody(required=false) Map<String,String> body, Authentication auth){
        return refundService.reject(id, adminEmail(auth), body!=null? body.get("note"):null);
    }

    @PostMapping("/{id}/paid")
    public RefundDto paid(@PathVariable Long id, @RequestBody(required=false) Map<String,String> body, Authentication auth){
        return refundService.markPaid(id, adminEmail(auth), body!=null? body.get("note"):null);
    }
}
*/
package com.example.E_Ticket.controller.api;

import com.example.E_Ticket.dto.RefundCreateReq;
import com.example.E_Ticket.dto.RefundDto;
import com.example.E_Ticket.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/v1/refunds")
@RequiredArgsConstructor
public class RefundAdminApi {
    private final RefundService refundService;

    private String adminEmail(Authentication a){
        return (a!=null ? a.getName() : "admin");
    }

    @PostMapping
    public RefundDto create(@Valid @RequestBody RefundCreateReq req, Authentication auth){
        return refundService.create(req, adminEmail(auth));
    }

    @GetMapping("/by-order/{orderId}")
    public List<RefundDto> listByOrder(@PathVariable Long orderId){
        return refundService.listByOrder(orderId);
    }

    @PostMapping("/{id}/approve")
    public RefundDto approve(@PathVariable Long id, @RequestBody(required=false) Map<String,String> body, Authentication auth){
        return refundService.approve(id, adminEmail(auth), body!=null? body.get("note"):null);
    }

    @PostMapping("/{id}/reject")
    public RefundDto reject(@PathVariable Long id, @RequestBody(required=false) Map<String,String> body, Authentication auth){
        return refundService.reject(id, adminEmail(auth), body!=null? body.get("note"):null);
    }

    @PostMapping("/{id}/paid")
    public RefundDto paid(@PathVariable Long id, @RequestBody(required=false) Map<String,String> body, Authentication auth){
        return refundService.markPaid(id, adminEmail(auth), body!=null? body.get("note"):null);
    }
    //  Admin badge
    @GetMapping("/pending-count")
    public Map<String, Long> pendingCount() {
        return Map.of("count", refundService.countPending());
    }

    //  Panel 20 pending
    @GetMapping("/recent-pending")
    public List<RefundDto> recentPending() {
        return refundService.recentPending();
    }
}
