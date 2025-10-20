/*
package com.example.E_Ticket.service.impl;

import com.example.E_Ticket.entity.Order;
import com.example.E_Ticket.entity.Payment;
import com.example.E_Ticket.exception.NotFoundException;
import com.example.E_Ticket.repository.OrderRepository;
import com.example.E_Ticket.repository.PaymentRepository;
import com.example.E_Ticket.service.MailService;
import com.example.E_Ticket.service.MomoPaymentService;
import com.example.E_Ticket.service.TicketService;
import com.example.E_Ticket.service.momo.MomoClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class MomoPaymentServiceImpl implements MomoPaymentService {

    private final OrderRepository orderRepo;
    private final PaymentRepository paymentRepo;
    private final MomoClient momo;
    private final TicketService ticketService;
    private final MailService mailService;

    @Value("${app.web.base-url:http://localhost:8080}")
    private String appBaseUrl;

    @Override
    public String createAndGetPayUrl(String orderCode, String method) {
        Order o = orderRepo.findByCode(orderCode)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        if (o.getStatus() == Order.Status.PAID) throw new IllegalStateException("Order already PAID");

        String requestType = switch (method) {
            case "MOMO_ATM"    -> "payWithATM";
            case "MOMO_WALLET" -> "captureWallet";
            default            -> "captureWallet";
        };
        var res = momo.createPayment(o.getTotal().longValue(), o.getCode(),
                "Thanh toan don " + o.getCode(), requestType);

        // upsert payment
        Payment p = paymentRepo.findByOrderId(o.getId()).orElse(null);
        if (p == null) p = new Payment();
        p.setOrder(o);
        p.setProvider("MOMO");
        p.setAmount(o.getTotal());
        p.setStatus(res.resultCode()==0 ? "PENDING" : "FAILED");
        p.setTxnRef(res.requestId());
        paymentRepo.save(p);

        if (res.resultCode()!=0 || res.payUrl()==null)
            throw new IllegalStateException("Create MoMo failed: " + res.message());
        return res.payUrl();
    }

    @Override
    public void markPaid(String momoOrderId, String transId, String requestId) {
        Order order = orderRepo.findByCode(momoOrderId).orElseThrow();
        Payment p = paymentRepo.findByOrderId(order.getId())
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        if ("SUCCESS".equalsIgnoreCase(p.getStatus())) return;

        p.setStatus("SUCCESS");
        p.setTxnRef(transId);
        paymentRepo.save(p);

        order.setStatus(Order.Status.PAID);
        order.setPaymentMethod("MOMO");
        orderRepo.save(order);

        // 1) Phát hành vé
        var tickets = ticketService.issueTickets(order);

        // 2) Gửi email kèm QR
        StringBuilder html = new StringBuilder("""
          <h3>Vé của bạn (đơn %s)</h3>
          <p>Cảm ơn bạn đã mua vé. Mã vé & QR như bên dưới:</p>
          <ul>
        """.formatted(order.getCode()));

        for (var t : tickets) {
            html.append("""
            <li>
              <b>%s</b><br>
              <img src="%s" alt="QR" style="height:160px;border:1px solid #eee;border-radius:8px;margin:6px 0">
            </li>
          """.formatted(t.getCode(), appBaseUrl + t.getQrImagePath()));
        }
        html.append("</ul>");

        if (order.getUser() != null && order.getUser().getEmail() != null) {
            mailService.send(order.getUser().getEmail(),
                    "Vé của bạn – " + order.getCode(),
                    html.toString());
        }
    }

    @Override
    public void markFailed(String momoOrderId, Integer resultCode, String message, String requestId) {
        Order o = orderRepo.findByCode(momoOrderId).orElseThrow();
        Payment p = paymentRepo.findByOrderId(o.getId())
                .orElseThrow(() -> new NotFoundException("Payment not found"));
        if ("SUCCESS".equalsIgnoreCase(p.getStatus())) return;
        p.setStatus("FAILED");
        paymentRepo.save(p);
        // order có thể để PENDING hoặc CANCELLED tùy business
    }
}
*/
package com.example.E_Ticket.service.impl;

