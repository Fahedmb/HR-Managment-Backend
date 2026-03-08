package com.react.project.Repository;

import com.react.project.Model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /** Fetch conversation between two users (both directions) ordered by time */
    @Query("SELECT m FROM ChatMessage m WHERE m.messageType = 'DIRECT' AND " +
           "((m.sender.id = :userId1 AND m.recipient.id = :userId2) OR " +
           "(m.sender.id = :userId2 AND m.recipient.id = :userId1)) " +
           "ORDER BY m.createdAt ASC")
    List<ChatMessage> findConversation(Long userId1, Long userId2);

    /** All messages in a team channel */
    List<ChatMessage> findByTeamIdOrderByCreatedAtAsc(Long teamId);

    /** Unread direct messages for a user */
    @Query("SELECT m FROM ChatMessage m WHERE m.recipient.id = :userId AND :userId NOT IN (SELECT u.id FROM m.readBy u)")
    List<ChatMessage> findUnreadDirectMessages(Long userId);

    /** Count unread messages from a specific sender */
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.messageType='DIRECT' AND m.sender.id = :senderId AND m.recipient.id = :recipientId AND :recipientId NOT IN (SELECT u.id FROM m.readBy u)")
    long countUnread(Long senderId, Long recipientId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM chat_message_read_by WHERE user_id = :userId", nativeQuery = true)
    void removeFromReadBy(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ChatMessage cm WHERE cm.sender.id = :userId OR cm.recipient.id = :userId")
    void deleteBySenderOrRecipient(@Param("userId") Long userId);
}
