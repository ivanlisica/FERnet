package com.ppks.fernet.backend.controllers;

import com.ppks.fernet.backend.dto.LoginDto;
import com.ppks.fernet.backend.dto.RegisterDto;
import com.ppks.fernet.backend.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterDto request
    ) {
        System.out.println("controller " + request);
        return service.register(request);
    }


    @PostMapping("/login")
    public ResponseEntity<?> authenticate(
            @RequestBody LoginDto request
    ) {
        System.out.println("controller " + request);
        return service.authenticate(request);
    }

    @PostMapping("/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws java.io.IOException {
        service.refreshToken(request, response);
    }


}
