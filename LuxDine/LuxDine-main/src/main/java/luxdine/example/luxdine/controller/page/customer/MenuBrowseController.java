package luxdine.example.luxdine.controller.page.customer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import luxdine.example.luxdine.domain.catalog.dto.request.MenuSearchRequest;
import luxdine.example.luxdine.domain.catalog.dto.response.*;
import luxdine.example.luxdine.domain.catalog.enums.BundleType;
import luxdine.example.luxdine.service.catalog.MenuService;
import luxdine.example.luxdine.service.catalog.BundleService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/browse")
public class MenuBrowseController {

    final MenuService menuService;
    final BundleService bundleService;

    // ========== WEB PAGES ==========

    @GetMapping("/menu")
    public String browseMenu(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) Long category,
                             @RequestParam(required = false) Double minPrice,
                             @RequestParam(required = false) Double maxPrice,
                             @RequestParam(required = false, defaultValue = "name") String sortBy,
                             @RequestParam(required = false, defaultValue = "asc") String sortOrder,
                             @RequestParam(required = false, defaultValue = "0") Integer page,
                             Model model) {

        MenuSearchRequest searchRequest = MenuSearchRequest.builder()
                .keyword(keyword)
                .categoryId(category)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .page(page)
                .size(12)
                .build();

        MenuSearchResponse searchResponse = menuService.searchMenuItems(searchRequest);
        List<CategoryResponse> categories = menuService.getAllCategories();

        List<Long> top10Ids = menuService.getTop10Items()
                .stream()
                .map(item -> item.getId())
                .toList();

        model.addAttribute("top10Ids", top10Ids);
        model.addAttribute("items", searchResponse.getItems());
        model.addAttribute("categories", categories);
        model.addAttribute("searchResponse", searchResponse);
        model.addAttribute("currentKeyword", keyword);
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentMinPrice", minPrice);
        model.addAttribute("currentMaxPrice", maxPrice);
        model.addAttribute("currentSortBy", sortBy);
        model.addAttribute("currentSortOrder", sortOrder);
        model.addAttribute("currentPage", page);

        return "customer/menu/browse-menu";
    }

    @GetMapping("/dish/{slug}")
    public String viewDishDetails(@PathVariable String slug, Model model) {
        MenuItemDetailResponse item = menuService.getMenuItemDetailBySlug(slug)
                .orElse(null);

        if (item == null) {
            model.addAttribute("errorMessage", "Dish not found!");
            return "error/error";
        }

        List<Long> top10Ids = menuService.getTop10Items()
                .stream()
                .map(i -> i.getId())
                .toList();
        model.addAttribute("top10Ids", top10Ids);
        model.addAttribute("item", item);
        return "customer/menu/dish-details";
    }

    // ===================== BUNDLES =====================

    @GetMapping("/bundles")
    public String viewBundles(Model model) {
        model.addAttribute("bundles", bundleService.getAllBundles());
        return "customer/menu/bundle-list";
    }

    @GetMapping("/bundle/{slug}")
    public String viewBundleDetails(@PathVariable String slug, Model model) {
        BundleDetailResponse bundle = bundleService.getBundleDetailBySlug(slug)
                .orElse(null);

        if (bundle == null) {
            model.addAttribute("errorMessage", "Bundle not found!");
            return "error/error";
        }

        model.addAttribute("bundle", bundle);
        return "customer/menu/bundle-details";
    }

    // ========== REST API ENDPOINTS ==========

    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<MenuSearchResponse> searchMenuItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false, defaultValue = "name") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "12") Integer size) {

        MenuSearchRequest searchRequest = MenuSearchRequest.builder()
                .keyword(keyword)
                .categoryId(category)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .page(page)
                .size(size)
                .build();

        MenuSearchResponse response = menuService.searchMenuItems(searchRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/dish/{slug}")
    @ResponseBody
    public ResponseEntity<MenuItemDetailResponse> getDishDetails(@PathVariable String slug) {
        return menuService.getMenuItemDetailBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/bundles")
    @ResponseBody
    public ResponseEntity<List<BundleResponse>> getBundles() {
        List<BundleResponse> bundles = bundleService.getAllBundles();
        return ResponseEntity.ok(bundles);
    }

    @GetMapping("/api/bundle/{slug}")
    @ResponseBody
    public ResponseEntity<BundleDetailResponse> getBundleDetails(@PathVariable String slug) {
        return bundleService.getBundleDetailBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/bundles/{type}")
    public String viewBundlesByType(@PathVariable BundleType type, Model model) {
        List<BundleResponse> bundles = bundleService.getBundlesByType(type);
        model.addAttribute("bundles", bundles);
        model.addAttribute("bundleType", type.name());
        return "customer/menu/bundle-list";

    }

    @GetMapping("/api/bundles/type/{type}")
    @ResponseBody
    public ResponseEntity<List<BundleResponse>> getBundlesByType(@PathVariable BundleType type) {
        List<BundleResponse> bundles = bundleService.getBundlesByType(type);
        return ResponseEntity.ok(bundles);
    }
}
