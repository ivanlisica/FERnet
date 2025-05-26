package com.ppks.fernet.backend.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class RegisterDto {
    private String username;
    private String password;
    private String email;
    private String role;
    private String firstName;
    private String lastName;

}
