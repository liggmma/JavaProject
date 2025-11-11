package luxdine.example.luxdine.service.ai;

import lombok.Data;
import luxdine.example.luxdine.domain.payment.enums.PaymentMethod;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model for storing reservation booking conversation state
 * Used by the AI chatbot to track slot-filling progress
 */
@Data
public class ReservationChatState {

    /**
     * Conversation state enum
     */
    public enum ConversationStep {
        GREETING,           // Initial greeting, check login
        ASK_DATE_TIME,      // Asking for date and time
        ASK_GUESTS,         // Asking for number of guests
        ASK_AREA_TABLE,     // Optional: asking for area/table preference
        ASK_NOTES,          // Optional: asking for special requests
        ASK_PAYMENT_METHOD, // Asking for payment method
        CONFIRM,            // Final confirmation
        COMPLETED           // Booking completed
    }

    // Session information
    private String sessionId;
    private Long userId;
    private String username;
    private boolean isLoggedIn;

    // Current conversation step
    private ConversationStep currentStep;

    // Collected reservation data
    private OffsetDateTime reservationDate;
    private Integer numberOfGuests;
    private String areaName;         // Optional area preference
    private Long tableId;            // Optional specific table
    private String notes;            // Special requests
    private PaymentMethod paymentMethod;

    // Validation and availability
    private boolean dateTimeValid;
    private boolean availabilityChecked;
    private List<TableInfo> availableTables;

    // Conversation tracking
    private int retryCount;          // Number of retry attempts for current slot
    private List<String> conversationHistory;

    // Timestamps
    private long createdAt;
    private long lastUpdated;

    public ReservationChatState(String sessionId) {
        this.sessionId = sessionId;
        this.currentStep = ConversationStep.GREETING;
        this.retryCount = 0;
        this.conversationHistory = new ArrayList<>();
        this.availableTables = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
        this.lastUpdated = System.currentTimeMillis();
        this.dateTimeValid = false;
        this.availabilityChecked = false;
        this.isLoggedIn = false;
    }

    /**
     * Update the last activity timestamp
     */
    public void touch() {
        this.lastUpdated = System.currentTimeMillis();
    }

    /**
     * Add a message to conversation history
     */
    public void addToHistory(String message) {
        if (conversationHistory == null) {
            conversationHistory = new ArrayList<>();
        }
        conversationHistory.add(message);
        touch();
    }

    /**
     * Move to next conversation step
     */
    public void nextStep() {
        ConversationStep[] steps = ConversationStep.values();
        int currentIndex = currentStep.ordinal();
        if (currentIndex < steps.length - 1) {
            currentStep = steps[currentIndex + 1];
            retryCount = 0;
        }
        touch();
    }

    /**
     * Increment retry count for current slot
     */
    public void incrementRetry() {
        this.retryCount++;
        touch();
    }

    /**
     * Reset to allow editing - clears reservation data but keeps session info
     */
    public void resetToEdit() {
        // Reset to ASK_DATE_TIME step
        this.currentStep = ConversationStep.ASK_DATE_TIME;
        this.retryCount = 0;
        
        // Clear reservation data
        this.reservationDate = null;
        this.numberOfGuests = null;
        this.areaName = null;
        this.tableId = null;
        this.notes = null;
        this.paymentMethod = null;
        
        // Reset validation flags
        this.dateTimeValid = false;
        this.availabilityChecked = false;
        this.availableTables = new ArrayList<>();
        
        touch();
    }

    /**
     * Check if all required fields are filled
     */
    public boolean isComplete() {
        return reservationDate != null
                && numberOfGuests != null
                && paymentMethod != null
                && dateTimeValid
                && availabilityChecked;
    }

    /**
     * Check if the state has expired (15 minutes of inactivity)
     */
    public boolean isExpired() {
        long fifteenMinutes = 15 * 60 * 1000;
        return (System.currentTimeMillis() - lastUpdated) > fifteenMinutes;
    }

    /**
     * Simple table information for display
     */
    @Data
    public static class TableInfo {
        private Long tableId;
        private String tableName;
        private Integer capacity;
        private String areaName;
        private String tableType;
        private Double depositAmount;

        public TableInfo(Long tableId, String tableName, Integer capacity,
                        String areaName, String tableType, Double depositAmount) {
            this.tableId = tableId;
            this.tableName = tableName;
            this.capacity = capacity;
            this.areaName = areaName;
            this.tableType = tableType;
            this.depositAmount = depositAmount;
        }
    }
}
