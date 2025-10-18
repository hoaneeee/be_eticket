package com.example.E_Ticket.service.impl;

import com.example.E_Ticket.entity.Order;
import com.example.E_Ticket.entity.Ticket;
import com.example.E_Ticket.repository.TicketRepository;
import com.example.E_Ticket.service.QrService;
import com.example.E_Ticket.service.TicketService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketServiceImpl implements TicketService {
    private final TicketRepository ticketRepo;
    private final QrService qrService;

    @Override
    public List<Ticket> issueTickets(Order order){
        // tranh trung
        if (!ticketRepo.findByOrder_Id(order.getId()).isEmpty()) {
            return ticketRepo.findByOrder_Id(order.getId());
        }

        int total = order.getItems().stream().mapToInt(i -> i.getQty()).sum();
        List<Ticket> out = new ArrayList<>(total);

        for (int i=0; i<total; i++){
            String code = "TCK-" + RandomStringUtils.randomAlphanumeric(6).toUpperCase();
            String payload = "ETK|" + order.getCode() + "|" + code; // nội dung QR

            Ticket t = Ticket.builder()
                    .order(order)
                    .code(code)
                    .qrContent(payload)
                    .status(Ticket.Status.NEW)
                    .build();

            t = ticketRepo.save(t);

            // xuất ảnh QR 512px
            String path = qrService.createPng(payload, code, 512);
            t.setQrImagePath(null);
            out.add(t);
        }
        return out;
    }

    @Override
    public Ticket checkIn(String codeOrPayload){
        String code = codeOrPayload;
        if (codeOrPayload != null && codeOrPayload.contains("|")) {
            String[] p = codeOrPayload.split("\\|");
            code = p[p.length-1];
        }
        var t = ticketRepo.findByCode(code).orElseThrow();
        if (t.getStatus() == Ticket.Status.CHECKED_IN) return t;
        t.setStatus(Ticket.Status.CHECKED_IN);
        return t;
    }
}
