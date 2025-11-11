package luxdine.example.luxdine.service.seating;

import luxdine.example.luxdine.domain.table.dto.request.TableLayoutRequest;
import luxdine.example.luxdine.domain.table.dto.response.TableLayoutResponse;
import luxdine.example.luxdine.domain.table.entity.Areas;
import luxdine.example.luxdine.domain.table.entity.TableLayout;
import luxdine.example.luxdine.domain.table.entity.Tables;
import luxdine.example.luxdine.domain.table.enums.TableStatus;
import luxdine.example.luxdine.domain.table.repository.AreasRepository;
import luxdine.example.luxdine.domain.table.repository.TableLayoutRepository;
import luxdine.example.luxdine.domain.table.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TableLayoutService {

    private final TableLayoutRepository tableLayoutRepository;
    private final AreasRepository areasRepository;
    private final TableRepository tableRepository;

    /**
     * Get all layouts by area ID
     * @param areaId Area ID
     * @return List of table layout responses
     */
    public List<TableLayoutResponse> getLayoutsByAreaId(Long areaId) {
        log.info("Fetching layouts for area ID: {}", areaId);
        
        List<TableLayout> layouts = tableLayoutRepository.findByAreaId(areaId);
        log.info("Found {} layouts for area {}", layouts.size(), areaId);
        
        return layouts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Create a new table layout
     * @param request Layout creation request
     * @return Created layout response
     */
    @Transactional
    public TableLayoutResponse createLayout(TableLayoutRequest request) {
        log.info("Creating new layout for area ID: {}", request.getAreaId());
        
        Areas area = areasRepository.findById(request.getAreaId())
                .orElseThrow(() -> new RuntimeException("Area not found with ID: " + request.getAreaId()));
        
        // Validate unique table name in area (case-insensitive)
        if (request.getTableName() != null && !request.getTableName().trim().isEmpty()) {
            boolean exists = tableLayoutRepository.existsByAreaIdAndTableNameIgnoreCase(area.getId(), request.getTableName().trim());
            if (exists) {
                throw new RuntimeException("Tên bàn đã tồn tại trong khu vực này");
            }
        }
        
        TableLayout layout = TableLayout.builder()
                .positionX(request.getPositionX())
                .positionY(request.getPositionY())
                .width(request.getWidth())
                .height(request.getHeight())
                .rotationAngle(request.getRotationAngle() != null ? request.getRotationAngle() : 0)
                .shape(request.getShape())
                .tableName(request.getTableName())
                .capacity(request.getCapacity() != null ? request.getCapacity() : 4)
                .area(area)
                .build();
        
        TableLayout saved = tableLayoutRepository.save(layout);
        log.info("Successfully created layout with ID: {}", saved.getId());
        
        // Ensure a corresponding Tables entity exists
        Tables table = Tables.builder()
                .tableName(saved.getTableName())
                .tableType(saved.getShape())
                .capacity(saved.getCapacity() != null ? saved.getCapacity() : 4)
                .depositAmount(0)
                .status(TableStatus.AVAILABLE)
                .tableLayout(saved)
                .area(area)
                .build();
        tableRepository.save(table);
        
        return convertToResponse(saved);
    }

    /**
     * Update an existing table layout
     * @param layoutId Layout ID to update
     * @param request Updated layout data
     * @return Updated layout response
     */
    @Transactional
    public TableLayoutResponse updateLayout(Long layoutId, TableLayoutRequest request) {
        log.info("Updating layout with ID: {}", layoutId);
        
        TableLayout layout = tableLayoutRepository.findById(layoutId)
                .orElseThrow(() -> new RuntimeException("Layout not found with ID: " + layoutId));
        
        // Validate unique table name per area on update
        if (request.getTableName() != null && !request.getTableName().trim().isEmpty()) {
            Long areaId = layout.getArea() != null ? layout.getArea().getId() : null;
            if (areaId != null) {
                boolean exists = tableLayoutRepository.existsByAreaIdAndTableNameIgnoreCaseAndIdNot(areaId, request.getTableName().trim(), layoutId);
                if (exists) {
                    throw new RuntimeException("Tên bàn đã tồn tại trong khu vực này");
                }
            }
        }
        
        layout.setPositionX(request.getPositionX());
        layout.setPositionY(request.getPositionY());
        layout.setWidth(request.getWidth());
        layout.setHeight(request.getHeight());
        layout.setRotationAngle(request.getRotationAngle() != null ? request.getRotationAngle() : 0);
        layout.setShape(request.getShape());
        layout.setTableName(request.getTableName());
        layout.setCapacity(request.getCapacity() != null ? request.getCapacity() : 4);
        
        TableLayout saved = tableLayoutRepository.save(layout);
        log.info("Successfully updated layout with ID: {}", saved.getId());
        
        // Sync corresponding Tables entity
        tableRepository.findByTableLayoutId(saved.getId())
                .ifPresentOrElse(existing -> {
                    existing.setTableName(saved.getTableName());
                    existing.setTableType(saved.getShape());
                    existing.setCapacity(saved.getCapacity() != null ? saved.getCapacity() : existing.getCapacity());
                    if (existing.getArea() == null && saved.getArea() != null) {
                        existing.setArea(saved.getArea());
                    }
                    tableRepository.save(existing);
                }, () -> {
                    Tables t = Tables.builder()
                            .tableName(saved.getTableName())
                            .tableType(saved.getShape())
                            .capacity(saved.getCapacity() != null ? saved.getCapacity() : 4)
                            .depositAmount(0)
                            .status(TableStatus.AVAILABLE)
                            .tableLayout(saved)
                            .area(saved.getArea())
                            .build();
                    tableRepository.save(t);
                });
        
        return convertToResponse(saved);
    }

    /**
     * Delete a table layout
     * @param layoutId Layout ID to delete
     */
    @Transactional
    public void deleteLayout(Long layoutId) {
        log.info("Deleting layout with ID: {}", layoutId);
        
        if (!tableLayoutRepository.existsById(layoutId)) {
            throw new RuntimeException("Layout not found with ID: " + layoutId);
        }
        
        // Delete or detach corresponding Tables entity
        tableRepository.findByTableLayoutId(layoutId)
                .ifPresent(tableRepository::delete);
        
        tableLayoutRepository.deleteById(layoutId);
        log.info("Successfully deleted layout with ID: {}", layoutId);
    }

    /**
     * Batch save table layouts (create or update)
     * @param areaId Area ID
     * @param requests List of table layout requests
     * @return Number of successfully saved layouts
     */
    @Transactional
    public int batchSaveLayouts(Long areaId, List<TableLayoutRequest> requests) {
        log.info("Batch saving {} layouts for area ID: {}", requests.size(), areaId);
        
        Areas area = areasRepository.findById(areaId)
                .orElseThrow(() -> new RuntimeException("Area not found with ID: " + areaId));
        
        int successCount = 0;
        for (TableLayoutRequest request : requests) {
            try {
                // Check if this is an update (has temp_ prefix means new, otherwise check by tableName)
                if (request.getId() != null && request.getId() > 0) {
                    // Update existing
                    updateLayout(request.getId(), request);
                } else {
                    // Create new - auto-generate table name if not provided
                    if (request.getTableName() == null || request.getTableName().trim().isEmpty()) {
                        String generatedName = generateUniqueTableName(areaId);
                        request.setTableName(generatedName);
                    }
                    createLayout(request);
                }
                successCount++;
            } catch (Exception e) {
                log.error("Error saving layout in batch: {}", e.getMessage());
                // Continue with next item
            }
        }
        
        log.info("Successfully saved {}/{} layouts", successCount, requests.size());
        return successCount;
    }

    /**
     * Generate unique table name for an area
     * @param areaId Area ID
     * @return Unique table name like "Bàn 1", "Bàn 2", etc.
     */
    private String generateUniqueTableName(Long areaId) {
        List<TableLayout> existing = tableLayoutRepository.findByAreaId(areaId);
        java.util.Set<Integer> usedNumbers = existing.stream()
                .map(layout -> {
                    String name = layout.getTableName();
                    if (name != null && name.matches("(?i)Bàn\\s+(\\d+)")) {
                        try {
                            return Integer.parseInt(name.replaceAll("(?i)Bàn\\s+", "").trim());
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    }
                    return null;
                })
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        
        int nextNum = 1;
        while (usedNumbers.contains(nextNum)) {
            nextNum++;
        }
        
        return "Bàn " + nextNum;
    }

    /**
     * Convert entity to DTO
     * @param layout Entity to convert
     * @return TableLayoutResponse DTO
     */
    private TableLayoutResponse convertToResponse(TableLayout layout) {
        return TableLayoutResponse.builder()
                .id(layout.getId())
                .areaId(layout.getArea() != null ? layout.getArea().getId() : null)
                .positionX(layout.getPositionX())
                .positionY(layout.getPositionY())
                .width(layout.getWidth())
                .height(layout.getHeight())
                .rotationAngle(layout.getRotationAngle())
                .shape(layout.getShape())
                .tableName(layout.getTableName())
                .capacity(layout.getCapacity())
                .build();
    }
}


