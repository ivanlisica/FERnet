package com.ppks.fernet.backend.service;

import com.ppks.fernet.backend.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface AdminService {
    List<UserDto> getAllAdmins(UserDetails userDetails);
}
