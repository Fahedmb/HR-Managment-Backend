package com.react.project.DTO;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private Long id;
    private String messageType;  // "DIRECT" or "TEAM"
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private Long recipientId;    // for DIRECT
    private Long teamId;         // for TEAM
    private String content;
    private List<Long> readByIds;
    private boolean seen;        // convenience: has the other party seen it?
    private LocalDateTime createdAt;
}
