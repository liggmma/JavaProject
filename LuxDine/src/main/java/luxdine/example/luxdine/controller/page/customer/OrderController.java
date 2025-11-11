package luxdine.example.luxdine.controller.page.customer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import luxdine.example.luxdine.domain.order.dto.response.GroupedOrderItemResponse;
import luxdine.example.luxdine.domain.feedback.entity.FeedBacks;
import luxdine.example.luxdine.domain.order.entity.Orders;
import luxdine.example.luxdine.domain.order.enums.OrderStatus;
import luxdine.example.luxdine.domain.user.entity.User;
import luxdine.example.luxdine.service.order.OrderService;
import luxdine.example.luxdine.service.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderController {

    final OrderService orderService;
    final UserService userService;

    // Redirect /orders -> /orders/history
    @GetMapping("/orders")
    public String redirectOrdersToHistory() {
        return "redirect:/orders/history";
    }

    @GetMapping("/orders/history")
    public String viewOrderHistory(Model model,
                                   Principal principal,
                                   @RequestParam(name = "page", defaultValue = "0") int page,
                                   @RequestParam(name = "size", defaultValue = "5") int size) {

        // Chặn page âm
        int safePage = Math.max(page, 0);
        User currentUser = userService.findByUsername(principal.getName());
        Page<Orders> orderPage = (currentUser != null)
                ? orderService.getOrdersByUserAndStatus(currentUser, OrderStatus.COMPLETED, safePage, size)
                : orderService.getAllOrders(safePage, size);
        // Nếu người dùng nhập page quá lớn -> điều hướng về trang cuối cùng hợp lệ

        int totalPages = orderPage.getTotalPages();
        if (totalPages > 0 && safePage >= totalPages) {
            return "redirect:/orders/history?page=" + (totalPages - 1) + "&size=" + size;
        }

        List<Orders> orders = orderPage.getContent();
        model.addAttribute("orders", orders);
        model.addAttribute("currentPage", safePage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalElements", orderPage.getTotalElements());
        model.addAttribute("size", size); // ✅ dùng trong HTML khi build URL

        return "customer/order/viewordershistory";
    }

    @GetMapping("/orders/{id}")
    public String viewOrderDetail(@PathVariable Long id, Model model) {
        Orders order = orderService.getOrderById(id);
        if (order == null) return "error/error";

        List<GroupedOrderItemResponse> grouped = orderService.getOrderGrouped(id);

        boolean canEditFeedback = order.getFeedBacks() != null
                && order.getFeedBacks().getCreatedDate() != null
                && Instant.now().isBefore(order.getFeedBacks().getCreatedDate().plus(1, ChronoUnit.DAYS));

        model.addAttribute("order", order);
        model.addAttribute("grouped", grouped != null ? grouped : Collections.emptyList());
        model.addAttribute("canEditFeedback", canEditFeedback);
        return "customer/order/order-detail";
    }

    @PostMapping("/orders/{id}/feedback")
    public String submitFeedback(@PathVariable Long id,
                                 @RequestParam("rating") int rating,
                                 @RequestParam("comments") String comments,
                                 RedirectAttributes redirectAttributes) {
        Orders order = orderService.getOrderById(id);
        if (order == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng!");
            return "redirect:/orders/history";
        }

        FeedBacks feedback = new FeedBacks();
        feedback.setRating(rating);
        feedback.setComments(comments);

        order.setFeedBacks(feedback);
        orderService.saveOrder(order);

        redirectAttributes.addFlashAttribute("success", "Cảm ơn bạn đã gửi đánh giá!");
        return "redirect:/orders/" + id;
    }

    @PostMapping("/orders/{id}/feedback/edit")
    public String editFeedback(@PathVariable Long id,
                               @RequestParam("rating") int rating,
                               @RequestParam("comments") String comments,
                               RedirectAttributes redirectAttributes) {
        Orders order = orderService.getOrderById(id);
        if (order == null || order.getFeedBacks() == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đánh giá để chỉnh sửa!");
            return "redirect:/orders/" + id;
        }

        FeedBacks fb = order.getFeedBacks();
        if (fb == null || fb.getCreatedDate() == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy thời điểm tạo đánh giá.");
            return "redirect:/orders/" + id;
        }

        Instant created = fb.getCreatedDate();                 // kiểu Instant
        boolean over24h = created.plus(24, ChronoUnit.HOURS)   // created + 24h
                .isBefore(Instant.now());      // so với hiện tại (UTC)

        if (over24h) {
            redirectAttributes.addFlashAttribute("error", "Bạn chỉ có thể chỉnh sửa đánh giá trong vòng 24 giờ!");
            return "redirect:/orders/" + id;
        }

        fb.setRating(rating);
        fb.setComments(comments);
        orderService.saveOrder(order);

        redirectAttributes.addFlashAttribute("success", "Đã cập nhật đánh giá thành công!");
        return "redirect:/orders/" + id;
    }
}
