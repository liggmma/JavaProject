/**
 *  AdminMenuAPI
 *
 * REST Controller dành riêng cho quản trị viên (ROLE_ADMIN),
 * cho phép thao tác CRUD và thay đổi trạng thái hiển thị của các món ăn (menu items).
 *
 * <p><b>Base URL:</b> /api/admin/menu</p>
 *
 * <p>Tất cả các endpoint đều yêu cầu quyền truy cập ADMIN thông qua @PreAuthorize.</p>
 *
 * <p>Chức năng chính:</p>
 * <ul>
 *     <li>Lấy danh sách tất cả món ăn hoặc theo danh mục</li>
 *     <li>Xem chi tiết món ăn</li>
 *     <li>Tạo, cập nhật, xóa món ăn</li>
 *     <li>Ẩn/hiện món ăn, bật/tắt trạng thái khả dụng (availability)</li>
 * </ul>
 */

package luxdine.example.luxdine.controller.api;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.catalog.dto.request.MenuItemRequest;
import luxdine.example.luxdine.domain.catalog.dto.request.MenuItemUpdateRequest;
import luxdine.example.luxdine.domain.catalog.dto.response.MenuItemDetailResponse;
import luxdine.example.luxdine.domain.catalog.dto.response.MenuItemResponse;
import luxdine.example.luxdine.domain.catalog.dto.response.CategoryResponse;
import luxdine.example.luxdine.domain.catalog.enums.ItemVisibility;
import luxdine.example.luxdine.service.admin.AdminMenuService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/menu")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminMenuAPI {
    
    AdminMenuService adminMenuService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllMenuItems(
            @RequestParam(required = false) Long categoryId
    ) {
        List<MenuItemResponse> items;
        if (categoryId != null) {
            items = adminMenuService.getMenuItemsByCategory(categoryId);
        } else {
            items = adminMenuService.getAllMenuItems();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", items);
        response.put("total", items.size());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getMenuItemById(@PathVariable Long id) {
        MenuItemDetailResponse item = adminMenuService.getMenuItemById(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", item);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createMenuItem(@Valid @RequestBody MenuItemRequest request) {
        MenuItemDetailResponse created = adminMenuService.createMenuItem(request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Menu item created successfully");
        response.put("data", created);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemUpdateRequest request
    ) {
        MenuItemDetailResponse updated = adminMenuService.updateMenuItem(id, request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Menu item updated successfully");
        response.put("data", updated);
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteMenuItem(@PathVariable Long id) {
        adminMenuService.deleteMenuItem(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Menu item deleted successfully");
        
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/toggle-availability")
    public ResponseEntity<Map<String, Object>> toggleAvailability(@PathVariable Long id) {
        MenuItemDetailResponse updated = adminMenuService.toggleAvailability(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Availability toggled successfully");
        response.put("data", updated);
        
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/visibility")
    public ResponseEntity<Map<String, Object>> updateVisibility(
            @PathVariable Long id,
            @RequestParam ItemVisibility visibility
    ) {
        MenuItemDetailResponse updated = adminMenuService.updateVisibility(id, visibility);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Visibility updated successfully");
        response.put("data", updated);
        
        return ResponseEntity.ok(response);
    }
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getAllCategories() {
        List<CategoryResponse> categories = adminMenuService.getAllCategories();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", categories);

        return ResponseEntity.ok(response);
    }
}