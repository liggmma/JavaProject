package luxdine.example.luxdine.domain.chat.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import luxdine.example.luxdine.domain.chat.enums.ChatSessionStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "chat_sessions")
public class ChatSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "customer_id", nullable = false)
    Long customerId;

    @Column(name = "staff_id", nullable = true)
    Long staffId;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ChatMessage> messages;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    Instant updatedAt;

    @Column(name = "last_message_at")
    Instant lastMessageAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    ChatSessionStatus status = ChatSessionStatus.WAITING;

    @Column(name = "reopened_at")
    Instant reopenedAt; // Thời điểm session được reopen sau khi end (null cho session mới hoặc chưa được reopen)

    @Column(name = "joined_at")
    Instant joinedAt; // Thời điểm staff join vào phòng (null khi chưa có staff join)
}

