package com.example.E_Ticket.controller.web;

import com.example.E_Ticket.dto.CartDtos;
import com.example.E_Ticket.dto.CartView;
import com.example.E_Ticket.dto.TicketHoldCreateReq;
import com.example.E_Ticket.entity.Event;
import com.example.E_Ticket.entity.TicketType;
import com.example.E_Ticket.exception.BusinessException;
import com.example.E_Ticket.repository.EventRepository;
import com.example.E_Ticket.repository.TicketTypeRepository;
import com.example.E_Ticket.service.InventoryService;
import com.example.E_Ticket.service.impl.CartSessionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Validated
public class CartWebController {

    private final CartSessionService cartSession;
    private final TicketTypeRepository ticketTypeRepo;
    private final EventRepository eventRepo;
    private final InventoryService inventoryService;


    @PostMapping("/cart/purge-expired")
    @ResponseBody
    public Map<String,Object> purgeExpired(HttpSession session){
        int removed = cartSession.purgeExpired(session);
        return Map.of("ok", true, "removed", removed);
    }

    /* index gio hang */
    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model){
        CartView cart = cartSession.get(session);
        model.addAttribute("cart", cart);
        return "cart/index";
    }

    /* them ve 0 ghe  */
    @PostMapping(path="/cart/add", consumes="application/json")
    @ResponseBody
    public Map<String,Object> add(@Valid @RequestBody CartDtos.AddToCartReq req, HttpSession session){
        if (req.items().isEmpty()) throw new BusinessException("Danh sách vé trống");

        List<Long> ids = req.items().stream().map(CartDtos.AddToCartReq.Item::ticketTypeId).toList();
        List<TicketType> types = ticketTypeRepo.findAllById(ids);
        if (types.size() != ids.size()) throw new BusinessException("Ticket type không tồn tại");

        Long eventId = types.get(0).getEvent().getId();
        Event ev = eventRepo.findById(eventId).orElseThrow(() -> new BusinessException("Event không tồn tại"));
        boolean sameEvent = types.stream().allMatch(t -> t.getEvent().getId().equals(eventId));
        if (!sameEvent) throw new BusinessException("Các loại vé phải thuộc cùng 1 sự kiện");
        if (ev.getSeatMap() != null) throw new BusinessException("Sự kiện có sơ đồ ghế – hãy chọn chỗ");

        CartView cart = cartSession.get(session);

        for (var i : req.items()){
            TicketType t = types.stream().filter(x -> x.getId().equals(i.ticketTypeId())).findFirst().get();
            long price = (t.getPrice()==null? BigDecimal.ZERO : t.getPrice()).longValueExact();

            // tìm line hiện có
            CartView.Line line = cart.lines.stream()
                    .filter(l -> l.ticketTypeId.equals(t.getId()))
                    .findFirst().orElse(null);

            int newQty = i.qty();
            if (line != null) {
                // nếu đã có line → cộng vào tổng mới
                newQty = line.qty + i.qty();

                // release hold cũ trước khi thay bằng hold mới
                if (line.holdId != null) {
                    try { inventoryService.releaseHold(line.holdId); } catch (Exception ignored) {}
                }
            } else {
                // tạo line mới
                line = new CartView.Line();
                line.ticketTypeId = t.getId();
                line.eventId = ev.getId();
                line.eventTitle = ev.getTitle();
                line.ticketTypeName = t.getName();
                line.unitPrice = price;
                cart.lines.add(line);
            }

            // tạo hold mới theo tổng qty mới
            var holdDto = inventoryService.createHold(
                    new TicketHoldCreateReq(ev.getId(), t.getId(), newQty, null, session.getId())
            );

            // gắn qty + hold mới vào line
            line.qty = newQty;
            line.holdId = holdDto.id();
            line.holdExpiresAt = holdDto.expiresAt();
        }

        return Map.of("ok", true);
    }

    @PostMapping("/cart/remove")
    public String remove(@RequestParam Long ticketTypeId, HttpSession session){
        cartSession.removeByTicketType(session, ticketTypeId);
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove-seat")
    public String removeSeat(@RequestParam String name, HttpSession session){
        cartSession.removeSeatByName(session, name); // name = “Zone X – Ghế Y”
        return "redirect:/cart";
    }

    /* xoa all */
    @PostMapping("/cart/clear")
    public String clear(HttpSession session){
        cartSession.clear(session);
        return "redirect:/cart";
    }
//hien badge header
    @GetMapping("/cart/header.json")
    @ResponseBody
    public Map<String,Object> headerJson(HttpSession session){
        var c = cartSession.get(session);
        int count = c.lines.stream().mapToInt(l -> l.qty).sum();
        long total = c.grandTotal();
        return Map.of("count", count, "total", total);
    }
}
