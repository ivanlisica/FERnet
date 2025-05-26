package com.ppks.fernet.backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ppks.fernet.backend.dao.TokenRepository;
import com.ppks.fernet.backend.dao.UserRepository;
import com.ppks.fernet.backend.domain.Token;
import com.ppks.fernet.backend.domain.User;
import com.ppks.fernet.backend.domain.enumeration.Role;
import com.ppks.fernet.backend.domain.enumeration.TokenType;
import com.ppks.fernet.backend.dto.AuthenticationResponseDto;
import com.ppks.fernet.backend.dto.LoginDto;
import com.ppks.fernet.backend.dto.RegisterDto;
import com.ppks.fernet.backend.security.JwtService;
import com.ppks.fernet.backend.service.AuthenticationService;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService{
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    @SneakyThrows
    @Override
    public ResponseEntity<?> register(RegisterDto request) {
        System.out.println("Request " + request.toString());
        if(userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Username is already taken");
        }
        if(userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email is already taken");
        }
        System.out.println("username " + request.getUsername());
        System.out.println("email " + request.getEmail());
        System.out.println("raw password " + request.getPassword());
        //System.out.println("password" + passwordEncoder.encode(request.getPassword()));
        User user = User.builder().
                username(request.getUsername()).
                email(request.getEmail()).
                password(passwordEncoder.encode(request.getPassword())).
                firstName(request.getFirstName()).
                lastName(request.getLastName()).
                role(Role.fromString(request.getRole())).
                build();
        System.out.println("User spremljen: " + user.toString());
        userRepository.save(user);
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);

        return ResponseEntity.ok(AuthenticationResponseDto.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build());
    }

    @Transactional
    @Override
    public ResponseEntity<?> authenticate(LoginDto request) {
        System.out.println(request.getUsername());
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user name");
        }
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        return ResponseEntity.ok(AuthenticationResponseDto.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build());

    }
    @Override
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException, java.io.IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userName;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        userName = jwtService.extractUsername(refreshToken);
        if (userName != null) {
            User user = this.userRepository.findByUsername(userName)
                    .orElseThrow();
            if (jwtService.isTokenValid(refreshToken, user)) {
                String accessToken = jwtService.generateToken(user);
                revokeAllUserTokens(user);
                saveUserToken(user, accessToken);
                AuthenticationResponseDto authResponse = AuthenticationResponseDto.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    private void revokeAllUserTokens(User user) {
        List<Token> validClientTokens = tokenRepository.findAllValidTokenByClient(user.getUsername());
        if (validClientTokens.isEmpty())
            return;
        tokenRepository.deleteAll(validClientTokens);
    }
    private void saveUserToken(User user, String jwtToken) {
        Token token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }
}
