package luxdine.example.luxdine.domain.table.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TableResponse {
    Long id;
    String tableName;
    String tableType;
    Integer capacity;
    double depositAmount;
    String status;
    String areaName;
    Integer floor;
    Long areaId;
    Date createdDate;
    Date updatedDate;
    
    // Reservation details if table is reserved
    String reservationGuest;
    String reservationTime;
    Integer reservationPartySize;
    String reservationCode;
}
