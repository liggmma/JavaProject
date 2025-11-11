package luxdine.example.luxdine.domain.order.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KitchenOrderResponse {
    Long orderId;
    String orderCode;
    String tableName;
    String tableType;
    String priority; // NORMAL, HIGH, URGENT
    boolean isOverdue;
    boolean allReady; // Add this field
    String serverName;
    LocalDateTime orderTime;
    long elapsedMinutes;
    String notes;
    List<String> allergies;
    List<KitchenOrderItemResponse> orderItems;
}

