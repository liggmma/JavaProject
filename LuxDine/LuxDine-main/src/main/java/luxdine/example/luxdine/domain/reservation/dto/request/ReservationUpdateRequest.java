package luxdine.example.luxdine.domain.reservation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import luxdine.example.luxdine.domain.reservation.dto.response.ReservationResponse;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationUpdateRequest {

    /** Bắt buộc: id của reservation đang sửa */
    @NotNull
    private Long id;

    /** Các trường có thể thay đổi */
    @NotNull
    private OffsetDateTime reservationDate;
    private OffsetDateTime reservationDepartureTime;

    @NotNull @Min(1)
    private Integer numberOfGuests;

    @NotNull
    private Long tableId;

    private String notes;

    /** Preorder items */
    @Valid
    @Builder.Default
    private List<ReservationResponse.OrderCreateItem> items = new ArrayList<>();

    /** Tuỳ chọn: dùng nếu bạn cho chọn PM ngay trong checkout */
    private String paymentMethod;
}
