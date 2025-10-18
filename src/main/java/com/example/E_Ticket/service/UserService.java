package com.example.E_Ticket.service;

import com.example.E_Ticket.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<User> list(Pageable pageable);
    User getByEmail(String email);
    User getById(Long id);
    User create(User user, boolean encodePassword);
    User update(Long id, User patch, boolean encodePassword);
    void disable(Long id);

}
