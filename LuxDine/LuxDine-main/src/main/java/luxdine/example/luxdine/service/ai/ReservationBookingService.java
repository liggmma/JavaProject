package luxdine.example.luxdine.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.domain.payment.enums.PaymentMethod;
import luxdine.example.luxdine.domain.table.dto.response.TableResponse;
import luxdine.example.luxdine.service.seating.TableService;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for handling AI-powered reservation booking conversations
 * Implements slot-filling logic to collect reservation information via chat
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationBookingService {

    private final ChatbotContextService contextService;
    private final TableService tableService;
    private final QueryHandlerService queryHandler;


    /**
     * Process a user message in the reservation booking flow
     */
    public String processReservationMessage(String sessionId, String userMessage, Long userId, String username) {
        // Null safety checks
        if (sessionId == null || sessionId.trim().isEmpty()) {
            log.error("Invalid sessionId provided for reservation booking");
            return "I'm sorry, there was an error with your session. Please try again.";
        }
        
        if (userMessage == null || userMessage.trim().isEmpty()) {
            log.warn("Empty user message for reservation booking session: {}", sessionId);
            return "I didn't receive your message. Please try again.";
        }
        
        log.info("Processing reservation message for session {}: {}", sessionId, userMessage);

        // Get or create reservation state
        ReservationChatState state = contextService.getOrCreateReservationState(sessionId, userId, username);
        state.addToHistory(userMessage);

        // Check if user wants to cancel booking
        if (isCancelIntent(userMessage)) {
            contextService.clearReservationState(sessionId);
            return "Reservation booking cancelled. If you'd like to book again, just let me know!";
        }

        // Check if user wants to change date/time (even if they're past that step)
        if (isChangeDateTimeIntent(userMessage) && state.getCurrentStep() != ReservationChatState.ConversationStep.GREETING) {
            // Try to parse new date/time from message
            QueryHandlerService.ReservationParams params = queryHandler.extractReservationParams(userMessage);
            if (params != null && params.getDateTime() != null) {
                // Go back to ASK_DATE_TIME step and process
                state.setCurrentStep(ReservationChatState.ConversationStep.ASK_DATE_TIME);
                contextService.updateReservationState(sessionId, state);
                return handleDateTime(state, userMessage);
            }
        }

        // Process based on current step
        return switch (state.getCurrentStep()) {
            case GREETING -> handleGreeting(state, userMessage);
            case ASK_DATE_TIME -> handleDateTime(state, userMessage);
            case ASK_GUESTS -> handleGuests(state, userMessage);
            case ASK_AREA_TABLE -> handleAreaTable(state, userMessage);
            case ASK_NOTES -> handleNotes(state, userMessage);
            case ASK_PAYMENT_METHOD -> handlePaymentMethod(state, userMessage);
            case CONFIRM -> handleConfirmation(state, userMessage);
            case COMPLETED -> "Your reservation is complete! You can check your booking in your profile.";
        };
    }

    /**
     * Check if user wants to change date/time
     */
    private boolean isChangeDateTimeIntent(String message) {
        String lower = message.toLowerCase();
        return lower.contains("change time") || lower.contains("change date") ||
               lower.contains("different time") || lower.contains("different date") ||
               lower.contains("try") || lower.contains("how about") ||
               // Vietnamese patterns
               lower.contains("đổi thời gian") || lower.contains("đổi giờ") ||
               lower.contains("thay đổi") || lower.contains("thử") ||
               lower.contains("thế còn") || lower.contains("hay là");
    }

    /**
     * Handle greeting and login check
     */
    private String handleGreeting(ReservationChatState state, String userMessage) {
        if (!state.isLoggedIn() || state.getUserId() == null) {
            return "To book a table, please log in first.\n\n" +
                   "You can log in at: **/login**\n\n" +
                   "Once you're logged in, I'll be happy to help you make a reservation!";
        }

        // User is logged in, proceed to date/time
        state.nextStep();
        contextService.updateReservationState(state.getSessionId(), state);

        return String.format("Great! I'll help you book a table, **%s**.\n\n" +
                           "When would you like to dine with us?\n\n" +
                           "You can say:\n" +
                           "• \"Tomorrow at 7pm\"\n" +
                           "• \"November 10th at 6:30pm\"\n" +
                           "• \"Next Friday at 8pm\"\n" +
                           "• \"12/15/2025 at 7:00pm\"",
                           state.getUsername());
    }

    /**
     * Handle date and time input
     */
    private String handleDateTime(ReservationChatState state, String userMessage) {
        try {
            // Extract date/time using QueryHandlerService
            QueryHandlerService.ReservationParams params = queryHandler.extractReservationParams(userMessage);

            if (params != null && params.getDateTime() != null) {
                OffsetDateTime reservationDateTime = params.getDateTime();

                // Validate time is in the future
                if (reservationDateTime.isBefore(OffsetDateTime.now())) {
                    state.incrementRetry();
                    return "I'm sorry, but that time is in the past. Please choose a future date and time.\n\n" +
                           "Correct format examples:\n" +
                           "• \"Tomorrow at 7pm\"\n" +
                           "• \"November 10th at 6:30pm\"\n" +
                           "• \"Next Friday at 8pm\"\n" +
                           "• \"12/15/2025 at 7:00pm\"";
                }

                // Validate time is within restaurant hours (use configuration)
                int hour = reservationDateTime.getHour();
                int openHour = luxdine.example.luxdine.service.config.RestaurantConfigService.OPEN_HOUR;
                int closeHour = luxdine.example.luxdine.service.config.RestaurantConfigService.CLOSE_HOUR;

                if (hour < openHour || hour >= closeHour) {
                    state.incrementRetry();
                    return String.format("Our restaurant is open from %d:00 %s to %d:00 %s.\n\n" +
                            "Please choose a time within our operating hours.\n" +
                            "Example: \"Tomorrow at %dpm\" or \"December 5th at %dpm\"",
                            openHour, openHour < 12 ? "AM" : "PM",
                            closeHour, closeHour < 12 ? "AM" : "PM",
                            (openHour + closeHour) / 2,  // Suggest middle time
                            (openHour + closeHour) / 2);
                }

                // Save the date/time
                state.setReservationDate(reservationDateTime);
                state.setDateTimeValid(true);
                state.nextStep();
                contextService.updateReservationState(state.getSessionId(), state);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d 'at' h:mm a");
                String formattedDate = reservationDateTime.format(formatter);

                return String.format("Perfect! I've noted your reservation for **%s**.\n\n" +
                                   "How many guests will be joining you?\n" +
                                   "Tip: Just tell me the number (e.g., \"4\" or \"4 guests\")",
                                   formattedDate);
            } else {
                state.incrementRetry();

                // Progressive help messages
                if (state.getRetryCount() == 1) {
                    return "I couldn't understand that date and time. Let me help!\n\n" +
                           "You can say:\n" +
                           "• \"Tomorrow at 7pm\"\n" +
                           "• \"November 10th at 6:30pm\"\n" +
                           "• \"Next week at 8pm\"\n" +
                           "• \"12/15/2025 at 7:00pm\"\n\n" +
                           "When would you like to dine?";
                } else if (state.getRetryCount() == 2) {
                    return "I'm still having trouble understanding. Let's try again!\n\n" +
                           "Please use one of these formats:\n\n" +
                           "**Simple formats:**\n" +
                           "• Tomorrow at 7pm\n" +
                           "• Today at 8:30pm\n\n" +
                           "**Specific dates:**\n" +
                           "• November 10th at 6pm\n" +
                           "• Dec 25 at 7:30pm\n\n" +
                           "**Numeric format:**\n" +
                           "• 11/10/2025 at 6:00pm";
                } else {
                    // After 3 attempts, provide very detailed guidance
                    return "Let me help you with the correct format!\n\n" +
                           "Please enter your desired date and time like this:\n\n" +
                           "**Option 1 - Simple (Recommended):**\n" +
                           "Type: \"Tomorrow at 7pm\"\n\n" +
                           "**Option 2 - Specific Date:**\n" +
                           "Type: \"November 10th at 6:30pm\"\n\n" +
                           "**Option 3 - Numeric:**\n" +
                           "Type: \"11/10/2025 at 7:00pm\"\n\n" +
                           "Make sure to include both the date AND time!";
                }
            }
        } catch (Exception e) {
            log.error("Error parsing date/time", e);
            state.incrementRetry();
            return "I had trouble processing that. Please try again!\n\n" +
                   "Example format: \"Tomorrow at 7pm\" or \"December 25th at 6:30pm\"";
        }
    }

    /**
     * Handle number of guests input
     */
    private String handleGuests(ReservationChatState state, String userMessage) {
        try {
            // Extract number from message
            Integer guests = extractNumber(userMessage);

            if (guests == null || guests < 1) {
                state.incrementRetry();

                // Progressive help messages
                if (state.getRetryCount() == 1) {
                    return "I couldn't find a valid number of guests.\n\n" +
                           "Please tell me how many people:\n" +
                           "• \"4 guests\"\n" +
                           "• \"2 people\"\n" +
                           "• Just \"4\" is fine too!";
                } else if (state.getRetryCount() >= 2) {
                    return "Let me help you!\n\n" +
                           "Please enter the number of guests in one of these ways:\n" +
                           "• Type a number: \"4\"\n" +
                           "• Or say: \"4 guests\"\n" +
                           "• Or say: \"4 people\"\n\n" +
                           "We can accommodate 1-20 guests.";
                }
                return "Please provide a valid number of guests (at least 1).";
            }

            if (guests > 20) {
                return "For parties larger than 20 guests, please contact us directly!\n\n" +
                       "Phone: " + luxdine.example.luxdine.service.config.RestaurantConfigService.RESTAURANT_PHONE + "\n" +
                       "We'll be happy to arrange special accommodations for your large group!\n\n" +
                       "Would you like to book for a smaller party? (1-20 guests)";
            }

            state.setNumberOfGuests(guests);

            // Check table availability (reservationDate should be set at this point)
            if (state.getReservationDate() == null) {
                log.error("Reservation date is null when checking table availability");
                state.incrementRetry();
                return "I'm sorry, there was an error with your reservation date. Please try again.";
            }
            
            List<TableResponse> availableTables = tableService.getAvailableTablesForCapacity(
                    guests, state.getReservationDate());

            if (availableTables == null || availableTables.isEmpty()) {
                // DON'T clear state - keep it so user can change date/time
                state.incrementRetry();
                contextService.updateReservationState(state.getSessionId(), state);

                return String.format("I'm sorry, but we don't have any tables available for %d guests at that time.\n\n" +
                                   "Would you like to try a different date or time? You can say something like:\n" +
                                   "- \"Try tomorrow at 6pm\"\n" +
                                   "- \"How about next Friday?\"\n" +
                                   "- \"Cancel\" to start over",
                                   guests);
            }

            // Store available tables
            state.setAvailableTables(availableTables.stream()
                    .map(t -> new ReservationChatState.TableInfo(
                            t.getId(),
                            t.getTableName(),
                            t.getCapacity(),
                            t.getAreaName(),
                            t.getTableType(),
                            t.getDepositAmount()))
                    .toList());
            state.setAvailabilityChecked(true);

            state.nextStep();
            contextService.updateReservationState(state.getSessionId(), state);

            // Build available tables message
            StringBuilder response = new StringBuilder();
            response.append(String.format("Great! We have tables available for %d guests.\n\n", guests));
            response.append("Would you like to choose a specific table or area? Here are your options:\n\n");

            for (int i = 0; i < Math.min(5, availableTables.size()); i++) {
                TableResponse table = availableTables.get(i);
                response.append(String.format("- %s (Capacity: %d, Area: %s)\n",
                        table.getTableName(),
                        table.getCapacity(),
                        table.getAreaName() != null ? table.getAreaName() : "Main"));
            }

            if (availableTables.size() > 5) {
                response.append(String.format("\n... and %d more tables available", availableTables.size() - 5));
            }

            response.append("\n\n**How to choose:**\n");
            response.append("• Type the table name: \"Table 5\"\n");
            response.append("• Or say: \"any table\" and we'll pick the best one for you!");

            return response.toString();

        } catch (Exception e) {
            log.error("Error processing guests", e);
            state.incrementRetry();
            return "Please provide the number of guests as a number (e.g., \"4 guests\" or just \"4\").";
        }
    }

    /**
     * Handle area/table preference input
     */
    private String handleAreaTable(ReservationChatState state, String userMessage) {
        String lowerMessage = userMessage.toLowerCase().trim();

        // Check if user wants any table
        if (lowerMessage.contains("any") || lowerMessage.contains("don't care") ||
            lowerMessage.contains("doesn't matter") || lowerMessage.contains("skip")) {
            // Don't assign specific table, let system choose
            state.nextStep();
            contextService.updateReservationState(state.getSessionId(), state);
            return "Understood! We'll select the best available table for you.\n\n" +
                   "Do you have any special requests or notes? (allergies, occasion, preferences)\n" +
                   "Or you can say \"no\" to skip.";
        }

        // Try to match table name from available tables
        if (state.getAvailableTables() != null) {
            for (ReservationChatState.TableInfo table : state.getAvailableTables()) {
                if (lowerMessage.contains(table.getTableName().toLowerCase())) {
                    state.setTableId(table.getTableId());
                    state.nextStep();
                    contextService.updateReservationState(state.getSessionId(), state);

                    return String.format("Perfect! I've reserved %s for you.\n\n" +
                                       "Do you have any special requests or notes? (allergies, occasion, preferences)\n" +
                                       "Or you can say \"no\" to skip.",
                                       table.getTableName());
                }
            }
        }

        // Didn't understand table selection
        state.incrementRetry();
        if (state.getRetryCount() == 1) {
            return "I didn't quite understand which table you'd like.\n\n" +
                   "You can:\n" +
                   "• Choose a specific table: \"Table 5\" or \"VIP Room\"\n" +
                   "• Say: \"any table\" (we'll pick the best one)\n" +
                   "• Say: \"skip\" to let us choose";
        } else if (state.getRetryCount() > 2) {
            // Skip this step after multiple retries
            state.nextStep();
            contextService.updateReservationState(state.getSessionId(), state);
            return "No problem! We'll select the best available table for you.\n\n" +
                   "Do you have any special requests or notes?\n" +
                   "(dietary restrictions, special occasion, accessibility needs)\n\n" +
                   "Or say \"no\" to skip.";
        }

        return "Please tell me which table you'd like:\n" +
               "• Type a table name from the list above\n" +
               "• Or say: \"any table\" and we'll choose for you";
    }

    /**
     * Handle notes/special requests input
     */
    private String handleNotes(ReservationChatState state, String userMessage) {
        String lowerMessage = userMessage.toLowerCase().trim();

        if (lowerMessage.equals("no") || lowerMessage.equals("none") ||
            lowerMessage.contains("no notes") || lowerMessage.contains("nothing")) {
            // No special requests
            state.nextStep();
        } else {
            // Save the notes
            state.setNotes(userMessage);
            state.nextStep();
        }

        // Automatically set payment method to QR Code (only method available)
        state.setPaymentMethod(PaymentMethod.QR);
        state.nextStep(); // Move to CONFIRM step
        contextService.updateReservationState(state.getSessionId(), state);

        // Generate summary with QR Code payment method
        return buildReservationSummary(state);
    }

    /**
     * Handle payment method selection
     * Note: This method is kept for backward compatibility but should not be reached
     * as payment method is now automatically set to QR in handleNotes()
     */
    private String handlePaymentMethod(ReservationChatState state, String userMessage) {
        // Automatically set to QR Code (only method available)
        state.setPaymentMethod(PaymentMethod.QR);
        state.nextStep();
        contextService.updateReservationState(state.getSessionId(), state);

        // Generate summary
        return buildReservationSummary(state);
    }

    /**
     * Handle final confirmation
     */
    private String handleConfirmation(ReservationChatState state, String userMessage) {
        String lowerMessage = userMessage.toLowerCase().trim();

        if (lowerMessage.contains("yes") || lowerMessage.contains("confirm") ||
            lowerMessage.contains("ok") || lowerMessage.contains("proceed") ||
            lowerMessage.equals("y")) {

            // Mark as completed
            state.nextStep(); // Move to COMPLETED
            contextService.updateReservationState(state.getSessionId(), state);

            // Build checkout data before clearing
            String checkoutResponse = buildCheckoutRedirect(state);

            // Clear reservation state to allow new bookings
            contextService.clearReservationState(state.getSessionId());

            // Return special response with action data
            return checkoutResponse;

        } else if (lowerMessage.contains("no") || lowerMessage.contains("cancel")) {
            contextService.clearReservationState(state.getSessionId());
            return "Reservation cancelled.\n\nFeel free to start a new booking anytime! Just say \"I want to book a table\"";

        } else if (lowerMessage.contains("edit") || lowerMessage.contains("change") || lowerMessage.contains("modify") ||
                   lowerMessage.contains("sửa") || lowerMessage.contains("đổi") || lowerMessage.contains("thay đổi")) {
            // Allow editing - reset to date/time step but keep session info
            state.resetToEdit();
            contextService.updateReservationState(state.getSessionId(), state);
            return "No problem! Let's update your reservation.\n\n" +
                   "When would you like to dine with us?\n\n" +
                   "You can say:\n" +
                   "• \"Tomorrow at 7pm\"\n" +
                   "• \"November 10th at 6:30pm\"\n" +
                   "• \"Next Friday at 8pm\"\n" +
                   "• \"12/15/2025 at 7:00pm\"";

        } else {
            state.incrementRetry();
            if (state.getRetryCount() == 1) {
                return "I need your confirmation to proceed.\n\n" +
                       "Please say:\n" +
                       "• \"Yes\" or \"Confirm\" to proceed\n" +
                       "• \"Edit\" to make changes\n" +
                       "• \"Cancel\" to cancel booking";
            } else {
                return "Please respond with one of these options:\n\n" +
                       "**To confirm:** Type \"Yes\" or \"Confirm\"\n" +
                       "**To make changes:** Type \"Edit\"\n" +
                       "**To cancel:** Type \"Cancel\"";
            }
        }
    }

    /**
     * Build reservation summary for confirmation
     */
    private String buildReservationSummary(ReservationChatState state) {
        // Null safety checks
        if (state.getReservationDate() == null || state.getNumberOfGuests() == null) {
            log.error("Invalid reservation state: missing date or guests");
            return "I'm sorry, there was an error with your reservation. Please start over.";
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d 'at' h:mm a");
        String formattedDate = state.getReservationDate().format(formatter);

        StringBuilder summary = new StringBuilder();
        summary.append("**Reservation Summary**\n\n");
        summary.append("**Date & Time:** ").append(formattedDate).append("\n");
        summary.append("**Number of Guests:** ").append(state.getNumberOfGuests()).append(" ");
        summary.append(state.getNumberOfGuests() == 1 ? "guest" : "guests").append("\n");

        if (state.getTableId() != null && state.getAvailableTables() != null) {
            state.getAvailableTables().stream()
                    .filter(t -> t.getTableId().equals(state.getTableId()))
                    .findFirst()
                    .ifPresent(table -> {
                        summary.append("**Table:** ").append(table.getTableName());
                        summary.append(" (Capacity: ").append(table.getCapacity()).append(")\n");
                    });
        } else {
            summary.append("**Table:** We'll select the best table for you\n");
        }

        if (state.getNotes() != null && !state.getNotes().isEmpty()) {
            summary.append("**Special Requests:** ").append(state.getNotes()).append("\n");
        }

        summary.append("**Payment Method:** QR Code\n");

        summary.append("\n---\n\n");
        summary.append("**To confirm:** Type \"Yes\" or \"Confirm\"\n");
        summary.append("**To edit:** Type \"Edit\"\n");
        summary.append("**To cancel:** Type \"Cancel\"");

        return summary.toString();
    }

    /**
     * Build checkout redirect message with JSON data
     */
    private String buildCheckoutRedirect(ReservationChatState state) {
        // Null safety checks
        if (state.getReservationDate() == null || state.getNumberOfGuests() == null || state.getPaymentMethod() == null) {
            log.error("Invalid reservation state for checkout: missing required fields");
            return "I'm sorry, there was an error processing your reservation. Please try again.";
        }
        
        // This will be parsed by the frontend to trigger the checkout flow
        return String.format(
                "RESERVATION_CHECKOUT:{\"reservationDate\":\"%s\",\"numberOfGuests\":%d,\"tableId\":%s,\"notes\":\"%s\",\"paymentMethod\":\"%s\"}",
                state.getReservationDate().toString(),
                state.getNumberOfGuests(),
                state.getTableId() != null ? state.getTableId() : "null",
                state.getNotes() != null ? state.getNotes().replace("\"", "\\\"") : "",
                state.getPaymentMethod().name()
        );
    }

    /**
     * Check if user wants to cancel
     */
    private boolean isCancelIntent(String message) {
        String lower = message.toLowerCase();
        return lower.equals("cancel") || lower.equals("stop") ||
               lower.equals("quit") || lower.equals("exit") ||
               lower.contains("nevermind") || lower.contains("never mind") ||
               // Vietnamese patterns
               lower.equals("hủy") || lower.equals("thôi") || lower.equals("khỏi") ||
               lower.contains("không cần") || lower.contains("thôi không") ||
               lower.contains("khỏi cần") || lower.contains("để sau");
    }

    /**
     * Extract number from text (supports English and Vietnamese)
     */
    private Integer extractNumber(String text) {
        // Try to find a number in the text
        Pattern pattern = Pattern.compile("\\b(\\d+)\\b");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }

        // Try word numbers (English and Vietnamese)
        String lower = text.toLowerCase();

        // English word numbers
        if (lower.contains("one")) return 1;
        if (lower.contains("two")) return 2;
        if (lower.contains("three")) return 3;
        if (lower.contains("four")) return 4;
        if (lower.contains("five")) return 5;
        if (lower.contains("six")) return 6;
        if (lower.contains("seven")) return 7;
        if (lower.contains("eight")) return 8;
        if (lower.contains("nine")) return 9;
        if (lower.contains("ten")) return 10;

        // Vietnamese word numbers
        if (lower.contains("một người") || lower.matches(".*\\bmột\\b.*")) return 1;
        if (lower.contains("hai người") || lower.matches(".*\\bhai\\b.*")) return 2;
        if (lower.contains("ba người") || lower.matches(".*\\bba\\b.*")) return 3;
        if (lower.contains("bốn người") || lower.matches(".*\\bbốn\\b.*") || lower.matches(".*\\btư\\b.*")) return 4;
        if (lower.contains("năm người") || lower.matches(".*\\bnăm\\b.*")) return 5;
        if (lower.contains("sáu người") || lower.matches(".*\\bsáu\\b.*")) return 6;
        if (lower.contains("bảy người") || lower.matches(".*\\bbảy\\b.*") || lower.matches(".*\\bbẩy\\b.*")) return 7;
        if (lower.contains("tám người") || lower.matches(".*\\btám\\b.*")) return 8;
        if (lower.contains("chín người") || lower.matches(".*\\bchín\\b.*")) return 9;
        if (lower.contains("mười người") || lower.matches(".*\\bmười\\b.*")) return 10;

        return null;
    }

    /**
     * Check if session has active reservation booking
     */
    public boolean hasActiveBooking(String sessionId) {
        return contextService.hasActiveReservationBooking(sessionId);
    }

    /**
     * Start a new reservation booking flow
     */
    public String startReservationBooking(String sessionId, Long userId, String username) {
        ReservationChatState state = contextService.getOrCreateReservationState(sessionId, userId, username);

        if (state.getCurrentStep() != ReservationChatState.ConversationStep.GREETING) {
            // Already in progress
            return "You already have a reservation in progress. Would you like to continue or start over?";
        }

        return processReservationMessage(sessionId, "start booking", userId, username);
    }

}
