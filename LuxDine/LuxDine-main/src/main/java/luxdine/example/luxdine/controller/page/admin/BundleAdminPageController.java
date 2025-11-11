package luxdine.example.luxdine.controller.page.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.catalog.dto.response.BundleDetailResponse;
import luxdine.example.luxdine.domain.catalog.dto.response.BundleResponse;
import luxdine.example.luxdine.domain.catalog.enums.BundleType;
import luxdine.example.luxdine.service.catalog.BundleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Page Controller for Bundle Management (Admin)
 * Render Thymeleaf templates cho quản lý Bundles/Combos
 * 
 * @author Kiên Lê Ngọc Minh
 */
@Controller
@RequestMapping("/admin/bundles")
@RequiredArgsConstructor
@Slf4j
public class BundleAdminPageController {

    private final BundleService bundleService;

    /**
     * GET /admin/bundles
     * Trang quản lý bundles
     */
    @GetMapping
    public String bundleManagementPage(Model model) {
        log.info("PAGE: GET /admin/bundles - Rendering bundle management page");
        
        model.addAttribute("currentPage", "bundles");
        model.addAttribute("pageTitle", "Quản lý Combo/Bundle");
        
        return "admin/bundles";
    }

    /**
     * GET /admin/bundles/new
     * Trang tạo bundle mới
     */
    @GetMapping("/new")
    public String newBundlePage(Model model) {
        log.info("PAGE: GET /admin/bundles/new - Rendering new bundle page");
        
        model.addAttribute("currentPage", "bundles");
        model.addAttribute("pageTitle", "Tạo Combo/Bundle mới");
        model.addAttribute("isEditMode", false);
        
        return "admin/bundle-form";
    }

    /**
     * GET /admin/bundles/{id}/edit
     * Trang chỉnh sửa bundle
     */
    @GetMapping("/{id}/edit")
    public String editBundlePage(@PathVariable Long id, Model model) {
        log.info("PAGE: GET /admin/bundles/{}/edit - Rendering edit bundle page", id);
        
        BundleDetailResponse bundle = bundleService.getBundleByIdForAdmin(id);
        
        model.addAttribute("currentPage", "bundles");
        model.addAttribute("pageTitle", "Chỉnh sửa Combo/Bundle");
        model.addAttribute("isEditMode", true);
        model.addAttribute("bundle", bundle);
        
        return "admin/bundle-form";
    }

    /**
     * GET /admin/bundles/{id}
     * Trang xem chi tiết bundle
     */
    @GetMapping("/{id}")
    public String bundleDetailPage(@PathVariable Long id, Model model) {
        log.info("PAGE: GET /admin/bundles/{} - Rendering bundle detail page", id);

        BundleDetailResponse bundle = bundleService.getBundleByIdForAdmin(id);

        // Tính anchor price và analytics
        double anchorPrice = bundle.getItems().stream()
                .mapToDouble(item -> item.getItem().getPrice() * item.getQuantity())
                .sum();
        
        double savings = anchorPrice - bundle.getPrice();
        double savingsPercentage = anchorPrice > 0 ? (savings / anchorPrice) * 100 : 0;
        
        model.addAttribute("currentPage", "bundles");
        model.addAttribute("pageTitle", "Chi tiết Combo/Bundle");
        model.addAttribute("bundle", bundle);
        model.addAttribute("anchorPrice", anchorPrice);
        model.addAttribute("savings", savings);
        model.addAttribute("savingsPercentage", savingsPercentage);
        
        return "admin/bundle-detail";
    }

    /**
     * GET /admin/bundles/type/{type}
     * Trang danh sách bundles theo type
     */
    @GetMapping("/type/{type}")
    public String bundlesByTypePage(@PathVariable BundleType type, Model model) {
        log.info("PAGE: GET /admin/bundles/type/{} - Rendering bundles by type", type);
        
        List<BundleResponse> bundles = bundleService.getBundlesByType(type);
        
        model.addAttribute("currentPage", "bundles");
        model.addAttribute("pageTitle", "Danh sách " + type.name());
        model.addAttribute("bundleType", type);
        model.addAttribute("bundles", bundles);
        
        return "admin/bundle-list-by-type";
    }
}