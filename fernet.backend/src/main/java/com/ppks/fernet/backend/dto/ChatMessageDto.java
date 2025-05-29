package com.ppks.fernet.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    private UUID senderId;
    private String senderUsername;
    private UUID receiverId;
    private Long conversationId;
    private String content;
    private LocalDateTime timestamp;
    private boolean isRead;
    private Long messageId;

}