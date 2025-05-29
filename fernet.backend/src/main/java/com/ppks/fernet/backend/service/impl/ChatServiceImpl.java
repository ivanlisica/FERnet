package com.ppks.fernet.backend.service.impl;

import com.ppks.fernet.backend.dao.ConversationRepository;
import com.ppks.fernet.backend.dao.MessageRepository;
import com.ppks.fernet.backend.dao.UserRepository;
import com.ppks.fernet.backend.domain.Conversation;
import com.ppks.fernet.backend.domain.Message;
import com.ppks.fernet.backend.domain.User;
import com.ppks.fernet.backend.domain.enumeration.Role;
import com.ppks.fernet.backend.dto.ChatMessageDto;
import com.ppks.fernet.backend.dto.ConversationDto;
import com.ppks.fernet.backend.dto.UserDto;
import com.ppks.fernet.backend.service.ChatService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public Message saveMessage(ChatMessageDto chatMessageDto, String senderUsername) {
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Sender not found: " + senderUsername));

        Conversation conversation;
        if (chatMessageDto.getConversationId() != null) {
            conversation = conversationRepository.findById(chatMessageDto.getConversationId())
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));
        } else {
            throw new IllegalArgumentException("ConversationId must be provided for subsequent messages.");
        }

        if (!conversation.getClient().getId().equals(sender.getId()) &&
                (conversation.getAdmin() == null || !conversation.getAdmin().getId().equals(sender.getId()))) {
            throw new SecurityException("Sender does not belong to this conversation.");
        }

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(chatMessageDto.getContent());
        message.setTimestamp(LocalDateTime.now());

        conversation.setLastMessageAt(message.getTimestamp());
        conversationRepository.save(conversation);

        return messageRepository.save(message);
    }


    @Override
    public ConversationDto createConversation(UserDetails userDetails, String adminName) {
        if(userDetails == null || !userRepository.existsByUsername(userDetails.getUsername())) {
            throw new IllegalArgumentException("User details are invalid or user does not exist.");
        }

        User client = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Client not found"));
        System.out.println("Test");
        Conversation conversation = Conversation.builder()
                .client(client)
                .admin(adminName.isEmpty() ? null : userRepository.findByUsername(adminName)
                        .orElseThrow(() -> new RuntimeException("Admin not found")))
                .createdAt(LocalDateTime.now())
                .isClosed(false)
                .build();
        conversationRepository.save(conversation);
        // Vraćamo DTO, pretpostavljajući da postoji konverzija
        return ConversationDto.builder()
                .id(conversation.getId())
                .adminUsername(adminName.isEmpty() ? null : adminName)
                .clientUsername(client.getUsername())
                .isClosed(conversation.getIsClosed())
                .createdAt(conversation.getCreatedAt())
                .build();


    }

    @Override
    public ConversationDto getLatestConversationForClient(UserDetails userDetails) {
        if(userDetails == null || !userRepository.existsByUsername(userDetails.getUsername())) {
            throw new IllegalArgumentException("User details are invalid or user does not exist.");
        }
        User client = userRepository.findByUsername(userDetails.getUsername()).get();
        // Pronađi zadnju konverzaciju za klijenta
        List<Conversation> conversationList = conversationRepository.findByClientIdOrderByLastMessageAtDesc(client.getId());
        if (!conversationList.isEmpty()) {
            Conversation latestConversation = conversationList.get(0);
            return ConversationDto.builder()
                    .id(latestConversation.getId())
                    .adminUsername(latestConversation.getAdmin() != null ? latestConversation.getAdmin().getUsername() : null)
                    .clientUsername(client.getUsername())
                    .isClosed(latestConversation.getIsClosed())
                    .createdAt(latestConversation.getCreatedAt())
                    .lastMessageAt(latestConversation.getLastMessageAt())
                    .build();
        }

        return null;
    }

    // Admin dohvaća sve svoje aktivne konverzacije
    @Override
    public List<ConversationDto> getConversationsForAdmin(UserDetails userDetails) {
        User admin = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        if(admin.getRole().ordinal() != 2) {
            throw new SecurityException("User is not an admin.");
        }
        List<Conversation> conversations = conversationRepository.findByAdminId(admin.getId());
        if (conversations.isEmpty()) {
            return List.of(); // Vraća praznu listu ako nema konverzacija
        }
        return conversations.stream()
                .map(conv -> ConversationDto.builder()
                        .id(conv.getId())
                        .adminUsername(admin.getUsername())
                        .clientUsername(conv.getClient().getUsername())
                        .isClosed(conv.getIsClosed())
                        .createdAt(conv.getCreatedAt())
                        .lastMessageAt(conv.getLastMessageAt())
                        .build())
                .toList();

    }
    // Metoda koju admin poziva da preuzme konverzaciju koju je klijent započeo bez admina
    @Transactional
    @Override
    public ConversationDto assignAdminToConversation(Long conversationId, UserDetails userDetails) {
        User admin = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        if(admin.getRole().ordinal() != 2) {
            throw new SecurityException("User is not an admin.");
        }
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        // Provjeri je li konverzacija već dodijeljena adminu
        if (conversation.getAdmin() != null) {
            throw new IllegalStateException("This conversation is already assigned to an admin.");
        }
        // Postavi admina na konverzaciju
        conversation.setAdmin(admin);
        conversationRepository.save(conversation);
        // Vraćamo DTO, pretpostavljajući da postoji konverzija
        return ConversationDto.builder()
                .id(conversation.getId())
                .adminUsername(admin.getUsername())
                .clientUsername(conversation.getClient().getUsername())
                .isClosed(conversation.getIsClosed())
                .createdAt(conversation.getCreatedAt())
                .lastMessageAt(conversation.getLastMessageAt())
                .build();
    }

    // Metoda za dohvat poruka u konverzaciji
    @Override
    public List<ChatMessageDto> getMessagesForConversation(Long conversationId, UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        // Provjeri je li korisnik dio konverzacije
        if (!conversation.getClient().getId().equals(user.getId()) ||
            (conversation.getAdmin() != null && !conversation.getAdmin().getId().equals(user.getId()))){
            throw new SecurityException("User does not belong to this conversation.");
        }
        // Dohvati sve poruke u konverzaciji
        List<Message> messages = messageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
        // Mapiraj poruke u DTO
        return messages.stream()
                .map(message -> ChatMessageDto.builder()
                        .senderId(message.getSender().getId())
                        .senderUsername(message.getSender().getUsername())
                        .content(message.getContent())
                        .timestamp(message.getTimestamp())
                        .conversationId(message.getConversation().getId())
                        .receiverId(message.getConversation().getAdmin() != null ?
                            message.getConversation().getAdmin().getId() : null)
                        .build())
                .toList();
    }


}