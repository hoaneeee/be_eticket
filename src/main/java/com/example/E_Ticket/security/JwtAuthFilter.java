package com.example.E_Ticket.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1.get header Authorization from request
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 2. kiem tra header (start = "Bearer ")
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7); // loai bo "Bearer "

            try {
                // 3. Parse + verify JWT
                var claims = jwtService.parse(token).getPayload();
                String username = claims.getSubject();
                String rolesStr = String.valueOf(claims.get("roles"));  // "ROLE_ADMIN,ROLE_USER"

                // 4. chuyen roles thanh List<SimpleGrantedAuthority>
                List<SimpleGrantedAuthority> authorities =
                        rolesStr == null || rolesStr.isBlank()
                                ? List.of()
                                : Arrays.stream(rolesStr.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isBlank())
                                .map(SimpleGrantedAuthority::new)
                                .toList();
                // 5. create Authentication object
                var authentication = new UsernamePasswordAuthenticationToken(
                        username,      // principal (chi luu username)
                        null,          // credentials (JWT thay the password)
                        authorities    // quyen (ROLE_USER, ROLE_ADMIN...)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                //bat loi sai key or expire
                SecurityContextHolder.clearContext();
            }
        }

        // 7. Luôn cho request đi tiếp (kể cả không có token)
        filterChain.doFilter(request, response);
    }
}