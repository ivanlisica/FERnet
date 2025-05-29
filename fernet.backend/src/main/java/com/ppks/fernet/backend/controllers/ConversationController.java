package com.ppks.fernet.backend.controllers;

import com.ppks.fernet.backend.dto.ConversationDto;
import com.ppks.fernet.backend.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import java.util.List;


@RestController
@RequestMapping("/conversation")
@RequiredArgsConstructor
public class ConversationController {
    private final ChatService chatService;

    //Klijent započinje novu konverzaciju
    @PostMapping("/start/{adminUsername}")
    public ResponseEntity<?> startConversation(@AuthenticationPrincipal UserDetails userDetails,
                                                             @PathVariable String adminUsername) {
        try {
            ConversationDto conversationDto = chatService.createConversation(userDetails, adminUsername);
            return ResponseEntity.ok(conversationDto);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // Klijent dohvaća svoju aktivnu konverzaciju
    @GetMapping("/get/lastest")
    public ResponseEntity<?> getMyConversation(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            ConversationDto conversationDto = chatService.getLatestConversationForClient(userDetails);
            return ResponseEntity.ok(conversationDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Admin dohvaća sve svoje aktivne konverzacije
    @GetMapping("/admin-active")
    public ResponseEntity<?> getAdminConversations(@AuthenticationPrincipal UserDetails userDetails) {
       try {
           List<ConversationDto> conversations = chatService.getConversationsForAdmin(userDetails);
           if (conversations.isEmpty()) {
               return ResponseEntity.notFound().build();
           }
           return ResponseEntity.ok(conversations);
       } catch (Exception e) {
           return ResponseEntity.badRequest().body(e.getMessage());
       }
    }

    // Admin preuzima konverzaciju (ako nije dodijeljena)
    @PostMapping("/admin-claim/{conversationId}")
    public ResponseEntity<?> claimConversation(@PathVariable Long conversationId,
                                                             @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(chatService.assignAdminToConversation(conversationId, userDetails));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // Dohvaćanje poruka za specifičnu konverzaciju
    @GetMapping("/messages/{conversationId}")
    public ResponseEntity<?> getConversationMessages(@PathVariable Long conversationId,
                                                                        @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(chatService.getMessagesForConversation(conversationId, userDetails));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
