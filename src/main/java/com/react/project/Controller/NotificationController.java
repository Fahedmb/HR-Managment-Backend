package com.react.project.Controller;

import com.react.project.DTO.NotificationDTO;
import com.react.project.Enumirator.NotificationType;
import com.react.project.Service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{userId}")
    public List<NotificationDTO> getByUser(@PathVariable Long userId) {
        return notificationService.findByRecipientId(userId);
    }

    @GetMapping("/{userId}/unread-count")
    public Map<String, Long> countUnread(@PathVariable Long userId) {
        return Map.of("count", notificationService.countUnread(userId));
    }

    /** HR can push a custom notification to any user */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationDTO send(@RequestBody Map<String, String> body) {
        Long recipientId = Long.parseLong(body.get("recipientId"));
        String message = body.get("message");
        NotificationType type = NotificationType.valueOf(body.getOrDefault("type", "GENERIC"));
        return notificationService.create(recipientId, message, type);
    }

    @PatchMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
    }

    @PatchMapping("/user/{userId}/read-all")
    public void markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        notificationService.deleteById(id);
    }
}

