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
public class StaffJoinedNotification {
    @Builder.Default
    String type = "STAFF_JOINED";
    Long sessionId;
    Long staffId;
    String staffName;
    
    public static StaffJoinedNotification of(Long sessionId, Long staffId, String staffName) {
        return StaffJoinedNotification.builder()
                .type("STAFF_JOINED")
                .sessionId(sessionId)
                .staffId(staffId)
                .staffName(staffName)
                .build();
    }
}

