package com.sobolev.spring.taskmanagementsystem.service;

import com.sobolev.spring.taskmanagementsystem.model.Role;
import com.sobolev.spring.taskmanagementsystem.repository.RoleRepository;
import com.sobolev.spring.taskmanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    public Role findRoleByName(String name) {
        return roleRepository.findByName(name).orElse(null);
    }
}
