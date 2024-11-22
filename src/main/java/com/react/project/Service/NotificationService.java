package com.react.project.Service;


import com.react.project.DTO.NotificationDTO;

import java.util.List;

public interface NotificationService {
    List<NotificationDTO> findByRecipientId(Long recipientId);
    void markAsRead(Long id);
}