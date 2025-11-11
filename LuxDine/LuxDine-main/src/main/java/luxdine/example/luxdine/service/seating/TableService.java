package luxdine.example.luxdine.service.seating;

import luxdine.example.luxdine.common.constants.BusinessConstants;
import luxdine.example.luxdine.domain.table.dto.request.TableStatusUpdateRequest;
import luxdine.example.luxdine.domain.table.dto.response.TableResponse;
import luxdine.example.luxdine.domain.reservation.entity.Reservations;
import luxdine.example.luxdine.domain.table.entity.Tables;
import luxdine.example.luxdine.domain.table.enums.TableStatus;
import luxdine.example.luxdine.domain.reservation.enums.ReservationStatus;
import luxdine.example.luxdine.domain.reservation.repository.ReservationRepository;
import luxdine.example.luxdine.domain.table.repository.TableRepository;
import luxdine.example.luxdine.common.util.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TableService {

    private final TableRepository tableRepository;
    private final ReservationRepository reservationRepository;

    public Tables findById(Long id){
        return tableRepository.findById(id).orElse(null);
    }

    public List<Tables> findByStatus (TableStatus status){
        return tableRepository.findByStatus(status);
    }

    /**
     * Get all tables with optional area filter
     * @param areaName Optional area filter
     * @return List of table responses
     */
    @Transactional(readOnly = true)
    public List<TableResponse> getAllTables(String areaName) {
        log.info("Fetching tables with area filter: {}", areaName);
        
        List<Tables> tables;
        if (areaName != null && !areaName.isEmpty() && !"All Areas".equals(areaName)) {
            log.info("Filtering tables by area: {}", areaName);
            tables = tableRepository.findByAreaFilter(areaName);
        } else {
            log.info("Fetching all tables from database");
            tables = tableRepository.findAll();
        }
        
        log.info("Found {} tables in database", tables.size());
        
        // FIXED: Batch load reservations to avoid N+1 queries
        List<Long> tableIds = tables.stream().map(Tables::getId).collect(Collectors.toList());
        Map<Long, List<Reservations>> reservationsByTableId = loadReservationsForTables(tableIds);
        
        List<TableResponse> responses = tables.stream()
                .map(table -> convertToTableResponseOptimized(table, reservationsByTableId.get(table.getId())))
                .collect(Collectors.toList());
        
        log.info("Converted {} tables to response DTOs", responses.size());
        return responses;
    }

    /**
     * Get table by ID
     * @param id Table ID
     * @return Table response
     */
    @Transactional(readOnly = true)
    public TableResponse getTableById(Long id) {
        log.debug("Fetching table by ID: {}", id);
        
        Tables table = tableRepository.findByIdWithArea(id)
                .orElseThrow(() -> new RuntimeException("Table not found with id: " + id));
        
        return convertToTableResponse(table);
    }

    /**
     * Update table status
     * @param id Table ID
     * @param updateRequest Status update request
     * @return Updated table response
     */
    @Transactional
    public TableResponse updateTableStatus(Long id, TableStatusUpdateRequest updateRequest) {
        log.info("Starting table status update - ID: {}, Status: {}", id, updateRequest.getStatus());
        
        try {
            // Validate status using utility method
            String status = updateRequest.getStatus();
            ValidationUtils.validateStatus(status);
            
            // Validate status values using utility method
            if (!ValidationUtils.isValidTableStatus(status)) {
                String validStatuses = ValidationUtils.getValidTableStatuses();
                log.error("Invalid status '{}' for table {}. Valid statuses: {}", status, id, validStatuses);
                throw new IllegalArgumentException("Invalid status: " + status + ". Valid statuses are: " + validStatuses);
            }
            
            log.info("Fetching table with ID: {}", id);
            Tables table = tableRepository.findByIdWithArea(id)
                    .orElseThrow(() -> {
                        log.error("Table not found with id: {}", id);
                        return new RuntimeException(BusinessConstants.ERROR_TABLE_NOT_FOUND + id);
                    });
            
            TableStatus oldStatus = table.getStatus();
            log.info("Table {} current status: {}, updating to: {}", id, oldStatus, status);
            
            table.setStatus(TableStatus.valueOf(status));
            
            log.info("Saving table {} to database", id);
            Tables savedTable = tableRepository.save(table);
            log.info("Table status updated successfully: {} from {} to {}", savedTable.getId(), oldStatus, status);
            
            log.info("Converting table {} to response DTO", id);
            TableResponse response = convertToTableResponse(savedTable);
            log.info("Table status update completed successfully for table {}", id);
            return response;
            
        } catch (Exception e) {
            log.error("Error updating table status for ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update table status: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<Tables> findAvailableTable() {
        return tableRepository.findByStatusIn(
                List.of(TableStatus.AVAILABLE, TableStatus.RESERVED)
        );
    }

    /**
     * Get available tables for specific capacity and date/time
     * @param capacity Minimum capacity required
     * @param reservationDate Date and time of reservation
     * @return List of available tables that meet capacity requirements
     */
    @Transactional(readOnly = true)
    public List<TableResponse> getAvailableTablesForCapacity(Integer capacity, java.time.OffsetDateTime reservationDate) {
        log.info("Finding available tables for capacity: {}, date: {}", capacity, reservationDate);

        // Get all tables with sufficient capacity
        List<Tables> allTables = tableRepository.findAll();
        List<Tables> suitableTables = allTables.stream()
                .filter(table -> table.getCapacity() >= capacity)
                .filter(table -> TableStatus.AVAILABLE.equals(table.getStatus()) ||
                               TableStatus.RESERVED.equals(table.getStatus()))
                .collect(Collectors.toList());

        log.info("Found {} suitable tables with capacity >= {}", suitableTables.size(), capacity);

        // If reservationDate is provided, check for conflicts
        if (reservationDate != null) {
            // Define reservation window
            // Use configured duration from RestaurantConfigService (120 minutes = 2 hours)
            int reservationDurationMinutes = luxdine.example.luxdine.service.config.RestaurantConfigService.RESERVATION_DURATION_MINUTES;

            // Window: reservation start to end (no padding before, just duration after)
            java.time.OffsetDateTime windowStart = reservationDate;
            java.time.OffsetDateTime windowEnd = reservationDate.plusMinutes(reservationDurationMinutes);

            // Filter out tables with conflicting reservations
            suitableTables = suitableTables.stream()
                    .filter(table -> {
                        List<Reservations> conflicts = reservationRepository
                                .findConflictsByTableIdAndWindow(table.getId(), windowStart, windowEnd);
                        boolean available = conflicts.isEmpty();
                        if (!available) {
                            log.debug("Table {} has conflicting reservations", table.getTableName());
                        }
                        return available;
                    })
                    .collect(Collectors.toList());

            log.info("After checking conflicts, {} tables remain available", suitableTables.size());
        }

        // Convert to responses
        return suitableTables.stream()
                .map(this::convertToTableResponse)
                .collect(Collectors.toList());
    }
    

    /**
     * Get table status summary counts
     * @return Map of status counts
     */
    @Transactional(readOnly = true)
    public Map<TableStatus, Long> getTableStatusSummary() {
        log.debug("Fetching table status summary");
        
        Map<TableStatus, Long> summary = new HashMap<>();
        for (TableStatus status : TableStatus.values()) {
            summary.put(status, tableRepository.countByStatus(status));
        }
        return summary;
    }

    /**
     * Convert entity to DTO
     * @param table Entity to convert
     * @return TableResponse DTO
     */
    private TableResponse convertToTableResponse(Tables table) {
        log.debug("Converting table {} with status: {}", table.getId(), table.getStatus());
        
        TableResponse.TableResponseBuilder builder = buildBasicTableResponse(table);
        addReservationDetails(builder, table);
        
        return builder.build();
    }
    
    /**
     * Build basic table response without reservation details
     * @param table Table entity
     * @return TableResponse builder
     */
    private TableResponse.TableResponseBuilder buildBasicTableResponse(Tables table) {
        return TableResponse.builder()
                .id(table.getId())
                .tableName(table.getTableName())
                .tableType(table.getTableType())
                .depositAmount(table.getDepositAmount())
                .capacity(table.getCapacity())
                .status(String.valueOf(table.getStatus()))
                .areaName(table.getArea() != null ? table.getArea().getName() : BusinessConstants.UNKNOWN_VALUE)
                .areaId(table.getArea() != null ? table.getArea().getId() : null)
                .floor(table.getArea() != null ? table.getArea().getFloor() : null);
    }
    
    /**
     * Add reservation details to table response
     * @param builder TableResponse builder
     * @param table Table entity
     */
    private void addReservationDetails(TableResponse.TableResponseBuilder builder, Tables table) {
        try {
            List<Reservations> tableReservations = reservationRepository.findActiveReservationsByTableIdWithEagerLoading(table.getId());
            Reservations latestReservation = findLatestActiveReservation(tableReservations);
            
            if (latestReservation != null) {
                setReservationDetails(builder, latestReservation);
            } else {
                clearReservationDetails(builder);
            }
        } catch (Exception e) {
            log.warn("Error fetching reservations for table {}: {}", table.getId(), e.getMessage());
            clearReservationDetails(builder);
        }
    }
    
    /**
     * Find the latest active reservation from a list
     * @param reservations List of reservations
     * @return Latest active reservation or null
     */
    private Reservations findLatestActiveReservation(List<Reservations> reservations) {
        if (reservations == null || reservations.isEmpty()) {
            return null;
        }
        
        return reservations.stream()
                .filter(reservation -> !ReservationStatus.CANCELLED.equals(reservation.getStatus()))
                .max((r1, r2) -> r1.getReservationDate().compareTo(r2.getReservationDate()))
                .orElse(null);
    }
    
    /**
     * Set reservation details in builder
     * @param builder TableResponse builder
     * @param reservation Latest reservation
     */
    private void setReservationDetails(TableResponse.TableResponseBuilder builder, Reservations reservation) {
        builder.reservationGuest(reservation.getUser() != null ? reservation.getUser().getUsername() : BusinessConstants.UNKNOWN_VALUE)
               .reservationTime(reservation.getReservationDate().toString())
               .reservationPartySize(reservation.getNumberOfGuests())
               .reservationCode(reservation.getReservationCode());
    }
    
    /**
     * Clear reservation details in builder
     * @param builder TableResponse builder
     */
    private void clearReservationDetails(TableResponse.TableResponseBuilder builder) {
        builder.reservationGuest(null)
               .reservationTime(null)
               .reservationPartySize(null)
               .reservationCode(null);
    }
    
    /**
     * Batch load reservations for multiple tables to avoid N+1 queries
     * @param tableIds List of table IDs
     * @return Map of table ID to list of reservations
     */
    private Map<Long, List<Reservations>> loadReservationsForTables(List<Long> tableIds) {
        if (tableIds.isEmpty()) {
            return new HashMap<>();
        }
        
        log.debug("Batch loading reservations for {} tables", tableIds.size());
        List<Reservations> allReservations = reservationRepository.findActiveReservationsByTableIdsWithEagerLoading(tableIds);
        
        return allReservations.stream()
                .collect(Collectors.groupingBy(reservation -> reservation.getTable().getId()));
    }
    
    /**
     * Optimized version of convertToTableResponse that uses pre-loaded reservations
     * @param table Table entity
     * @param reservations Pre-loaded reservations for this table
     * @return TableResponse DTO
     */
    private TableResponse convertToTableResponseOptimized(Tables table, List<Reservations> reservations) {
        log.debug("Converting table {} with status: {} (optimized)", table.getId(), table.getStatus());
        
        TableResponse.TableResponseBuilder builder = buildBasicTableResponse(table);
        addReservationDetailsOptimized(builder, reservations);
        
        return builder.build();
    }
    
    /**
     * Add reservation details using pre-loaded reservations
     * @param builder TableResponse builder
     * @param reservations Pre-loaded reservations
     */
    private void addReservationDetailsOptimized(TableResponse.TableResponseBuilder builder, List<Reservations> reservations) {
        try {
            if (reservations != null && !reservations.isEmpty()) {
                Reservations latestReservation = findLatestActiveReservation(reservations);
                
                if (latestReservation != null) {
                    setReservationDetails(builder, latestReservation);
                } else {
                    clearReservationDetails(builder);
                }
            } else {
                clearReservationDetails(builder);
            }
        } catch (Exception e) {
            log.warn("Error processing reservations: {}", e.getMessage());
            clearReservationDetails(builder);
        }
    }
}
