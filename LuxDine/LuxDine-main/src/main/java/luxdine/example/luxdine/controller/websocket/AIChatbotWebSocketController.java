package luxdine.example.luxdine.controller.websocket;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.service.ai.AIChatService;
import luxdine.example.luxdine.service.user.UserService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket Controller for AI Chatbot
 * Handles real-time messaging for the AI chatbot
 */
@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AIChatbotWebSocketController {

    AIChatService aiChatService;
    UserService userService;
    SimpMessagingTemplate messagingTemplate;

    /**
     * Handle reservation booking messages via WebSocket
     * Endpoint: /app/ai/reservation
     * Subscribe to: /topic/ai-chatbot.{sessionId}
     */
    @MessageMapping("/ai/reservation")
    public void handleReservationMessage(@Payload AIChatbotMessageRequest request, Principal principal) {
        sendMessage(request, principal); // Reuse existing message handler
    }

    /**
     * Handle incoming messages from chatbot widget
     * Endpoint: /app/ai-chatbot.send
     * Subscribe to: /topic/ai-chatbot.{sessionId}
     */
    @MessageMapping("/ai-chatbot.send")
    public void sendMessage(@Payload AIChatbotMessageRequest request, Principal principal) {
        try {
            // Validate request
            if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                log.warn("Invalid AI chatbot message request");
                sendErrorToSession(request != null ? request.getSessionId() : null,
                        "Invalid message. Please try again.");
                return;
            }

            // Determine session ID
            String sessionId = request.getSessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                if (principal != null) {
                    // Logged-in user
                    String username = principal.getName();
                    Long userId = userService.findByUsername(username).getId();
                    sessionId = "user-" + userId;
                } else {
                    // Guest user - should have session ID from frontend
                    log.error("No session ID provided for guest user");
                    return;
                }
            }

            // Create or get session
            sessionId = aiChatService.createOrGetSession(sessionId);

            // Send "typing" indicator
            sendTypingIndicator(sessionId, true);

            // Get userId if user is logged in
            Long userId = null;
            if (principal != null) {
                try {
                    String username = principal.getName();
                    userId = userService.findByUsername(username).getId();
                } catch (Exception e) {
                    log.warn("Could not resolve user ID for principal: {}", principal.getName());
                }
            }

            // Process message with user context and get AI response
            AIChatService.ChatMessage chatMessage = aiChatService.processMessage(
                    sessionId,
                    request.getMessage(),
                    userId
            );

            // Stop "typing" indicator
            sendTypingIndicator(sessionId, false);

            // Build response
            AIChatbotMessageResponse response = new AIChatbotMessageResponse(
                    chatMessage.getMessageId(),
                    sessionId,
                    chatMessage.getUserMessage(),
                    chatMessage.getAiResponse(),
                    chatMessage.getLanguage(),
                    chatMessage.getTimestamp(),
                    "SUCCESS"
            );

            // Send response to the specific session topic
            String sessionTopic = "/topic/ai-chatbot." + sessionId;
            messagingTemplate.convertAndSend(sessionTopic, response);

            log.debug("AI chatbot message sent via WebSocket for session: {}", sessionId);

        } catch (Exception e) {
            log.error("Error processing AI chatbot message via WebSocket", e);
            sendErrorToSession(request != null ? request.getSessionId() : null,
                    "I apologize, but I encountered an error. Please try again.");
        }
    }

    /**
     * Send error message to a session
     */
    private void sendErrorToSession(String sessionId, String errorMessage) {
        try {
            if (sessionId != null) {
                String sessionTopic = "/topic/ai-chatbot." + sessionId;

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("type", "ERROR");
                errorResponse.put("message", errorMessage);
                errorResponse.put("timestamp", new Date());

                messagingTemplate.convertAndSend(sessionTopic, errorResponse);
            }
        } catch (Exception e) {
            log.error("Error sending error notification to session: {}", e.getMessage(), e);
        }
    }

    /**
     * Send typing indicator
     */
    private void sendTypingIndicator(String sessionId, boolean isTyping) {
        try {
            String sessionTopic = "/topic/ai-chatbot." + sessionId;

            Map<String, Object> typingIndicator = new HashMap<>();
            typingIndicator.put("type", "TYPING");
            typingIndicator.put("isTyping", isTyping);
            typingIndicator.put("timestamp", new Date());

            messagingTemplate.convertAndSend(sessionTopic, typingIndicator);
        } catch (Exception e) {
            log.error("Error sending typing indicator: {}", e.getMessage(), e);
        }
    }

    // DTO Classes
    public static class AIChatbotMessageRequest {
        private String sessionId;
        private String message;

        public AIChatbotMessageRequest() {
        }

        public AIChatbotMessageRequest(String sessionId, String message) {
            this.sessionId = sessionId;
            this.message = message;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    public static class AIChatbotMessageResponse {
        private String messageId;
        private String sessionId;
        private String userMessage;
        private String aiResponse;
        private String language;
        private Date timestamp;
        private String status;

        public AIChatbotMessageResponse() {
        }

        public AIChatbotMessageResponse(String messageId, String sessionId, String userMessage,
                                        String aiResponse, String language, Date timestamp, String status) {
            this.messageId = messageId;
            this.sessionId = sessionId;
            this.userMessage = userMessage;
            this.aiResponse = aiResponse;
            this.language = language;
            this.timestamp = timestamp;
            this.status = status;
        }

        // Getters and setters
        public String getMessageId() {
            return messageId;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getUserMessage() {
            return userMessage;
        }

        public void setUserMessage(String userMessage) {
            this.userMessage = userMessage;
        }

        public String getAiResponse() {
            return aiResponse;
        }

        public void setAiResponse(String aiResponse) {
            this.aiResponse = aiResponse;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
