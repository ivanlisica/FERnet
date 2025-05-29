package com.ppks.fernet.backend.service.impl;

import com.ppks.fernet.backend.dao.UserRepository;
import com.ppks.fernet.backend.domain.User;
import com.ppks.fernet.backend.domain.enumeration.Role;
import com.ppks.fernet.backend.dto.UserDto;
import com.ppks.fernet.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAllAdmins(UserDetails userDetails) {
        User client = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<User> adminList = userRepository.findAllByRole(Role.ADMIN);
        if (adminList.isEmpty()) {
            return List.of();
        }
        return adminList.stream()
                .map(admin -> UserDto.builder()
                        .username(admin.getUsername())
                        .firstName(admin.getFirstName())
                        .lastName(admin.getLastName())
                        .build())
                .toList();
    }
}
