package com.example.E_Ticket.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter jwtFilter;


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration c) throws Exception {
        return c.getAuthenticationManager();
    }

    // 1) API (JWT, stateless)
    @Bean @Order(1)
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .cors(c -> {})
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/api/admin/v1/auth/login").permitAll()
                        .requestMatchers("/api/public/**").permitAll()// <--- public đúng nghĩa
                        .requestMatchers("/api/v1/checkin").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/uploads/**").permitAll()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> res.setStatus(401))
                        .accessDeniedHandler((req, res, e) -> res.setStatus(403))
                );
        return http.build();
    }
    // 2) Web (Thymeleaf – form login, stateful)
    @Bean @Order(2)
    public SecurityFilterChain webChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/payment/momo/ipn", "/perform_login","/cart/**")) //  tắt cho nhẹ. PROD: bật lại cho form POST
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.POST, "/cart/**").permitAll()
                        .requestMatchers("/", "/index", "/register", "/login",
                                "/verify","/resend","/verify/**",      // nếu có dùng kiểu /verify?token=...
                                "/verify-code","/events/**",
                                "/css/**", "/js/**", "/images/**", "/uploads/**",
                                "/h2-console/**", "/error", "/favicon.ico","/whoami").permitAll()
                        .requestMatchers("/cart/**").permitAll()
                        .requestMatchers("/payment/momo/ipn","/payment/momo/return").permitAll()
                        .requestMatchers("/checkout/**", "/orders/**","/payment/**","/web/me/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(f -> f
                        .loginPage("/login")                  // GET /login -> trả form
                        .loginProcessingUrl("/perform_login") // POST /perform_login -> Security xử lý
                        .failureUrl("/login?error")
                        .defaultSuccessUrl("/", false)
                        .permitAll()
                )
                .logout(l -> l
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .headers(h -> h.frameOptions(fr -> fr.disable())); // cho H2 console
        return http.build();
    }
}
