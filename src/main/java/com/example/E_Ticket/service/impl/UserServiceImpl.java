package com.example.E_Ticket.service.impl;

import com.example.E_Ticket.entity.User;
import com.example.E_Ticket.exception.NotFoundException;
import com.example.E_Ticket.repository.UserRepository;
import com.example.E_Ticket.service.UserService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<User> list(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User getByEmail(String email) {
       return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public User create(User user, boolean encodePassword) {
        if (encodePassword) user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() ==null) user.setRole("ROLE_USER");
        if (user.getEnabled() ==null) user.setEnabled(true);
        return userRepository.save(user);
    }

    @Override
    public User update(Long id, User patch, boolean encodePassword) {
        User u = getById(id);
        if (patch.getFullName()!=null) u.setFullName(patch.getFullName());
        if (patch.getEmail()!=null) u.setEmail(patch.getEmail());
        if (patch.getRole()!=null) u.setRole(patch.getRole());
        if (patch.getEnabled()!=null) u.setEnabled(patch.getEnabled());
        if (patch.getPassword()!=null) u.setPassword(encodePassword? passwordEncoder.encode(patch.getPassword()): patch.getPassword());
        return u;
    }

    @Override
    public void disable(Long id) {
        getById(id).setEnabled(false);
    }
}
