package com.ppks.fernet.backend.controllers;

import com.ppks.fernet.backend.dao.UserRepository;
import com.ppks.fernet.backend.domain.Conversation;
import com.ppks.fernet.backend.domain.Message;
import com.ppks.fernet.backend.domain.User;
import com.ppks.fernet.backend.dto.ChatMessageDto;
import com.ppks.fernet.backend.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import lombok.RequiredArgsConstructor;
import java.security.Principal;


@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final UserRepository userRepository;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDto chatMessageDto, @AuthenticationPrincipal UserDetails userDetails) {
        String senderUsername = userDetails.getUsername();
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Autenticirani pošiljatelj nije pronađen"));

        chatMessageDto.setSenderId(sender.getId());
        chatMessageDto.setSenderUsername(senderUsername);


        Message savedMessage = chatService.saveMessage(chatMessageDto, senderUsername);

        chatMessageDto.setTimestamp(savedMessage.getTimestamp());
        chatMessageDto.setConversationId(savedMessage.getConversation().getId());


        Conversation conversation = savedMessage.getConversation();
        User clientInConversation = conversation.getClient();
        User adminInConversation = conversation.getAdmin();

        messagingTemplate.convertAndSendToUser(
                clientInConversation.getUsername(),
                "/queue/private",
                chatMessageDto
        );

        if (adminInConversation != null) {
            messagingTemplate.convertAndSendToUser(
                    adminInConversation.getUsername(),
                    "/queue/private",
                    chatMessageDto
            );
        } else {
            System.err.println("Greška: Admin nije postavljen u konverzaciji #" + conversation.getId() + " iako je klijent trebao odabrati admina.");
        }
    }
}