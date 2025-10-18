package com.example.E_Ticket.service.impl;

import com.example.E_Ticket.entity.User;
import com.example.E_Ticket.entity.VerificationToken;
import com.example.E_Ticket.exception.BusinessException;
import com.example.E_Ticket.repository.UserRepository;
import com.example.E_Ticket.repository.VerificationTokenRepository;
import com.example.E_Ticket.service.MailService;
import com.example.E_Ticket.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class VerificationServiceImpl implements VerificationService {

    private final VerificationTokenRepository tokenRepo;
    private final UserRepository userRepo;
    private final MailService mailService;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.verify.exp-minutes:10}")
    private long expMinutes;

    /* create otp*/
    private String genOtp() {
        SecureRandom rnd = new SecureRandom();
        int n = rnd.nextInt(900000) + 100000; // 100000..999999
        return String.valueOf(n);
    }

    @Override
    public void createAndSend(User user) {
        tokenRepo.deleteByUser_Id(user.getId()); // 1 user 1 opt

        String code = genOtp();
        var vt = VerificationToken.builder()
                .token(code)
                .user(user)
                .expiresAt(Instant.now().plus(expMinutes, ChronoUnit.MINUTES))
                .build();
        tokenRepo.save(vt);

        // Render email tu template mail/verify_code.html
        Context ctx = new Context();
        ctx.setVariable("fullName", user.getFullName());
        ctx.setVariable("code", code);
        ctx.setVariable("expMinutes", expMinutes);
        String html = templateEngine.process("mail/verify_code", ctx);

        mailService.send(user.getEmail(), "Mã xác thực E-Ticket", html);
    }

    @Override
    public String verify(String code) {
        var vt = tokenRepo.findByToken(code)
                .orElseThrow(() -> new BusinessException("Mã xác thực không đúng"));

        if (vt.getUsedAt() != null) throw new BusinessException("Mã đã được sử dụng");
        if (vt.getExpiresAt().isBefore(Instant.now())) throw new BusinessException("Mã đã hết hạn");

        var u = vt.getUser();
        u.setEnabled(true);
        userRepo.save(u);

        vt.setUsedAt(Instant.now());
        tokenRepo.save(vt);

        return u.getEmail();
    }

    @Override
    public void resendVerification(String email) {
        var u = userRepo.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Không tìm thấy tài khoản"));
        if (Boolean.TRUE.equals(u.getEnabled()))
            throw new BusinessException("Tài khoản đã kích hoạt");

        createAndSend(u);
    }
}
