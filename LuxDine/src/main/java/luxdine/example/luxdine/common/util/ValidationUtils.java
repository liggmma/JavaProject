package luxdine.example.luxdine.common.util;

import luxdine.example.luxdine.common.constants.BusinessConstants;
import luxdine.example.luxdine.domain.table.enums.TableStatus;

/**
 * Utility class for common validation operations
 */
public final class ValidationUtils {
    
    private ValidationUtils() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Validate if status is not null or empty
     * @param status Status to validate
     * @throws IllegalArgumentException if status is invalid
     */
    public static void validateStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException(BusinessConstants.ERROR_STATUS_CANNOT_BE_NULL);
        }
    }
    
    /**
     * Validate if table ID is valid
     * @param id Table ID to validate
     * @throws IllegalArgumentException if ID is invalid
     */
    public static void validateTableId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(BusinessConstants.ERROR_INVALID_TABLE_ID + id);
        }
    }
    
    /**
     * Validate if table status is valid
     * @param status Status to validate
     * @return true if valid
     */
    public static boolean isValidTableStatus(String status) {
        try {
            TableStatus.valueOf(status);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Get valid table statuses as string
     * @return Comma-separated list of valid statuses
     */
    public static String getValidTableStatuses() {
        StringBuilder validStatuses = new StringBuilder();
        for (TableStatus tableStatus : TableStatus.values()) {
            if (validStatuses.length() > 0) {
                validStatuses.append(", ");
            }
            validStatuses.append(tableStatus.name());
        }
        return validStatuses.toString();
    }
}
