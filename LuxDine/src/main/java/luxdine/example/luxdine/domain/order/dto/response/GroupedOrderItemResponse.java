package luxdine.example.luxdine.domain.order.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupedOrderItemResponse {
    Long itemId;
    String name;
    double unitPrice;
    int quantity;
    private int qQueued;
    private int qPrep;
    private int qReady;
    private int qServed;

    double lineTotal;
}
