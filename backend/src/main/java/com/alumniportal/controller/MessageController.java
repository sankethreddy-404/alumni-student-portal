package com.alumniportal.controller;

import com.alumniportal.dto.ChatMessageResponse;
import com.alumniportal.dto.ConversationSummary;
import com.alumniportal.dto.SendMessageRequest;
import com.alumniportal.security.CustomUserDetails;
import com.alumniportal.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<ChatMessageResponse> send(@AuthenticationPrincipal CustomUserDetails principal,
                                                     @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(messageService.sendMessage(principal.getId(), request));
    }

    @GetMapping("/conversation/{partnerId}")
    public ResponseEntity<List<ChatMessageResponse>> conversation(@AuthenticationPrincipal CustomUserDetails principal,
                                                                   @PathVariable Long partnerId) {
        return ResponseEntity.ok(messageService.getConversation(principal.getId(), partnerId));
    }

    @GetMapping("/inbox")
    public ResponseEntity<List<ConversationSummary>> inbox(@AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(messageService.listConversations(principal.getId()));
    }
}
