package luxdine.example.luxdine.service.seating;

import luxdine.example.luxdine.domain.table.dto.request.AreaRequest;
import luxdine.example.luxdine.domain.table.dto.response.AreaResponse;
import luxdine.example.luxdine.domain.table.entity.Areas;
import luxdine.example.luxdine.domain.table.repository.AreasRepository;
import luxdine.example.luxdine.domain.table.repository.TableLayoutRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AreasService {

    private final AreasRepository areasRepository;
    private final TableLayoutRepository tableLayoutRepository;

    /**
     * Get areas by floor number
     * @param floorNumber Floor number to filter areas
     * @return List of area responses for the floor
     */
    public List<Integer> getAvailableFloors() {
        log.info("Fetching available floors");
        
        List<Integer> floors = areasRepository.findDistinctFloors();
        log.info("Found {} floors: {}", floors.size(), floors);
        
        return floors.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<AreaResponse> getAreasByFloor(Integer floorNumber) {
        log.info("Fetching areas for floor: {}", floorNumber);
        
        List<Areas> areas = areasRepository.findByFloor(floorNumber);
        log.info("Found {} areas for floor {}", areas.size(), floorNumber);
        
        return areas.stream()
                .map(this::convertToAreaResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all areas from database
     * @return List of area responses
     */
    public List<AreaResponse> getAllAreas() {
        log.info("Fetching all areas from database");
        
        List<Areas> areas = areasRepository.findAll();
        log.info("Found {} areas in database", areas.size());
        
        return areas.stream()
                .map(this::convertToAreaResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all areas grouped by floor (sorted and organized)
     * @return Map of floor number to sorted list of area responses
     */
    public Map<Integer, List<AreaResponse>> getAreasGroupedByFloor() {
        log.info("Fetching areas grouped by floor");
        
        List<AreaResponse> allAreas = getAllAreas();
        
        Map<Integer, List<AreaResponse>> grouped = allAreas.stream()
                .collect(Collectors.groupingBy(
                    area -> area.getFloor() != null ? area.getFloor() : 1,
                    Collectors.toList()
                ));
        
        // Sort areas within each floor by name
        grouped.values().forEach(areas -> 
            areas.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
        );
        
        log.info("Grouped {} areas into {} floors", allAreas.size(), grouped.size());
        return grouped;
    }

    /**
     * Create a new area
     * @param request Area creation request
     * @return Created area response
     */
    @Transactional
    public AreaResponse createArea(AreaRequest request) {
        log.info("Creating new area: {}", request.getName());
        
        Areas area = Areas.builder()
                .name(request.getName())
                .floor(request.getFloor() != null ? request.getFloor() : 1)
                .description(request.getDescription())
                .positionX(request.getPositionX())
                .positionY(request.getPositionY())
                .width(request.getWidth())
                .height(request.getHeight())
                .build();
        
        Areas saved = areasRepository.save(area);
        log.info("Successfully created area with ID: {}", saved.getId());
        
        return convertToAreaResponse(saved);
    }

    /**
     * Update an existing area
     * @param areaId Area ID to update
     * @param request Updated area data
     * @return Updated area response
     */
    @Transactional
    public AreaResponse updateArea(Long areaId, AreaRequest request) {
        log.info("Updating area with ID: {}", areaId);

        Areas area = areasRepository.findById(areaId)
                .orElseThrow(() -> new RuntimeException("Area not found with ID: " + areaId));

        area.setName(request.getName());
        area.setFloor(request.getFloor() != null ? request.getFloor() : 1);
        area.setDescription(request.getDescription());
        area.setPositionX(request.getPositionX());
        area.setPositionY(request.getPositionY());
        area.setWidth(request.getWidth());
        area.setHeight(request.getHeight());

        Areas saved = areasRepository.save(area);
        log.info("Successfully updated area with ID: {}", saved.getId());

        return convertToAreaResponse(saved);
    }

    /**
     * Delete an area and all its layouts
     * @param areaId Area ID to delete
     */
    @Transactional
    public void deleteArea(Long areaId) {
        log.info("Deleting area with ID: {}", areaId);

        Areas area = areasRepository.findById(areaId)
                .orElseThrow(() -> new RuntimeException("Area not found with ID: " + areaId));

        // Delete all layouts associated with this area
        List<luxdine.example.luxdine.domain.table.entity.TableLayout> layouts =
                tableLayoutRepository.findByAreaId(areaId);
        tableLayoutRepository.deleteAll(layouts);
        log.info("Deleted {} layouts for area {}", layouts.size(), areaId);

        // Delete the area
        areasRepository.delete(area);
        log.info("Successfully deleted area with ID: {}", areaId);
    }

    /**
     * Save area layout positions for a specific floor
     * @param floorNumber Floor number
     * @param areaLayouts List of area layout data
     */
    @Transactional
    public void saveAreaLayoutForFloor(Integer floorNumber, List<AreaRequest> areaLayouts) {
        log.info("Saving area layout for floor: {}", floorNumber);
        
        for (AreaRequest layout : areaLayouts) {
            if (layout.getName() != null) {
                // Find area by name and floor
                List<Areas> areas = areasRepository.findAll().stream()
                        .filter(area -> area.getName().equals(layout.getName()) && 
                                       area.getFloor() == floorNumber)
                        .collect(Collectors.toList());
                
                if (!areas.isEmpty()) {
                    Areas area = areas.get(0);
                    area.setPositionX(layout.getPositionX());
                    area.setPositionY(layout.getPositionY());
                    area.setWidth(layout.getWidth());
                    area.setHeight(layout.getHeight());
                    areasRepository.save(area);
                } else {
                    log.warn("No area found with name '{}' on floor {}", layout.getName(), floorNumber);
                }
                }
            }
    }

    /**
     * Convert entity to DTO
     * @param area Entity to convert
     * @return AreaResponse DTO
     */
    private AreaResponse convertToAreaResponse(Areas area) {
        // Count tables using TableLayoutRepository for accurate count
        int tableCount = tableLayoutRepository.findByAreaId(area.getId()).size();
        
        return AreaResponse.builder()
                .id(area.getId())
                .name(area.getName())
                .floor(area.getFloor())
                .description(area.getDescription())
                .tableCount(tableCount)
                .positionX(area.getPositionX())
                .positionY(area.getPositionY())
                .width(area.getWidth())
                .height(area.getHeight())
                .build();
    }
}
