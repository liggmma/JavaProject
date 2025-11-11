
/**
 * AdminMenuService
 * Service xử lý nghiệp vụ quản lý Menu Item cho Admin:
 *  - CRUD món ăn / đồ uống (Items)
 *  - Kiểm tra danh mục (Category)
 *  - Quản lý metadata (thời gian chuẩn bị, allergens)
 * Sử dụng:
 *  - AppException + ErrorCode: ném lỗi nghiệp vụ (vd: ITEM_NOT_FOUND)
 *  - GlobalExceptionHandler: bắt AppException và trả JSON hợp l
 * Author: Kiên Lê Ngọc Minh
 */
package luxdine.example.luxdine.service.admin;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import luxdine.example.luxdine.domain.catalog.dto.request.MenuItemRequest;
import luxdine.example.luxdine.domain.catalog.dto.request.MenuItemUpdateRequest;
import luxdine.example.luxdine.domain.catalog.dto.response.MenuItemDetailResponse;
import luxdine.example.luxdine.domain.catalog.dto.response.MenuItemResponse;
import luxdine.example.luxdine.domain.catalog.dto.response.CategoryResponse;
import luxdine.example.luxdine.domain.catalog.entity.Categories;
import luxdine.example.luxdine.domain.catalog.entity.Items;
import luxdine.example.luxdine.domain.catalog.entity.Allergen;
import luxdine.example.luxdine.domain.catalog.enums.ItemVisibility;
import luxdine.example.luxdine.domain.catalog.repository.CategoriesRepository;
import luxdine.example.luxdine.domain.catalog.repository.ItemsRepository;
import luxdine.example.luxdine.domain.catalog.repository.AllergenRepository;
// import luxdine.example.luxdine.exception.AppException;
// import luxdine.example.luxdine.exception.ErrorCode;
import luxdine.example.luxdine.exception.AppException;
import luxdine.example.luxdine.exception.ErrorCode;
import luxdine.example.luxdine.exception.GlobalExceptionHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AdminMenuService {
    
    ItemsRepository itemsRepository;
    CategoriesRepository categoriesRepository;
    AllergenRepository allergenRepository;

    /**
     * Get all menu items for admin
     */
    public List<MenuItemResponse> getAllMenuItems() {
        return itemsRepository.findAll().stream()
                .map(this::toMenuItemResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get menu items by category
     */
    public List<MenuItemResponse> getMenuItemsByCategory(Long categoryId) {
        return itemsRepository.findByCategoryId(categoryId).stream()
                .map(this::toMenuItemResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get menu item detail by ID
     */
    public MenuItemDetailResponse getMenuItemById(Long id) {
        Items item = itemsRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
        
        return toMenuItemDetailResponse(item);
    }
    
    /**
     * Create new menu item
     */
    @Transactional
    public MenuItemDetailResponse createMenuItem(MenuItemRequest request) {
        log.info("Creating new menu item: {}", request.getName());
        
        // Validate category exists
        Categories category = categoriesRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        
        // Create slug from name
        String slug = generateSlug(request.getName());
        
        // Check if slug already exists
        if (itemsRepository.findBySlug(slug).isPresent()) {
            slug = slug + "-" + System.currentTimeMillis();
        }
        List<Allergen> allergenEntities = resolveAllergens(request.getAllergens());
        Items item = Items.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .slug(slug)
                .imageUrl(request.getImageUrl())
                .visibility(request.getVisibility() != null ? ItemVisibility.valueOf(request.getVisibility()) : ItemVisibility.PUBLIC)
                .isAvailable(true)
                .soldCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .category(category)
                .allergens(allergenEntities)
                .build();
        
        Items savedItem = itemsRepository.save(item);

        log.info("Successfully created menu item with ID: {}", savedItem.getId());
        
        return toMenuItemDetailResponse(savedItem);
    }
    
    /**
     * Update existing menu item
     */
    @Transactional
    public MenuItemDetailResponse updateMenuItem(Long id, MenuItemUpdateRequest request) {
        log.info("Updating menu item ID: {}", id);
        
        Items item = itemsRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
        
        // Update fields if provided
        if (request.getName() != null && !request.getName().equals(item.getName())) {
            item.setName(request.getName());
            item.setSlug(generateSlug(request.getName()));
        }
        
        if (request.getDescription() != null) {
            item.setDescription(request.getDescription());
        }
        
        if (request.getPrice() != null) {
            item.setPrice(request.getPrice());
        }
        
        if (request.getCategoryId() != null) {
            Categories category = categoriesRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            item.setCategory(category);
        }
        
        if (request.getImageUrl() != null) {
            item.setImageUrl(request.getImageUrl());
        }
        
        if (request.getVisibility() != null) {
            item.setVisibility(request.getVisibility());
        }
        
        if (request.getIsAvailable() != null) {
            item.setAvailable(request.getIsAvailable());
        }
        // CẬP NHẬT ALLERGENS
        if (request.getAllergens() != null) {
            List<Allergen> allergenEntities = resolveAllergens(request.getAllergens());
            item.setAllergens(allergenEntities);
            log.info("Updated allergens for item ID {}", id);
        }
        
        item.setUpdatedAt(Instant.now());

        
        Items updatedItem = itemsRepository.save(item);
        


        log.info("Successfully updated menu item ID: {}", id);
        
        return toMenuItemDetailResponse(updatedItem);
    }
    
    /**
     * Delete menu item
     */
    @Transactional
    public void deleteMenuItem(Long id) {
        log.info("Deleting menu item ID: {}", id);
        
        Items item = itemsRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
        
        // Check if item is in any active orders (optional)
        // You might want to soft delete instead
        
        itemsRepository.delete(item);
        log.info("Successfully deleted menu item ID: {}", id);
    }
    
    /**
     * Toggle item availability
     */
    @Transactional
    public MenuItemDetailResponse toggleAvailability(Long id) {
        log.info("Toggling availability for menu item ID: {}", id);
        
        Items item = itemsRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
        
        item.setAvailable(!item.isAvailable());
        item.setUpdatedAt(Instant.now());

        
        Items updatedItem = itemsRepository.save(item);
        log.info("Menu item ID {} availability set to: {}", id, updatedItem.isAvailable());
        
        return toMenuItemDetailResponse(updatedItem);
    }
    
    /**
     * Update item visibility
     */
    @Transactional
    public MenuItemDetailResponse updateVisibility(Long id, ItemVisibility visibility) {
        log.info("Updating visibility for menu item ID: {} to {}", id, visibility);
        
        Items item = itemsRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
        
        item.setVisibility(visibility);
        item.setUpdatedAt(Instant.now());

        
        Items updatedItem = itemsRepository.save(item);
        log.info("Successfully updated visibility for menu item ID: {}", id);
        
        return toMenuItemDetailResponse(updatedItem);
    }
    
    // ========== Helper Methods ==========
    
    private MenuItemResponse toMenuItemResponse(Items item) {
        // Lấy allergen names từ list entity
        List<String> allergenNames = (item.getAllergens() != null)
                ? item.getAllergens().stream().map(Allergen::getName).collect(Collectors.toList())
                : new ArrayList<>();

    return MenuItemResponse.builder()
            .id(item.getId())
            .name(item.getName())
            .description(item.getDescription())
            .soldCount(item.getSoldCount())
            .price(item.getPrice())
            .slug(item.getSlug())
            .imageUrl(item.getImageUrl())
            .visibility(item.getVisibility() != null ? item.getVisibility().name() : ItemVisibility.PUBLIC.name())
            .isAvailable(item.isAvailable())
            .createdAt(item.getCreatedAt() != null ? Date.from(item.getCreatedAt()) : null)
            .updatedAt(item.getUpdatedAt() != null ? Date.from(item.getUpdatedAt()) : null)
            .category(toCategoryResponse(item.getCategory()))
            .allergens(allergenNames)
            .build();
}

    
   private MenuItemDetailResponse toMenuItemDetailResponse(Items item) {
       // Lấy allergen names từ list entity
       List<String> allergenNames = (item.getAllergens() != null)
               ? item.getAllergens().stream().map(Allergen::getName).collect(Collectors.toList())
               : new ArrayList<>();

    return MenuItemDetailResponse.builder()
            .id(item.getId())
            .name(item.getName())
            .description(item.getDescription())
            .soldCount(item.getSoldCount())
            .price(item.getPrice())
            .slug(item.getSlug())
            .imageUrl(item.getImageUrl())
            .visibility(item.getVisibility() != null ? item.getVisibility().name() : "PUBLIC")
            .isAvailable(item.isAvailable())
            .createdAt(item.getCreatedAt() != null ? Date.from(item.getCreatedAt()) : null)
            .updatedAt(item.getUpdatedAt() != null ? Date.from(item.getUpdatedAt()) : null)
            .category(toCategoryResponse(item.getCategory()))
            .allergens(allergenNames)
            .build();

            
}

    /**
     * Get all categories
     */
    public List<CategoryResponse> getAllCategories() {
        return categoriesRepository.findAll().stream()
                .map(this::toCategoryResponse)
                .collect(Collectors.toList());
    }
    
    private CategoryResponse toCategoryResponse(Categories category) {
        if (category == null) return null;
        
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .build();
    }


    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }

    /**
     * List<Allergen> (entity).
     * Tự động tìm hoặc tạo mới allergen nếu chưa có trong DB.
     */
    private List<Allergen> resolveAllergens(List<String> allergenNames) {
        if (allergenNames == null || allergenNames.isEmpty()) {
            return new ArrayList<>();
        }

        return allergenNames.stream()
                .map(name -> allergenRepository.findByName(name)
                        .orElseGet(() -> allergenRepository.save(
                                Allergen.builder().name(name).build()
                        ))
                )
                .collect(Collectors.toList());
    }
}