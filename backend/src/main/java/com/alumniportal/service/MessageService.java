package com.alumniportal.service;

import com.alumniportal.dto.ChatMessageResponse;
import com.alumniportal.dto.ConversationSummary;
import com.alumniportal.dto.SendMessageRequest;
import com.alumniportal.entity.Message;
import com.alumniportal.entity.User;
import com.alumniportal.exception.ApiException;
import com.alumniportal.repository.MessageRepository;
import com.alumniportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MentorshipService mentorshipService;

    @Transactional
    public ChatMessageResponse sendMessage(Long senderId, SendMessageRequest request) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new ApiException("Recipient not found", HttpStatus.NOT_FOUND));

        // Chat only unlocks once a mentorship request between the two users has been
        // accepted (or scheduled/completed). This also prevents direct exposure of contact
        // info before a mentorship relationship is confirmed.
        if (!mentorshipService.isChatUnlocked(senderId, receiver.getId())) {
            throw new ApiException(
                    "Chat is only available after a mentorship request between you has been accepted",
                    HttpStatus.FORBIDDEN);
        }

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(request.getContent())
                .isRead(false)
                .build();
        message = messageRepository.save(message);
        return toResponse(message);
    }

    @Transactional
    public List<ChatMessageResponse> getConversation(Long userId, Long partnerId) {
        if (!mentorshipService.isChatUnlocked(userId, partnerId)) {
            throw new ApiException(
                    "Chat is only available after a mentorship request between you has been accepted",
                    HttpStatus.FORBIDDEN);
        }
        List<Message> messages = messageRepository.findConversation(userId, partnerId);
        // Mark messages sent to this user as read
        messages.stream()
                .filter(m -> m.getReceiver().getId().equals(userId) && !m.isRead())
                .forEach(m -> m.setRead(true));
        messageRepository.saveAll(messages);
        return messages.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ConversationSummary> listConversations(Long userId) {
        List<Long> partnerIds = messageRepository.findConversationPartnerIds(userId);
        return partnerIds.stream().map(partnerId -> {
            User partner = userRepository.findById(partnerId).orElse(null);
            if (partner == null) return null;
            List<Message> conv = messageRepository.findConversation(userId, partnerId);
            Message last = conv.stream().max(Comparator.comparing(Message::getSentAt)).orElse(null);
            long unread = conv.stream()
                    .filter(m -> m.getReceiver().getId().equals(userId) && !m.isRead())
                    .count();
            return ConversationSummary.builder()
                    .partnerId(partner.getId())
                    .partnerName(partner.getName())
                    .partnerRole(partner.getRole().name())
                    .lastMessage(last != null ? last.getContent() : null)
                    .lastMessageAt(last != null ? last.getSentAt() : null)
                    .unreadCount(unread)
                    .build();
        }).filter(java.util.Objects::nonNull)
          .sorted((a, b) -> {
              if (a.getLastMessageAt() == null) return 1;
              if (b.getLastMessageAt() == null) return -1;
              return b.getLastMessageAt().compareTo(a.getLastMessageAt());
          })
          .collect(Collectors.toList());
    }

    private ChatMessageResponse toResponse(Message m) {
        return ChatMessageResponse.builder()
                .id(m.getId())
                .senderId(m.getSender().getId())
                .senderName(m.getSender().getName())
                .receiverId(m.getReceiver().getId())
                .receiverName(m.getReceiver().getName())
                .content(m.getContent())
                .read(m.isRead())
                .sentAt(m.getSentAt())
                .build();
    }
}
