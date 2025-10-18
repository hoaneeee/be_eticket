
package com.example.E_Ticket.controller.api;

import com.example.E_Ticket.dto.OrderDto;
import com.example.E_Ticket.mapper.OrderMapper;
import com.example.E_Ticket.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/v1/orders")
@RequiredArgsConstructor
public class OrderAdminApi {
    private final OrderService orderService;

    @GetMapping
    public org.springframework.data.domain.Page<OrderDto> list(@RequestParam(defaultValue="0") int page,
                                                               @RequestParam(defaultValue="10") int size){
        var pr = PageRequest.of(page, size, Sort.by("id").descending());
        return orderService.list(pr).map(OrderMapper::toDto);
    }

    @GetMapping("/{code}")
    public OrderDto get(@PathVariable String code){
        return OrderMapper.toDto(orderService.getByCode(code));
    }

    @PostMapping("/{code}/cancel")
    public void cancel(@PathVariable String code){
        // Logic (check PAID/cancel) đã nằm trong service.cancel(...)
        orderService.cancel(code);
    }
}
