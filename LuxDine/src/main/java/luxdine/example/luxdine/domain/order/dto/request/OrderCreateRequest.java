package luxdine.example.luxdine.domain.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import luxdine.example.luxdine.domain.reservation.entity.Reservations;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCreateRequest {

    @NotNull(message = "Vui lòng chọn bàn")
    Long tableId;

    @Size(max = 512, message = "Ghi chú tối đa 512 ký tự")
    String notes;

    @NotEmpty(message = "Đơn hàng phải có ít nhất 1 món")
    @Valid
    @Builder.Default
    List<OrderCreateItem> items = new ArrayList<>();

    Long reservationId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderCreateItem {
        @NotNull(message = "Thiếu itemId")
        private Long itemId;

        @NotNull(message = "Thiếu số lượng")
        @Min(value = 1, message = "Số lượng tối thiểu là 1")
        private Integer quantity;
    }
}
