package com.example.E_Ticket.config;

import com.example.E_Ticket.entity.User;
import com.example.E_Ticket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebUtils {

    private final UserRepository userRepository;

    /*return userId neu da log in ( principal la CustomUserDetails, UserDetails mac dinh hay String email) */
    public Long currentUserIdOrNull() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated()) return null;

        Object principal = a.getPrincipal();

        // 1)  use CustomUserDetails
        if (principal instanceof CustomUserDetails cud) {
            return cud.getId();
        }

        // 2) Spring UserDetails default
        if (principal instanceof UserDetails ud) {
            return userRepository.findByEmail(ud.getUsername())
                    .map(User::getId)
                    .orElse(null);
        }

        // 3) JWT filter hay noi khac set principal la chuoi email
        if (principal instanceof String s && !"anonymousUser".equals(s)) {
            return userRepository.findByEmail(s)
                    .map(User::getId)
                    .orElse(null);
        }

        return null;
    }
}
