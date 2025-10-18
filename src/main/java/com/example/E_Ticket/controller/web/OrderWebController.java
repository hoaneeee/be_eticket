/*
package com.example.E_Ticket.controller.web;

import com.example.E_Ticket.entity.Order;
import com.example.E_Ticket.entity.Ticket;
import com.example.E_Ticket.exception.NotFoundException;
import com.example.E_Ticket.repository.OrderRepository;
import com.example.E_Ticket.repository.PaymentRepository;
import com.example.E_Ticket.repository.RefundRepository;
import com.example.E_Ticket.repository.TicketRepository;
import com.example.E_Ticket.repository.UserRepository;
import com.example.E_Ticket.service.impl.OrderRefundPolicy;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderWebController {

    private final OrderRepository orderRepo;
    private final UserRepository userRepo;
    private final RefundRepository refundRepo;
    private final TicketRepository ticketRepo;
    private final PaymentRepository paymentRepo;
    private final OrderRefundPolicy policy;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public String myOrders(Authentication auth, Model model){
        var user = userRepo.findByEmail(auth.getName()).orElseThrow();
        var list = orderRepo.findAllByUser_IdOrderByCreatedAtDesc(user.getId());
        model.addAttribute("orders", list);
        return "orders/list";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{code}")
    public String detail(@PathVariable String code, Authentication auth, Model model) {
        // Lấy thông tin người dùng hiện tại
        var user = userRepo.findByEmail(auth.getName()).orElseThrow();

        // Lấy đơn hàng theo mã đơn hàng và người dùng
        Order o = orderRepo.findByCodeAndUser_Id(code, user.getId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        model.addAttribute("order", o);
        model.addAttribute("refunds", refundRepo.findByOrder_Id(o.getId()));

        boolean refundable = policy.isRefundable(o) && refundRepo.findByOrder_Id(o.getId()).isEmpty();
        model.addAttribute("refundable", refundable);

        // Đưa vé ra view
        model.addAttribute("tickets", ticketRepo.findByOrder_Id(o.getId()));

        // Tạo mã QR cho toàn bộ đơn hàng
        String qrData = createQRCodeData(o);  // Tạo chuỗi dữ liệu cho mã QR
        String qrCode = qrCodeService.generateQRCode(qrData);  // Tạo mã QR từ chuỗi
        model.addAttribute("qrCode", qrCode);

        return "orders/detail";
    }
    private String createQRCodeData(Order order) {
        JSONObject json = new JSONObject();
        json.put("orderId", order.getCode());
        json.put("status", order.getStatus());
        json.put("total", order.getTotal());

        // Thêm thông tin từng vé vào chuỗi
        JSONArray tickets = new JSONArray();
        for (Ticket ticket : order.getTickets()) {
            JSONObject ticketJson = new JSONObject();
            ticketJson.put("ticketType", ticket.getType());
            ticketJson.put("quantity", ticket.getQuantity());
            ticketJson.put("price", ticket.getPrice());
            tickets.put(ticketJson);
        }
        json.put("tickets", tickets);

        return json.toString();  // Trả về chuỗi JSON
    }
}
*/
package com.example.E_Ticket.controller.web;

import com.example.E_Ticket.entity.Order;
import com.example.E_Ticket.entity.Ticket;
import com.example.E_Ticket.exception.NotFoundException;
import com.example.E_Ticket.repository.OrderRepository;
import com.example.E_Ticket.repository.PaymentRepository;
import com.example.E_Ticket.repository.RefundRepository;
import com.example.E_Ticket.repository.TicketRepository;
import com.example.E_Ticket.repository.UserRepository;
import com.example.E_Ticket.service.impl.CartSessionService;
import com.example.E_Ticket.service.impl.OrderRefundPolicy;
import com.example.E_Ticket.service.QrService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderWebController {

    private final OrderRepository orderRepo;
    private final UserRepository userRepo;
    private final RefundRepository refundRepo;
    private final TicketRepository ticketRepo;
    private final PaymentRepository paymentRepo;
    private final OrderRefundPolicy policy;
    private final QrService qrCodeService;
    private final CartSessionService cartSessionService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public String myOrders(Authentication auth, Model model) {
        var user = userRepo.findByEmail(auth.getName()).orElseThrow();
        var list = orderRepo.findAllByUser_IdOrderByCreatedAtDesc(user.getId());
        model.addAttribute("orders", list);
        return "orders/list";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{code}")
    public String detail(@PathVariable String code, Authentication auth, Model model,
                         HttpSession session) {
        var user = userRepo.findByEmail(auth.getName()).orElseThrow();
        Order o = orderRepo.findByCodeAndUser_Id(code, user.getId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        model.addAttribute("order", o);
        model.addAttribute("refunds", refundRepo.findByOrder_Id(o.getId()));

        boolean refundable = policy.isRefundable(o) && refundRepo.findByOrder_Id(o.getId()).isEmpty();
        model.addAttribute("refundable", refundable);

        model.addAttribute("tickets", ticketRepo.findByOrder_Id(o.getId()));

        if (o.getStatus() == Order.Status.PAID && o.getEvent() != null) {
            cartSessionService.clearByEvent(session, o.getEvent().getId());
        }

        return "orders/detail";
    }

    private String createQRCodeData(Order order) {
        JSONObject json = new JSONObject();
        json.put("orderId", order.getCode());
        json.put("status", order.getStatus());
        json.put("total", order.getTotal());

        // Add details for the entire order, not per ticket
        JSONArray tickets = new JSONArray();
        for (Ticket ticket : order.getTickets()) {
            JSONObject ticketJson = new JSONObject();

            // Ensure ticketType, quantity, and price are valid
            if (ticket.getTicketType() != null) {
                ticketJson.put("ticketType", ticket.getTicketType().getName());
            } else {
                ticketJson.put("ticketType", "Unknown");  // Handle null ticket type
            }

            // Check if quantity and price are set, if not, use default values
            ticketJson.put("quantity", ticket.getQuantity() > 0 ? ticket.getQuantity() : 1);
            ticketJson.put("price", ticket.getPrice() > 0 ? ticket.getPrice() : 100000);  // Set a default price if missing

            tickets.put(ticketJson);
        }
        json.put("tickets", tickets);

        // Return the full order data as a JSON string
        return json.toString();
    }


}
