/**
 *  AdminCategoryAPI
 *
 * REST API dành riêng cho quản trị viên (ROLE_ADMIN) để thao tác với danh mục (categories).
 * Cung cấp endpoint cho việc truy xuất danh sách toàn bộ danh mục hiện có trong hệ thống.
 *
 * <p>Base URL: <b>/api/admin/categories</b></p>
 *
 * <p><b>Authorization:</b> Chỉ người dùng có role ADMIN mới được phép truy cập (được cấu hình qua @PreAuthorize).</p>
 */

package luxdine.example.luxdine.controller.api;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.catalog.dto.request.CategoryRequest;
import luxdine.example.luxdine.domain.catalog.dto.response.CategoryResponse;
import luxdine.example.luxdine.domain.catalog.entity.Categories;
import luxdine.example.luxdine.domain.catalog.repository.CategoriesRepository;
import luxdine.example.luxdine.domain.catalog.repository.ItemsRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryAPI {
    
    CategoriesRepository categoriesRepository;
    ItemsRepository itemsRepository; // ← THÊM REPOSITORY NÀY
    
    /**
     * GET /api/admin/categories
     * Lấy tất cả categories kèm số lượng món ăn
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCategories() {
        log.info("GET /api/admin/categories");
        
        List<Map<String, Object>> categories = categoriesRepository.findAll().stream()
                .map(cat -> {
                    Map<String, Object> catMap = new HashMap<>();
                    catMap.put("id", cat.getId());
                    catMap.put("name", cat.getName());
                    catMap.put("slug", cat.getSlug());
                    // ← THÊM SỐ LƯỢNG MÓN ĂN
                    catMap.put("itemCount", itemsRepository.countByCategoryId(cat.getId()));
                    return catMap;
                })
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", categories);
        response.put("total", categories.size());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * GET /api/admin/categories/{id}
     * Lấy chi tiết 1 category
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCategoryById(@PathVariable Long id) {
        log.info("GET /api/admin/categories/{}", id);
        
        return categoriesRepository.findById(id)
                .map(cat -> {
                    Map<String, Object> catData = new HashMap<>();
                    catData.put("id", cat.getId());
                    catData.put("name", cat.getName());
                    catData.put("slug", cat.getSlug());
                    catData.put("itemCount", itemsRepository.countByCategoryId(cat.getId()));
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("data", catData);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Không tìm thấy danh mục");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                });
    }
    
    /**
     * POST /api/admin/categories
     * Tạo category mới
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCategory(@Valid @RequestBody CategoryRequest request) {
        log.info("POST /api/admin/categories: {}", request);
        
        try {
            // Tạo slug từ name
            String slug = request.getSlug() != null && !request.getSlug().isEmpty() 
                    ? request.getSlug()
                    : generateSlug(request.getName());
            
            Categories category = Categories.builder()
                    .name(request.getName())
                    .slug(slug)
                    .build();
            
            Categories saved = categoriesRepository.save(category);
            
            Map<String, Object> catData = new HashMap<>();
            catData.put("id", saved.getId());
            catData.put("name", saved.getName());
            catData.put("slug", saved.getSlug());
            catData.put("itemCount", 0);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tạo danh mục thành công");
            response.put("data", catData);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Lỗi tạo danh mục", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * PUT /api/admin/categories/{id}
     * Cập nhật category
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        log.info("PUT /api/admin/categories/{}: {}", id, request);
        
        return categoriesRepository.findById(id)
                .map(existing -> {
                    existing.setName(request.getName());
                    if (request.getSlug() != null && !request.getSlug().isEmpty()) {
                        existing.setSlug(request.getSlug());
                    } else {
                        existing.setSlug(generateSlug(request.getName()));
                    }
                    
                    Categories updated = categoriesRepository.save(existing);
                    
                    Map<String, Object> catData = new HashMap<>();
                    catData.put("id", updated.getId());
                    catData.put("name", updated.getName());
                    catData.put("slug", updated.getSlug());
                    catData.put("itemCount", itemsRepository.countByCategoryId(updated.getId()));
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "Cập nhật danh mục thành công");
                    response.put("data", catData);
                    
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "Không tìm thấy danh mục");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                });
    }
    
    /**
     * DELETE /api/admin/categories/{id}
     * Xóa category (chỉ khi không có món ăn)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        log.info("DELETE /api/admin/categories/{}", id);
        
        Map<String, Object> response = new HashMap<>();
        
        return categoriesRepository.findById(id)
                .map(category -> {
                    // ← KIỂM TRA SỐ LƯỢNG MÓN ĂN
                    long itemCount = itemsRepository.countByCategoryId(id);
                    
                    if (itemCount > 0) {
                        response.put("success", false);
                        response.put("message", "Không thể xóa danh mục này vì đang có " + itemCount + " món ăn");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                    }
                    
                    categoriesRepository.deleteById(id);
                    
                    response.put("success", true);
                    response.put("message", "Xóa danh mục thành công");
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    response.put("success", false);
                    response.put("message", "Không tìm thấy danh mục");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                });
    }
    
    /**
     * Tạo slug từ tên danh mục
     */
    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[àáảãạăắằẳẵặâấầẩẫậ]", "a")
                .replaceAll("[èéẻẽẹêếềểễệ]", "e")
                .replaceAll("[ìíỉĩị]", "i")
                .replaceAll("[òóỏõọôốồổỗộơớờởỡợ]", "o")
                .replaceAll("[ùúủũụưứừửữự]", "u")
                .replaceAll("[ỳýỷỹỵ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}