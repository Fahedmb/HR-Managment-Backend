package com.react.project.Mapper;

import com.react.project.Model.Notification;
import com.react.project.DTO.NotificationDTO;
import com.react.project.Model.User;

public class NotificationMapper {

    public static NotificationDTO toDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .recipientId(notification.getRecipient().getId())
                .message(notification.getMessage())
                .type(notification.getType())
                .read(notification.getRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    public static Notification toEntity(NotificationDTO dto, User recipient) {
        return Notification.builder()
                .id(dto.getId())
                .recipient(recipient)
                .message(dto.getMessage())
                .type(dto.getType())
                .read(dto.getRead())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}
