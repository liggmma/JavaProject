package luxdine.example.luxdine.service.reservation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.common.constants.BusinessConstants;
import luxdine.example.luxdine.domain.catalog.entity.Items;
import luxdine.example.luxdine.domain.catalog.repository.ItemsRepository;
import luxdine.example.luxdine.domain.order.entity.OrderItems;
import luxdine.example.luxdine.domain.order.entity.Orders;
import luxdine.example.luxdine.domain.order.enums.OrderItemStatus;
import luxdine.example.luxdine.domain.order.enums.OrderStatus;
import luxdine.example.luxdine.domain.order.repository.OrderItemsRepository;
import luxdine.example.luxdine.domain.order.repository.OrdersRepository;
import luxdine.example.luxdine.domain.reservation.dto.request.ReservationCreateRequest;
import luxdine.example.luxdine.domain.reservation.dto.response.ReservationResponse;
import luxdine.example.luxdine.domain.reservation.entity.Reservations;
import luxdine.example.luxdine.domain.table.entity.Tables;
import luxdine.example.luxdine.domain.user.entity.User;
import luxdine.example.luxdine.domain.reservation.enums.ReservationOrigin;
import luxdine.example.luxdine.domain.reservation.enums.ReservationStatus;
import luxdine.example.luxdine.domain.table.enums.TableStatus;
import luxdine.example.luxdine.domain.reservation.repository.ReservationRepository;
import luxdine.example.luxdine.domain.table.repository.TableRepository;
import luxdine.example.luxdine.domain.user.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.DESC;

