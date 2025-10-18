    package com.example.E_Ticket.controller.api;

    import com.example.E_Ticket.security.JwtService;
    import jakarta.validation.Valid;
    import jakarta.validation.constraints.Email;
    import jakarta.validation.constraints.NotBlank;
    import lombok.RequiredArgsConstructor;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestBody;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RestController;

    @RestController
    @RequestMapping("/api/admin/v1/auth")
    @RequiredArgsConstructor
    public class AuthAdminApi {
        private final AuthenticationManager authManager;
        private final JwtService jwt;

        public record LoginReq(@NotBlank @Email String email, @NotBlank String password) {}
        public record TokenRes(String token, long expiresInSeconds) {}

        @PostMapping("/login")
        public TokenRes login(@RequestBody @Valid LoginReq req) {
            var auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password()));
            var user = (UserDetails) auth.getPrincipal();

            // (tuỳ chọn) chặn non-admin: chỉ cho ROLE_ADMIN dùng endpoint này
            boolean isAdmin = user.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                throw new org.springframework.security.access.AccessDeniedException("Not admin");
            }

            String token = jwt.generate(user.getUsername(), user.getAuthorities());
            return new TokenRes(token, 7200);
        }
    }
