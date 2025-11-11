package luxdine.example.luxdine.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.catalog.dto.request.CreateBundleRequest;
import luxdine.example.luxdine.domain.catalog.dto.request.UpdateBundleRequest;
import luxdine.example.luxdine.domain.catalog.dto.response.BundleDetailResponse;
import luxdine.example.luxdine.domain.catalog.dto.response.BundleResponse;
import luxdine.example.luxdine.service.catalog.BundleService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/bundles")
@RequiredArgsConstructor
@Slf4j
public class BundleAdminApiController {

    private final BundleService bundleService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllBundles() {
        log.info("API: GET /api/admin/bundles - Fetching all bundles for admin");
        
        List<BundleResponse> bundles = bundleService.getAllBundlesForAdmin();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Tải danh sách combo thành công");
        response.put("data", bundles);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getBundleById(@PathVariable Long id) {
        log.info("API: GET /api/admin/bundles/{} - Fetching bundle details", id);
        
        BundleDetailResponse bundle = bundleService.getBundleByIdForAdmin(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", bundle);
        
        return ResponseEntity.ok(response);
    }

   
    @GetMapping("/type/{type}")
    public ResponseEntity<Map<String, Object>> listByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        log.info("API: GET /api/admin/bundles/type/{}?includeInactive={}", type, includeInactive);
        var data = bundleService.getBundlesByTypeForAdmin(type, includeInactive);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", data);
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<Map<String, Object>> createBundle(
            @Valid @RequestPart("bundle") CreateBundleRequest dto,
            @RequestPart("imageFile") MultipartFile imageFile
    ) {
        log.info("API: POST /api/admin/bundles - Creating bundle with image: {}", dto.getName());

        try {
            BundleDetailResponse bundle = bundleService.createBundle(dto, imageFile);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tạo combo thành công");
            response.put("data", bundle);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception ex) {
            // Bắt lỗi chung (bao gồm cả lỗi upload, lỗi file...)
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateBundle(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBundleRequest request) {
        log.info("API: PUT /api/admin/bundles/{} - Updating bundle", id);
        
        BundleDetailResponse bundle = bundleService.updateBundle(id, request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cập nhật combo thành công");
        response.put("data", bundle);
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteBundle(@PathVariable Long id) {
        log.info("API: DELETE /api/admin/bundles/{} - Deleting bundle", id);
        
        bundleService.deleteBundle(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Xóa combo thành công");
        
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleBundleAvailability(
            @PathVariable Long id,
            @RequestParam boolean isActive) {
        log.info("API: PATCH /api/admin/bundles/{}/toggle - isActive={}", id, isActive);
        
        bundleService.toggleBundleAvailability(id, isActive);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", isActive ? "Kích hoạt combo thành công" : "Vô hiệu hóa combo thành công");
        
        return ResponseEntity.ok(response);
    }
}