package luxdine.example.luxdine.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import java.util.Date;

/**
 * Utility class for date and time operations
 */
public final class DateTimeUtils {
    private static final ZoneId VENUE_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private DateTimeUtils() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Calculate elapsed minutes from a date
     * @param date Date to calculate from
     * @return Elapsed minutes
     */
    public static long calculateElapsedMinutes(Date date) {
        if (date == null) return 0;
        
        long diffInMillis = System.currentTimeMillis() - date.getTime();
        return diffInMillis / (60 * 1000); // Convert to minutes
    }
    
    /**
     * Convert Date to LocalDateTime
     * @param date Date to convert
     * @return LocalDateTime or null if date is null
     */
    public static LocalDateTime convertToLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
    
    /**
     * Check if current time is within restaurant operating hours
     * @param time Time to check
     * @return true if within operating hours
     */
    public static boolean isWithinOperatingHours(LocalTime time) {
        if (time == null) return false;
        
        return !time.isBefore(LocalTime.of(9, 0)) && !time.isAfter(LocalTime.of(22, 0));
    }
    
    /**
     * Check if date is in the past
     * @param date Date to check
     * @return true if date is in the past
     */
    public static boolean isDateInPast(LocalDate date) {
        if (date == null) return false;
        return date.isBefore(LocalDate.now());
    }

    public static ZoneId getVenueZoneId() {
        return VENUE_ZONE;
    }
}
