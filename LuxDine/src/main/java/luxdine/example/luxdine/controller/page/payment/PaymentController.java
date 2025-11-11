package luxdine.example.luxdine.controller.page.payment;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.common.util.DateTimeUtils;
import luxdine.example.luxdine.domain.order.dto.request.OrderPaymentRequest;
import luxdine.example.luxdine.domain.order.dto.response.GroupedOrderItemResponse;
import luxdine.example.luxdine.domain.order.entity.Orders;
import luxdine.example.luxdine.domain.payment.entity.Payments;
import luxdine.example.luxdine.domain.payment.enums.PaymentStatus;
import luxdine.example.luxdine.domain.reservation.dto.request.ReservationCreateRequest;
import luxdine.example.luxdine.domain.reservation.dto.request.ReservationUpdateRequest;
import luxdine.example.luxdine.domain.reservation.dto.response.ReservationResponse;
import luxdine.example.luxdine.domain.reservation.entity.Reservations;
import luxdine.example.luxdine.domain.table.entity.Tables;
import luxdine.example.luxdine.service.order.OrderService;
import luxdine.example.luxdine.service.payment.PaymentService;
import luxdine.example.luxdine.service.reservation.CustomerReservationService;
import luxdine.example.luxdine.service.reservation.ReservationService;
import luxdine.example.luxdine.service.seating.TableService;
import luxdine.example.luxdine.service.user.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/payment")
public class PaymentController {

    PaymentService paymentService;
    OrderService orderService;
    UserService userService;
    ReservationService reservationService;
    TableService tableService;
    CustomerReservationService customerReservationService;

    // ================== STAFF / ORDER ==================

    @PostMapping("/order")
    public String payOrder(@ModelAttribute OrderPaymentRequest req) {
        String redirect = paymentService.orderPay(req); // trả về redirect:/payment/qr?paymentId=... nếu là QR
        return redirect != null ? redirect : "redirect:/payment/failed?error=UNKNOWN";
    }

    // ================== CUSTOMER / RESERVATION ==================

    /**
     * Khởi tạo thanh toán Reservation:
     * - NEW_DEPOSIT: không tạo Reservation trước; PaymentService.startNewReservationDeposit(...)
     * - DEPOSIT_TOP_UP: top-up cọc cho reservation đã có; PaymentService.startReservationTopUp(...)
     */
    @PostMapping("/reservation")
    public String payReservation(
            @ModelAttribute("createReq") ReservationCreateRequest createReq,
            @ModelAttribute("updateReq") ReservationUpdateRequest updateReq,
            @RequestParam(required = false) Long reservationId,
            @RequestParam(required = false) Long amountToPay,
            @RequestParam(defaultValue = "NEW_DEPOSIT") String paymentPurpose,
            Authentication auth
    ) {
        final String purpose = (paymentPurpose == null ? "NEW_DEPOSIT" : paymentPurpose.trim().toUpperCase());

        // --- TOP-UP ---
        if ("DEPOSIT_TOP_UP".equals(purpose) && reservationId != null) {
            long pay = (amountToPay == null) ? 0L : Math.max(0L, amountToPay);
            String redirect = paymentService.startReservationTopUp(reservationId, pay, updateReq, "QR");
            return redirect != null ? redirect
                    : "redirect:/payment/failed?paymentId=-1&error=UNKNOWN";
        }

        // --- NEW_DEPOSIT ---
        String username = auth.getName();
        String redirect = paymentService.startNewReservationDeposit(username, createReq, "QR");
        if (createReq.getDepositAmount() == 0) {
            Payments payment = paymentService.getPaymentFromredirect(redirect);
            paymentService.markPaymentCompleted(payment, null, 2);
            return "redirect:/payment/success?paymentId=" + payment.getId();
        }
        return redirect != null ? redirect
                : "redirect:/payment/failed?paymentId=-1&error=UNKNOWN";
    }

