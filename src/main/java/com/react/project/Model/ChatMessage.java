package com.react.project.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** DIRECT or TEAM */
    @Column(nullable = false)
    private String messageType; // "DIRECT" or "TEAM"

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /** Set for DIRECT messages */
    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;

    /** Set for TEAM messages */
    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /** Users who have read this message */
    @ManyToMany
    @JoinTable(
        name = "chat_message_read_by",
        joinColumns = @JoinColumn(name = "message_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private List<User> readBy = new ArrayList<>();

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() { createdAt = LocalDateTime.now(); }
}
