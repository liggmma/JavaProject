package luxdine.example.luxdine.domain.order.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KitchenOrderUpdateRequest {
    @NotNull(message = "Order item ID is required")
    Long orderItemId;
    
    @NotNull(message = "Status is required")
    @Pattern(regexp = "^(PENDING|COOKING|READY|SERVED)$", 
             message = "Status must be one of: PENDING, COOKING, READY, SERVED")
    String status;
}
