package com.example.E_Ticket.controller.web;

import com.example.E_Ticket.service.impl.SeatMapReadService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class SeatWebController {
    private final SeatMapReadService seatMapRead;

    @GetMapping("/events/{slug}/seats")
    public String seats(@PathVariable String slug, Model model, HttpSession session){
        var pack = seatMapRead.loadForEventSlug(slug);

        model.addAttribute("e", pack.event());
        model.addAttribute("zones", pack.zones());
        model.addAttribute("svg", pack.inlineSvg());

        var capMap = pack.zones().stream()
                .collect(java.util.stream.Collectors.toMap(z -> z.getId(), z -> z.getCapacity()));
        model.addAttribute("zoneCapMap", capMap);

        // set "zoneId:seatNo" đã bán -> dùng để tô đỏ ngay khi mở trang
        model.addAttribute("soldKeys", pack.soldKeys());

        return "events/seats";
    }
}
