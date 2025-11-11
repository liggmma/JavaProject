package luxdine.example.luxdine.domain.chat.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatListResponse {
    List<ChatSessionResponse> sessions;
    Long totalUnreadCount;
}

