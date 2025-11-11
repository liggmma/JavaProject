package luxdine.example.luxdine.service.seating;

import luxdine.example.luxdine.domain.table.dto.request.FloorFeatureRequest;
import luxdine.example.luxdine.domain.table.dto.response.FloorFeatureResponse;
import luxdine.example.luxdine.domain.table.entity.Areas;
import luxdine.example.luxdine.domain.table.entity.FloorFeature;
import luxdine.example.luxdine.domain.table.enums.FeatureType;
import luxdine.example.luxdine.domain.table.repository.AreasRepository;
import luxdine.example.luxdine.domain.table.repository.FloorFeatureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FloorFeatureService {

    private final FloorFeatureRepository featureRepository;
    private final AreasRepository areasRepository;

    public List<FloorFeatureResponse> getByArea(Long areaId) {
        return featureRepository.findByArea_Id(areaId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public FloorFeatureResponse create(FloorFeatureRequest req) {
        Areas area = areasRepository.findById(req.getAreaId())
                .orElseThrow(() -> new RuntimeException("Area not found: " + req.getAreaId()));

        FloorFeature entity = FloorFeature.builder()
                .type(FeatureType.valueOf(req.getType()))
                .label(req.getLabel())
                .positionX(n(req.getPositionX()))
                .positionY(n(req.getPositionY()))
                .width(n(req.getWidth()))
                .height(n(req.getHeight()))
                .rotationAngle(req.getRotationAngle() != null ? req.getRotationAngle() : 0)
                .area(area)
                .build();
        return toResponse(featureRepository.save(entity));
    }

    @Transactional
    public FloorFeatureResponse update(Long id, FloorFeatureRequest req) {
        FloorFeature entity = featureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feature not found: " + id));
        if (req.getType() != null) entity.setType(FeatureType.valueOf(req.getType()));
        entity.setLabel(req.getLabel());
        if (req.getPositionX() != null) entity.setPositionX(req.getPositionX());
        if (req.getPositionY() != null) entity.setPositionY(req.getPositionY());
        if (req.getWidth() != null) entity.setWidth(req.getWidth());
        if (req.getHeight() != null) entity.setHeight(req.getHeight());
        if (req.getRotationAngle() != null) entity.setRotationAngle(req.getRotationAngle());
        return toResponse(featureRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        featureRepository.deleteById(id);
    }

    /**
     * Batch save features (create or update)
     * @param areaId Area ID
     * @param requests List of feature requests
     * @return Number of successfully saved features
     */
    @Transactional
    public int batchSave(Long areaId, List<FloorFeatureRequest> requests) {
        log.info("Batch saving {} features for area ID: {}", requests.size(), areaId);
        
        Areas area = areasRepository.findById(areaId)
                .orElseThrow(() -> new RuntimeException("Area not found: " + areaId));
        
        int successCount = 0;
        for (FloorFeatureRequest req : requests) {
            try {
                // Auto-generate label if not provided
                if (req.getLabel() == null || req.getLabel().trim().isEmpty()) {
                    String generatedLabel = generateUniqueLabel(areaId, req.getType());
                    req.setLabel(generatedLabel);
                }
                
                if (req.getId() != null && req.getId() > 0) {
                    // Update existing
                    update(req.getId(), req);
                } else {
                    // Create new
                    req.setAreaId(areaId);
                    create(req);
                }
                successCount++;
            } catch (Exception e) {
                log.error("Error saving feature in batch: {}", e.getMessage());
                // Continue with next item
            }
        }
        
        log.info("Successfully saved {}/{} features", successCount, requests.size());
        return successCount;
    }

    /**
     * Generate unique feature label for an area
     * @param areaId Area ID
     * @param type Feature type
     * @return Unique label like "DOOR 1", "WINDOW 1", etc.
     */
    private String generateUniqueLabel(Long areaId, String type) {
        List<FloorFeature> existing = featureRepository.findByArea_Id(areaId);
        java.util.Set<Integer> usedNumbers = existing.stream()
                .filter(f -> f.getType() != null && f.getType().name().equals(type))
                .map(f -> {
                    String label = f.getLabel();
                    if (label != null && label.toUpperCase().startsWith(type)) {
                        String numStr = label.substring(type.length()).trim();
                        try {
                            return Integer.parseInt(numStr);
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
        
        return type + " " + nextNum;
    }

    private FloorFeatureResponse toResponse(FloorFeature f) {
        return FloorFeatureResponse.builder()
                .id(f.getId())
                .areaId(f.getArea() != null ? f.getArea().getId() : null)
                .type(f.getType() != null ? f.getType().name() : null)
                .label(f.getLabel())
                .positionX(f.getPositionX())
                .positionY(f.getPositionY())
                .width(f.getWidth())
                .height(f.getHeight())
                .rotationAngle(f.getRotationAngle())
                .build();
    }

    private double n(Double v) { return v != null ? v : 0d; }
}


