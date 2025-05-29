package com.ppks.fernet.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationDto {
    private Long id;
    private String clientUsername;
    private String adminUsername;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;
    private Boolean isClosed;



}
