package luxdine.example.luxdine.controller.api;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.table.dto.request.FloorFeatureRequest;
import luxdine.example.luxdine.domain.table.dto.response.FloorFeatureResponse;
import luxdine.example.luxdine.service.seating.FloorFeatureService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@RequestMapping("/api/admin/features")
public class FloorFeatureAPI {

    final FloorFeatureService featureService;

    @GetMapping("/area/{areaId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public List<FloorFeatureResponse> getByArea(@PathVariable Long areaId) {
        return featureService.getByArea(areaId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> create(@Valid @RequestBody FloorFeatureRequest request) {
        try {
            var res = featureService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (Exception e) {
            log.error("Create feature error", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody FloorFeatureRequest request) {
        try {
            var res = featureService.update(id, request);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            log.error("Update feature error", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            featureService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        } catch (Exception e) {
            log.error("Delete feature error", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Batch save features for an area (create or update)
     * @param areaId Area ID
     * @param requests List of feature requests
     * @return Success message with count
     */
    @PostMapping("/batch/{areaId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> batchSave(@PathVariable Long areaId,
                                       @RequestBody List<FloorFeatureRequest> requests) {
        try {
            log.info("Batch saving {} features for area ID: {}", requests.size(), areaId);
            int successCount = featureService.batchSave(areaId, requests);
            return ResponseEntity.ok(Map.of(
                "message", "Batch save completed",
                "successCount", successCount,
                "totalCount", requests.size()
            ));
        } catch (Exception e) {
            log.error("Batch save feature error", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}


