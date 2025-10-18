package com.example.E_Ticket.controller.web;

import com.example.E_Ticket.dto.AuthDtos;
import com.example.E_Ticket.entity.User;
import com.example.E_Ticket.exception.BusinessException;
import com.example.E_Ticket.repository.UserRepository;
import com.example.E_Ticket.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthWebController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value="error", required=false) String error,
                            @RequestParam(value="registered", required=false) String registered,
                            Model model){
        model.addAttribute("error", error!=null);
        model.addAttribute("registered", registered!=null);
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("form", new AuthDtos.RegisterForm("", "", ""));
        return "auth/register";
    }

    @PostMapping("/register")
    public String doRegister(@Valid @ModelAttribute("form") AuthDtos.RegisterForm form,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) return "auth/register";
        if (userRepository.existsByEmail(form.email())) {
            bindingResult.rejectValue("email", "email.exists", "Email đã tồn tại");
            return "auth/register";
        }
        var user = User.builder()
                .fullName(form.fullName())
                .email(form.email())
                .password(passwordEncoder.encode(form.password()))
                .role("ROLE_USER")
                .enabled(false)
                .build();
        userRepository.save(user);

        verificationService.createAndSend(user);
        // sang trang nhập mã
        model.addAttribute("email", user.getEmail());
        return "auth/verify_code_form";
    }

    /** Hiển thị form nhập mã (khi user bấm “nhập lại email” hoặc quay lại) */
    @GetMapping("/verify-code")
    public String verifyCodePage(@RequestParam(value = "email", required = false) String email, Model model) {
        model.addAttribute("email", email);
        return "auth/verify_code_form";
    }

    /** Nhận mã, xác thực */
    @PostMapping("/verify-code")
    public String doVerifyCode(@RequestParam String email,
                               @RequestParam String code,
                               Model model) {
        try {
            // (tuỳ chọn) kiểm tra email tồn tại
            userRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException("Email không tồn tại"));
            String okEmail = verificationService.verify(code.trim());
            model.addAttribute("ok", true);
            model.addAttribute("email", okEmail);
        } catch (BusinessException ex) {
            model.addAttribute("ok", false);
            model.addAttribute("message", ex.getMessage());
        }
        return "auth/verify_result"; // trang kết quả dùng lại template cũ
    }

    /** Gửi lại mã */
    @PostMapping("/resend")
    public String resend(@RequestParam("email") String email, Model model) {
        try {
            verificationService.resendVerification(email);
            model.addAttribute("email", email);
            model.addAttribute("resent", true);
        } catch (BusinessException ex) {
            model.addAttribute("email", email);
            model.addAttribute("message", ex.getMessage());
        }
        return "auth/verify_code_form";
    }
    //test session
    @GetMapping("/whoami")
    @ResponseBody
    public java.util.Map<String,Object> whoami(
            org.springframework.security.core.Authentication auth,
            jakarta.servlet.http.HttpSession session) {
        return java.util.Map.of(
                "user", auth!=null && auth.isAuthenticated()? auth.getName(): null,
                "sid",  session!=null? session.getId(): null
        );
    }
}
