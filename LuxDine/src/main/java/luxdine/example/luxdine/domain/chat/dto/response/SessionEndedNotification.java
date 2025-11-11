package luxdine.example.luxdine.domain.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = PRIVATE)
public class SessionEndedNotification {
    @Builder.Default
    String type = "SESSION_ENDED";
    Long sessionId;
    
    public static SessionEndedNotification of(Long sessionId) {
        return SessionEndedNotification.builder()
                .type("SESSION_ENDED")
                .sessionId(sessionId)
                .build();
    }
}

