package com.react.project.ServiceImpl;

import com.react.project.DTO.ChatMessageDTO;
import com.react.project.Enumirator.NotificationType;
import com.react.project.Exception.UserException;
import com.react.project.Model.ChatMessage;
import com.react.project.Model.Team;
import com.react.project.Model.User;
import com.react.project.Repository.ChatMessageRepository;
import com.react.project.Repository.TeamRepository;
import com.react.project.Repository.UserRepository;
import com.react.project.Service.ChatService;
import com.react.project.Service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    @Override
    public ChatMessageDTO sendMessage(ChatMessageDTO dto) {
        User sender = userRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new UserException("Sender not found"));
        ChatMessage message = ChatMessage.builder()
                .messageType(dto.getMessageType())
                .sender(sender)
                .content(dto.getContent())
                .build();

        if ("DIRECT".equalsIgnoreCase(dto.getMessageType())) {
            User recipient = userRepository.findById(dto.getRecipientId())
                    .orElseThrow(() -> new UserException("Recipient not found"));
            message.setRecipient(recipient);
            ChatMessage saved = chatMessageRepository.save(message);
            ChatMessageDTO result = toDTO(saved);
            // Push to recipient's personal WebSocket topic
            messagingTemplate.convertAndSend("/topic/chat/user/" + dto.getRecipientId(), result);
            // Notify recipient
            notificationService.create(dto.getRecipientId(),
                    sender.getFirstName() + " sent you a message.",
                    NotificationType.CHAT_MESSAGE);
            return result;
        } else {
            // TEAM message
            Team team = teamRepository.findById(dto.getTeamId())
                    .orElseThrow(() -> new UserException("Team not found"));
            message.setTeam(team);
            ChatMessage saved = chatMessageRepository.save(message);
            ChatMessageDTO result = toDTO(saved);
            // Broadcast to team channel
            messagingTemplate.convertAndSend("/topic/chat/team/" + dto.getTeamId(), result);
            return result;
        }
    }

    @Override
    public List<ChatMessageDTO> getConversation(Long userId1, Long userId2) {
        return chatMessageRepository.findConversation(userId1, userId2)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ChatMessageDTO> getTeamMessages(Long teamId) {
        return chatMessageRepository.findByTeamIdOrderByCreatedAtAsc(teamId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChatMessageDTO markAsRead(Long messageId, Long readerId) {
        ChatMessage msg = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new UserException("Message not found"));
        User reader = userRepository.findById(readerId)
                .orElseThrow(() -> new UserException("User not found"));
        boolean alreadyRead = msg.getReadBy().stream().anyMatch(u -> u.getId().equals(readerId));
        if (!alreadyRead) {
            msg.getReadBy().add(reader);
            chatMessageRepository.save(msg);
        }
        return toDTO(msg);
    }

    @Override
    @Transactional
    public void markConversationAsRead(Long senderId, Long recipientId) {
        List<ChatMessage> messages = chatMessageRepository.findConversation(senderId, recipientId);
        User reader = userRepository.findById(recipientId)
                .orElseThrow(() -> new UserException("User not found"));
        messages.stream()
                .filter(m -> "DIRECT".equals(m.getMessageType()) && m.getSender().getId().equals(senderId))
                .filter(m -> m.getReadBy().stream().noneMatch(u -> u.getId().equals(recipientId)))
                .forEach(m -> {
                    m.getReadBy().add(reader);
                    chatMessageRepository.save(m);
                });
    }

    @Override
    @Transactional
    public void markTeamMessagesAsRead(Long teamId, Long userId) {
        List<ChatMessage> messages = chatMessageRepository.findByTeamIdOrderByCreatedAtAsc(teamId);
        User reader = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("User not found"));
        messages.stream()
                .filter(m -> m.getReadBy().stream().noneMatch(u -> u.getId().equals(userId)))
                .forEach(m -> {
                    m.getReadBy().add(reader);
                    chatMessageRepository.save(m);
                });
    }

    @Override
    public void broadcastTyping(String channelId, Long senderId, String senderName, boolean isTyping) {
        java.util.Map<String, Object> payload = java.util.Map.of(
                "channelId", channelId,
                "senderId", senderId,
                "senderName", senderName,
                "isTyping", isTyping
        );
        messagingTemplate.convertAndSend("/topic/chat/typing/" + channelId, payload);
    }

    private ChatMessageDTO toDTO(ChatMessage m) {
        return ChatMessageDTO.builder()
                .id(m.getId())
                .messageType(m.getMessageType())
                .senderId(m.getSender().getId())
                .senderName(m.getSender().getFirstName() + " " + m.getSender().getLastName())
                .recipientId(m.getRecipient() != null ? m.getRecipient().getId() : null)
                .teamId(m.getTeam() != null ? m.getTeam().getId() : null)
                .content(m.getContent())
                .readByIds(m.getReadBy().stream().map(User::getId).collect(Collectors.toList()))
                .createdAt(m.getCreatedAt())
                .build();
    }
}
