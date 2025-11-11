package luxdine.example.luxdine.domain.catalog.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportResponse {
    double totalRevenue;
    long totalOrders;
    long totalCancelledOrders;
    long totalRefundedPayments;
    long totalUsers;
    long totalStaff;
    long totalReservedTables;
    List<Map<String, Object>> top5Items;
    List<Map<String, Object>> top5RevenueItems;
    Map<String, Double> revenueByDate;
    LocalDate reportStartDate;
    LocalDate reportEndDate;
}
