package com.ppks.fernet.backend.service;

import com.ppks.fernet.backend.domain.Conversation;
import com.ppks.fernet.backend.domain.Message;
import com.ppks.fernet.backend.dto.ChatMessageDto;
import com.ppks.fernet.backend.dto.ConversationDto;
import com.ppks.fernet.backend.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.UUID;

public interface ChatService {

    Message saveMessage(ChatMessageDto chatMessageDto, String senderUsername);
    ConversationDto createConversation(UserDetails userDetails, String adminName);
    ConversationDto getLatestConversationForClient(UserDetails userDetails);
    List<ConversationDto> getConversationsForAdmin(UserDetails userDetails);
    ConversationDto assignAdminToConversation(Long conversationId, UserDetails userDetails);
    List<ChatMessageDto> getMessagesForConversation(Long conversationId, UserDetails userDetails);

}
