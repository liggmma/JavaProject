package luxdine.example.luxdine.controller.page.staff;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.order.dto.response.KitchenOrderResponse;
import luxdine.example.luxdine.service.order.KitchenService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@RequestMapping("/staff")
public class KitchenController {

    final KitchenService kitchenService;

    /**
     * Display Kitchen Display System page
     */
    @GetMapping("/kitchen")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public String kitchenDisplay(Model model) {
        log.debug("Loading Kitchen Display System page");
        
        List<KitchenOrderResponse> orders = kitchenService.getAllKitchenOrders();
        KitchenService.KitchenSummaryResponse summary = kitchenService.getKitchenSummary();
        
        model.addAttribute("orders", orders);
        model.addAttribute("summary", summary);
        model.addAttribute("currentTime", java.time.LocalTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
        model.addAttribute("currentPage", "kitchen");
        
        return "staff/kitchen/kitchen";
    }
}
