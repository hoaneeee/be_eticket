package com.example.E_Ticket.controller.web;

import com.example.E_Ticket.dto.CartView;
import com.example.E_Ticket.exception.BusinessException;
import com.example.E_Ticket.service.impl.CartSessionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.Instant;
import java.util.Map;

@org.springframework.web.bind.annotation.ControllerAdvice
@RequiredArgsConstructor
public class ControllerAdvice {
    private final CartSessionService cartSessionService;

    public record HeaderCartVM(int count, long total, CartView cart){}

    @ModelAttribute("headerCart")
    public HeaderCartVM inject(HttpSession session){
        CartView cart = cartSessionService.get(session);
        int count = cart.lines.stream().mapToInt(l -> l.qty).sum();
        long total = cart.grandTotal();
        return new HeaderCartVM(count, total, cart);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String,Object>> handleBiz(BusinessException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "ok", false,
                "code", ex.getMessage(),
                "ts", Instant.now().toString()
        ));
    }
}
