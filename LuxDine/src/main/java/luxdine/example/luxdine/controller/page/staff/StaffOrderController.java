package luxdine.example.luxdine.controller.page.staff;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.order.dto.request.OrderCreateRequest;
import luxdine.example.luxdine.domain.order.dto.request.OrderFilterRequest;
import luxdine.example.luxdine.domain.order.dto.response.GroupedOrderItemResponse;
import luxdine.example.luxdine.domain.order.dto.response.OrderResponse;
import luxdine.example.luxdine.domain.order.entity.Orders;
import luxdine.example.luxdine.domain.order.enums.OrderStatus;
import luxdine.example.luxdine.service.catalog.ItemsService;
import luxdine.example.luxdine.service.catalog.MenuService;
import luxdine.example.luxdine.service.order.OrderService;
import luxdine.example.luxdine.service.reservation.ReservationService;
import luxdine.example.luxdine.service.seating.TableService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@RequestMapping("/staff")
public class StaffOrderController {
    final OrderService orderService;
    final ItemsService itemService;
    final TableService tableService;
    final MenuService menuService;

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/orders")
    public String staffOrders(@ModelAttribute OrderFilterRequest filter, Model model) {
        List<OrderResponse> orders = orderService.searchOrders(filter);
        model.addAttribute("filter", filter);
        List<OrderStatus> status = new ArrayList<>();
        for (OrderStatus orderStatus : List.of(OrderStatus.values())) {
            if (!orderStatus.equals(OrderStatus.PENDING)) {
                status.add(orderStatus);
            }
        }
        model.addAttribute("statuses", status);
        model.addAttribute("orders", orders);

        return "staff/order/staff-orders";
    }

    @PreAuthorize("hasRole('STAFF')")
    @PostMapping("/orders/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        String result = orderService.cancelOrder(id);
        log.info("Cancel order result: {}", result);
        if (result != null) {
            ra.addFlashAttribute("error", result);
        } else {
            ra.addFlashAttribute("success", "Cancelled order " + id.toString() +  " successfully");
        }
        return "redirect:/staff/orders";
    }

    @GetMapping("/orders/{id}")
    public String detail(@PathVariable Long id, Model model){
        Orders order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        List<GroupedOrderItemResponse> grouped = orderService.getOrderGrouped(id);
        model.addAttribute("groupedItems", grouped);
        model.addAttribute("menuItems", itemService.getAllPublicItems()); // cho dropdown Add
        model.addAttribute("currentPage", "orders");
        return "staff/order/staff-orderdetail";
    }

    @PostMapping("/orders/{id}/items")
    public String addItem(@PathVariable Long id, @RequestParam Long itemId, @RequestParam int quantity, RedirectAttributes ra){
        var msg = orderService.addItem(id, itemId, quantity);
        if (msg != null) ra.addFlashAttribute("error", msg); else ra.addFlashAttribute("success","Added item");
        return "redirect:/staff/orders/{id}";
    }

    @PostMapping("/orders/{id}/items/{oiId}/cancelAll")
    public String cancelOrderItem(@PathVariable Long id, @PathVariable Long oiId, RedirectAttributes ra){
        var msg = orderService.cancelOrderItemByItemId(id, oiId);
        if (msg != null) ra.addFlashAttribute("error", msg); else ra.addFlashAttribute("success","Canceled item");
        return "redirect:/staff/orders/{id}";
    }

    @GetMapping("/orders/new")
    public String newOrder(Model model){
        model.addAttribute("orderCreate", new OrderCreateRequest());
        model.addAttribute("tables", tableService.findAvailableTable());
        model.addAttribute("category", menuService.getAllCategories());
        model.addAttribute("menuItems", itemService.getAllPublicItems());
        return "staff/order/staff-neworder";
    }

    @PostMapping("/orders")
    public String create(@ModelAttribute OrderCreateRequest req, RedirectAttributes ra){
        String id = orderService.createOrder(req);
        ra.addFlashAttribute("success", "Created order #" + id);
        return "redirect:/staff/orders/" + id;
    }

}
