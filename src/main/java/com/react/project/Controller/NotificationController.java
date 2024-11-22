package com.react.project.Controller;

import com.react.project.DTO.NotificationDTO;
import com.react.project.Service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/recipient/{recipientId}")
    public List<NotificationDTO> getNotificationsByRecipientId(@PathVariable Long recipientId) {
        return notificationService.findByRecipientId(recipientId);
    }

    @PutMapping("/{id}/mark-as-read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markNotificationAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
    }
}
