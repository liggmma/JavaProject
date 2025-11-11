package luxdine.example.luxdine.domain.reservation.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import jakarta.validation.constraints.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReservationSearchRequest {
    @Size(max = 100, message = "Search term cannot exceed 100 characters")
    String searchTerm;
    
    @Pattern(regexp = "^(CONFIRMED|PENDING|CANCELLED|COMPLETED|)$", 
             message = "Status must be one of: CONFIRMED, PENDING, CANCELLED, COMPLETED or empty")
    String status;
    
    @Pattern(regexp = "^(today|tomorrow|this_week|this_month|)$", 
             message = "Date range must be one of: today, tomorrow, this_week, this_month or empty")
    String dateRange;
    
    @Pattern(regexp = "^(reservationDate|status|numberOfGuests|)$", 
             message = "Sort by must be one of: reservationDate, status, numberOfGuests or empty")
    String sortBy;
    
    @Pattern(regexp = "^(asc|desc|)$", 
             message = "Sort direction must be asc, desc or empty")
    String sortDirection;
}
