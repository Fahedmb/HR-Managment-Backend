package com.react.project.Service;

import com.react.project.DTO.ChatMessageDTO;

import java.util.List;

public interface ChatService {
    /** Send and persist a message (DIRECT or TEAM), then broadcast via WebSocket. */
    ChatMessageDTO sendMessage(ChatMessageDTO dto);

    /** Get full conversation between two users */
    List<ChatMessageDTO> getConversation(Long userId1, Long userId2);

    /** Get all messages in a team channel */
    List<ChatMessageDTO> getTeamMessages(Long teamId);

    /** Mark a message as read by a user */
    ChatMessageDTO markAsRead(Long messageId, Long readerId);

    /** Mark all messages in a conversation as read */
    void markConversationAsRead(Long senderId, Long recipientId);

    /** Mark all team messages as read for user */
    void markTeamMessagesAsRead(Long teamId, Long userId);

    /**
     * Broadcast a typing indicator to a channel.
     * channelId = "user_{userId}" for DMs or "team_{teamId}" for team channels.
     */
    void broadcastTyping(String channelId, Long senderId, String senderName, boolean isTyping);
}
