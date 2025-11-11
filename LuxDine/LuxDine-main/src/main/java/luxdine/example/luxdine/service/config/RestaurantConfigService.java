package luxdine.example.luxdine.service.config;

import luxdine.example.luxdine.common.constants.BusinessConstants;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for managing restaurant static configuration and information
 * Provides centralized access to restaurant details for the chatbot
 */
@Service
public class RestaurantConfigService {

    // Restaurant Basic Information
    public static final String RESTAURANT_NAME = "LuxDine";
    public static final String RESTAURANT_ADDRESS = "123 Luxury Street, District 1, Ho Chi Minh City, Vietnam";
    public static final String RESTAURANT_PHONE = "+84 (028) 1234 5678";
    public static final String RESTAURANT_EMAIL = "info@luxdine.com";

    // Operating Hours
    public static final int OPEN_HOUR = BusinessConstants.RESTAURANT_OPEN_HOUR; // 9 AM
    public static final int CLOSE_HOUR = BusinessConstants.RESTAURANT_CLOSE_HOUR; // 10 PM
    public static final String OPERATING_HOURS = "Monday-Sunday, 9:00 AM - 10:00 PM";
    public static final String KITCHEN_HOURS = "9:00 AM - 9:30 PM (last order)";

    // Facilities
    public static final boolean HAS_PARKING = true;
    public static final String PARKING_INFO = "Free parking available for customers";
    public static final boolean HAS_WIFI = true;
    public static final String WIFI_INFO = "Free WiFi available throughout the restaurant";
    public static final boolean HAS_OUTDOOR_SEATING = true;
    public static final boolean HAS_PRIVATE_ROOMS = true;

    // Services
    public static final boolean ACCEPTS_RESERVATIONS = true;
    public static final boolean OFFERS_TAKEOUT = true;
    public static final boolean OFFERS_DELIVERY = true;
    public static final boolean OFFERS_CATERING = true;

    // Payment Methods
    public static final List<String> PAYMENT_METHODS = List.of(
        "Cash",
        "Credit/Debit Cards (Visa, Mastercard, JCB)",
        "QR Code Payment (Momo, ZaloPay, ViettelPay)",
        "Bank Transfer"
    );

    // Reservation Policies
    public static final int MIN_ADVANCE_BOOKING_HOURS = 2;
    public static final int LARGE_GROUP_SIZE = 8;
    public static final double LARGE_GROUP_DEPOSIT_PERCENT = 20.0;
    public static final int CANCELLATION_NOTICE_HOURS = 2;
    public static final int RESERVATION_DURATION_MINUTES = 120;

    // Dining Policies
    public static final int MAX_PARTY_SIZE = 20;
    public static final boolean ALLOWS_PETS = false;
    public static final boolean REQUIRES_DRESS_CODE = false;
    public static final boolean KIDS_FRIENDLY = true;

    // Special Services
    public static final boolean OFFERS_BIRTHDAY_PACKAGES = true;
    public static final boolean OFFERS_ANNIVERSARY_PACKAGES = true;
    public static final boolean OFFERS_CORPORATE_EVENTS = true;

    /**
     * Get formatted restaurant basic information
     */
    public String getBasicInfo() {
        return String.format("""
            RESTAURANT: %s
            ADDRESS: %s
            PHONE: %s
            EMAIL: %s
            HOURS: %s
            """, RESTAURANT_NAME, RESTAURANT_ADDRESS, RESTAURANT_PHONE,
                 RESTAURANT_EMAIL, OPERATING_HOURS);
    }

    /**
     * Get formatted facilities information
     */
    public String getFacilitiesInfo() {
        StringBuilder info = new StringBuilder("FACILITIES & AMENITIES:\n");
        if (HAS_PARKING) info.append("- ").append(PARKING_INFO).append("\n");
        if (HAS_WIFI) info.append("- ").append(WIFI_INFO).append("\n");
        if (HAS_OUTDOOR_SEATING) info.append("- Outdoor seating available\n");
        if (HAS_PRIVATE_ROOMS) info.append("- Private dining rooms for special occasions\n");
        return info.toString();
    }

    /**
     * Get formatted services information
     */
    public String getServicesInfo() {
        StringBuilder info = new StringBuilder("SERVICES:\n");
        if (ACCEPTS_RESERVATIONS) info.append("- Table reservations (online & phone)\n");
        if (OFFERS_TAKEOUT) info.append("- Takeout service available\n");
        if (OFFERS_DELIVERY) info.append("- Delivery service available\n");
        if (OFFERS_CATERING) info.append("- Catering for events and parties\n");
        if (OFFERS_BIRTHDAY_PACKAGES) info.append("- Birthday celebration packages\n");
        if (OFFERS_ANNIVERSARY_PACKAGES) info.append("- Anniversary dinner packages\n");
        if (OFFERS_CORPORATE_EVENTS) info.append("- Corporate event hosting\n");
        return info.toString();
    }

    /**
     * Get formatted payment methods
     */
    public String getPaymentMethodsInfo() {
        StringBuilder info = new StringBuilder("PAYMENT METHODS:\n");
        for (String method : PAYMENT_METHODS) {
            info.append("- ").append(method).append("\n");
        }
        return info.toString();
    }

    /**
     * Get formatted reservation policies
     */
    public String getReservationPolicies() {
        return String.format("""
            RESERVATION POLICIES:
            - Advance booking: Minimum %d hours in advance
            - Large groups (%d+ people): %.0f%% deposit required
            - Cancellation: Free cancellation up to %d hours before reservation
            - Reservation duration: %d minutes (2 hours)
            - Maximum party size: %d people
            - For larger groups, please contact us directly
            """, MIN_ADVANCE_BOOKING_HOURS, LARGE_GROUP_SIZE, LARGE_GROUP_DEPOSIT_PERCENT,
                 CANCELLATION_NOTICE_HOURS, RESERVATION_DURATION_MINUTES, MAX_PARTY_SIZE);
    }

    /**
     * Get formatted dining policies
     */
    public String getDiningPolicies() {
        StringBuilder info = new StringBuilder("DINING POLICIES:\n");
        info.append(String.format("- Maximum party size: %d people\n", MAX_PARTY_SIZE));
        info.append(KIDS_FRIENDLY ? "- Family-friendly (kids welcome)\n" : "");
        info.append(ALLOWS_PETS ? "- Pets allowed in outdoor seating\n" : "- Pets not allowed\n");
        info.append(REQUIRES_DRESS_CODE ? "- Smart casual dress code\n" : "- No dress code required\n");
        return info.toString();
    }

    /**
     * Get complete restaurant information
     */
    public String getCompleteInfo() {
        return getBasicInfo() + "\n" +
               getFacilitiesInfo() + "\n" +
               getServicesInfo() + "\n" +
               getPaymentMethodsInfo() + "\n" +
               getReservationPolicies() + "\n" +
               getDiningPolicies();
    }

    // Getters for individual values
    public String getRestaurantName() { return RESTAURANT_NAME; }
    public String getRestaurantAddress() { return RESTAURANT_ADDRESS; }
    public String getRestaurantPhone() { return RESTAURANT_PHONE; }
    public String getRestaurantEmail() { return RESTAURANT_EMAIL; }
    public String getOperatingHours() { return OPERATING_HOURS; }
    public int getOpenHour() { return OPEN_HOUR; }
    public int getCloseHour() { return CLOSE_HOUR; }
    public boolean hasParking() { return HAS_PARKING; }
    public boolean hasWifi() { return HAS_WIFI; }
    public List<String> getPaymentMethods() { return PAYMENT_METHODS; }
}
