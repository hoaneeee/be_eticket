// src/main/java/com/example/E_Ticket/service/momo/MomoClient.java
package com.example.E_Ticket.service.momo;

import com.example.E_Ticket.config.HmacUtil;
import com.example.E_Ticket.config.MomoProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@RequiredArgsConstructor
public class MomoClient {
    private final MomoProperties props;
    private final RestTemplate rest = new RestTemplate();

    public record CreateRes(int resultCode, String message, String payUrl, String requestId, String orderId) {}

    public CreateRes createPayment(long amount, String orderId, String orderInfo, String requestType) {
        String requestId = UUID.randomUUID().toString();
        String extraData = "";

        String raw = "accessKey=" + props.getAccessKey()
                + "&amount=" + amount
                + "&extraData=" + extraData
                + "&ipnUrl=" + props.getIpnUrl()
                + "&orderId=" + orderId
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + props.getPartnerCode()
                + "&redirectUrl=" + props.getRedirectUrl()
                + "&requestId=" + requestId
                + "&requestType=" + requestType;

        String signature = HmacUtil.hmacSHA256(raw, props.getSecretKey());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("partnerCode", props.getPartnerCode());
        body.put("accessKey",   props.getAccessKey());
        body.put("requestId",   requestId);
        body.put("amount",      String.valueOf(amount));
        body.put("orderId",     orderId);
        body.put("orderInfo",   orderInfo);
        body.put("redirectUrl", props.getRedirectUrl());
        body.put("ipnUrl",      props.getIpnUrl());
        body.put("extraData",   extraData);
        body.put("requestType", requestType);
        body.put("signature",   signature);
        body.put("lang",        "vi");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        var resp = rest.exchange(
                props.getEndpoint(),
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );
        Map<String, Object> m = resp.getBody();
        int rc = ((Number) m.getOrDefault("resultCode", 999)).intValue();

        return new CreateRes(
                rc,
                (String) m.getOrDefault("message",""),
                (String) m.get("payUrl"),
                (String) m.get("requestId"),
                (String) m.get("orderId")
        );
    }

    // ===== VERIFY =====

    /** Thứ tự khóa cho RETURN theo tài liệu MoMo v2 */
    private static final String[] RETURN_KEYS = new String[]{
            "accessKey","amount","extraData","message","orderId","orderInfo","orderType",
            "partnerCode","payType","requestId","responseTime","resultCode","transId"
    };

    /** Verify cho RETURN (redirect). Bù accessKey nếu thiếu + URL-decode orderInfo/extraData */
    public boolean verifyReturnSignature(Map<String, ?> params) {
        Map<String,String> ordered = new LinkedHashMap<>();
        for (String k : RETURN_KEYS) {
            String v;
            if ("accessKey".equals(k)) {
                // nếu không gửi accessKey ở return → dùng accessKey cấu hình
                Object got = params.get("accessKey");
                v = (got == null || String.valueOf(got).isBlank())
                        ? props.getAccessKey()
                        : String.valueOf(got);
            } else {
                Object got = params.get(k);
                v = got == null ? "" : String.valueOf(got);
            }
            if ("orderInfo".equals(k) || "extraData".equals(k)) {
                v = urlDecode(v);
            }
            ordered.put(k, v);
        }

        String raw = joinKeyValue(ordered);
        String calc = HmacUtil.hmacSHA256(raw, props.getSecretKey());
        Object sigObj = params.get("signature");
        String sig = sigObj == null ? "" : String.valueOf(sigObj);

        // DEBUG (giữ lại tạm thời)
        System.out.println("=== RETURN CHECK ===");
        System.out.println("RAW  = " + raw);
        System.out.println("CALC = " + calc);
        System.out.println("SIGN = " + sig);

        return calc.equals(sig);
    }

    /** Verify cho IPN: thường IPN đã đủ trường và không URL-encode → có thể sort hoặc theo thứ tự IPN */
    public boolean verifyIpnSignature(Map<String, ?> params) {
        Map<String,String> sorted = new TreeMap<>();
        for (Map.Entry<String, ?> e : params.entrySet()) {
            if (e.getKey() == null) continue;
            if ("signature".equalsIgnoreCase(e.getKey())) continue;
            sorted.put(e.getKey(), e.getValue() == null ? "" : String.valueOf(e.getValue()));
        }
        String raw = joinKeyValue(sorted);
        String calc = HmacUtil.hmacSHA256(raw, props.getSecretKey());
        Object sigObj = params.get("signature");
        String sig = sigObj == null ? "" : String.valueOf(sigObj);
        return calc.equals(sig);
    }

    private static String joinKeyValue(Map<String,String> m) {
        StringBuilder sb = new StringBuilder();
        var it = m.entrySet().iterator();
        while (it.hasNext()) {
            var e = it.next();
            sb.append(e.getKey()).append('=').append(e.getValue() == null ? "" : e.getValue());
            if (it.hasNext()) sb.append('&');
        }
        return sb.toString();
    }

    private static String urlDecode(String s) {
        if (s == null) return "";
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception ignore) { return s; }
    }
}
