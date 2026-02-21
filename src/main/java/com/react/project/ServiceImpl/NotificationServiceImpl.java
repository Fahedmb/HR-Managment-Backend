package com.react.project.ServiceImpl;

import com.react.project.DTO.NotificationDTO;
import com.react.project.Enumirator.NotificationType;
import com.react.project.Mapper.NotificationMapper;
import com.react.project.Model.Notification;
import com.react.project.Model.User;
import com.react.project.Repository.NotificationRepository;
import com.react.project.Repository.UserRepository;
import com.react.project.Service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public List<NotificationDTO> findByRecipientId(Long recipientId) {
        return notificationRepository.findByRecipientId(recipientId)
                .stream()
                .map(NotificationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public long countUnread(Long recipientId) {
        return notificationRepository.findByRecipientId(recipientId).stream()
                .filter(n -> Boolean.FALSE.equals(n.getRead()))
                .count();
    }

    @Override
    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(Long recipientId) {
        List<Notification> list = notificationRepository.findByRecipientId(recipientId);
        list.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(list);
    }

    @Override
    public NotificationDTO create(Long recipientId, String message, NotificationType type) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new RuntimeException("User not found: " + recipientId));
        Notification notification = Notification.builder()
                .recipient(recipient)
                .message(message)
                .type(type)
                .read(false)
                .build();
        NotificationDTO dto = NotificationMapper.toDTO(notificationRepository.save(notification));
        // Push real-time to the user's personal topic
        messagingTemplate.convertAndSend("/topic/notifications/" + recipientId, dto);
        return dto;
    }

    @Override
    public void deleteById(Long id) {
        notificationRepository.deleteById(id);
    }
}
