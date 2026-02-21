package com.react.project.Controller;

import com.react.project.DTO.ChatMessageDTO;
import com.react.project.Service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Handles both REST and WebSocket STOMP chat operations.
 *
 * WebSocket flow (STOMP):
 *   Client  → /app/chat.send      → ChatController#wsSend()   → broadcast
 *   Client  → /app/chat.typing    → ChatController#wsTyping()  → broadcast
 *
 * WebSocket subscriptions:
 *   /topic/chat/user/{recipientId}   – direct messages
 *   /topic/chat/team/{teamId}        – team channel
 *   /topic/chat/typing/{channelId}   – typing indicators
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // ── REST endpoints ─────────────────────────────────────────────────

    /** Get DM conversation between two users */
    @GetMapping("/conversation/{userId1}/{userId2}")
    public List<ChatMessageDTO> getConversation(@PathVariable Long userId1,
                                                @PathVariable Long userId2) {
        return chatService.getConversation(userId1, userId2);
    }

    /** Get all messages in a team channel */
    @GetMapping("/team/{teamId}")
    public List<ChatMessageDTO> getTeamMessages(@PathVariable Long teamId) {
        return chatService.getTeamMessages(teamId);
    }

    /** REST fallback for sending a message */
    @PostMapping("/send")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatMessageDTO send(@RequestBody ChatMessageDTO dto) {
        return chatService.sendMessage(dto);
    }

    /** Mark a single message as read */
    @PatchMapping("/{messageId}/read/{readerId}")
    public ChatMessageDTO markAsRead(@PathVariable Long messageId,
                                     @PathVariable Long readerId) {
        return chatService.markAsRead(messageId, readerId);
    }

    /** Mark entire DM conversation as read */
    @PatchMapping("/conversation/{senderId}/{recipientId}/read")
    public void markConversationAsRead(@PathVariable Long senderId,
                                       @PathVariable Long recipientId) {
        chatService.markConversationAsRead(senderId, recipientId);
    }

    /** Mark all team messages as read for a user */
    @PatchMapping("/team/{teamId}/read/{userId}")
    public void markTeamMessagesAsRead(@PathVariable Long teamId,
                                       @PathVariable Long userId) {
        chatService.markTeamMessagesAsRead(teamId, userId);
    }

    // ── WebSocket (STOMP) endpoints ────────────────────────────────────

    /**
     * Client sends: /app/chat.send
     * ChatService broadcasts to the appropriate /topic/chat/... destination.
     */
    @MessageMapping("/chat.send")
    public void wsSend(@Payload ChatMessageDTO dto) {
        chatService.sendMessage(dto);
    }

    /**
     * Client sends: /app/chat.typing  { channelId, senderId, senderName, isTyping }
     * ChatServiceImpl broadcasts the typing indicator to /topic/chat/typing/{channelId}.
     */
    @MessageMapping("/chat.typing")
    public void wsTyping(@Payload Map<String, Object> payload) {
        String channelId  = (String) payload.get("channelId");
        Long   senderId   = Long.valueOf(payload.get("senderId").toString());
        String senderName = (String) payload.get("senderName");
        boolean isTyping  = Boolean.parseBoolean(payload.getOrDefault("isTyping", false).toString());
        chatService.broadcastTyping(channelId, senderId, senderName, isTyping);
    }
}