/**
 * Lớp Service quản lý logic nghiệp vụ cho các thao tác đặt bàn của khách hàng (Customer).
 * <p>
 * Lớp này chịu trách nhiệm xử lý việc tạo, xem, cập nhật, và hủy đặt bàn,
 * đồng thời đảm bảo rằng khách hàng chỉ có thể thao tác trên các đặt bàn của chính họ.
 * Mọi thao tác thay đổi dữ liệu đều được quản lý trong một giao dịch (@Transactional).
 * <p>Tác giả: Lê Ngọc Minh Kiên
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerReservationService {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final TableRepository tableRepository;
    private final ItemsRepository itemRepository;
    private final OrdersRepository orderRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final ReservationService reservationService; // Service phụ trợ cho các logic chung


    public double calculateDepositAmount(Long tableId, List<ReservationResponse.OrderCreateItem> items) {
        Tables table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found: " + tableId));
        double depositAmount = table.getDepositAmount();

        // Tăng cọc nếu có món pre-order giá trị cao
        if (items != null && !items.isEmpty()) {
            double totalPreorderValue = 0.0;
            for (ReservationResponse.OrderCreateItem it : items) {
                if (it == null || it.getItemId() == null || it.getQuantity() == null || it.getQuantity() <= 0) {
                    continue;
                }
                Items item = itemRepository.findById(it.getItemId())
                        .orElseThrow(() -> new RuntimeException("Item not found: " + it.getItemId()));
                totalPreorderValue += item.getPrice() * it.getQuantity();
            }
            depositAmount = Math.max(depositAmount, totalPreorderValue * 10 / 100); // 10% giá trị pre-order
        }

        return depositAmount;
    }

    /**
     * Lấy thông tin chi tiết một đặt bàn theo ID, đồng thời xác thực quyền sở hữu.
     *
     * @param username Tên đăng nhập của người dùng yêu cầu.
     * @param id       ID của đặt bàn cần xem.
     * @return DTO chứa thông tin chi tiết của đặt bàn.
     * @throws RuntimeException nếu đặt bàn không tồn tại hoặc người dùng không có quyền xem.
     */
    @Transactional(readOnly = true)
    public ReservationResponse getMyReservationById(String username, Long id) {
        Reservations reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + id));

        // Logic quan trọng: Kiểm tra xem người dùng có phải là chủ của đặt bàn này không
        if (reservation.getUser() == null || !reservation.getUser().getUsername().equals(username)) {
            throw new RuntimeException("You are not allowed to view this reservation");
        }
        return reservationService.convertToReservationResponse(reservation);
    }

    /**
     * Cập nhật thông tin của một đặt bàn đã có, đồng thời xác thực quyền sở hữu.
     *
     * @param username Tên đăng nhập của người dùng yêu cầu cập nhật.
     * @param id       ID của đặt bàn cần cập nhật.
     * @param request  DTO chứa thông tin mới cần cập nhật.
     * @return DTO chứa thông tin của đặt bàn sau khi đã được cập nhật.
     * @throws RuntimeException nếu đặt bàn không tồn tại hoặc người dùng không có quyền cập nhật.
     */
    @Transactional
    public ReservationResponse updateMyReservation(String username, Long id, ReservationCreateRequest request) {
        Reservations reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + id));

        // Quyền sở hữu
        if (reservation.getUser() == null || !reservation.getUser().getUsername().equals(username)) {
            throw new RuntimeException("You are not allowed to update this reservation");
        }

        // Chỉ cho phép cập nhật khi chưa CONFIRMED/CANCELLED
        ReservationStatus currentStatus = reservation.getStatus();
        if (ReservationStatus.CONFIRMED.equals(currentStatus) ||
                ReservationStatus.CANCELLED.equals(currentStatus)) {
            throw new RuntimeException("Cannot update reservation with status: " + currentStatus + ". Only PENDING reservations can be updated.");
        }

        // Validate input (giờ mở cửa, logic khác)
        reservationService.validateReservationRequest(request);

        // Cập nhật các trường cơ bản
        if (request.getReservationDate() != null) {
            reservation.setReservationDate(request.getReservationDate());
        }
        if (request.getNumberOfGuests() != null) {
            reservation.setNumberOfGuests(request.getNumberOfGuests());
        }
        if (request.getNotes() != null) {
            reservation.setNotes(request.getNotes());
        }
        if (request.getTableId() != null) {
            Tables table = tableRepository.findById(request.getTableId())
                    .orElseThrow(() -> new RuntimeException("Table not found: " + request.getTableId()));
            reservation.setTable(table);
        }

        // ====== Cập nhật OrderItems trên order đầu tiên ======
        if (request.getItems() != null) {
            // Lấy order đầu tiên theo createdDate (fallback theo id nếu cần)
            Orders targetOrder = (reservation.getOrders() == null || reservation.getOrders().isEmpty())
                    ? null
                    : reservation.getOrders().stream()
                    .sorted(Comparator.comparing(Orders::getCreatedDate)) // hoặc Comparator.comparing(Orders::getId)
                    .findFirst()
                    .orElse(null);

            if (targetOrder != null) {
                // Xoá sạch items cũ (tránh orphan do không bật orphanRemoval)
                orderItemsRepository.deleteByOrder(targetOrder);
                targetOrder.setOrderItems(new ArrayList<>()); // reset collection trong context

                List<OrderItems> rebuilt = new ArrayList<>();
                for (ReservationResponse.OrderCreateItem itReq : request.getItems()) {
                    if (itReq == null || itReq.getItemId() == null || itReq.getQuantity() == null) continue;
                    if (itReq.getQuantity() < 1) continue;

                    Items item = itemRepository.findById(itReq.getItemId())
                            .orElseThrow(() -> new RuntimeException("Item not found: " + itReq.getItemId()));

                    // Tạo N bản ghi theo quantity (vì OrderItems không có cột quantity)
                    for (int i = 0; i < itReq.getQuantity(); i++) {
                        OrderItems oi = OrderItems.builder()
                                .order(targetOrder)
                                .item(item)
                                .bundle(null) // bỏ bundle trong preorder
                                .nameSnapshot(item.getName())
                                .priceSnapshot(item.getPrice())
                                .status(OrderItemStatus.PENDING) // trạng thái khởi tạo phù hợp hệ thống của bạn
                                .build();
                        rebuilt.add(oi);
                    }
                }

                // Gán danh sách mới
                targetOrder.getOrderItems().addAll(rebuilt);

                // (tuỳ chọn) cập nhật lại subTotal của order nếu bạn muốn:
                // double newSubTotal = rebuilt.stream().mapToDouble(OrderItems::getPriceSnapshot).sum();
                // targetOrder.setSubTotal(newSubTotal);
                // targetOrder.setAmountDue(calcAmountDue(targetOrder)); // nếu có service tính toán
            }
            // Nếu không có order nào, có thể bỏ qua hoặc tạo order mới (tùy rule hệ thống)
        }

        Reservations saved = reservationRepository.save(reservation);
        log.info("User '{}' updated reservation with code '{}'", username, saved.getReservationCode());
        return reservationService.convertToReservationResponse(saved);
    }

    /**
     * Hủy một đặt bàn đang ở trạng thái PENDING.
     *
     * @param username      Tên đăng nhập của người dùng yêu cầu hủy.
     * @param reservationId ID của đặt bàn cần hủy.
     * @return DTO của đặt bàn sau khi đã được hủy.
     * @throws RuntimeException nếu đặt bàn không tồn tại hoặc người dùng không có quyền hủy.
     */
    @Transactional
    public ReservationResponse cancelMyReservation(String username, Long reservationId) {
        Reservations reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + reservationId));

        // Xác thực quyền sở hữu
        if (reservation.getUser() == null || !reservation.getUser().getUsername().equals(username)) {
            throw new RuntimeException("You are not allowed to cancel this reservation");
        }

        // Chỉ cho phép hủy khi trạng thái là PENDING
        if (ReservationStatus.CANCELLED.equals(reservation.getStatus()) || ReservationStatus.ARRIVED.equals(reservation.getStatus())) {
            throw new IllegalStateException("Only reservations with PENDING or CONFIRMED status can be cancelled.");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservations saved = reservationRepository.save(reservation);
        log.info("User '{}' cancelled reservation with code '{}'", username, saved.getReservationCode());
        return reservationService.convertToReservationResponse(saved);
    }

    /**
     * Lấy danh sách tất cả các đặt bàn của người dùng hiện tại.
     *
     * @param username Tên đăng nhập của người dùng.
     * @return Một danh sách các DTO ReservationResponse.
     */
    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyReservations(String username) {
        Sort sort = Sort.by(DESC, "reservationDate");
        return reservationRepository
                .findByUserUsernameAndOrigin(username, ReservationOrigin.ONLINE, sort)
                .stream()
                .map(reservationService::convertToReservationResponse)
                .collect(Collectors.toList());
    }

    public List<Reservations> findConflictsByTableIdAndWindow(Long tableId, OffsetDateTime start, OffsetDateTime end) {
        return reservationRepository.findConflictsByTableIdAndWindow(tableId, start, end);
    }

    public List<Reservations> findConflictsByTableIdAndTimeRange(Long tableId, OffsetDateTime requestStart, OffsetDateTime requestEnd) {
        return reservationRepository.findConflictsByTableIdAndTimeRange(tableId, requestStart, requestEnd);
    }

}