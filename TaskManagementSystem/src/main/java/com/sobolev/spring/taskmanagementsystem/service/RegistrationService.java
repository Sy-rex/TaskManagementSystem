package com.sobolev.spring.taskmanagementsystem.service;

import com.sobolev.spring.taskmanagementsystem.model.Role;
import com.sobolev.spring.taskmanagementsystem.model.User;
import com.sobolev.spring.taskmanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    @Transactional
    public void register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role role = roleService.findRoleByName("ROLE_USER"); // администраторы назначаются вручную в самой БД
        user.setRoles(List.of(role));
        userRepository.save(user);
    }
}
