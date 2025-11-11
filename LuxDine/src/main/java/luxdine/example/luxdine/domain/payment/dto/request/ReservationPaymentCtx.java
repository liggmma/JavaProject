package luxdine.example.luxdine.domain.payment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import luxdine.example.luxdine.domain.reservation.dto.request.ReservationCreateRequest;
import luxdine.example.luxdine.domain.reservation.dto.request.ReservationUpdateRequest;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationPaymentCtx {
    // NEW_DEPOSIT | DEPOSIT_TOP_UP
    private String purpose;

    // Ai là chủ reservation (để tạo mới)
    private String username;

    // Tạo mới: payload tạo reservation (đầy đủ trường)
    private ReservationCreateRequest create;

    // Top-up: id reservation + payload update
    private Long reservationId;
    private ReservationUpdateRequest update;

    // Số tiền phải trả (đã round long)
    private Long amount;
}