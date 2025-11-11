package luxdine.example.luxdine.domain.chat.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatSessionResponse {
    Long id;
    Long customerId;
    String customerName;
    Long staffId;
    String staffName;
    Instant createdAt;
    Instant updatedAt;
    Instant lastMessageAt;
    String lastMessage;
    Long unreadCount;
}