    /**
     * Trang QR hợp nhất cho cả Order/Reservation dựa trên paymentId.
     * Nếu payment đã COMPLETED/FAILED sẽ tự redirect sang success/failed.
     */
    @GetMapping("/qr")
    public String qrPage(@RequestParam Long paymentId, Model model) {
        Payments payment = paymentService.getPaymentById(paymentId);
        if (payment == null) {
            return "redirect:/payment/failed?paymentId=" + paymentId + "&error=PAYMENT_NOT_FOUND";
        }

        if (PaymentStatus.COMPLETED.equals(payment.getStatus())) {
            return "redirect:/payment/success?paymentId=" + paymentId;
        }
        if (PaymentStatus.FAILED.equals(payment.getStatus())) {
            return "redirect:/payment/failed?paymentId=" + paymentId + "&error=FAILED";
        }

        String qrUrl = paymentService.buildQrUrl(payment);
        long expiresAt = payment.getCreatedDate()
                .plus(Duration.ofMinutes(5))
                .toEpochMilli();

        Orders order = payment.getOrder();
        Reservations reservation = (order != null) ? order.getReservation() : null;

        model.addAttribute("payment", payment);
        model.addAttribute("order", order);
        model.addAttribute("reservation", reservation);
        model.addAttribute("qrUrl", qrUrl);
        model.addAttribute("expiresAt", expiresAt);
        model.addAttribute("topic", "/topic/payments/" + payment.getReferenceCode());

        return "customer/payment/payment-qr";
    }

    /**
     * Trang success hợp nhất:
     * - Nếu purpose là NEW_DEPOSIT/DEPOSIT_TOP_UP => render reservation-success
     * - Ngược lại => render order-success (kể cả WALK_IN)
     */
    @GetMapping("/success")
    public String successPage(@RequestParam Long paymentId, Model model) {
        Payments payment = paymentService.getPaymentById(paymentId);
        if (payment == null) {
            return "redirect:/payment/failed?paymentId=" + paymentId + "&error=PAYMENT_NOT_FOUND";
        }
        if (!PaymentStatus.COMPLETED.equals(payment.getStatus())) {
            return "redirect:/payment/failed?paymentId=" + paymentId + "&error=PAYMENT_NOT_COMPLETED";
        }

        Orders order = payment.getOrder();
        if (order == null) {
            // Với NEW_DEPOSIT, PaymentService sẽ bind order vào payment sau webhook.
            return "redirect:/payment/failed?paymentId=" + paymentId + "&error=ORDER_NOT_LINKED";
        }

        order = orderService.getOrderById(order.getId()); // re-fetch đầy đủ
        List<GroupedOrderItemResponse> grouped = orderService.getOrderGrouped(order.getId());
        model.addAttribute("groupedItems", grouped);
        model.addAttribute("payment", payment);
        model.addAttribute("order", order);

        String purpose = paymentService.getContextPurpose(payment).orElse(null);
        if ("NEW_DEPOSIT".equalsIgnoreCase(purpose) || "DEPOSIT_TOP_UP".equalsIgnoreCase(purpose)) {
            if (order.getReservation() != null) {
                Reservations resv = reservationService.getById(order.getReservation().getId());
                resv.setReservationDate(resv.getReservationDate().atZoneSameInstant(DateTimeUtils.getVenueZoneId()).toOffsetDateTime());
                model.addAttribute("reservation", resv);
                model.addAttribute("tableName", resv.getTable() != null ? resv.getTable().getTableName() : null);
            }
            return "customer/payment/reservation-success";
        }

        return "staff/payment/order-success";
    }

    /**
     * Trang failed hợp nhất (Order/Reservation).
     */
    @GetMapping("/failed")
    public String failedPage(@RequestParam(required = false) Long paymentId,
                             @RequestParam(required = false) String error,
                             Model model) {
        Payments p = null;
        if (paymentId != null) {
            p = paymentService.getPaymentById(paymentId);
            if (p != null && PaymentStatus.COMPLETED.equals(p.getStatus())) {
                return "redirect:/payment/success?paymentId=" + paymentId;
            }
            model.addAttribute("payment", p);
        }
        String purpose = paymentService.getContextPurpose(p).orElse(null);
        if (purpose == null || purpose.isEmpty()) {
            assert p != null;
            model.addAttribute("order", p.getOrder());
            return "staff/payment/order-failed";
        }
        model.addAttribute("errorMessage", error);
        model.addAttribute("errorCode", error);
        return "customer/payment/reservation-failed";
    }

    // ================== CUSTOMER CHECKOUT (View prepare) ==================

