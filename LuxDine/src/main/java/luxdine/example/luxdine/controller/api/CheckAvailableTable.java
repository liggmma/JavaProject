package luxdine.example.luxdine.controller.api;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.reservation.dto.request.ReservationCreateRequest;
import luxdine.example.luxdine.domain.reservation.entity.Reservations;
import luxdine.example.luxdine.domain.table.entity.Tables;
import luxdine.example.luxdine.domain.reservation.enums.ReservationStatus;
import luxdine.example.luxdine.domain.table.enums.TableStatus;
import luxdine.example.luxdine.service.reservation.CustomerReservationService;
import luxdine.example.luxdine.domain.table.repository.TableRepository;
import luxdine.example.luxdine.common.util.DateTimeUtils;
import luxdine.example.luxdine.service.seating.TableService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class CheckAvailableTable {

    final CustomerReservationService customerReservationService;
    final TableRepository tableRepository;
    final TableService tableService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/available-tables")
    public List<Map<String, Object>> getAvailableTables(
            @RequestParam("date") String date,              // ISO-8601, ví dụ: 2025-10-22
            @RequestParam("time") String time,              // ISO-8601, ví dụ: 18:30
            @RequestParam("departureTime") String departureTime, // ISO-8601, ví dụ: 20:30
            @RequestParam("numberOfGuests") Integer numberOfGuests) {

        log.info("Getting available tables for date: {}, time: {}, departure: {}, guests: {}",
                 date, time, departureTime, numberOfGuests);

        try {
            if (numberOfGuests == null || numberOfGuests <= 0) {
                return List.of();
            }

            // 1) Parse string -> LocalDate & LocalTime
            LocalDate localDate = LocalDate.parse(date);          // yyyy-MM-dd
            LocalTime localTime = LocalTime.parse(time);          // HH:mm[:ss]
            LocalTime localDepartureTime = LocalTime.parse(departureTime); // HH:mm[:ss]

            // 2) Chuẩn hoá sang OffsetDateTime theo múi giờ nhà hàng
            ZoneId venueZone = DateTimeUtils.getVenueZoneId();    // ví dụ Asia/Ho_Chi_Minh
            OffsetDateTime reservationDate = LocalDateTime.of(localDate, localTime)
                    .atZone(venueZone)
                    .toOffsetDateTime();
            OffsetDateTime reservationDepartureDate = LocalDateTime.of(localDate, localDepartureTime)
                    .atZone(venueZone)
                    .toOffsetDateTime();

            // 3) Validate input theo business rule chung
            ReservationCreateRequest req = new ReservationCreateRequest();
            req.setReservationDate(reservationDate);
            req.setReservationDepartureTime(reservationDepartureDate);
            req.setNumberOfGuests(numberOfGuests);

            // Validate operating hours (9:00 AM - 10:00 PM)
            if (!DateTimeUtils.isWithinOperatingHours(localTime) || !DateTimeUtils.isWithinOperatingHours(localDepartureTime)) {
                log.warn("Reservation time {} or departure {} is outside operating hours (9:00 AM - 10:00 PM)", localTime, localDepartureTime);
                return List.of();
            }

            // Validate departure is after arrival
            if (localDepartureTime.isBefore(localTime)) {
                log.warn("Departure time {} is before arrival time {}", localDepartureTime, localTime);
                return List.of();
            }

            // 5) Lấy ứng viên: bàn AVAILABLE + RESERVED có capacity >= số khách
            List<Tables> available = tableRepository.findByStatusWithArea(TableStatus.AVAILABLE);
            List<Tables> reserved  = tableRepository.findByStatusWithArea(TableStatus.RESERVED);

            List<Tables> candidates = Stream.concat(available.stream(), reserved.stream())
                    .filter(t -> t.getCapacity() >= numberOfGuests)
                    .toList();

            // 6) Các trạng thái reservation đang "giữ bàn"
            EnumSet<ReservationStatus> blockingStatuses = EnumSet.of(
                    ReservationStatus.PENDING,
                    ReservationStatus.CONFIRMED
            );

            // 7) Loại bỏ bàn có conflict trong khoảng thời gian
            return candidates.stream()
                    .filter(t -> {
                        List<Reservations> conflicts =
                                customerReservationService.findConflictsByTableIdAndTimeRange(
                                        t.getId(), reservationDate, reservationDepartureDate);
                        // Giữ lại bàn nếu không có reservation "blocking"
                        return conflicts.stream().noneMatch(r -> blockingStatuses.contains(r.getStatus()));
                    })
                    .map(t -> {
                        Map<String, Object> tableInfo = new HashMap<>();
                        tableInfo.put("id", t.getId());
                        tableInfo.put("tableName", t.getTableName());
                        tableInfo.put("capacity", t.getCapacity());
                        tableInfo.put("depositAmount", t.getDepositAmount());
                        tableInfo.put("tableType", t.getTableType());
                        tableInfo.put("areaId", t.getArea() != null ? t.getArea().getId() : null);
                        tableInfo.put("areaName", t.getArea() != null ? t.getArea().getName() : "Unknown");
                        tableInfo.put("floor", t.getArea() != null ? t.getArea().getFloor() : null);
                        return tableInfo;
                    })
                    .collect(Collectors.toList());

        } catch (DateTimeParseException e) {
            log.error("Invalid date/time: {}", e.getMessage());
            return List.of();
        } catch (Exception e) {
            log.error("Error getting available tables", e);
            return List.of();
        }
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/api/available-tables-old")
    public List<Map<String, Object>> getAvailableTables(
            @RequestParam("date") String date,              // ISO-8601, ví dụ: 2025-10-22
            @RequestParam("time") String time,              // ISO-8601, ví dụ: 18:30
            @RequestParam("numberOfGuests") Integer numberOfGuests) {

        log.info("Getting available tables for date: {}, time: {}, guests: {}", date, time, numberOfGuests);

        try {
            if (numberOfGuests == null || numberOfGuests <= 0) {
                return List.of();
            }

            // 1) Parse string -> LocalDate & LocalTime
            LocalDate localDate = LocalDate.parse(date);          // yyyy-MM-dd
            LocalTime localTime = LocalTime.parse(time);          // HH:mm[:ss]

            // 2) Chuẩn hoá sang OffsetDateTime theo múi giờ nhà hàng
            ZoneId venueZone = DateTimeUtils.getVenueZoneId();    // ví dụ Asia/Ho_Chi_Minh
            OffsetDateTime reservationDate = LocalDateTime.of(localDate, localTime)
                    .atZone(venueZone)
                    .toOffsetDateTime();

            // 3) Validate input theo business rule chung
            ReservationCreateRequest req = new ReservationCreateRequest();
            req.setReservationDate(reservationDate);
            req.setNumberOfGuests(numberOfGuests);

            // Validate operating hours (9:00 AM - 10:00 PM)
            if (!DateTimeUtils.isWithinOperatingHours(localTime)) {
                log.warn("Reservation time {} is outside operating hours (9:00 AM - 10:00 PM)", localTime);
                return List.of(); // Return empty list for times outside operating hours
            }


            // 4) Khung thời gian kiểm tra trùng lịch: ±120 phút quanh thời điểm đặt
            final int WINDOW_MINUTES = 120;
            OffsetDateTime start = reservationDate.minusMinutes(WINDOW_MINUTES);
            OffsetDateTime end   = reservationDate.plusMinutes(WINDOW_MINUTES);

            // 5) Lấy ứng viên: bàn AVAILABLE + RESERVED có capacity >= số khách
            // Nếu repository của bạn là enum:
            List<Tables> available = tableService.findByStatus(TableStatus.AVAILABLE);
            List<Tables> reserved  = tableService.findByStatus(TableStatus.RESERVED);
            // Nếu repository vẫn dùng String -> dùng .name(): findByStatus(TableStatus.AVAILABLE.name())

            List<Tables> candidates = Stream.concat(available.stream(), reserved.stream())
                    .filter(t -> t.getCapacity() >= numberOfGuests)
                    .toList();

            // 6) Các trạng thái reservation đang "giữ bàn" (tuỳ business của bạn)
            EnumSet<ReservationStatus> blockingStatuses = EnumSet.of(
                    ReservationStatus.PENDING,
                    ReservationStatus.CONFIRMED
            ); // CANCELLED không chặn

            // 7) Loại bỏ bàn có conflict trong cửa sổ
            return candidates.stream()
                    .filter(t -> {
                        List<Reservations> conflicts =
                                customerReservationService.findConflictsByTableIdAndWindow(t.getId(), start, end);
                        // Giữ lại bàn nếu không có reservation "blocking"
                        return conflicts.stream().noneMatch(r -> blockingStatuses.contains(r.getStatus()));
                    })
                    .map(t -> {
                        Map<String, Object> tableInfo = new HashMap<>();
                        tableInfo.put("id", t.getId());
                        tableInfo.put("tableName", t.getTableName());
                        tableInfo.put("capacity", t.getCapacity());
                        tableInfo.put("depositAmount", t.getDepositAmount());
                        tableInfo.put("tableType", t.getTableType());
                        tableInfo.put("areaName", t.getArea() != null ? t.getArea().getName() : "Unknown");
                        return tableInfo;
                    })
                    .collect(Collectors.toList());

        } catch (DateTimeParseException e) {
            log.error("Invalid date/time: {}", e.getMessage());
            return List.of();
        } catch (Exception e) {
            log.error("Error getting available tables", e);
            return List.of();
        }
    }
}
