package luxdine.example.luxdine.domain.reservation.dto.response;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import luxdine.example.luxdine.domain.reservation.dto.request.ReservationCreateRequest;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sửa kiểu dữ liệu của thời gian từ Date thành ZoneDatedTime
 * <p> Tác giá: Lê Ngọc Minh Kiên</p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReservationResponse {
    Long id;
    String reservationCode;
    String status;
    OffsetDateTime reservationDate;
    OffsetDateTime reservationDepartureTime;
    OffsetDateTime actualArrivalTime;
    Integer numberOfGuests;
    Double depositAmount;
    String notes;
    Instant createdAt;
    
    // User info
    String customerName;
    String customerEmail;
    String customerPhone;
    
    // Table info (if assigned)
    Long tableId;
    String tableName;
    String tableType;
    Integer tableCapacity;
    String areaName;

    @Builder.Default
    List<OrderCreateItem> items = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderCreateItem {
        private Long itemId;
        private String itemName;

        @NotNull(message = "Thiếu số lượng")
        @Min(value = 1, message = "Số lượng tối thiểu là 1")
        private Integer quantity;
    }
}
