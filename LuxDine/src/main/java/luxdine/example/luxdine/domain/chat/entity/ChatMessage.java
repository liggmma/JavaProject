package luxdine.example.luxdine.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    Long senderId;

    @Column(nullable = false)
    Long receiverId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    ChatSession session;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    String message;

    @Builder.Default
    @Column(nullable = false)
    Boolean isRead = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    Instant sentAt;
}

