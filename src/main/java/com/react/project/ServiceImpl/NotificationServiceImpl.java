package com.react.project.ServiceImpl;

import com.react.project.Model.Notification;
import com.react.project.DTO.NotificationDTO;
import com.react.project.Mapper.NotificationMapper;
import com.react.project.Repository.NotificationRepository;
import com.react.project.Service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<NotificationDTO> findByRecipientId(Long recipientId) {
        return notificationRepository.findByRecipientId(recipientId)
                .stream()
                .map(NotificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }
}
