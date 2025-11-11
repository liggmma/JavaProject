package luxdine.example.luxdine.domain.order.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import luxdine.example.luxdine.domain.order.enums.OrderStatus;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    Long id;
    Instant createdDate;
    String tableName;
    int itemsCount;
    double subTotal;
    OrderStatus status;
}