    /**
     * Chuẩn bị UI thanh toán cho khách (tính số tiền cần trả).
     * Không tạo Reservation; chỉ hiển thị để khách xác nhận.
     */
    @PostMapping("/customer/checkout")
    public String customerCheckout(
            @ModelAttribute("createReq") ReservationCreateRequest createReq,   // NEW_DEPOSIT
            @ModelAttribute("updateReq") ReservationUpdateRequest updateReq,   // TOP-UP
            @RequestParam(required = false) Long reservationId,                // TOP-UP
            @RequestParam(defaultValue = "NEW_DEPOSIT") String paymentPurpose, // NEW_DEPOSIT | DEPOSIT_TOP_UP
            Model model,
            Authentication auth
    ) {
        final String purpose = (paymentPurpose == null ? "NEW_DEPOSIT" : paymentPurpose.trim().toUpperCase());

        // --- TOP-UP UI ---
        if ("DEPOSIT_TOP_UP".equals(purpose) && reservationId != null) {
            String username = auth.getName();
            ReservationResponse existing = customerReservationService.getMyReservationById(username, reservationId);

            long currentPaid = Math.max(0L, Math.round(
                    existing.getDepositAmount() == null ? 0.0 : existing.getDepositAmount()
            ));

            Long targetTableId = (updateReq != null && updateReq.getTableId() != null)
                    ? updateReq.getTableId() : existing.getTableId();

            List<ReservationResponse.OrderCreateItem> targetItems =
                    (updateReq != null && updateReq.getItems() != null)
                            ? updateReq.getItems() : List.of();

            double required = customerReservationService.calculateDepositAmount(targetTableId, targetItems);
            long payAmount = Math.max(0L, Math.round(required) - currentPaid);

            model.addAttribute("reservationId", reservationId);
            model.addAttribute("reservation", updateReq);
            model.addAttribute("payAmount", payAmount);
            model.addAttribute("paymentPurpose", "DEPOSIT_TOP_UP");
            return "customer/payment/customer-checkout";
        }

        // --- NEW_DEPOSIT UI ---
        Long tableId = createReq.getTableId();
        String tableName = tableService.findById(tableId).getTableName();
        List<ReservationResponse.OrderCreateItem> items =
                (createReq.getItems() != null) ? createReq.getItems() : List.of();

        double required = customerReservationService.calculateDepositAmount(tableId, items);
        long payAmount = Math.max(0L, Math.round(required));
        createReq.setDepositAmount(required);
        model.addAttribute("tableName", tableName);
        model.addAttribute("reservation", createReq);
        model.addAttribute("payAmount", payAmount);
        model.addAttribute("paymentPurpose", "NEW_DEPOSIT");
        return "customer/payment/customer-checkout";
    }

    // ================== STAFF CHECKOUT PAGE ==================

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/staff/checkout")
    public String staffCheckout(@RequestParam("orderId") Long id, Model model) {
        model.addAttribute("users", userService.getAllCustomer());
        List<GroupedOrderItemResponse> grouped = orderService.getOrderGrouped(id);
        model.addAttribute("groupedItems", grouped);

        Orders order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        model.addAttribute("customer",
                (order != null && order.getReservation() != null) ? order.getReservation().getUser() : null);

        return "staff/payment/staff-checkout";
    }

    // ================== Legacy alias → gom về /qr ==================

    @GetMapping("/qr-order")
    public String legacyQrOrder(@RequestParam Long orderId, @RequestParam Long paymentId) {
        Payments p = paymentService.getPaymentById(paymentId);
        if (p == null || p.getOrder() == null || !Objects.equals(p.getOrder().getId(), orderId)) {
            return "redirect:/payment/failed?paymentId=" + paymentId + "&error=INVALID_COMBINATION";
        }
        return "redirect:/payment/qr?paymentId=" + paymentId;
    }

    @GetMapping("/qr-reservation")
    public String legacyQrReservation(@RequestParam Long reservationId, @RequestParam Long paymentId) {
        Payments p = paymentService.getPaymentById(paymentId);
        if (p == null || p.getOrder() == null || p.getOrder().getReservation() == null
                || !Objects.equals(p.getOrder().getReservation().getId(), reservationId)) {
            return "redirect:/payment/failed?paymentId=" + paymentId + "&error=INVALID_COMBINATION";
        }
        return "redirect:/payment/qr?paymentId=" + paymentId;
    }
}