import com.example.E_Ticket.entity.Order;
import com.example.E_Ticket.entity.Payment;
import com.example.E_Ticket.exception.NotFoundException;
import com.example.E_Ticket.repository.OrderRepository;
import com.example.E_Ticket.repository.PaymentRepository;
import com.example.E_Ticket.service.*;
import com.example.E_Ticket.service.momo.MomoClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class MomoPaymentServiceImpl implements MomoPaymentService {
    private final OrderRepository orderRepo;
    private final PaymentRepository paymentRepo;
    private final MomoClient momo;
    private final TicketService ticketService;
    private final MailService mailService;
    private final QrService qrService;
    private final InventoryService inventoryService;
    private final SeatSoldService seatSoldService;


    @Value("${app.web.base-url:http://localhost:8080}")
    private String appBaseUrl;

    private static String baseOrderCode(String momoOrderId) {
        if (momoOrderId == null) return null;
        int i = momoOrderId.indexOf('-');
        return (i > 0) ? momoOrderId.substring(0, i) : momoOrderId;
    }

  /*  @Override
    public String createAndGetPayUrl(String orderCode, String method) {
        Order o = orderRepo.findByCode(orderCode)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (o.getStatus() == Order.Status.PAID) {
            throw new IllegalStateException("Order already PAID");
        }

        String requestType = switch (method) {
            case "MOMO_ATM" -> "payWithATM";
            case "MOMO_WALLET" -> "captureWallet";
            default -> "captureWallet";
        };
        Payment p = paymentRepo.findByOrderId(o.getId()).orElse(null);
        if (p == null) {
            p = new Payment();
            p.setOrder(o);
            p.setProvider("MOMO");
            p.setAmount(o.getTotal());
            p.setStatus("PENDING");
            p.setAttempt(0); // bắt đầu từ 0
        } else {

            if ("PENDING".equalsIgnoreCase(p.getStatus())
                    && p.getPayUrl() != null && !p.getPayUrl().isBlank()) {
                paymentRepo.save(p);
                return p.getPayUrl();
            }
        }

        int nextAttempt = (p.getAttempt() == null ? 0 : p.getAttempt()) + 1;
        String momoOrderId = o.getCode() + "-" + nextAttempt;

        var res = momo.createPayment(
                o.getTotal().longValue(),
                momoOrderId,
                "Thanh toan don " + o.getCode(),
                requestType
        );

        if (res.resultCode() != 0 || res.payUrl() == null) {
            throw new IllegalStateException("Create MoMo failed: " + res.message());
        }

        // lưu lại thông tin giao dịch
        p.setAttempt(nextAttempt);
        p.setTxnRef(res.requestId());
        p.setRequestId(res.requestId());
        p.setMomoOrderId(momoOrderId);
        p.setPayUrl(res.payUrl());
        p.setAmount(o.getTotal());
        p.setStatus("PENDING");
        paymentRepo.save(p);

        return res.payUrl();

    }*/
  @Override
  public String createAndGetPayUrl(String orderCode, String method, String sessionId) { // FIX
      Order o = orderRepo.findByCode(orderCode).orElseThrow(() -> new NotFoundException("Order not found"));
      if (o.getStatus() == Order.Status.PAID) throw new IllegalStateException("Order already PAID");

      String requestType = switch (method) {
          case "MOMO_ATM" -> "payWithATM";
          case "MOMO_WALLET" -> "captureWallet";
          default -> "captureWallet";
      };

      Payment p = paymentRepo.findByOrderId(o.getId()).orElse(null);
      if (p == null) {
          p = new Payment();
          p.setOrder(o);
          p.setProvider("MOMO");
          p.setAmount(o.getTotal());
          p.setStatus("PENDING");
          p.setAttempt(0);
      } else {
          if ("PENDING".equalsIgnoreCase(p.getStatus())
                  && p.getPayUrl()!=null && !p.getPayUrl().isBlank()) {
              // FIX: cập nhật sessionId nếu có (chẳng hạn user tạo lại)
              if (sessionId!=null) p.setHoldSessionId(sessionId);
              paymentRepo.save(p);
              return p.getPayUrl();
          }
      }

      int nextAttempt = (p.getAttempt()==null?0:p.getAttempt()) + 1;
      String momoOrderId = o.getCode() + "-" + nextAttempt;

      var res = momo.createPayment(o.getTotal().longValue(), momoOrderId,
              "Thanh toan don " + o.getCode(), requestType);
      if (res.resultCode()!=0 || res.payUrl()==null)
          throw new IllegalStateException("Create MoMo failed: " + res.message());

      p.setAttempt(nextAttempt);
      p.setTxnRef(res.requestId());
      p.setRequestId(res.requestId());
      p.setMomoOrderId(momoOrderId);
      p.setPayUrl(res.payUrl());
      p.setAmount(o.getTotal());
      p.setStatus("PENDING");
      p.setHoldSessionId(sessionId); // FIX: LƯU sessionId GIỮ GHẾ
      paymentRepo.save(p);

      return res.payUrl();
  }

   @Override
   public Order markPaid(String momoOrderId, String transId, String requestId, String sessionId) {

       String baseCode = baseOrderCode(momoOrderId);

       Order order = orderRepo.findByCode(baseCode).orElseThrow();
       Payment p = paymentRepo.findByOrderId(order.getId())
               .orElseThrow(() -> new NotFoundException("Payment not found"));

       if ("SUCCESS".equalsIgnoreCase(p.getStatus())) return order;

       p.setStatus("SUCCESS");
       p.setTxnRef(transId);
       p.setTransId(transId);
       p.setMomoOrderId(momoOrderId);
       paymentRepo.save(p);

       order.setStatus(Order.Status.PAID);
       order.setPaymentMethod("MOMO");

       if (order.getQrImagePath() == null) {
           String qrData = """
                 {"orderId":"%s","status":"%s","total":%s}
            """.formatted(order.getCode(), order.getStatus(), order.getTotal());
           String path = qrService.createPng(qrData, order.getCode(), 400);
           order.setQrImagePath("/" + path);
       }
       orderRepo.save(order);

       String sid = (sessionId!=null && !sessionId.isBlank()) ? sessionId : p.getHoldSessionId();

       if (sid != null) {
           try { seatSoldService.commitSeatsForOrder(order, sid); } catch (Exception ignored) {}
           try { inventoryService.consumeAllActiveBySessionAndEvent(sid, order.getEvent().getId()); } catch (Exception ignored) {}
       }

       ticketService.issueTickets(order);

       return order;
   }

    @Override
    public void markFailed(String momoOrderId, Integer resultCode, String message, String requestId) {
     /*   Order o = orderRepo.findByCode(momoOrderId).orElseThrow();
        Payment p = paymentRepo.findByOrderId(o.getId()).orElseThrow(() -> new NotFoundException("Payment not found"));
        if ("SUCCESS".equalsIgnoreCase(p.getStatus())) return;
        p.setStatus("FAILED");
        paymentRepo.save(p);*/
        String baseCode = baseOrderCode(momoOrderId);

        Order o = orderRepo.findByCode(baseCode).orElseThrow();
        Payment p = paymentRepo.findByOrderId(o.getId())
                .orElseThrow(() -> new NotFoundException("Payment not found"));
        if ("SUCCESS".equalsIgnoreCase(p.getStatus())) return;

        p.setStatus("FAILED");
        p.setMomoResultCode(resultCode);
        p.setMomoMessage(message);
        p.setRequestId(requestId);
        p.setMomoOrderId(momoOrderId); // FIX
        paymentRepo.save(p);
    }
}
