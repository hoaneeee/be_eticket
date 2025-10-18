package com.example.E_Ticket.controller.web;

import com.example.E_Ticket.dto.EventDto;
import com.example.E_Ticket.dto.TicketTypeDto;
import com.example.E_Ticket.entity.Event;
import com.example.E_Ticket.entity.TicketType;
import com.example.E_Ticket.exception.NotFoundException;
import com.example.E_Ticket.mapper.EventMapper;
import com.example.E_Ticket.mapper.TicketTypeMapper;
import com.example.E_Ticket.repository.EventRepository;
import com.example.E_Ticket.repository.PriceRuleRepository;
import com.example.E_Ticket.repository.TicketHoldRepository;
import com.example.E_Ticket.repository.TicketTypeRepository;
import com.example.E_Ticket.service.TicketAvailabilityService;
import com.example.E_Ticket.service.TicketTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class EventWebController {

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketAvailabilityService ticketAvailabilityService;
    private final PriceRuleRepository priceRuleRepository;

    /** Detail: /events/{slug} */
    @GetMapping("/events/{slug}")
    public String detail(@PathVariable String slug, Model model) {
        Event e = eventRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        EventDto dto = EventMapper.toDto(e);

        // get cac loai ve cua event
        List<TicketType> types = ticketTypeRepository.findByEventId(e.getId());
        List<TicketTypeDto> typeDtos = types.stream().map(TicketTypeMapper::toDto).toList();


        Map<Long,Integer> availableMap = ticketAvailabilityService.availableByTicketType(e.getId()).stream()
                        .collect(Collectors.toMap(TicketAvailabilityService.Item::ticketTypeId,
                                TicketAvailabilityService.Item::available));
        model.addAttribute("e", dto);
        model.addAttribute("types", typeDtos);
        model.addAttribute("hasSeatMap", dto.seatMapId() != null);
        model.addAttribute("availableMap", availableMap);

        var rules = priceRuleRepository.findActiveByEventAt(e.getId(), Instant.now());
        if (!rules.isEmpty()){
            var r = rules.get(0);
            String msg;
            if (r.getPercentOff()!=null && r.getPercentOff().signum()>0){
                msg = "Giá đang giảm " + r.getPercentOff().stripTrailingZeros().toPlainString() +
                        "% đến " + (r.getEndsAt()!=null? r.getEndsAt() : "");
            } else if (r.getAmountOff()!=null){
                msg = "Giá đang giảm " + r.getAmountOff() + "đ" +
                        (r.getEndsAt()!=null? " đến " + r.getEndsAt() : "");
            } else msg = "Đang ưu đãi";

            model.addAttribute("priceRuleMsg", msg);
            model.addAttribute("priceRuleLabel", r.getLabel()!=null? r.getLabel() : r.getKind().name());
            model.addAttribute("priceRuleEnds", r.getEndsAt());
        }
        return "events/detail";
    }
}
