package luxdine.example.luxdine.controller.api;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.table.dto.response.AreaResponse;
import luxdine.example.luxdine.domain.table.dto.response.FloorFeatureResponse;
import luxdine.example.luxdine.domain.table.dto.response.TableLayoutResponse;
import luxdine.example.luxdine.service.seating.AreasService;
import luxdine.example.luxdine.service.seating.FloorFeatureService;
import luxdine.example.luxdine.service.seating.TableLayoutService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for staff read-only access to area and layout data
 */
@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@RequestMapping("/api/staff")
public class StaffAreaLayoutReadController {

    final AreasService areasService;
    final TableLayoutService tableLayoutService;
    final FloorFeatureService floorFeatureService;

    // ========== AREA READ-ONLY ENDPOINTS ==========

    /**
     * Get all areas (read-only for staff)
     * @return List of area responses
     */
    @GetMapping("/areas")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<?> getAllAreas() {
        try {
            log.info("Fetching all areas");
            List<AreaResponse> areas = areasService.getAllAreas();
            return ResponseEntity.ok(areas);
        } catch (Exception e) {
            log.error("Error fetching areas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get areas by floor number (read-only for staff)
     * @param floorNumber Floor number to filter areas
     * @return List of area responses for the floor
     */
    @GetMapping("/areas/floor/{floorNumber}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<?> getAreasByFloor(@PathVariable Integer floorNumber) {
        try {
            log.info("Fetching areas for floor: {}", floorNumber);
            
            if (floorNumber == null || floorNumber <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid floor number"));
            }
            
            List<AreaResponse> areas = areasService.getAreasByFloor(floorNumber);
            return ResponseEntity.ok(areas);
        } catch (Exception e) {
            log.error("Error fetching areas for floor", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    // ========== LAYOUT READ-ONLY ENDPOINTS ==========

    /**
     * Get all layouts by area ID (read-only for staff)
     * @param areaId Area ID to filter layouts
     * @return List of table layout responses
     */
    @GetMapping("/layouts/area/{areaId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<?> getLayoutsByArea(@PathVariable Long areaId) {
        try {
            log.info("Fetching layouts for area ID: {}", areaId);
            
            if (areaId == null || areaId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid area ID"));
            }
            
            List<TableLayoutResponse> layouts = tableLayoutService.getLayoutsByAreaId(areaId);
            return ResponseEntity.ok()
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("Expires", "0")
                    .body(layouts);
        } catch (Exception e) {
            log.error("Error fetching layouts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    // ========== FEATURE READ-ONLY ENDPOINTS ==========

    /**
     * Get features by area ID (read-only for staff)
     * @param areaId Area ID to filter features
     * @return List of floor feature responses
     */
    @GetMapping("/features/area/{areaId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @ResponseBody
    public ResponseEntity<?> getFeaturesByArea(@PathVariable Long areaId) {
        try {
            log.info("Fetching features for area ID: {}", areaId);
            
            if (areaId == null || areaId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid area ID"));
            }
            
            List<FloorFeatureResponse> features = floorFeatureService.getByArea(areaId);
            return ResponseEntity.ok()
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("Expires", "0")
                    .body(features);
        } catch (Exception e) {
            log.error("Error fetching features", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }
}

