package luxdine.example.luxdine.controller.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import luxdine.example.luxdine.domain.reservation.dto.request.ReservationCreateRequest;
import luxdine.example.luxdine.domain.reservation.dto.response.ReservationResponse;
import luxdine.example.luxdine.service.reservation.CustomerReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationPricingApi {

    private final CustomerReservationService customerReservationService;

    @PostMapping("/calculate-deposit")
    public ResponseEntity<CalcDepositResponse> calculateDeposit(@Valid @RequestBody CalcDepositRequest req) {
        double amount = customerReservationService.calculateDepositAmount(req.getTableId(), req.getItems());
        // VND không có phần thập phân: làm tròn về số nguyên
        long rounded = Math.round(amount);
        return ResponseEntity.ok(new CalcDepositResponse(rounded));
    }

    // --- DTOs ---
    @Data
    public static class CalcDepositRequest {
        @NotNull
        private Long tableId;
        @NotNull
        List<ReservationResponse.OrderCreateItem> items;
    }

    public record CalcDepositResponse(long depositAmount) {}
}