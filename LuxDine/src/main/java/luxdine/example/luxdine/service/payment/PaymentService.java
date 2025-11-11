package luxdine.example.luxdine.service.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import luxdine.example.luxdine.common.util.DateTimeUtils;
import luxdine.example.luxdine.domain.catalog.entity.Items;
import luxdine.example.luxdine.domain.catalog.repository.ItemsRepository;
import luxdine.example.luxdine.domain.order.dto.request.OrderPaymentRequest;
import luxdine.example.luxdine.domain.order.entity.OrderItems;
import luxdine.example.luxdine.domain.order.entity.Orders;
import luxdine.example.luxdine.domain.order.enums.OrderItemStatus;
import luxdine.example.luxdine.domain.order.enums.OrderStatus;
import luxdine.example.luxdine.domain.order.repository.OrderItemsRepository;
import luxdine.example.luxdine.domain.order.repository.OrdersRepository;
import luxdine.example.luxdine.domain.payment.dto.request.ReservationPaymentCtx;
import luxdine.example.luxdine.domain.payment.entity.Payments;
import luxdine.example.luxdine.domain.payment.enums.PaymentMethod;
import luxdine.example.luxdine.domain.payment.enums.PaymentStatus;
import luxdine.example.luxdine.domain.payment.repository.PaymentRepository;
import luxdine.example.luxdine.domain.reservation.dto.request.ReservationCreateRequest;
import luxdine.example.luxdine.domain.reservation.dto.request.ReservationUpdateRequest;
import luxdine.example.luxdine.domain.reservation.dto.response.ReservationResponse;
import luxdine.example.luxdine.domain.reservation.entity.Reservations;
import luxdine.example.luxdine.domain.reservation.enums.ReservationOrigin;
import luxdine.example.luxdine.domain.reservation.enums.ReservationStatus;
import luxdine.example.luxdine.domain.reservation.repository.ReservationRepository;
import luxdine.example.luxdine.domain.table.entity.Tables;
import luxdine.example.luxdine.domain.table.enums.TableStatus;
import luxdine.example.luxdine.domain.table.repository.TableRepository;
import luxdine.example.luxdine.domain.user.entity.User;
import luxdine.example.luxdine.domain.user.repository.UserRepository;
import luxdine.example.luxdine.service.reservation.ReservationService;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {

    // ============ Dependencies ============
    ObjectMapper objectMapper;
    PaymentRepository paymentRepository;
    OrdersRepository orderRepository;
    ItemsRepository itemsRepository;
    ReservationRepository reservationRepository;
    TableRepository tableRepository;
    UserRepository userRepository;
    ReservationService reservationService;   // dùng validate rules, helper convert, generate code
    SimpMessageSendingOperations messaging;

    // ============ Config ============
    String sepayAcc  = "0000139713489";
    String sepayBank = "MBBank";


    // ============ Public APIs ============

    /**
     * Staff checkout order tại quầy (CASH/CARD/QR). Không đổi hành vi cũ.
     */
    @Transactional
    public String orderPay(OrderPaymentRequest req) {
        Orders order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + req.getOrderId()));

        // Gắn user cho reservation nếu có userInfo
        Reservations res = order.getReservation();
        if (res != null && res.getUser() == null && req.getUserInfo() != null && !req.getUserInfo().isBlank()) {
            User user = Optional.ofNullable(userRepository.findByPhoneNumber(req.getUserInfo()).orElse(null))
                    .orElseGet(() -> userRepository.findByEmail(req.getUserInfo()).orElse(null));
            if (user != null) res.setUser(user);
        }

        double discounted = Math.max(0, Math.min(req.getDiscounted(), order.getSubTotal()));
        order.setDiscountTotal(discounted);

        double total = order.getSubTotal()
                + order.getServiceCharge()
                + order.getTax()
                - order.getDepositApplied()
                - order.getDiscountTotal();

        double amountDue = Math.max(0, total);
        order.setAmountDue(amountDue);

        PaymentMethod method = parsePaymentMethod(req.getPaymentMethod());
        if (method == null) {
            orderRepository.save(order);
            return "redirect:/payment/failed?error=UNSUPPORTED_METHOD";
        }

        if (amountDue <= 0.0) {
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);
            Payments p = Payments.builder()
                    .order(order).method(PaymentMethod.NULL).amount(0.0)
                    .status(PaymentStatus.COMPLETED).build();
            paymentRepository.save(p);
            return "redirect:/payment/success?paymentId=" + p.getId();
        }

        return switch (method) {
            case CASH -> payByCash(order, amountDue);
            case CARD, QR -> startQrForOrder(order, amountDue); // hiển thị QR với paymentId
            default -> {
                orderRepository.save(order);
                yield "redirect:/payment/failed?error=UNSUPPORTED_METHOD";
            }
        };
    }

    private String payByCash(Orders order, double amountDue) {
        Payments p = new Payments();
        p.setOrder(order);
        p.setMethod(PaymentMethod.CASH);
        p.setAmount(amountDue);
        p.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(p);

        completeOrderAndTable(order, amountDue);
        push(p, "COMPLETED");
        return "redirect:/payment/success?paymentId=" + p.getId();
    }

    /**
     * Khởi tạo thanh toán cọc cho việc TẠO MỚI reservation (chưa tạo reservation trước).
     * Luôn nhét đầy đủ createReq, username, amount vào contextJson.
     * Chỉ hỗ trợ QR (CARD có thể mở rộng sau).
     */
    @Transactional
    public String startNewReservationDeposit(String username,
                                             ReservationCreateRequest createReq,
                                             String paymentMethod) {
        // Validate input (không tạo reservation, chỉ validate rule)
        reservationService.validateReservationRequest(createReq);

        // Tính tiền cọc yêu cầu server-side
        long amount = computeRequiredDepositRounded(createReq);

        PaymentMethod m = parsePaymentMethod(paymentMethod);
        if (m == null || !isReservationMethodSupported(m)) {
            return "redirect:/payment/reservation-failed?error=UNSUPPORTED_METHOD";
        }

        ReservationPaymentCtx ctx = new ReservationPaymentCtx(
                "NEW_DEPOSIT", username, createReq, null, null, amount
        );
        System.out.println("111");
        System.out.println(createReq.getReservationDate());
        System.out.println(ctx.getCreate().getReservationDate());

        Payments p = Payments.builder()
                .order(null) // NEW_DEPOSIT: chưa có reservation/order
                .method(PaymentMethod.QR)
                .amount((double) amount)
                .status(PaymentStatus.PENDING)
                .referenceCode(genRef(2))
                .contextJson(toJson(ctx))
                .build();

        paymentRepository.save(p);
        return "redirect:/payment/qr?paymentId=" + p.getId();
    }

    /**
     * Khởi tạo thanh toán TOP-UP cho 1 reservation đã có; update sẽ áp dụng khi webhook xác nhận.
     * Chỉ hỗ trợ QR (CARD có thể mở rộng sau).
     */
    @Transactional
    public String startReservationTopUp(Long reservationId,
                                        long amountToPay,
                                        ReservationUpdateRequest updateReq,
                                        String paymentMethod) {
        if (reservationId == null || amountToPay <= 0L) {
            return redirectFail(reservationId, "INVALID_INPUT");
        }

        Reservations r = reservationRepository.findById(reservationId).orElse(null);
        if (r == null) return redirectFail(reservationId, "RESERVATION_NOT_FOUND");

        Orders order = orderRepository.findFirstByReservation(r).orElse(null);
        if (order == null) return redirectFail(reservationId, "ORDER_NOT_FOUND");

        PaymentMethod m = parsePaymentMethod(paymentMethod);
        if (m == null || !isReservationMethodSupported(m)) {
            return redirectFail(reservationId, "UNSUPPORTED_METHOD");
        }

        // Chốt context update (đảm bảo id khớp)
        updateReq.setId(reservationId);


        ReservationPaymentCtx ctx = new ReservationPaymentCtx(
                "DEPOSIT_TOP_UP", null, null, reservationId, deepCopy(updateReq), amountToPay
        );

        Payments p = Payments.builder()
                .order(order) // TOP_UP: đã có order
                .method(PaymentMethod.QR)
                .amount((double) amountToPay)
                .status(PaymentStatus.PENDING)
                .referenceCode(genRef(2))
                .contextJson(toJson(ctx))
                .build();

        paymentRepository.save(p);
        return "redirect:/payment/qr?paymentId=" + p.getId();
    }

    public Payments getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId).orElse(null);
    }

    public String buildQrUrl(Payments payment) {
        String des  = URLEncoder.encode(payment.getReferenceCode(), StandardCharsets.UTF_8);
        String bank = URLEncoder.encode(sepayBank, StandardCharsets.UTF_8);
        long amount = Math.round(payment.getAmount());
        return String.format("https://qr.sepay.vn/img?acc=%s&bank=%s&amount=%d&des=%s", sepayAcc, bank, amount, des);
    }

    public List<Payments> findByStatusAndMethod(PaymentStatus status, PaymentMethod method) {
        return paymentRepository.findByStatusAndMethod(status, method);
    }

    public List<Payments> findByMethodAndStatusAndCreatedDateBefore(PaymentMethod method,
                                                                    PaymentStatus status,
                                                                    Instant before) {
        return paymentRepository.findByMethodAndStatusAndCreatedDateBefore(method, status, before);
    }

    // ============ Webhook hooks ============

    /**
     * Gọi khi webhook SePay xác nhận chuyển tiền vào.
     * type=1: đơn tại quầy; type=2: luồng reservation (NEW_DEPOSIT | DEPOSIT_TOP_UP)
     */
    @Transactional
    public void markPaymentCompleted(Payments p, String transactionId, int type) {
        if (PaymentStatus.COMPLETED.equals(p.getStatus())) return;

        p.setStatus(PaymentStatus.COMPLETED);
        p.setTransactionId(transactionId);
        paymentRepository.save(p);

        if (type == 1) {
            // Order tại quầy
            completeOrderAndTable(p.getOrder(), p.getAmount());
            push(p, "COMPLETED");
            return;
        }

        // type == 2: reservation flow
        ReservationPaymentCtx ctx = fromJson(p.getContextJson());
        if (ctx == null) {
            // Không có context → bỏ qua (an toàn)
            push(p, "COMPLETED");
            return;
        }

        switch (safeUpper(ctx.getPurpose())) {
            case "NEW_DEPOSIT" -> {
                // Tạo reservation + order, set deposit bằng số đã thanh toán
                var created = createReservationAfterPaid(ctx.getUsername(), ctx.getCreate(), p.getAmount());
                // Liên kết payment với order vừa tạo
                if (created != null && created.order != null) {
                    p.setOrder(created.order);
                    paymentRepository.save(p);
                }
                push(p, "COMPLETED");
            }
            case "DEPOSIT_TOP_UP" -> {
                System.out.println("999777");
                // Cộng cọc vào reservation hiện có + áp dụng update
                Reservations r = reservationRepository.findById(ctx.getReservationId()).orElse(null);
                if (r != null) {
                    // Cộng cọc & về PENDING (đã có cọc)
                    addDepositAndPend(r, p.getAmount());

                    // Áp dụng update nếu có
                    if (ctx.getUpdate() != null) {
                        try {
                            // ReservationService.updateReservation đã xử lý đổi bàn/giờ/items/notes…
                            reservationService.updateReservation(ctx.getUpdate());
                        } catch (Exception ignore) { /* có thể log chi tiết nếu cần */ }
                    }
                }
                push(p, "COMPLETED");
            }
            default -> push(p, "COMPLETED");
        }

    }

    @Transactional
    public void markPaymentFailed(Payments p, String reason) {
        if (!PaymentStatus.PENDING.equals(p.getStatus())) return;
        p.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(p);
        push(p, (reason == null ? "FAILED" : reason));
    }

    @Transactional
    public Payments findPendingQrByRefAndAmountForUpdate(String rawRefToken, long transferAmountVnd) {
        if (rawRefToken == null) return null;

        // Chuẩn hoá giống phía webhook: bỏ ký tự không [A-Za-z0-9], upper-case
        String norm = rawRefToken.replaceAll("[^A-Za-z0-9]", "")
                .toUpperCase(Locale.ROOT);

        return paymentRepository.findByRefNormAndAmountForUpdate(
                        norm,
                        transferAmountVnd,
                        PaymentMethod.QR,
                        PaymentStatus.PENDING
                )
                .orElse(null);
    }

    // ============ Internals ============

    private String startQrForOrder(Orders order, double amountDue) {
        Payments p = Payments.builder()
                .order(order)
                .method(PaymentMethod.QR)
                .amount(amountDue)
                .status(PaymentStatus.PENDING)
                .referenceCode(genRef(1))
                .build();
        paymentRepository.save(p);
        // Trang QR chung theo paymentId
        return "redirect:/payment/qr?paymentId=" + p.getId();
    }

    private String genRef(int type) {
        String raw = UUID.randomUUID().toString().replace("-", "");
        String prefix = (type == 2) ? "PAY2" : "PAY1";
        return prefix + raw;
    }

    private PaymentMethod parsePaymentMethod(String raw) {
        if (raw == null) return null;
        try {
            return PaymentMethod.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean isReservationMethodSupported(PaymentMethod m) {
        // Hiện tại dùng QR; CARD có thể mở sau (redirect cổng thẻ)
        return m == PaymentMethod.QR || m == PaymentMethod.CARD;
    }

    private String redirectFail(Long reservationId, String code) {
        return "redirect:/payment/reservation-failed?reservationId=" + (reservationId == null ? -1 : reservationId) + "&error=" + code;
    }

    private void push(Payments p, String reason) {
        // Chủ đề WS: fallback sang paymentId nếu referenceCode null
        final String ref = (p.getReferenceCode() != null) ? p.getReferenceCode() : ("PID-" + p.getId());
        final String topic = "/topic/payments/" + ref;

        Map<String, Object> payload = new java.util.HashMap<>();
        // Các trường luôn có hoặc chuyển về chuỗi an toàn
        payload.put("paymentId", p.getId());
        payload.put("status", p.getStatus() != null ? p.getStatus().name() : "UNKNOWN");
        payload.put("method", p.getMethod() != null ? p.getMethod().name() : "UNKNOWN");

        // Chỉ put khi không null để tránh NPE
        if (p.getOrder() != null) {
            payload.put("orderId", p.getOrder().getId());
            if (p.getOrder().getReservation() != null) {
                payload.put("reservationId", p.getOrder().getReservation().getId());
            }
        }
        if (reason != null && !reason.isBlank()) {
            payload.put("reason", reason);
        }

        messaging.convertAndSend(topic, payload);
    }

    // ----- JSON helpers -----
    private String toJson(Object o) {
        try { return objectMapper.writeValueAsString(o); }
        catch (Exception e) { return null; }
    }
    private ReservationPaymentCtx fromJson(String s) {
        try { return (s == null || s.isBlank()) ? null : objectMapper.readValue(s, ReservationPaymentCtx.class); }
        catch (Exception e) { return null; }
    }
    private String safeUpper(String s) { return s == null ? "" : s.trim().toUpperCase(Locale.ROOT); }

    // ----- Order/Reservation completion helpers -----

    private void completeOrderAndTable(Orders o, double amountPaid) {
        if (o == null) return;
        o.setAmountDue(amountPaid);
        o.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(o);

        Reservations r = o.getReservation();
        if (r != null && r.getTable() != null) {
            Tables table = tableRepository.findById(r.getTable().getId()).orElse(null);
            if (table != null) {
                table.setStatus(TableStatus.AVAILABLE);
                tableRepository.save(table);
            }
        }
    }

    private void addDepositAndPend(Reservations reservation, Double paidAmount) {
        reservation.setStatus(ReservationStatus.PENDING);
        if (paidAmount != null) {
            double current = reservation.getDepositAmount();
            reservation.setDepositAmount(current + paidAmount);
        }
        reservationRepository.save(reservation);
    }

    // ----- NEW_DEPOSIT creation flow -----

    /**
     * Tạo reservation + order + orderItems sau khi đã nhận tiền (NEW_DEPOSIT).
     * Trả về pair (reservation, order).
     */
    private Created createReservationAfterPaid(String username,
                                               ReservationCreateRequest req,
                                               double paidAmount) {
        if (username == null || req == null) return null;
        // Validate lại lần cuối trước khi tạo
        reservationService.validateReservationRequest(req);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // Build reservation
        Reservations reservation = Reservations.builder()
                .reservationCode(reservationService.generateReservationCode())
                .status(ReservationStatus.PENDING) // đã có cọc
                .origin(ReservationOrigin.ONLINE)
                .reservationDate(req.getReservationDate())
                .reservationDepartureTime(req.getReservationDepartureTime())
                .numberOfGuests(req.getNumberOfGuests() == null ? 1 : req.getNumberOfGuests())
                .depositAmount(paidAmount) // đã thu
                .notes(req.getNotes())
                .user(user)
                .build();

        // Assign table nếu có & hợp lệ
        if (req.getTableId() != null) {
            Tables table = tableRepository.findById(req.getTableId())
                    .orElseThrow(() -> new RuntimeException("Table not found: " + req.getTableId()));
            reservation.setTable(table);
        }

        // Save reservation trước để có id (FK)
        Reservations savedReservation = reservationRepository.save(reservation);

        // Tạo order & preorder items nếu có
        Orders order = new Orders();
        order.setReservation(savedReservation);
        order.setStatus(OrderStatus.PENDING);

        // Gom quantity by itemId
        List<ReservationResponse.OrderCreateItem> reqItems =
                (req.getItems() == null) ? List.of() : req.getItems();
        Map<Long, Integer> itemQty = new LinkedHashMap<>();
        for (ReservationResponse.OrderCreateItem it : reqItems) {
            if (it == null || it.getItemId() == null || it.getQuantity() == null || it.getQuantity() <= 0) continue;
            itemQty.merge(it.getItemId(), it.getQuantity(), Integer::sum);
        }

        List<OrderItems> orderItems = new ArrayList<>();
        double subTotal = 0d;

        for (Map.Entry<Long, Integer> e : itemQty.entrySet()) {
            Long itemId = e.getKey();
            int qty = e.getValue();

            Items item = itemsRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Item not found: " + itemId));

            subTotal += (item.getPrice() * qty);

            for (int i = 0; i < qty; i++) {
                OrderItems oi = new OrderItems();
                oi.setOrder(order);
                oi.setItem(item);
                oi.setNameSnapshot(item.getName());
                oi.setPriceSnapshot(item.getPrice());
                oi.setStatus(OrderItemStatus.PENDING);
                orderItems.add(oi);
            }
        }

        order.setOrderItems(orderItems);
        order.setSubTotal(subTotal);
        order.setDiscountTotal(0d);
        order.setTax(0d);
        order.setServiceCharge(0d);
        order.setDepositApplied(savedReservation.getDepositAmount());
        order.setAmountDue(Math.max(0d, subTotal));

        orderRepository.save(order); // cascade items nếu cấu hình; nếu không thì dùng orderItemsRepository.saveAll(orderItems)

        return new Created(savedReservation, order);
    }

    @AllArgsConstructor
    static class Created {
        Reservations reservation;
        Orders order;
    }

    // ----- Deposit computation -----

    /**
     * Tính cọc yêu cầu: max(table.depositAmount, 10% tổng giá trị pre-order)
     * Round về long (VND).
     */
    private long computeRequiredDepositRounded(ReservationCreateRequest req) {
        double deposit = 0d;

        if (req.getTableId() != null) {
            Tables table = tableRepository.findById(req.getTableId())
                    .orElseThrow(() -> new RuntimeException("Table not found: " + req.getTableId()));
            deposit = Math.max(deposit, table.getDepositAmount());
        }

        double preorderTotal = 0d;
        if (req.getItems() != null) {
            for (ReservationResponse.OrderCreateItem it : req.getItems()) {
                if (it == null || it.getItemId() == null || it.getQuantity() == null || it.getQuantity() <= 0) continue;
                Items item = itemsRepository.findById(it.getItemId())
                        .orElseThrow(() -> new RuntimeException("Item not found: " + it.getItemId()));
                preorderTotal += item.getPrice() * it.getQuantity();
            }
            deposit = Math.max(deposit, preorderTotal * 0.10d);
        }

        return Math.max(0L, Math.round(deposit));
    }

    public java.util.Optional<String> getContextPurpose(Payments payment) {
        if (payment == null) return java.util.Optional.empty();
        return getContextPurpose(payment.getContextJson());
    }

    /** Lấy purpose từ chuỗi contextJson. */
    public java.util.Optional<String> getContextPurpose(String contextJson) {
        try {
            if (contextJson == null || contextJson.isBlank()) {
                return java.util.Optional.empty();
            }

            JsonNode root = objectMapper.readTree(contextJson);

            // 1) Ưu tiên trường purpose nếu có
            String purpose = readText(root.get("purpose"));

            // 2) Nếu không có purpose -> suy luận theo cấu trúc
            if (purpose == null) {
                purpose = inferPurpose(root);
            }

            if (purpose == null || purpose.isBlank()) {
                return java.util.Optional.empty();
            }

            String normalized = purpose.trim().toUpperCase(java.util.Locale.ROOT);

            // (tuỳ chọn) Nếu muốn chỉ chấp nhận các giá trị đã biết, dùng dòng dưới:
            // return KNOWN_PURPOSES.contains(normalized) ? Optional.of(normalized) : Optional.empty();

            // Còn nếu muốn vẫn trả về cả các giá trị lạ để controller tự quyết:
            return java.util.Optional.of(normalized);

        } catch (Exception ex) {
            // Nếu lớp có @Slf4j
            // log.warn("Cannot parse contextJson purpose: {}", ex.toString());
            return java.util.Optional.empty();
        }
    }

    /** Đọc text an toàn từ JsonNode (null-safe). */
    private static String readText(JsonNode node) {
        return (node != null && !node.isNull()) ? node.asText(null) : null;
    }

    /** Suy luận purpose khi không có trường purpose rõ ràng. */
    private String inferPurpose(JsonNode root) {
        if (root == null || root.isNull()) return null;

        // Gợi ý heuristic:
        // - Có orderId  -> ORDER (thanh toán hoá đơn tại chỗ)
        // - Có update + (reservationId hoặc update.id) -> DEPOSIT_TOP_UP
        // - Có create hoặc reservation -> NEW_DEPOSIT (đặt cọc tạo mới)
        if (root.hasNonNull("orderId")) {
            return "ORDER";
        }
        if (root.hasNonNull("update") &&
                (root.hasNonNull("reservationId") || root.path("update").hasNonNull("id"))) {
            return "DEPOSIT_TOP_UP";
        }
        if (root.hasNonNull("create") || root.hasNonNull("reservation")) {
            return "NEW_DEPOSIT";
        }
        return null;
    }

    public Payments getPaymentFromredirect(String redirect) {
        if (redirect == null || redirect.isBlank()) {
            throw new IllegalArgumentException("redirect rỗng/blank");
        }

        // Bỏ tiền tố "redirect:" nếu có
        String url = redirect.startsWith("redirect:")
                ? redirect.substring("redirect:".length())
                : redirect;

        String idStr = null;

        // 1) Ưu tiên lấy từ query param ?paymentId=...
        try {
            UriComponents uc = UriComponentsBuilder.fromUriString(url).build();
            idStr = uc.getQueryParams().getFirst("paymentId");
        } catch (Exception ignore) {
            // Nếu URL không parse được bằng UriComponentsBuilder, fallback regex phía dưới
        }

        // 2) Fallback: path variable /payment/qr/{id}
        if (idStr == null || idStr.isBlank()) {
            Matcher m = Pattern.compile("/payment/qr/(\\d+)(?:/)?(?:\\?.*)?$").matcher(url);
            if (m.find()) {
                idStr = m.group(1);
            }
        }

        if (idStr == null || idStr.isBlank()) {
            throw new IllegalArgumentException("Không tìm thấy paymentId trong redirect: " + redirect);
        }

        long paymentId;
        try {
            paymentId = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("paymentId không hợp lệ: " + idStr, e);
        }

        // Truy vấn entity
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy payment: " + paymentId));
    }

    // ----- Utils -----

    private <T> T deepCopy(T obj) {
        try {
            if (obj == null) return null;
            String s = objectMapper.writeValueAsString(obj);
            @SuppressWarnings("unchecked")
            T out = (T) objectMapper.readValue(s, obj.getClass());
            return out;
        } catch (Exception e) {
            return obj; // fallback shallow
        }
    }
}
