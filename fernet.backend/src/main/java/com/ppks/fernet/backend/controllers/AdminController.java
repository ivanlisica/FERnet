package com.ppks.fernet.backend.controllers;

import com.ppks.fernet.backend.dto.UserDto;
import com.ppks.fernet.backend.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/all")
    public ResponseEntity<List<UserDto>> getAllAdmins(@AuthenticationPrincipal UserDetails userDetails) {
        List<UserDto> adminList = adminService.getAllAdmins(userDetails);
        if (adminList.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(adminList);
    }
}
