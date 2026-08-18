package com.alumniportal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationSummary {
    private Long partnerId;
    private String partnerName;
    private String partnerRole;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private long unreadCount;
}
