package luxdine.example.luxdine.service.catalog;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import luxdine.example.luxdine.domain.catalog.dto.request.CategoryRequest;
import luxdine.example.luxdine.domain.catalog.dto.request.MenuItemRequest;
import luxdine.example.luxdine.domain.catalog.dto.request.MenuSearchRequest;
import luxdine.example.luxdine.domain.catalog.dto.response.CategoryResponse;
import luxdine.example.luxdine.domain.catalog.dto.response.MenuItemResponse;
import luxdine.example.luxdine.domain.catalog.dto.response.MenuItemDetailResponse;
import luxdine.example.luxdine.domain.catalog.dto.response.MenuSearchResponse;
import luxdine.example.luxdine.domain.catalog.entity.Categories;
import luxdine.example.luxdine.domain.catalog.entity.Items;
import luxdine.example.luxdine.domain.catalog.enums.ItemVisibility;
import luxdine.example.luxdine.domain.catalog.repository.CategoriesRepository;
import luxdine.example.luxdine.domain.catalog.repository.ItemsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MenuService {

    ItemsRepository itemsRepository;
    CategoriesRepository categoriesRepository;

    // ========== CATEGORY METHODS ==========

    public List<CategoryResponse> getAllCategories() {
        return categoriesRepository.findAll().stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    public Optional<CategoryResponse> getCategoryById(Long id) {
        return categoriesRepository.findById(id)
                .map(this::mapToCategoryResponse);
    }

    public Optional<CategoryResponse> getCategoryBySlug(String slug) {
        return categoriesRepository.findBySlug(slug)
                .map(this::mapToCategoryResponse);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        Categories category = Categories.builder()
                .name(request.getName())
                .slug(request.getSlug() != null ? request.getSlug() : generateSlug(request.getName()))
                .build();

        Categories savedCategory = categoriesRepository.save(category);
        return mapToCategoryResponse(savedCategory);
    }

    @Transactional
    public Optional<CategoryResponse> updateCategory(Long id, CategoryRequest request) {
        return categoriesRepository.findById(id)
                .map(category -> {
                    category.setName(request.getName());
                    if (request.getSlug() != null) {
                        category.setSlug(request.getSlug());
                    }
                    Categories savedCategory = categoriesRepository.save(category);
                    return mapToCategoryResponse(savedCategory);
                });
    }

    @Transactional
    public boolean deleteCategory(Long id) {
        if (categoriesRepository.existsById(id)) {
            categoriesRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // ========== MENU ITEM METHODS ==========

    public List<MenuItemResponse> getAllMenuItems() {
        return itemsRepository.findAll().stream()
                .map(this::mapToMenuItemResponse)
                .collect(Collectors.toList());
    }

    public List<MenuItemResponse> getPublicMenuItems() {
        return itemsRepository.findByVisibility(ItemVisibility.PUBLIC).stream()
                .map(this::mapToMenuItemResponse)
                .collect(Collectors.toList());
    }

    public List<MenuItemResponse> getAvailableMenuItems() {
        return itemsRepository.findByIsAvailableTrue().stream()
                .map(this::mapToMenuItemResponse)
                .collect(Collectors.toList());
    }

    public List<MenuItemResponse> getMenuItemsByCategory(Long categoryId) {
        return categoriesRepository.findById(categoryId)
                .map(category -> itemsRepository.findByCategoryAndIsAvailableTrue(category).stream()
                        .map(this::mapToMenuItemResponse)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    public List<MenuItemResponse> getBestSellingItems() {
        return itemsRepository.findBestSellingItems().stream()
                .map(this::mapToMenuItemResponse)
                .collect(Collectors.toList());
    }

    public List<MenuItemResponse> getBestSellingItemsByCategory(Long categoryId) {
        return categoriesRepository.findById(categoryId)
                .map(category -> itemsRepository.findBestSellingItemsByCategory(category).stream()
                        .map(this::mapToMenuItemResponse)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    public Optional<MenuItemResponse> getMenuItemById(Long id) {
        return itemsRepository.findById(id)
                .map(this::mapToMenuItemResponse);
    }

    public Optional<MenuItemResponse> getMenuItemBySlug(String slug) {
        return itemsRepository.findBySlug(slug)
                .map(this::mapToMenuItemResponse);
    }

    @Transactional
    public MenuItemResponse createMenuItem(MenuItemRequest request) {
        Categories category = null;
        if (request.getCategoryId() != null) {
            category = categoriesRepository.findById(request.getCategoryId()).orElse(null);
        }

        Items item = Items.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .slug(request.getSlug() != null ? request.getSlug() : generateSlug(request.getName()))
                .imageUrl(request.getImageUrl())
                .visibility(ItemVisibility.valueOf(request.getVisibility() != null ? request.getVisibility() : ItemVisibility.PUBLIC.name()))
                .isAvailable(request.getAvailable() != null ? request.getAvailable() : true)
                .category(category)
                .build();

        Items savedItem = itemsRepository.save(item);
        return mapToMenuItemResponse(savedItem);
    }

    @Transactional
    public Optional<MenuItemResponse> updateMenuItem(Long id, MenuItemRequest request) {
        return itemsRepository.findById(id)
                .map(item -> {
                    item.setName(request.getName());
                    item.setDescription(request.getDescription());
                    item.setPrice(request.getPrice());
                    if (request.getSlug() != null) {
                        item.setSlug(request.getSlug());
                    }
                    item.setImageUrl(request.getImageUrl());
                    if (request.getVisibility() != null) {
                        item.setVisibility(ItemVisibility.valueOf(request.getVisibility()));
                    }
                    if (request.getAvailable() != null) {
                        item.setAvailable(request.getAvailable());
                    }
                    if (request.getCategoryId() != null) {
                        Categories category = categoriesRepository.findById(request.getCategoryId()).orElse(null);
                        item.setCategory(category);
                    }

                    Items savedItem = itemsRepository.save(item);
                    return mapToMenuItemResponse(savedItem);
                });
    }

    @Transactional
    public boolean deleteMenuItem(Long id) {
        if (itemsRepository.existsById(id)) {
            itemsRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // ========== SEARCH AND DETAIL METHODS ==========

    public MenuSearchResponse searchMenuItems(MenuSearchRequest request) {
        // Create sort
        Sort sort = createSort(request.getSortBy(), request.getSortOrder());

        // Create pageable
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        // Search items based on criteria
        Page<Items> itemsPage = searchItems(request, pageable);

        // Convert to response
        List<MenuItemResponse> items = itemsPage.getContent().stream()
                .map(this::mapToMenuItemResponse)
                .collect(Collectors.toList());

        return MenuSearchResponse.builder()
                .items(items)
                .totalElements(itemsPage.getTotalElements())
                .totalPages(itemsPage.getTotalPages())
                .currentPage(request.getPage())
                .pageSize(request.getSize())
                .sortBy(request.getSortBy())
                .sortOrder(request.getSortOrder())
                .build();
    }

    public Optional<MenuItemDetailResponse> getMenuItemDetailById(Long id) {
        return itemsRepository.findById(id)
                .map(this::mapToMenuItemDetailResponse);
    }

    public Optional<MenuItemDetailResponse> getMenuItemDetailBySlug(String slug) {
        return itemsRepository.findBySlug(slug)
                .map(this::mapToMenuItemDetailResponse);
    }

    // ========== HELPER METHODS ==========

    private CategoryResponse mapToCategoryResponse(Categories category) {
        List<MenuItemResponse> items = category.getItems() != null
                ? category.getItems().stream()
                    .map(this::mapToMenuItemResponse)
                    .collect(Collectors.toList())
                : List.of();

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .items(items)
                .build();
    }

    private MenuItemResponse mapToMenuItemResponse(Items item) {
        CategoryResponse category = null;
        if (item.getCategory() != null) {
            category = CategoryResponse.builder()
                    .id(item.getCategory().getId())
                    .name(item.getCategory().getName())
                    .slug(item.getCategory().getSlug())
                    .build();
        }

        return MenuItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .soldCount(item.getSoldCount())
                .price(item.getPrice())
                .slug(item.getSlug())
                .imageUrl(item.getImageUrl())
                .visibility(String.valueOf(item.getVisibility()))
                .isAvailable(item.isAvailable())
                .category(category)
                .build();
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .trim();
    }

    private Sort createSort(String sortBy, String sortOrder) {
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "name";
        }
        if (sortOrder == null || sortOrder.isEmpty()) {
            sortOrder = "asc";
        }

        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return Sort.by(direction, sortBy);
    }

    private Page<Items> searchItems(MenuSearchRequest request, Pageable pageable) {
        // 🔹 Khởi tạo Specification rỗng (thay cho Specification.where(null))
        Specification<Items> spec = (root, query, cb) -> cb.conjunction();

        // 🔍 Keyword filter
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + request.getKeyword().toLowerCase() + "%"));
        }

        // 🏷️ Category filter
        if (request.getCategoryId() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("category").get("id"), request.getCategoryId()));
        }

        // 💰 Price range filter
        if (request.getMinPrice() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("price"), request.getMinPrice()));
        }

        if (request.getMaxPrice() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("price"), request.getMaxPrice()));
        }

        spec = spec.and((root, query, cb) ->
                cb.and(
                        cb.equal(root.get("visibility"), "PUBLIC"),
                        cb.isTrue(root.get("isAvailable"))
                )
        );

        return itemsRepository.findAll(spec, pageable);
    }
    public List<Items> getTop10Items() {
        Pageable topTen = PageRequest.of(0, 10);
        return itemsRepository.findTop10BestSellingItems(ItemVisibility.PUBLIC, topTen);
    }




    private MenuItemDetailResponse mapToMenuItemDetailResponse(Items item) {
        CategoryResponse category = null;
        if (item.getCategory() != null) {
            category = CategoryResponse.builder()
                    .id(item.getCategory().getId())
                    .name(item.getCategory().getName())
                    .slug(item.getCategory().getSlug())
                    .build();
        }

        return MenuItemDetailResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .soldCount(item.getSoldCount())
                .price(item.getPrice())
                .slug(item.getSlug())
                .imageUrl(item.getImageUrl())
                .visibility(String.valueOf(item.getVisibility()))
                .isAvailable(item.isAvailable())
                .category(category)
                .build();
    }
}
