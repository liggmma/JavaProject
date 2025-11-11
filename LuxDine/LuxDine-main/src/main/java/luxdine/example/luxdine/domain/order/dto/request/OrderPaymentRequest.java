package luxdine.example.luxdine.domain.order.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderPaymentRequest {
    Long orderId;
    String userInfo;
    String paymentMethod;
    double discounted;
}
