package luxdine.example.luxdine.common.constants;

/**
 * Business constants for LuxDine application
 * Centralizes all magic numbers and business rules
 */
public final class BusinessConstants {

    public static final String ERROR_TABLE_TIME_CONFLICT = "Table is already reserved for a similar time slot. Please choose a different time or table.";

    // Private constructor to prevent instantiation
    private BusinessConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    // ========== RESTAURANT OPERATING HOURS ==========
    public static final int RESTAURANT_OPEN_HOUR = 9;
    public static final int RESTAURANT_CLOSE_HOUR = 22;
    
    // ========== KITCHEN DISPLAY SYSTEM (KDS) ==========
    public static final int URGENT_THRESHOLD_MINUTES = 20;
    public static final int HIGH_PRIORITY_THRESHOLD_MINUTES = 10;
    public static final int ITEM_OVERDUE_THRESHOLD_MINUTES = 15;
    public static final int ORDER_TIMEOUT_MINUTES = 360; // 6 hours
    
    
    // ========== KDS STATUS MAPPING ==========
    public static final String KDS_STATUS_PENDING = "PENDING";
    public static final String KDS_STATUS_COOKING = "COOKING";
    public static final String KDS_STATUS_READY = "READY";
    public static final String KDS_STATUS_SERVED = "SERVED";
    
    // ========== PRIORITY LEVELS ==========
    public static final String PRIORITY_URGENT = "URGENT";
    public static final String PRIORITY_HIGH = "HIGH";
    public static final String PRIORITY_NORMAL = "NORMAL";
    
    // ========== DEFAULT VALUES ==========
    public static final int DEFAULT_NUMBER_OF_GUESTS = 1;
    public static final double DEFAULT_DEPOSIT_AMOUNT = 0.0;
    public static final String UNKNOWN_VALUE = "Unknown";
    public static final String NOT_ASSIGNED = "Not assigned";
    
    // ========== ERROR MESSAGES ==========
    public static final String ERROR_TABLE_NOT_FOUND = "Table not found with id: ";
    public static final String ERROR_RESERVATION_NOT_FOUND = "Reservation not found with id: ";
    public static final String ERROR_USER_NOT_FOUND = "User not found: ";
    public static final String ERROR_INVALID_TABLE_ID = "Invalid table ID: ";
    public static final String ERROR_STATUS_CANNOT_BE_NULL = "Status cannot be null or empty";
    public static final String ERROR_CANNOT_BOOK_IN_PAST = "Cannot book in the past";
    public static final String ERROR_OUTSIDE_OPERATING_HOURS = "Reservation time is outside operating hours (9:00 AM - 10:00 PM)";
    public static final String ERROR_GUESTS_EXCEED_CAPACITY = "Number of guests (%d) exceeds table capacity (%d)";
    public static final String ERROR_TABLE_NOT_AVAILABLE = "Table is not available. Current status: %s";
}
