package com.example.E_Ticket.mapper;

import com.example.E_Ticket.dto.UserDTO;
import com.example.E_Ticket.dto.UserUpserReq;
import com.example.E_Ticket.entity.User;

public class UserMapper {
    public static UserDTO toDto(User user) {
    return new UserDTO(user.getId(), user.getEmail(), user.getFullName(), user.getRole(), user.getEnabled(),user.getCreatedAt());
    }
    public static User toEntity(UserUpserReq r) {
        if (r == null) return null;
        return User.builder()
                .email(r.email())
                .password(r.password())
                .fullName(r.fullname())
                .role(r.role() == null ? "ROLE_USER" : r.role())
                .enabled(r.enable() == null ? Boolean.TRUE : r.enable())
                .build();
    }
}


