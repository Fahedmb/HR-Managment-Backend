package com.react.project.Service;

import com.react.project.DTO.NotificationDTO;
import com.react.project.Enumirator.NotificationType;

import java.util.List;

public interface NotificationService {
    List<NotificationDTO> findByRecipientId(Long recipientId);
    long countUnread(Long recipientId);
    void markAsRead(Long id);
    void markAllAsRead(Long recipientId);
    NotificationDTO create(Long recipientId, String message, NotificationType type);
    void deleteById(Long id);
}