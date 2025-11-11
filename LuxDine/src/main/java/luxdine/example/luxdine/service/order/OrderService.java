package luxdine.example.luxdine.service.order;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.catalog.repository.ItemsRepository;
import luxdine.example.luxdine.domain.order.dto.request.OrderCreateRequest;
import luxdine.example.luxdine.domain.order.dto.request.OrderFilterRequest;
import luxdine.example.luxdine.domain.order.dto.response.OrderResponse;
import luxdine.example.luxdine.domain.order.entity.OrderItems;
import luxdine.example.luxdine.domain.order.entity.Orders;
import luxdine.example.luxdine.domain.order.enums.OrderItemStatus;
import luxdine.example.luxdine.domain.order.enums.OrderStatus;
import luxdine.example.luxdine.domain.order.repository.OrderItemsRepository;
import luxdine.example.luxdine.domain.order.repository.OrdersRepository;
import luxdine.example.luxdine.domain.reservation.entity.Reservations;
import luxdine.example.luxdine.domain.reservation.enums.ReservationOrigin;
import luxdine.example.luxdine.domain.reservation.enums.ReservationStatus;
import luxdine.example.luxdine.domain.reservation.repository.ReservationRepository;
import luxdine.example.luxdine.domain.table.enums.TableStatus;
import luxdine.example.luxdine.domain.table.repository.TableRepository;
import luxdine.example.luxdine.domain.order.dto.response.GroupedOrderItemResponse;
import luxdine.example.luxdine.domain.catalog.entity.Items;
import luxdine.example.luxdine.domain.table.entity.Tables;
import luxdine.example.luxdine.domain.user.entity.User;
import luxdine.example.luxdine.mapper.OrderMapper;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService {

    OrdersRepository ordersRepository;
    OrderItemsRepository orderItemsRepository;
    ItemsRepository itemsRepository;
    TableRepository tableRepository;
    ReservationRepository reservationsRepository;
    OrderMapper orderMapper;

    public Page<Orders> getOrdersByUserAndStatus(User user, OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        return ordersRepository.findByReservation_UserAndStatus(user, status, pageable);
    }

    public Page<Orders> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        return ordersRepository.findAll(pageable);
    }

    public Orders getOrderById(Long id) {
        return ordersRepository.findById(id).orElse(null);
    }

    public List<OrderResponse> searchOrders(OrderFilterRequest filter) {
        Specification<Orders> spec = buildSpec(filter);
        Sort sort = Sort.by(Sort.Order.desc("createdDate"));
        List<Orders> orders = ordersRepository.findAll(spec, sort);
        List<OrderResponse> response = new ArrayList<>();
        for (Orders order : orders) {
            if (order.getStatus().equals(OrderStatus.IN_PROGRESS)) {
                OrderResponse orderResponse = orderMapper.toOrderResponse(order);
                orderResponse.setItemsCount(countItemsNotByStatus(order, OrderItemStatus.CANCELLED));
                orderResponse.setTableName(order.getReservation().getTable().getTableName());
                response.add(orderResponse);
            }
        }
        return response;
    }

    private int countItemsNotByStatus(Orders order, OrderItemStatus status) {
        if (order == null || status == null) return 0;

        var items = Optional.ofNullable(order.getOrderItems())
                .orElseGet(java.util.List::of);

        long cnt = items.stream()
                .filter(Objects::nonNull)
                .filter(i -> !status.equals(i.getStatus()))
                .count();

        return Math.toIntExact(cnt); // trả về int an toàn
    }


    public String cancelOrder(Long id) {
        Orders order = ordersRepository.findById(id).orElse(null);
        if (order == null) {
            return "Order not found";
        }
        Tables table = tableRepository.findById(order.getReservation().getTable().getId()).orElse(null);
        if (table != null) {
            table.setStatus(TableStatus.AVAILABLE);
            tableRepository.save(table);
        }
        order.setStatus(OrderStatus.CANCELLED);
        List<OrderItems> orderItems = order.getOrderItems();
        for (OrderItems item : orderItems) {
            item.setStatus(OrderItemStatus.valueOf(OrderItemStatus.CANCELLED.name()));
        }
        ordersRepository.save(order);
        return null;
    }


    public String addItem(Long orderId, Long itemId, int quantity) {
        Orders order = ordersRepository.findById(orderId).orElse(null);
        if (order == null) {
            return "Order not found";
        }
        if (!OrderStatus.IN_PROGRESS.equals(order.getStatus())) {
            return "Only IN_PROGRESS orders can be modified";
        }
        Items item = itemsRepository.findById(itemId).orElse(null);
        if (item == null) {
            return "Item not found";
        }
        if (quantity <= 0) {
            return "Quantity must be positive";
        }

        double addedTotal = item.getPrice() * quantity;
        order.setSubTotal(order.getSubTotal() + addedTotal);
        ordersRepository.save(order);
        for (int i = 0; i < quantity; i++) {
            OrderItems orderItem = new OrderItems();
            orderItem.setOrder(order);
            orderItem.setItem(item);
            orderItem.setNameSnapshot(item.getName());
            orderItem.setPriceSnapshot(item.getPrice());
            orderItem.setStatus(OrderItemStatus.QUEUED);
            orderItemsRepository.save(orderItem);
        }
        return null;
    }

    public List<GroupedOrderItemResponse> getOrderGrouped(Long id) {
        // 1) Lấy order
        Orders order = ordersRepository.findById(id).orElse(null);
        if (order == null) {
            return Collections.emptyList();
        }

        List<OrderItems> items = order.getOrderItems();
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        // 2) Nhóm theo itemId (bỏ qua bản ghi không có Item để tránh NPE)
        Map<Long, List<OrderItems>> byItem = new LinkedHashMap<>();
        for (OrderItems oi : items) {
            if (oi == null || oi.getItem() == null || oi.getStatus().equals(OrderItemStatus.CANCELLED)) continue;
            Long key = oi.getItem().getId();
            byItem.computeIfAbsent(key, k -> new ArrayList<>()).add(oi);
        }

        // 3) Duyệt từng nhóm và tạo DTO
        List<GroupedOrderItemResponse> result = new ArrayList<>();

        for (Map.Entry<Long, List<OrderItems>> entry : byItem.entrySet()) {
            List<OrderItems> group = entry.getValue();
            if (group.isEmpty()) continue;

            OrderItems first = group.get(0);

            // 3.1) Tính tổng quantity (fallback: nếu 0 hoặc âm thì coi là 1)
            int totalQty = 0;
            for (OrderItems oi : group) {
                if (!oi.getStatus().equals(OrderItemStatus.CANCELLED)) {
                    totalQty += 1;
                }
            }

            // 3.2) Đếm theo trạng thái
            int qQueued = 0, qPrep = 0, qReady = 0, qServed = 0;

            for (OrderItems oi : group) {
                OrderItemStatus st = oi.getStatus();

                if (OrderItemStatus.QUEUED.equals(st)) {
                    qQueued += 1;
                } else if (OrderItemStatus.PREPARING.equals(st)) {
                    qPrep += 1;
                } else if (OrderItemStatus.READY.equals(st)) {
                    qReady += 1;
                } else if (OrderItemStatus.SERVED.equals(st)) {
                    qServed += 1;
                }
            }

            // 3.4) Giá & thành tiền
            double unitPrice = first.getPriceSnapshot();
            double lineTotal = unitPrice * totalQty;

            // 3.5) Tên hiển thị ưu tiên dùng snapshot
            String name = (first.getNameSnapshot() != null && !first.getNameSnapshot().isEmpty())
                    ? first.getNameSnapshot()
                    : (first.getItem() != null ? first.getItem().getName() : "Unknown");

            // 3.6) Tạo DTO
            GroupedOrderItemResponse dto = GroupedOrderItemResponse.builder()
                    .itemId(first.getItem().getId())
                    .name(name)
                    .unitPrice(unitPrice)
                    .quantity(totalQty)
                    .qQueued(qQueued)
                    .qPrep(qPrep)
                    .qReady(qReady)
                    .qServed(qServed)
                    .lineTotal(lineTotal)
                    .build();

            result.add(dto);
        }

        // 4) Sắp xếp theo tên món
        result.sort(Comparator.comparing(GroupedOrderItemResponse::getName, Comparator.nullsLast(String::compareTo)));

        return result;
    }

    public String cancelOrderItemByItemId(Long orderId, Long itemId) {
        Orders order = ordersRepository.findById(orderId).orElse(null);
        if (order == null) {
            return "Order not found";
        }
        if (!OrderStatus.IN_PROGRESS.equals(order.getStatus())) {
            return "Only IN_PROGRESS orders can be modified";
        }
        for (OrderItems oi : order.getOrderItems()) {
            if (oi.getItem() != null && oi.getItem().getId().equals(itemId)) {
                oi.setStatus(OrderItemStatus.CANCELLED);
                orderItemsRepository.save(oi);
            }
        }
        double newSubtotal = 0.0;
        for (OrderItems oi : order.getOrderItems()) {
            if (oi.getItem() != null && !oi.getStatus().equals(OrderItemStatus.CANCELLED)) {
                newSubtotal += oi.getPriceSnapshot();
            }
        }
        order.setSubTotal(newSubtotal);
        ordersRepository.save(order);

        return null;
    }

    @Transactional
    public String createOrder(OrderCreateRequest req) {
        // 1) Lấy bàn (có thể dùng @Lock(PESSIMISTIC_WRITE) ở repo để chống double-seat)
        Tables table = tableRepository.findById(req.getTableId())
                .orElseThrow(() -> new IllegalArgumentException("Table not found: " + req.getTableId()));

        if (table.getStatus() == TableStatus.OUT_OF_SERVICE) {
            throw new IllegalStateException("Table is out of service");
        }

        // 2) Tạo reservation tức thì cho walk-in (nếu hệ thống bạn chưa có reservationId)
        Reservations reservation = new Reservations();
        reservation.setReservationCode("R-" + UUID.randomUUID().toString().substring(0, 8));
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setReservationDate(OffsetDateTime.now());
        reservation.setActualArrivalTime(OffsetDateTime.now());
        reservation.setTable(table);
        reservation.setNotes(req.getNotes());
        reservation.setOrigin(ReservationOrigin.WALK_IN);
        reservation = reservationsRepository.save(reservation);

        // 3) Khởi tạo Order, gắn với Reservation
        Orders order = new Orders();
        order.setNotes(req.getNotes());
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setReservation(reservation);

        double subtotal = 0.0;
        var lines = new ArrayList<OrderItems>();

        // 4) Tạo OrderItems, gắn 2 chiều, tính subtotal
        if (req.getItems() != null) {
            for (OrderCreateRequest.OrderCreateItem line : req.getItems()) {
                if (line == null) continue;

                var item = itemsRepository.findById(line.getItemId()).orElse(null);
                if (item == null) continue;

                int quantity = Math.max(0, line.getQuantity());
                for (int i = 0; i < quantity; i++) {
                    OrderItems oi = new OrderItems();
                    oi.setItem(item);
                    oi.setNameSnapshot(item.getName());
                    oi.setPriceSnapshot(item.getPrice());
                    oi.setStatus(OrderItemStatus.QUEUED); // enum, KHÔNG dùng .name()

                    // gắn 2 chiều
                    oi.setOrder(order);
                    lines.add(oi);

                    subtotal += oi.getPriceSnapshot();
                }
            }
        }

        order.setOrderItems(lines);

        // 5) Tính tiền cơ bản (tuỳ nghiệp vụ bạn bù thuế/fee/đặt cọc…)
        order.setSubTotal(subtotal);
        order.setDiscountTotal(0.0);
        order.setTax(0.0);
        order.setServiceCharge(0.0);

        double depositApplied = 0.0;
        if (reservation.getDepositAmount() > 0) {
            depositApplied = Math.min(reservation.getDepositAmount(), subtotal);
        }
        order.setDepositApplied(depositApplied);

        double amountDue = subtotal - order.getDiscountTotal() + order.getTax() + order.getServiceCharge() - depositApplied;
        order.setAmountDue(Math.max(0.0, amountDue));

        // 6) Lưu order (cascade sẽ lưu luôn orderItems nếu bạn để cascade = ALL ở Orders.orderItems)
        Orders saved = ordersRepository.save(order);

        // 7) Cập nhật trạng thái bàn
        table.setStatus(TableStatus.OCCUPIED);
        tableRepository.save(table);

        return saved.getId().toString();
    }

    private Specification<Orders> buildSpec(OrderFilterRequest f) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();

            // LEFT JOIN để có thể lọc/ search theo tableName
            Join<Orders, Reservations> resJoin = root.join("reservation", JoinType.LEFT);
            Join<Reservations, Tables> tableJoin = resJoin.join("table", JoinType.LEFT);

            // status
            if (StringUtils.hasText(f.getStatus())) {
                ps.add(cb.equal(root.get("status"), f.getStatus().trim()));
            }

            // date range: [dateFrom, dateTo+1day)
            if (f.getDateFrom() != null) {
                Date from = Date.from(f.getDateFrom().atStartOfDay(ZoneId.systemDefault()).toInstant());
                ps.add(cb.greaterThanOrEqualTo(root.get("createdDate"), from));
            }
            if (f.getDateTo() != null) {
                Date toExclusive = Date.from(f.getDateTo().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
                ps.add(cb.lessThan(root.get("createdDate"), toExclusive));
            }

            // q: id OR notes like OR tableName like
            if (StringUtils.hasText(f.getQ())) {
                String q = f.getQ().trim();
                String qLike = "%" + q.toLowerCase() + "%";

                Predicate byNotes = cb.like(cb.lower(root.get("notes")), qLike);
                Predicate byTable = cb.like(cb.lower(tableJoin.get("tableName")), qLike);
                Predicate or = cb.or(byNotes, byTable);

                if (q.chars().allMatch(Character::isDigit)) {
                    try {
                        Long id = Long.valueOf(q);
                        or = cb.or(or, cb.equal(root.get("id"), id));
                    } catch (NumberFormatException ignored) {}
                }
                ps.add(or);
            }

            return cb.and(ps.toArray(new Predicate[0]));
        };
    }

    public Orders saveOrder(Orders order) {
        return ordersRepository.save(order);
    }

    /**
     * Create an empty order for a reservation
     * This is used when marking a reservation as arrived
     * @param reservation Reservation to create order for
     * @return Created order
     */
    @Transactional
    public Orders createEmptyOrderForReservation(Reservations reservation) {
        log.info("Creating empty order for reservation {}", reservation.getId());
        
        Orders order = new Orders();
        order.setStatus(OrderStatus.IN_PROGRESS);
        order.setReservation(reservation);
        order.setNotes("Order for reservation " + reservation.getReservationCode());
        
        // Initialize financial fields to 0
        order.setSubTotal(0.0);
        order.setDiscountTotal(0.0);
        order.setTax(0.0);
        order.setServiceCharge(0.0);
        order.setDepositApplied(0.0);
        order.setAmountDue(0.0);
        
        // Set empty order items list
        order.setOrderItems(new ArrayList<>());
        
        Orders savedOrder = ordersRepository.save(order);
        log.info("Created empty order {} for reservation {}", savedOrder.getId(), reservation.getId());
        
        return savedOrder;
    }
}
