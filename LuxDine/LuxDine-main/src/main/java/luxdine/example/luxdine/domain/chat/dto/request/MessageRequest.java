package luxdine.example.luxdine.domain.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageRequest {
    @NotNull(message = "Session ID không được để trống")
    Long sessionId;
    
    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    String message;
}

