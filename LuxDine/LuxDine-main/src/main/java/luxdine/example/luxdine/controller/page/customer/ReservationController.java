package luxdine.example.luxdine.controller.page.customer;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.reservation.dto.request.ReservationCreateRequest;
import luxdine.example.luxdine.domain.reservation.dto.response.ReservationResponse;
import luxdine.example.luxdine.service.catalog.ItemsService;
import luxdine.example.luxdine.service.reservation.CustomerReservationService;
import luxdine.example.luxdine.service.seating.AreasService;
import luxdine.example.luxdine.service.seating.TableService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * ReservationController
 *
 * <p>Quản lý các thao tác đặt bàn của khách hàng (Customer),
 * bao gồm: xem danh sách đặt bàn, tạo mới, hủy, và thanh toán.
 *
 * <p>Tất cả các endpoint trong controller này đều yêu cầu người dùng
 * phải đăng nhập (được kiểm tra bằng @PreAuthorize("isAuthenticated()")).
 *
 * <p>Tác giả: Lê Ngọc Minh Kiên
 */

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class ReservationController {

    final CustomerReservationService customerReservationService;
    final ItemsService itemService;
    final AreasService areasService;
    final TableService tableService;

    /**
     * Helper method để generate danh sách giờ (9-22)
     */
    @ModelAttribute("hours")
    public List<String> getHours() {
        return IntStream.rangeClosed(9, 22)
                .mapToObj(h -> String.format("%02d", h))
                .collect(Collectors.toList());
    }

    /**
     * Helper method để generate danh sách phút (0-59)
     */
    @ModelAttribute("minutes")
    public List<String> getMinutes() {
        return IntStream.rangeClosed(0, 59)
                .mapToObj(m -> String.format("%02d", m))
                .collect(Collectors.toList());
    }

    /**
     * Helper method để lấy ngày tối thiểu (hôm nay)
     */
    @ModelAttribute("minDate")
    public String getMinDate() {
        return LocalDate.now().toString();
    }


    /**
     * Hiển thị danh sách đặt bàn của người dùng hiện tại.
     *
     * @param model Model để truyền dữ liệu sang view
     * @param auth  Authentication để lấy thông tin người dùng đăng nhập
     * @return Trang hiển thị danh sách đặt bàn của khách hàng
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/reservations")
    public String myReservations(Model model, Authentication auth) {
        String username = auth.getName();
        List<ReservationResponse> myReservations = customerReservationService.getMyReservations(username);
        model.addAttribute("reservations", myReservations);
        return "customer/reservation/customer-reservations";
    }


    /**
     * Hiển thị form tạo đặt bàn mới.
     *
     * @param model Model để thêm đối tượng trống (ReservationCreateRequest)
     * @return Trang form đặt bàn mới
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/reservations/new")
    public String newReservationForm(Model model, @RequestParam(required = false) String tableId) {
        model.addAttribute("reservation", new ReservationCreateRequest());
        model.addAttribute("tables", new ArrayList<>());
        model.addAttribute("menuItems", itemService.getAllPublicItems());
        if (tableId != null) {
            model.addAttribute("preselectedTableId", tableId);
        }
        return "customer/reservation/reservation-new";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/reservations/select-table")
    public String selectTableForm(Model model,
                                   @RequestParam String date,
                                   @RequestParam String time,
                                   @RequestParam String departureTime,
                                   @RequestParam Integer numberOfGuests) {
        model.addAttribute("date", date);
        model.addAttribute("time", time);
        model.addAttribute("departureTime", departureTime);
        model.addAttribute("numberOfGuests", numberOfGuests);
        model.addAttribute("areas", areasService.getAllAreas());
        model.addAttribute("tables", tableService.getAllTables(null));
        return "customer/reservation/reservation-select-table";
    }



    @PreAuthorize("isAuthenticated()")
    @GetMapping("/reservations/{id}")
    public String viewReservation(@PathVariable("id") Long id, Authentication auth, Model model) {
        String username = auth.getName();
        ReservationResponse reservation = customerReservationService.getMyReservationById(username, id);
        model.addAttribute("reservation", reservation);
        return "customer/reservation/customer-reservation-detail";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/reservations/{id}/edit")
    public String editReservation(@PathVariable("id") Long id, Authentication auth, Model model) {
        String username = auth.getName();
        ReservationResponse reservation = customerReservationService.getMyReservationById(username, id);
        
        // Kiểm tra trạng thái reservation - không cho phép chỉnh sửa nếu đã ARRIVED hoặc CANCELLED
        String currentStatus = reservation.getStatus();
        if ("ARRIVED".equals(currentStatus) ||
            "CANCELLED".equals(currentStatus)) {
            return "redirect:/reservations/" + id + "?error=Cannot edit reservation with status: " + currentStatus;
        }
        model.addAttribute("reservation", reservation);
        model.addAttribute("menuItems", itemService.getAllPublicItems());
        model.addAttribute("tables", new ArrayList<>());
        return "customer/reservation/customer-reservation-edit";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reservations/{id}")
    public String updateReservation(@PathVariable("id") Long id,
                                    @ModelAttribute("reservation") ReservationCreateRequest request,
                                    Authentication auth) {
        String username = auth.getName();
        customerReservationService.updateMyReservation(username, id, request);
        return "redirect:/reservations/" + id;
    }


    /**
     * Hủy một đặt bàn thuộc về người dùng hiện tại.
     *
     * @param id   ID của đặt bàn cần hủy
     * @param auth Thông tin người dùng đang đăng nhập
     * @return Chuyển hướng về danh sách đặt bàn của khách hàng
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reservations/{id}/cancel")
    public String cancelReservation(@PathVariable("id") Long id,
                                    Authentication auth,
                                    RedirectAttributes ra) {
        String username = auth.getName();
        try {
            ReservationResponse reservation = customerReservationService.cancelMyReservation(username, id);
            ra.addFlashAttribute("success", "Phiếu đặt bàn " +  reservation.getReservationCode() +  " đã được hủy thành công.");
        } catch (IllegalStateException | IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/reservations";
    }

}




