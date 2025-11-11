package luxdine.example.luxdine.controller.api;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.table.dto.request.TableStatusUpdateRequest;
import luxdine.example.luxdine.domain.table.dto.response.TableResponse;
import luxdine.example.luxdine.service.seating.TableService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@RequestMapping("/api/tables")
public class FloorPlanAPI {

    final TableService tableService;

    /**
     * Get all tables with area filter for AJAX requests
     * @param area Optional area filter
     * @return List of table responses
     */
    @GetMapping
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public List<TableResponse> getAllTables(@RequestParam(required = false) String area) {
        try {
            return tableService.getAllTables(area);
        } catch (RuntimeException e) {
            log.error("Error fetching tables for area {}: {}", area, e.getMessage());
            throw new RuntimeException("Failed to fetch tables", e);
        } catch (Exception e) {
            log.error("Unexpected error fetching tables for area {}: {}", area, e.getMessage());
            throw new RuntimeException("An unexpected error occurred", e);
        }
    }

    /**
     * Get table details for AJAX requests
     * @param id Table ID
     * @return Table response
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public TableResponse getTableDetails(@PathVariable Long id) {
        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("Invalid table ID: " + id);
            }
            
            return tableService.getTableById(id);
        } catch (IllegalArgumentException e) {
            log.error("Invalid table ID: {}", id);
            throw e;
        } catch (RuntimeException e) {
            log.error("Error fetching table details for ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to fetch table details", e);
        } catch (Exception e) {
            log.error("Unexpected error fetching table details for ID {}: {}", id, e.getMessage());
            throw new RuntimeException("An unexpected error occurred", e);
        }
    }

    /**
     * Update table status
     * @param id Table ID
     * @param updateRequest Status update request
     * @return Updated table response
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public TableResponse updateTableStatus(@PathVariable Long id, @Valid @RequestBody TableStatusUpdateRequest updateRequest) {
        try {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("Invalid table ID: " + id);
            }
            
            if (updateRequest == null || updateRequest.getStatus() == null) {
                throw new IllegalArgumentException("Status update request cannot be null");
            }
            
            return tableService.updateTableStatus(id, updateRequest);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for table ID {}: {}", id, e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            log.error("Error updating table status for ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to update table status", e);
        } catch (Exception e) {
            log.error("Unexpected error updating table status for ID {}: {}", id, e.getMessage());
            throw new RuntimeException("An unexpected error occurred", e);
        }
    }
}

