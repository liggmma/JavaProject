package luxdine.example.luxdine.service.reservation;

import luxdine.example.luxdine.common.constants.BusinessConstants;
import luxdine.example.luxdine.domain.catalog.repository.ItemsRepository;
import luxdine.example.luxdine.domain.order.entity.OrderItems;
import luxdine.example.luxdine.domain.order.entity.Orders;
import luxdine.example.luxdine.domain.order.enums.OrderItemStatus;
import luxdine.example.luxdine.domain.order.enums.OrderStatus;
import luxdine.example.luxdine.domain.order.repository.OrderItemsRepository;
import luxdine.example.luxdine.domain.order.repository.OrdersRepository;
import luxdine.example.luxdine.domain.reservation.dto.request.ReservationCreateRequest;
import luxdine.example.luxdine.domain.reservation.dto.request.ReservationSearchRequest;
import luxdine.example.luxdine.domain.reservation.dto.request.ReservationUpdateRequest;
import luxdine.example.luxdine.domain.reservation.dto.response.ReservationResponse;
import luxdine.example.luxdine.domain.reservation.entity.Reservations;
import luxdine.example.luxdine.domain.reservation.enums.ReservationOrigin;
import luxdine.example.luxdine.domain.table.entity.Tables;
import luxdine.example.luxdine.domain.reservation.enums.ReservationStatus;
import luxdine.example.luxdine.domain.table.enums.TableStatus;
import luxdine.example.luxdine.domain.reservation.repository.ReservationRepository;
import luxdine.example.luxdine.domain.table.repository.TableRepository;
import luxdine.example.luxdine.service.order.OrderService;
import luxdine.example.luxdine.common.util.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TableRepository tableRepository;
    private final OrdersRepository  ordersRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final ItemsRepository itemsRepository;

    public Reservations getById(long id) {
        return reservationRepository.findById(id).orElse(null);
    }

    /**
     * Get today's reservations for staff dashboard
     * @return List of today's reservations
     */
    @Transactional(readOnly = true)
    public List<ReservationResponse> getTodayReservations() {
        log.debug("Fetching today's reservations");
        OffsetDateTime today = OffsetDateTime.now();
        List<Reservations> todayReservations = reservationRepository.findByReservationDate(today);

        return todayReservations.stream()
                .map(this::convertToReservationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get reservations by status
     * @param status Reservation status
     * @return List of reservations with specified status
     */
    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByStatus(ReservationStatus status) {
        log.debug("Fetching reservations with status: {}", status);
        return reservationRepository.findByStatus(status).stream()
                .map(this::convertToReservationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert entity to DTO
     * @param reservation Entity to convert
     * @return ReservationResponse DTO
     */
    public ReservationResponse convertToReservationResponse(Reservations reservation) {
        log.debug("Converting reservation {} with date: {}", reservation.getId(), reservation.getReservationDate());

        List<ReservationResponse.OrderCreateItem> items =
                buildItemsFromOrders(
                        Optional.ofNullable(reservation.getOrders()).orElseGet(List::of)
                );

        return ReservationResponse.builder()
                .id(reservation.getId())
                .reservationCode(reservation.getReservationCode())
                .status(String.valueOf(reservation.getStatus()))
                .reservationDate(reservation.getReservationDate() != null ? reservation.getReservationDate().atZoneSameInstant(DateTimeUtils.getVenueZoneId()).toOffsetDateTime() : null)
                .reservationDepartureTime(reservation.getReservationDepartureTime() != null ? reservation.getReservationDepartureTime().atZoneSameInstant(DateTimeUtils.getVenueZoneId()).toOffsetDateTime() : null)
                .actualArrivalTime(reservation.getActualArrivalTime() != null ? reservation.getActualArrivalTime().atZoneSameInstant(DateTimeUtils.getVenueZoneId()).toOffsetDateTime() : null)
                .numberOfGuests(reservation.getNumberOfGuests())
                .depositAmount(reservation.getDepositAmount())
                .notes(reservation.getNotes())
                .createdAt(reservation.getCreatedAt())
                .customerName(buildCustomerName(reservation))
                .customerEmail(getCustomerEmail(reservation))
                .customerPhone(getCustomerPhone(reservation))
                .tableName(getTableName(reservation))
                .tableType(getTableType(reservation))
                .tableCapacity(getTableCapacity(reservation))
                .areaName(getAreaName(reservation))
                .tableId(reservation.getTable().getId())
                .items(items)
                .build();
    }

    private List<ReservationResponse.OrderCreateItem> buildItemsFromOrders(List<Orders> orders) {
        if (orders == null || orders.isEmpty()) return List.of();

        // Giữ thứ tự gặp đầu tiên
        Map<Long, ReservationResponse.OrderCreateItem> acc = new LinkedHashMap<>();

        orders.stream()
                .filter(Objects::nonNull)
                .flatMap(o -> {
                    List<OrderItems> list = o.getOrderItems();
                    return (list == null) ? java.util.stream.Stream.<OrderItems>empty() : list.stream();
                })
                .filter(Objects::nonNull)
                // chỉ tính các dòng là item thường; bỏ qua bundle
                .filter(oi -> oi.getItem() != null)
                .forEach(oi -> {
                    Long itemId = oi.getItem().getId();
                    String itemName = (oi.getNameSnapshot() != null && !oi.getNameSnapshot().isBlank())
                            ? oi.getNameSnapshot()
                            : oi.getItem().getName();

                    acc.compute(itemId, (k, v) -> {
                        if (v == null) {
                            return new ReservationResponse.OrderCreateItem(itemId, itemName, 1);
                        }
                        v.setQuantity(v.getQuantity() + 1);
                        return v; // giữ nguyên tên lần gặp đầu tiên
                    });
                });

        return new ArrayList<>(acc.values());
    }



    private String buildCustomerName(Reservations reservation) {
        if (reservation.getUser() == null) {
            return "Unknown";
        }
        return String.format("%s %s",
            reservation.getUser().getFirstName(),
            reservation.getUser().getLastName());
    }

    private String getCustomerEmail(Reservations reservation) {
        return reservation.getUser() != null ? reservation.getUser().getEmail() : "";
    }

    private String getCustomerPhone(Reservations reservation) {
        return reservation.getUser() != null ? reservation.getUser().getPhoneNumber() : "";
    }

    private String getTableName(Reservations reservation) {
        return reservation.getTable() != null ? reservation.getTable().getTableName() : BusinessConstants.NOT_ASSIGNED;
    }

    private String getTableType(Reservations reservation) {
        return reservation.getTable() != null ? reservation.getTable().getTableType() : "";
    }

    private Integer getTableCapacity(Reservations reservation) {
        return reservation.getTable() != null ? reservation.getTable().getCapacity() : 0;
    }

    private String getAreaName(Reservations reservation) {
        return reservation.getTable() != null && reservation.getTable().getArea() != null
            ? reservation.getTable().getArea().getName() : BusinessConstants.UNKNOWN_VALUE;
    }

    /**
     * Get all reservations with search and filter capabilities
     * @param searchRequest Search and filter parameters
     * @return List of filtered reservations
     */
    @Transactional(readOnly = true)
    public List<ReservationResponse> getAllReservations(ReservationSearchRequest searchRequest) {
        log.debug("Fetching reservations with search and filters: {}", searchRequest);

        String searchTerm = searchRequest.getSearchTerm();
        ReservationStatus status = ReservationStatus.fromNullable(searchRequest.getStatus());
        Date startDate = null;
        Date endDate = null;

        // Parse date range if provided
        if (searchRequest.getDateRange() != null && !searchRequest.getDateRange().isEmpty()) {
            Date[] dateRange = parseDateRange(searchRequest.getDateRange());
            startDate = dateRange[0];
            endDate = dateRange[1];
        }

        List<Reservations> reservations;

        // FIXED: Use eager loading to avoid N+1 queries
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            reservations = reservationRepository.findBySearchAndFiltersWithEagerLoading(
                searchTerm.trim(), status, startDate, endDate);
        } else {
            reservations = reservationRepository.findByFiltersWithEagerLoading(status, startDate, endDate);
        }

        List<Reservations> removedWalkIn = new ArrayList<>();
        for (Reservations reservation : reservations) {
            if (!reservation.getOrigin().equals(ReservationOrigin.WALK_IN)) {
                removedWalkIn.add(reservation);
            }
        }

        return removedWalkIn.stream()
                .map(this::convertToReservationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search reservations by term
     * @param searchTerm Search term (name, email, or reservation code)
     * @return List of matching reservations
     */
    @Transactional(readOnly = true)
    public List<ReservationResponse> searchReservations(String searchTerm) {
        log.debug("Searching reservations with term: {}", searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllReservations(ReservationSearchRequest.builder().build());
        }

        List<Reservations> reservations = reservationRepository.findBySearchTerm(searchTerm.trim());

        return reservations.stream()
                .map(this::convertToReservationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Filter reservations by status and date range
     * @param status Reservation status
     * @param dateRange Date range string
     * @return List of filtered reservations
     */
    @Transactional(readOnly = true)
    public List<ReservationResponse> filterReservations(ReservationStatus status, String dateRange) {
        log.debug("Filtering reservations by status: {} and dateRange: {}", status.name(), dateRange);

        // Handle status filter - convert empty string to null for "All Statuses"
        if (status != null) {
            status = null;
        }

        Date startDate = null;
        Date endDate = null;

        if (dateRange != null && !dateRange.isEmpty()) {
            Date[] dates = parseDateRange(dateRange);
            startDate = dates[0];
            endDate = dates[1];
        }

        List<Reservations> reservations = reservationRepository.findByFilters(status, startDate, endDate);

        return reservations.stream()
                .map(this::convertToReservationResponse)
                .collect(Collectors.toList());
    }


    @Transactional
    public ReservationResponse updateReservation(ReservationUpdateRequest req) {
        final Long id = req.getId();
        if (id == null) {
            throw new IllegalArgumentException("Reservation id is required");
        }

        // 1) Tải reservation và chặn các trạng thái không cho sửa
        Reservations r = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + id));
        if (r.getStatus() == ReservationStatus.CONFIRMED
                || r.getStatus() == ReservationStatus.ARRIVED
                || r.getStatus() == ReservationStatus.CANCELLED) {
            throw new RuntimeException(
                    "Cannot update reservation with status: " + r.getStatus()
                            + ". Only WAITING_FOR_PAYMENT or PENDING can be updated.");
        }

        // 2) Chuẩn bị dữ liệu cập nhật (null -> giữ nguyên)
        OffsetDateTime newDate    = (req.getReservationDate() != null) ? req.getReservationDate() : r.getReservationDate();
        Integer        newGuests  = (req.getNumberOfGuests() != null)  ? req.getNumberOfGuests()  : r.getNumberOfGuests();
        Long           newTableId = req.getTableId(); // nếu null => giữ nguyên bàn hiện tại
        String         newNotes   = (req.getNotes() != null) ? req.getNotes() : r.getNotes();

        // 3) Validate bàn & sức chứa
        Tables currentTable = r.getTable();
        Tables targetTable  = currentTable;

        if (newTableId != null && (currentTable == null || !currentTable.getId().equals(newTableId))) {
            targetTable = tableRepository.findById(newTableId)
                    .orElseThrow(() -> new RuntimeException("Table not found with id: " + newTableId));
            if (targetTable.getStatus() != TableStatus.AVAILABLE) {
                throw new RuntimeException("Table is not available. Current status: " + targetTable.getStatus());
            }
        }
        // check capacity nếu đã có bàn (targetTable) và đã biết số khách
        if (targetTable != null && newGuests != null && newGuests > targetTable.getCapacity()) {
            throw new RuntimeException("Number of guests (" + newGuests + ") exceeds table capacity ("
                    + targetTable.getCapacity() + ")");
        }

        // 4) Áp dụng cập nhật cơ bản
        r.setReservationDate(newDate);
        r.setNumberOfGuests(newGuests);
        r.setNotes(newNotes);

        // đổi bàn (nếu có)
        if (targetTable != null && (currentTable == null || !currentTable.getId().equals(targetTable.getId()))) {
            if (currentTable != null && !currentTable.getId().equals(targetTable.getId())) {
                currentTable.setStatus(TableStatus.AVAILABLE);
                tableRepository.save(currentTable);
            }
            // KHÔNG đổi trạng thái targetTable ở đây (giữ AVAILABLE),
            // scheduler sẽ chuyển RESERVED đúng thời điểm
            r.setTable(targetTable);
        }

        // 5) Cập nhật preorder items vào "order đầu tiên"
        if (req.getItems() != null) {
            Orders firstOrder = (r.getOrders() == null || r.getOrders().isEmpty())
                    ? null
                    : r.getOrders().stream()
                    .sorted(Comparator.comparing(Orders::getCreatedDate))
                    .findFirst().orElse(null);

            if (firstOrder == null) {
                // nếu vì lý do nào đó chưa có order => tạo skeleton
                firstOrder = new Orders();
                firstOrder.setReservation(r);
                firstOrder.setStatus(OrderStatus.PENDING);
                ordersRepository.save(firstOrder);
                // liên kết 2 chiều
                r.getOrders().add(firstOrder);
            }

            // Xóa sạch item cũ và rebuild
            orderItemsRepository.deleteByOrder(firstOrder);
            if (firstOrder.getOrderItems() != null) {
                firstOrder.getOrderItems().clear();
            }

            // gộp quantity theo itemId
            Map<Long, Integer> qtyByItem = new LinkedHashMap<>();
            for (ReservationResponse.OrderCreateItem it : req.getItems()) {
                if (it == null || it.getItemId() == null || it.getQuantity() == null) continue;
                if (it.getQuantity() < 1) continue;
                qtyByItem.merge(it.getItemId(), it.getQuantity(), Integer::sum);
            }

            List<OrderItems> rebuilt = new ArrayList<>();
            double subTotal = 0d;

            for (Map.Entry<Long, Integer> e : qtyByItem.entrySet()) {
                Long itemId = e.getKey();
                int q = e.getValue();
                var item = itemsRepository.findById(itemId)
                        .orElseThrow(() -> new RuntimeException("Item not found: " + itemId));

                for (int i = 0; i < q; i++) {
                    OrderItems oi = OrderItems.builder()
                            .order(firstOrder)
                            .item(item)
                            .bundle(null)
                            .nameSnapshot(item.getName())
                            .priceSnapshot(item.getPrice())
                            .status(OrderItemStatus.PENDING)
                            .build();
                    rebuilt.add(oi);
                    subTotal += item.getPrice();
                }
            }

            firstOrder.setOrderItems(rebuilt);
            firstOrder.setSubTotal(subTotal);
            firstOrder.setDiscountTotal(0d);
            firstOrder.setTax(0d);
            firstOrder.setServiceCharge(0d);
            firstOrder.setDepositApplied(r.getDepositAmount());
            firstOrder.setAmountDue(Math.max(0d, subTotal)); // nếu có rule khác thì cập nhật ở đây

            ordersRepository.save(firstOrder);
        }

        // 6) Lưu reservation & trả DTO
        Reservations saved = reservationRepository.save(r);
        return convertToReservationResponse(saved);
    }

    /**
     * Handle table status changes when reservation status changes
     */
    private void handleReservationStatusChange(Reservations reservation, ReservationStatus oldStatus, ReservationStatus newStatus) {
        Tables table = reservation.getTable();
        if (table == null) {
            return; // No table assigned, nothing to update
        }

        // If reservation is cancelled, free the table
        if (ReservationStatus.CANCELLED.equals(newStatus)) {
            table.setStatus(TableStatus.AVAILABLE);
            tableRepository.save(table);
            log.info("Table {} freed due to reservation status change to {}", table.getId(), newStatus);
        }
        // If reservation is arrived, change table from RESERVED to OCCUPIED
        else if (ReservationStatus.ARRIVED.equals(newStatus)) {
            table.setStatus(TableStatus.OCCUPIED);
            tableRepository.save(table);
            log.info("✅ Table {} status changed from RESERVED to OCCUPIED due to reservation arrival", table.getId());
        }
        // If reservation is confirmed, check if it's time to activate table immediately
        else if (ReservationStatus.CONFIRMED.equals(newStatus)) {
            // FIXED: Always check if reservation time has arrived when confirming
            if (isReservationTimeReached(reservation)) {
                // Time has arrived, activate table immediately
                table.setStatus(TableStatus.RESERVED);
                tableRepository.save(table);
                log.info("✅ Table {} immediately activated to RESERVED due to reservation confirmation at scheduled time", table.getId());
            } else {
                // FIXED: If reservation time hasn't arrived, keep table AVAILABLE
                // This prevents double booking and allows other reservations to use the table
                if (TableStatus.RESERVED.equals(table.getStatus())) {
                    table.setStatus(TableStatus.AVAILABLE);
                    tableRepository.save(table);
                    log.info("🔄 Table {} reverted to AVAILABLE - reservation time not yet reached", table.getId());
                } else {
                    log.info("⏰ Table {} remains {} - will be activated at reservation time",
                            table.getId(), table.getStatus());
                }
            }
        }
        // If reservation goes back to PENDING, keep table status unchanged
        else if (ReservationStatus.PENDING.equals(newStatus)) {
            // NEW LOGIC: Không thay đổi trạng thái bàn khi reservation về PENDING
            // Bàn vẫn giữ nguyên trạng thái hiện tại
            log.info("Table {} remains {} due to reservation status change to PENDING",
                    table.getId(), table.getStatus());
        }
    }

    /**
     * Get reservation by ID
     * @param id Reservation ID
     * @return Reservation response
     */
    public ReservationResponse getReservationById(Long id) {
        log.debug("Fetching reservation by ID: {}", id);

        Reservations reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + id));

        return convertToReservationResponse(reservation);
    }

    /**
     * Accept a reservation (change status from PENDING to CONFIRMED)
     * @param reservationId Reservation ID to accept
     * @return Updated reservation response
     */
    @Transactional
    public ReservationResponse acceptReservation(Long reservationId) {
        log.info("Accepting reservation {}", reservationId);

        Reservations reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + reservationId));

        // Check if reservation is in PENDING status
        if (!ReservationStatus.PENDING.equals(reservation.getStatus())) {
            throw new RuntimeException("Cannot accept reservation with status: " + reservation.getStatus() + ". Only PENDING reservations can be accepted.");
        }

        // Update reservation status to CONFIRMED
        ReservationStatus oldStatus = reservation.getStatus();
        reservation.setStatus(ReservationStatus.CONFIRMED);

        // Handle table status change
        handleReservationStatusChange(reservation, oldStatus, ReservationStatus.CONFIRMED);

        Reservations savedReservation = reservationRepository.save(reservation);
        log.info("Reservation {} accepted successfully", reservationId);

        return convertToReservationResponse(savedReservation);
    }

    /**
     * Mark reservation as arrived (change status from CONFIRMED to ARRIVED)
     * and update table status from RESERVED to OCCUPIED
     * Also creates an empty order for staff to add items to
     * @param reservationId Reservation ID
     * @return Updated reservation response
     */
    @Transactional
    public ReservationResponse markReservationAsArrived(Long reservationId) {
        log.info("Marking reservation {} as arrived", reservationId);

        Reservations reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found: " + reservationId));

        // Check if reservation is in CONFIRMED status
        ReservationStatus oldStatus = reservation.getStatus();
        if (!ReservationStatus.CONFIRMED.equals(oldStatus)) {
            throw new RuntimeException("Cannot mark as arrived - reservation is not in CONFIRMED status. Current status: " + oldStatus);
        }

        // Set actual arrival time
        reservation.setActualArrivalTime(OffsetDateTime.now());

        // Update reservation status to ARRIVED
        reservation.setStatus(ReservationStatus.ARRIVED);

        // Handle table status change
        handleReservationStatusChange(reservation, oldStatus, ReservationStatus.ARRIVED);

        Reservations savedReservation = reservationRepository.save(reservation);

        log.info("Reservation {} marked as arrived successfully", reservationId);

        return convertToReservationResponse(savedReservation);
    }



    /**
     * Check and activate tables for reservations that are due now
     * This method can be called immediately when needed
     * @return Number of tables activated
     */
    @Transactional
    public int checkAndActivateDueReservations() {
        log.info("🔍 Checking for reservations that are due now");

        try {
            // Get all confirmed reservations that should be activated now
            List<Reservations> confirmedReservations = reservationRepository.findConfirmedReservationsForActivation();
            log.info("📋 Found {} confirmed reservations to check for activation", confirmedReservations.size());

            if (confirmedReservations.isEmpty()) {
                log.info("✅ No reservations need activation at this time");
                return 0;
            }

            int activatedCount = 0;
            for (Reservations reservation : confirmedReservations) {
                try {
                    log.info("🔍 Checking reservation {} with date: {}", reservation.getId(), reservation.getReservationDate());

                    if (reservation.getTable() != null) {
                        Tables table = reservation.getTable();
                        log.info("🪑 Table {} current status: {}", table.getId(), table.getStatus());

                        // NEW LOGIC: Chỉ kích hoạt nếu bàn vẫn AVAILABLE
                        if (TableStatus.AVAILABLE.name().equals(table.getStatus())) {
                            // Chuyển AVAILABLE → RESERVED
                            table.setStatus(TableStatus.RESERVED);
                            tableRepository.save(table);
                            activatedCount++;

                            log.info("✅ Table {} activated from AVAILABLE to RESERVED for reservation {} immediately",
                                    table.getId(), reservation.getId());
                        } else {
                            log.info("ℹ️ Table {} for reservation {} is already in status: {}",
                                    table.getId(), reservation.getId(), table.getStatus());
                        }
                    } else {
                        log.warn("⚠️ Reservation {} has no table assigned", reservation.getId());
                    }
                } catch (Exception e) {
                    log.error("❌ Error processing reservation {}: {}", reservation.getId(), e.getMessage(), e);
                    // Continue processing other reservations
                }
            }

            if (activatedCount > 0) {
                log.info("🎉 Activated {} tables for confirmed reservations", activatedCount);
            } else {
                log.info("ℹ️ No tables were activated in this run");
            }

            return activatedCount;
        } catch (Exception e) {
            log.error("❌ Error in checkAndActivateDueReservations: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Force activate table for confirmed reservation (for immediate activation)
     * @param reservationId Reservation ID
     * @return true if table was activated
     */
    @Transactional
    public boolean forceActivateTableForReservation(Long reservationId) {
        log.info("Force activating table for reservation: {}", reservationId);

        try {
            Reservations reservation = reservationRepository.findById(reservationId)
                    .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + reservationId));

            if (!"CONFIRMED".equals(reservation.getStatus())) {
                log.warn("Cannot activate table for reservation {} - status is not CONFIRMED: {}",
                        reservationId, reservation.getStatus());
                return false;
            }

            if (reservation.getTable() == null) {
                log.warn("Cannot activate table for reservation {} - no table assigned", reservationId);
                return false;
            }

            Tables table = reservation.getTable();
            table.setStatus(TableStatus.RESERVED);
            tableRepository.save(table);

            log.info("Table {} force activated for reservation {}", table.getId(), reservationId);
            return true;

        } catch (Exception e) {
            log.error("Error force activating table for reservation {}: {}", reservationId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Check if reservation time has been reached
     * @param reservation Reservation to check
     * @return true if reservation time has been reached
     */
    private boolean isReservationTimeReached(Reservations reservation) {
        if (reservation.getReservationDate() == null) {
            log.debug("Reservation {} has null reservation date", reservation.getId());
            return false;
        }

        // Use UTC for consistent comparison to avoid timezone issues
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime reservationTime = reservation.getReservationDate();

        // Check if current time is at or after reservation time (no buffer)
        boolean isTimeReached = now.isAfter(reservationTime) || now.isEqual(reservationTime);

        log.info("⏰ Time check for reservation {}: now={} UTC, reservation={} UTC, reached={}",
                reservation.getId(), now, reservationTime, isTimeReached);

        return isTimeReached;
    }

    /**
     * Parse date range string to start and end dates
     * @param dateRange Date range string (e.g., "today", "tomorrow", "this_week", "this_month")
     * @return Array of [startDate, endDate]
     */
    private Date[] parseDateRange(String dateRange) {
        LocalDate today = LocalDate.now();
        Date startDate = null;
        Date endDate = null;

        switch (dateRange.toLowerCase()) {
            case "today":
                startDate = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
                // End of today (23:59:59)
                endDate = Date.from(today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
                break;
            case "tomorrow":
                LocalDate tomorrow = today.plusDays(1);
                startDate = Date.from(tomorrow.atStartOfDay(ZoneId.systemDefault()).toInstant());
                // End of tomorrow (23:59:59)
                endDate = Date.from(tomorrow.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
                break;
            case "this_week":
                // Start of week (Monday)
                LocalDate startOfWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
                // End of week (Sunday)
                LocalDate endOfWeek = startOfWeek.plusDays(6);
                startDate = Date.from(startOfWeek.atStartOfDay(ZoneId.systemDefault()).toInstant());
                endDate = Date.from(endOfWeek.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
                break;
            case "this_month":
                // Start of month
                LocalDate startOfMonth = today.withDayOfMonth(1);
                // End of month
                LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
                startDate = Date.from(startOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant());
                endDate = Date.from(endOfMonth.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
                break;
        }

        return new Date[]{startDate, endDate};
    }

    //Thêm method tạo mã reservation
    public  String generateReservationCode() {
        return "RSV-" + System.currentTimeMillis();
    }

    /**
     * Validate reservation request
     * @param req Reservation create request
     * @throws RuntimeException if validation fails
     */
    public void validateReservationRequest(ReservationCreateRequest req) {

        // 1) Không cho đặt ngày quá khứ (so với "bây giờ" UTC)
        if (req.getReservationDate() != null
                && req.getReservationDate().isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
            throw new RuntimeException(BusinessConstants.ERROR_CANNOT_BOOK_IN_PAST);
        }

        // 2) Kiểm tra trong giờ mở cửa
        if (req.getReservationDate() != null) {

            ZoneId venueZone = DateTimeUtils.getVenueZoneId();
            LocalTime localTimeAtVenue = req.getReservationDate()
                    .atZoneSameInstant(venueZone)
                    .toLocalTime();

            if (!DateTimeUtils.isWithinOperatingHours(localTimeAtVenue)) {
                throw new RuntimeException(BusinessConstants.ERROR_OUTSIDE_OPERATING_HOURS);
            }
        }

        // 3) Kiểm tra sức chứa bàn
        if (req.getTableId() != null && req.getNumberOfGuests() != null) {
            Tables table = tableRepository.findById(req.getTableId())
                    .orElseThrow(() -> new RuntimeException("Table not found"));
            if (req.getNumberOfGuests() > table.getCapacity()) {
                throw new RuntimeException(String.format(
                        BusinessConstants.ERROR_GUESTS_EXCEED_CAPACITY,
                        req.getNumberOfGuests(), table.getCapacity()));
            }
        }

        // 4) Kiểm tra trùng lịch + trạng thái bàn
        if (req.getTableId() != null) {
            Tables table = tableRepository.findById(req.getTableId())
                    .orElseThrow(() -> new RuntimeException("Table not found"));

            // Lấy mốc kiểm tra: ưu tiên actualArrivalDate, nếu null thì dùng reservationDate
            OffsetDateTime base = req.getReservationDate();

            if (base != null) {
                // Cửa sổ 2 giờ mỗi bên
                OffsetDateTime start = base.minusMinutes(120);
                OffsetDateTime end   = base.plusMinutes(120);

                // Gợi ý: truy vấn xung đột dùng COALESCE(actualArrivalDate, reservationDate)
                List<Reservations> conflicts =
                        reservationRepository.findConflictsByTableIdAndWindow(req.getTableId(), start, end);

                // Loại bỏ các trạng thái không còn chiếm bàn (ví dụ CANCELLED)
                conflicts = conflicts.stream()
                        .filter(r -> !EnumSet.of(ReservationStatus.CANCELLED)
                                .contains(r.getStatus()))
                        .toList();

                if (!conflicts.isEmpty()) {
                    throw new RuntimeException(
                            BusinessConstants.ERROR_TABLE_TIME_CONFLICT
                    );
                }
            }

            // Chỉ cho phép assign nếu bàn đang AVAILABLE hoặc RESERVED
            if (table.getStatus() != TableStatus.AVAILABLE && table.getStatus() != TableStatus.RESERVED) {
                throw new RuntimeException(String.format(
                        BusinessConstants.ERROR_TABLE_NOT_AVAILABLE, table.getStatus()));
            }
        }
    }

    public Long createOrFindReservationOrder(Long reservationId) {
        Reservations reservations = reservationRepository.getReferenceById(reservationId);
        if (reservations == null) {
            return null;
        } else {
            Orders orders = reservations.getOrders().getFirst();
            if (orders.getStatus().equals(OrderStatus.PENDING)) {
                orders.setStatus(OrderStatus.IN_PROGRESS);
                ordersRepository.save(orders);
            }
            return orders.getId();
        }
    }

}
