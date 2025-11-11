    package luxdine.example.luxdine.service.admin;

    import lombok.AccessLevel;
    import lombok.RequiredArgsConstructor;
    import lombok.experimental.FieldDefaults;
    import luxdine.example.luxdine.domain.catalog.dto.response.ReportResponse;
    import luxdine.example.luxdine.domain.payment.entity.Payments;
    import luxdine.example.luxdine.domain.payment.repository.PaymentRepository;
    import luxdine.example.luxdine.domain.order.repository.OrderItemsRepository;
    import luxdine.example.luxdine.domain.order.repository.OrdersRepository;
    import luxdine.example.luxdine.domain.user.enums.Role;
    import luxdine.example.luxdine.domain.user.repository.UserRepository;
    import luxdine.example.luxdine.domain.order.enums.OrderStatus;
    import luxdine.example.luxdine.domain.order.enums.OrderItemStatus;
    import luxdine.example.luxdine.domain.payment.enums.PaymentStatus;
    import luxdine.example.luxdine.domain.reservation.repository.ReservationRepository;
    import org.springframework.stereotype.Service;

    import java.time.*;
    import java.util.*;
    import java.util.stream.Collectors;

    @Service
    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public class ReportService {

        PaymentRepository paymentRepository;
        OrdersRepository ordersRepository;
        OrderItemsRepository orderItemsRepository;
        UserRepository userRepository;
        ReservationRepository reservationRepository;

        // ========================== Lấy báo cáo theo chế độ (week / month / quarter / year) ==========================
        public ReportResponse getReportByMode(String mode, int year, int month, int weekNumber, int quarter) {
            LocalDate startDate;
            LocalDate endDate;

            switch (mode.toLowerCase()) {
                case "week" -> {
                    LocalDate firstOfMonth = LocalDate.of(year, month, 1);
                    startDate = firstOfMonth.plusWeeks(weekNumber - 1).with(DayOfWeek.MONDAY);
                    endDate = startDate.plusDays(6);
                    if (endDate.getMonthValue() != month) {
                        endDate = LocalDate.of(year, month, firstOfMonth.lengthOfMonth());
                    }
                }
                case "month" -> {
                    startDate = LocalDate.of(year, month, 1);
                    endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
                }
                case "quarter" -> {
                    int startMonth = (quarter - 1) * 3 + 1;
                    startDate = LocalDate.of(year, startMonth, 1);
                    int endMonth = startMonth + 2;
                    endDate = LocalDate.of(year, endMonth, Month.of(endMonth).length(Year.isLeap(year)));
                }
                case "year" -> {
                    startDate = LocalDate.of(year, 1, 1);
                    endDate = LocalDate.of(year, 12, 31);
                }
                default -> throw new IllegalArgumentException("Invalid mode: " + mode);
            }

            return getReport(startDate, endDate, mode, year);
        }

        // ========================== Tổng hợp dữ liệu báo cáo ==========================
        private ReportResponse getReport(LocalDate startDate, LocalDate endDate, String mode, int year) {
            Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

            // ✅ Doanh thu trong khoảng thời gian xem
            double totalRevenue = paymentRepository
                    .findByStatusAndCreatedDateBetween(PaymentStatus.COMPLETED, startInstant, endInstant)
                    .stream().mapToDouble(Payments::getAmount).sum();

            // ✅ Chuyển đổi sang Instant khi gọi OrdersRepository (đảm bảo cùng kiểu dữ liệu)
            Instant startInstantForOrders = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endInstantForOrders = endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

            // ✅ Đơn hàng, đơn hủy, đơn hoàn tiền theo thời gian xem
            long totalOrders = ordersRepository.findByCreatedDateBetween(startInstantForOrders, endInstantForOrders).size();

            long totalCancelledOrders = ordersRepository
                    .findByStatusAndCreatedDateBetween(OrderStatus.CANCELLED, startInstantForOrders, endInstantForOrders)
                    .size();

            long totalRefundedPayments = paymentRepository
                    .findByStatusAndCreatedDateBetween(PaymentStatus.REFUNDED, startInstant, endInstant)
                    .size();

            // ✅ Đếm bàn đặt bằng Reservation (lọc theo thời gian)
            OffsetDateTime start = startDate.atStartOfDay().atOffset(ZoneOffset.UTC);
            OffsetDateTime end = endDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

            long totalReservedTables = reservationRepository.countReservedTablesBetween(start, end);

            // ✅ Người dùng
            long totalUsers = userRepository.findAllByRole(Role.CUSTOMER).size();
            long totalStaff = userRepository.findAllByRole(Role.STAFF).size();

            // ✅ Biểu đồ doanh thu
            Map<String, Double> revenueChart = switch (mode.toLowerCase()) {
                case "week" -> getRevenueByWeek(startDate);
                case "month" -> getRevenueByMonthDays(startDate.getYear(), startDate.getMonthValue());
                case "quarter" -> getRevenueByQuarterMonths(year, (startDate.getMonthValue() - 1) / 3 + 1);
                case "year" -> getRevenueByYearMonths(year);
                default -> new LinkedHashMap<>();
            };

            // ✅ Top 5 món bán chạy
            List<Map<String, Object>> top5Items = getTop5SellingItemsAllTime();

            return ReportResponse.builder()
                    .totalRevenue(totalRevenue)
                    .totalOrders(totalOrders)
                    .totalCancelledOrders(totalCancelledOrders)
                    .totalRefundedPayments(totalRefundedPayments)
                    .totalUsers(totalUsers)
                    .totalStaff(totalStaff)
                    .totalReservedTables(totalReservedTables)
                    .top5Items(top5Items)
                    .revenueByDate(revenueChart)
                    .reportStartDate(startDate)
                    .reportEndDate(endDate)
                    .build();
        }

        // ========================== Doanh thu theo tuần ==========================
        private Map<String, Double> getRevenueByWeek(LocalDate startOfWeek) {
            Map<String, Double> data = new LinkedHashMap<>();
            String[] days = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};
            for (int i = 0; i < 7; i++) {
                LocalDate date = startOfWeek.plusDays(i);
                Instant s = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
                Instant e = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

                double total = paymentRepository.findByStatusAndCreatedDateBetween(PaymentStatus.COMPLETED, s, e)
                        .stream().mapToDouble(Payments::getAmount).sum();

                data.put(days[i], total);
            }
            return data;
        }

        // ========================== Doanh thu theo ngày trong tháng ==========================
        private Map<String, Double> getRevenueByMonthDays(int year, int month) {
            Map<String, Double> data = new LinkedHashMap<>();
            LocalDate start = LocalDate.of(year, month, 1);
            int days = start.lengthOfMonth();

            for (int i = 1; i <= days; i++) {
                LocalDate date = LocalDate.of(year, month, i);
                Instant s = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
                Instant e = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

                double total = paymentRepository.findByStatusAndCreatedDateBetween(PaymentStatus.COMPLETED, s, e)
                        .stream().mapToDouble(Payments::getAmount).sum();

                data.put(String.valueOf(i), total);
            }
            return data;
        }

        // ========================== Doanh thu theo tháng trong năm ==========================
        private Map<String, Double> getRevenueByYearMonths(int year) {
            Map<String, Double> data = new LinkedHashMap<>();
            for (int i = 1; i <= 12; i++) {
                LocalDate start = LocalDate.of(year, i, 1);
                LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
                Instant s = start.atStartOfDay(ZoneId.systemDefault()).toInstant();
                Instant e = end.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

                double total = paymentRepository.findByStatusAndCreatedDateBetween(PaymentStatus.COMPLETED, s, e)
                        .stream().mapToDouble(Payments::getAmount).sum();

                data.put("Tháng " + i, total);
            }
            return data;
        }

        // ========================== Doanh thu theo quý ==========================
        private Map<String, Double> getRevenueByQuarterMonths(int year, int quarter) {
            Map<String, Double> data = new LinkedHashMap<>();
            int startMonth = (quarter - 1) * 3 + 1;

            for (int i = 0; i < 3; i++) {
                int currentMonth = startMonth + i;
                LocalDate start = LocalDate.of(year, currentMonth, 1);
                LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
                Instant s = start.atStartOfDay(ZoneId.systemDefault()).toInstant();
                Instant e = end.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

                double total = paymentRepository.findByStatusAndCreatedDateBetween(PaymentStatus.COMPLETED, s, e)
                        .stream().mapToDouble(Payments::getAmount).sum();

                data.put("Tháng " + currentMonth, total);
            }
            return data;
        }

    // ========================== Top 5 món bán chạy ==========================
    // Fix: Optimized query - no longer using findAll() to avoid loading entire DB
    private List<Map<String, Object>> getTop5SellingItemsAllTime() {
        List<Object[]> results = orderItemsRepository.findTop5SellingItems(OrderItemStatus.SERVED);
        
        return results.stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", row[0] != null ? row[0].toString() : "Unknown");
                    map.put("totalSold", row[1] != null ? ((Number) row[1]).longValue() : 0L);
                    return map;
                })
                .collect(Collectors.toList());
    }
    }
