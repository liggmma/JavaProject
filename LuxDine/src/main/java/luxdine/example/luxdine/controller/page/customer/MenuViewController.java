package luxdine.example.luxdine.controller.page.customer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import luxdine.example.luxdine.domain.catalog.dto.response.CategoryResponse;
import luxdine.example.luxdine.domain.catalog.dto.response.MenuItemResponse;
import luxdine.example.luxdine.domain.catalog.entity.Categories;
import luxdine.example.luxdine.domain.catalog.entity.Items;
import luxdine.example.luxdine.domain.catalog.repository.CategoriesRepository;
import luxdine.example.luxdine.service.catalog.MenuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuViewController {

    private final MenuService menuService;

    @GetMapping
    public String viewMenu(@RequestParam(value = "category", required = false) String categorySlug,
                           @RequestParam(value = "tab", required = false, defaultValue = "main-courses") String tab,
                           Model model) {

        // 1) Categories luôn có type
        List<CategoryResponse> categories = java.util.Optional
                .ofNullable(menuService.getAllCategories())
                .orElse(java.util.List.of());

        // 2) Resolve selected category theo slug
        CategoryResponse selectedCategory = null;
        if (categorySlug != null && !categorySlug.isBlank()) {
            selectedCategory = categories.stream()
                    .filter(cat -> categorySlug.equals(cat.getSlug()))
                    .findFirst()
                    .orElse(null);
        }

        // 3) Lấy source items theo category/public — cả 2 case đều là List<MenuItemResponse>
        List<MenuItemResponse> sourceItems = (selectedCategory != null)
                ? java.util.Optional.ofNullable(menuService.getMenuItemsByCategory(selectedCategory.getId())).orElse(java.util.List.of())
                : java.util.Optional.ofNullable(menuService.getPublicMenuItems()).orElse(java.util.List.of());

        // 4) Lọc available + sort theo soldCount (null-safe) + giới hạn 8
        List<MenuItemResponse> menuItems = sourceItems.stream()
                .filter(mi -> java.util.Objects.equals(Boolean.TRUE, mi.getIsAvailable()))
                .sorted(java.util.Comparator.comparingInt(
                        (MenuItemResponse mi) -> java.util.Objects.requireNonNullElse(mi.getSoldCount(), 0)
                ).reversed())
                .limit(8)
                .toList();

        // 5) Top10 ids cho badge Popular
        // Trường hợp A: getTop10Items() trả về List<MenuItemResponse>
        List<Long> top10Ids = java.util.Optional.ofNullable(menuService.getTop10Items()).orElse(java.util.List.of())
                .stream()
                .map(Items::getId)
                .toList();

        // 6) Model attrs
        model.addAttribute("categories", categories);
        model.addAttribute("menuItems", menuItems);
        model.addAttribute("top10Ids", top10Ids);
        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("currentCategory", categorySlug);
        model.addAttribute("currentTab", tab);

        return "customer/menu/menu-view";
    }

    @GetMapping("/item/{slug}")
    public String viewMenuItem(@PathVariable String slug, Model model) {
        // Redirect to the new dish details page for consistency
        return "redirect:/browse/dish/" + slug;
    }
}


