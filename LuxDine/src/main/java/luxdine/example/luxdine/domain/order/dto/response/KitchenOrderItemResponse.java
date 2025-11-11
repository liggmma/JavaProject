package luxdine.example.luxdine.domain.order.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KitchenOrderItemResponse {
    Long itemId;
    String itemName;
    int quantity;
    String status; // PENDING, COOKING, READY
    String notes;
    LocalDateTime startedAt;
    long cookingMinutes;
    boolean isOverdue;
}
