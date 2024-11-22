package com.react.project.DTO;

import com.react.project.Enumirator.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long id;
    private Long recipientId;
    private String message;
    private NotificationType type;
    private Boolean read;
    private LocalDateTime createdAt;
}
