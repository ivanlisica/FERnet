package com.ppks.fernet.backend.service;

import com.ppks.fernet.backend.domain.User;
import com.ppks.fernet.backend.dto.LoginDto;
import com.ppks.fernet.backend.dto.RegisterDto;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface AuthenticationService {
    ResponseEntity<?> register(RegisterDto request);
    ResponseEntity<?> authenticate(LoginDto request);
    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException, java.io.IOException;

}
