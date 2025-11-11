package luxdine.example.luxdine.controller.api;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.table.dto.response.AreaResponse;
import luxdine.example.luxdine.domain.table.dto.response.FloorFeatureResponse;
import luxdine.example.luxdine.domain.table.dto.response.TableLayoutResponse;
import luxdine.example.luxdine.domain.table.dto.response.TableResponse;
import luxdine.example.luxdine.service.seating.AreasService;
import luxdine.example.luxdine.service.seating.FloorFeatureService;
import luxdine.example.luxdine.service.seating.TableLayoutService;
import luxdine.example.luxdine.service.seating.TableService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for customer public read-only access to area layout data
 * Allows customers to view restaurant layouts and table information
 */
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@RequestMapping("/api/public")
public class CustomerAreaLayoutAPI {

    final AreasService areasService;
    final TableLayoutService tableLayoutService;
    final FloorFeatureService floorFeatureService;
    final TableService tableService;

    /**
     * Get available floors (public read-only for customers)
     * @return List of available floor numbers
     */
    @GetMapping("/floors")
    public ResponseEntity<?> getAvailableFloors() {
        try {
            log.info("Fetching available floors (public)");
            List<Integer> floors = areasService.getAvailableFloors();
            return ResponseEntity.ok(floors);
        } catch (Exception e) {
            log.error("Error fetching available floors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Get areas by floor number (public read-only for customers)
     * @param floorNumber Floor number to filter areas
     * @return List of area responses for the floor
     */
    @GetMapping("/areas/floor/{floorNumber}")
    public ResponseEntity<?> getAreasByFloor(@PathVariable Integer floorNumber) {
        try {
            log.info("Fetching areas for floor: {} (public)", floorNumber);
            
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

    /**
     * Get layouts by area ID (public read-only for customers)
     * @param areaId Area ID to filter layouts
     * @return List of table layout responses
     */
    @GetMapping("/layouts/area/{areaId}")
    public ResponseEntity<?> getLayoutsByArea(@PathVariable Long areaId) {
        try {
            log.info("Fetching layouts for area ID: {} (public)", areaId);
            
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
        } catch (RuntimeException e) {
            log.error("Error fetching layouts", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching layouts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Get features by area ID (public read-only for customers)
     * @param areaId Area ID to filter features
     * @return List of floor feature responses
     */
    @GetMapping("/features/area/{areaId}")
    public ResponseEntity<?> getFeaturesByArea(@PathVariable Long areaId) {
        try {
            log.info("Fetching features for area ID: {} (public)", areaId);
            
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
        } catch (RuntimeException e) {
            log.error("Error fetching features", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching features", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Get table details by ID (public read-only for customers)
     * @param id Table ID
     * @return Table response
     */
    @GetMapping("/tables/{id}")
    public ResponseEntity<?> getTableDetails(@PathVariable Long id) {
        try {
            log.info("Fetching table details for ID: {} (public)", id);
            
            if (id == null || id <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid table ID"));
            }
            
            TableResponse table = tableService.getTableById(id);
            return ResponseEntity.ok(table);
        } catch (IllegalArgumentException e) {
            log.error("Invalid table ID: {}", id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Error fetching table details", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching table details", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }
}

