package luxdine.example.luxdine.controller.api;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.table.dto.request.AreaRequest;
import luxdine.example.luxdine.domain.table.dto.request.TableLayoutRequest;
import luxdine.example.luxdine.domain.table.dto.response.AreaResponse;
import luxdine.example.luxdine.domain.table.dto.response.TableLayoutResponse;
import luxdine.example.luxdine.service.seating.AreasService;
import luxdine.example.luxdine.service.seating.TableLayoutService;
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
@RequestMapping("/api/admin")
public class AdminAreaLayoutController {

    final AreasService areasService;
    final TableLayoutService tableLayoutService;

    // ========== AREA ENDPOINTS ==========

    /**
     * Get all areas
     * @return List of area responses
     */
    @GetMapping("/areas")
    @PreAuthorize("hasRole('ADMIN')")
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
     * Create a new area
     * @param request Area creation request
     * @return Created area response
     */
    @PostMapping("/areas")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> createArea(@Valid @RequestBody AreaRequest request) {
        try {
            log.info("Creating new area: {}", request.getName());
            
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Area name is required"));
            }
            
            AreaResponse created = areasService.createArea(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            log.error("Error creating area", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating area", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Update an area
     * @param id Area ID to update
     * @param request Updated area data
     * @return Updated area response
     */
    @PutMapping("/areas/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> updateArea(@PathVariable Long id, 
                                      @Valid @RequestBody AreaRequest request) {
        try {
            log.info("Updating area with ID: {}", id);
            
            if (id == null || id <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid area ID"));
            }
            
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Area name is required"));
            }
            
            AreaResponse updated = areasService.updateArea(id, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("Error updating area", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating area", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Delete an area
     * @param id Area ID to delete
     * @return Success message
     */
    @DeleteMapping("/areas/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> deleteArea(@PathVariable Long id) {
        try {
            log.info("Deleting area with ID: {}", id);
            
            if (id == null || id <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid area ID"));
            }
            
            areasService.deleteArea(id);
            return ResponseEntity.ok(Map.of("message", "Area deleted successfully"));
        } catch (RuntimeException e) {
            log.error("Error deleting area", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting area", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Get all available floor numbers
     * @return List of floor numbers that have areas
     */
    @GetMapping("/areas/floors")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> getAvailableFloors() {
        try {
            log.info("Fetching available floors");
            List<Integer> floors = areasService.getAvailableFloors();
            return ResponseEntity.ok(floors);
        } catch (Exception e) {
            log.error("Error fetching available floors", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch available floors"));
        }
    }

    /**
     * Get areas by floor number
     * @param floorNumber Floor number to filter areas
     * @return List of area responses for the floor
     */
    @GetMapping("/areas/floor/{floorNumber}")
    @PreAuthorize("hasRole('ADMIN')")
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
        } catch (RuntimeException e) {
            log.error("Error fetching areas for floor", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching areas for floor", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Get all areas grouped by floor (already sorted and grouped)
     * @return Map of floor number to list of area responses
     */
    @GetMapping("/areas/grouped-by-floor")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> getAreasGroupedByFloor() {
        try {
            log.info("Fetching areas grouped by floor");
            Map<Integer, List<AreaResponse>> grouped = areasService.getAreasGroupedByFloor();
            return ResponseEntity.ok(grouped);
        } catch (Exception e) {
            log.error("Error fetching areas grouped by floor", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch grouped areas"));
        }
    }

    /**
     * Save area layout positions for a floor
     * @param floorNumber Floor number to save layouts for
     * @param request List of area layout data
     * @return Success message
     */
    @PostMapping("/areas/layout/floor/{floorNumber}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> saveAreaLayoutForFloor(@PathVariable Integer floorNumber,
                                                   @RequestBody List<AreaRequest> request) {
        try {
            log.info("Saving area layout for floor: {}", floorNumber);
            
            if (floorNumber == null || floorNumber <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid floor number"));
            }
            
            areasService.saveAreaLayoutForFloor(floorNumber, request);
            return ResponseEntity.ok(Map.of("message", "Area layout saved successfully"));
        } catch (RuntimeException e) {
            log.error("Error saving area layout", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error saving area layout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    // ========== LAYOUT ENDPOINTS ==========

    /**
     * Get all layouts by area ID
     * @param areaId Area ID to filter layouts
     * @return List of table layout responses
     */
    @GetMapping("/layouts/area/{areaId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> getLayoutsByArea(@PathVariable Long areaId) {
        try {
            log.info("Fetching layouts for area ID: {}", areaId);
            
            if (areaId == null || areaId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid area ID"));
            }
            
            List<TableLayoutResponse> layouts = tableLayoutService.getLayoutsByAreaId(areaId);
            return ResponseEntity.ok(layouts);
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
     * Create a new table layout
     * @param request Layout creation request
     * @return Created layout response
     */
    @PostMapping("/layouts")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> createLayout(@Valid @RequestBody TableLayoutRequest request) {
        try {
            log.info("Creating new layout for area ID: {}", request.getAreaId());
            
            if (request.getAreaId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Area ID is required"));
            }
            
            TableLayoutResponse created = tableLayoutService.createLayout(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            log.error("Error creating layout", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating layout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Update a table layout
     * @param id Layout ID to update
     * @param request Updated layout data
     * @return Updated layout response
     */
    @PutMapping("/layouts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> updateLayout(@PathVariable Long id, 
                                         @Valid @RequestBody TableLayoutRequest request) {
        try {
            log.info("Updating layout with ID: {}", id);
            
            if (id == null || id <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid layout ID"));
            }
            
            TableLayoutResponse updated = tableLayoutService.updateLayout(id, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("Error updating layout", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating layout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Delete a table layout
     * @param id Layout ID to delete
     * @return Success message
     */
    @DeleteMapping("/layouts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> deleteLayout(@PathVariable Long id) {
        try {
            log.info("Deleting layout with ID: {}", id);
            
            if (id == null || id <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid layout ID"));
            }
            
            tableLayoutService.deleteLayout(id);
            return ResponseEntity.ok(Map.of("message", "Layout deleted successfully"));
        } catch (RuntimeException e) {
            log.error("Error deleting layout", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting layout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    /**
     * Batch save table layouts for an area (create or update)
     * @param areaId Area ID
     * @param requests List of table layout requests
     * @return Success message with count
     */
    @PostMapping("/layouts/batch/{areaId}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<?> batchSaveLayouts(@PathVariable Long areaId,
                                              @RequestBody List<TableLayoutRequest> requests) {
        try {
            log.info("Batch saving {} layouts for area ID: {}", requests.size(), areaId);
            
            if (areaId == null || areaId <= 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Invalid area ID"));
            }
            
            int successCount = tableLayoutService.batchSaveLayouts(areaId, requests);
            return ResponseEntity.ok(Map.of(
                "message", "Batch save completed",
                "successCount", successCount,
                "totalCount", requests.size()
            ));
        } catch (RuntimeException e) {
            log.error("Error batch saving layouts", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error batch saving layouts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }
}


