package luxdine.example.luxdine.domain.chat.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageResponse {
    Long id;
    Long senderId;
    String senderName;
    String senderRole; // Role của người gửi (STAFF, ADMIN, CUSTOMER)
    Long receiverId;
    String receiverName;
    Long sessionId;
    String message;
    Boolean isRead;
    Instant sentAt;
}

