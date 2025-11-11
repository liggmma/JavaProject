package luxdine.example.luxdine.domain.reservation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import luxdine.example.luxdine.domain.order.dto.request.OrderCreateRequest;
import luxdine.example.luxdine.domain.reservation.dto.response.ReservationResponse;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO (Data Transfer Object) dùng để nhận dữ liệu khi khách hàng
 * tạo mới một đơn đặt bàn (Reservation).
 *
 * Class này thường được sử dụng trong Controller để hứng dữ liệu
 * từ form hoặc JSON request gửi lên từ client.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReservationCreateRequest {
    private OffsetDateTime reservationDate;
    private OffsetDateTime reservationDepartureTime;
    @NotNull
    private Integer numberOfGuests;
    private Double depositAmount;
    private String notes;
    @NotNull
    private Long tableId;
    String paymentMethod;
    @Builder.Default
    List<ReservationResponse.OrderCreateItem> items = new ArrayList<>();
}




