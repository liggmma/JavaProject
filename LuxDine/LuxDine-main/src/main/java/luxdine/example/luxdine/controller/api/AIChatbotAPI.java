package luxdine.example.luxdine.controller.api;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import luxdine.example.luxdine.service.ai.AIChatService;
import luxdine.example.luxdine.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

/**
 * REST API Controller for AI Chatbot
 * Handles chatbot interactions for both logged-in users and guests
 */
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/api/ai-chatbot")
public class AIChatbotAPI {

    AIChatService aiChatService;
    UserService userService;
    luxdine.example.luxdine.service.ai.SecureSessionManager secureSessionManager;

    /**
     * Create or get a chat session
     * For logged-in users: uses secure hashed session ID
     * For guests: generates a random UUID session ID
     */
    @PostMapping("/sessions")
    public ResponseEntity<SessionResponse> createSession(Principal principal) {
        try {
            Long userId = null;
            if (principal != null) {
                // Logged-in user - get userId
                String username = principal.getName();
                userId = userService.findByUsername(username).getId();
            }

            // Generate secure session ID (does not expose userId)
            String sessionId = secureSessionManager.getOrCreateSecureSessionId(userId);
            aiChatService.createOrGetSession(sessionId);

            SessionResponse response = new SessionResponse(sessionId, new Date());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error creating AI chatbot session", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Send a message and get AI response
     */
    @PostMapping("/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @RequestBody MessageRequest request,
            Principal principal) {
        try {
            // Validate session ID
            String sessionId = request.sessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse(
                                UUID.randomUUID().toString(),
                                request.message(),
                                "Session ID is required. Please create a session first by calling /api/ai-chatbot/sessions",
                                "en",
                                new Date()
                        ));
            }

            // Get userId from session (secure lookup)
            Long userId = secureSessionManager.getUserIdFromSession(sessionId);

            // If no userId from session but user is logged in, update session mapping
            if (userId == null && principal != null) {
                try {
                    String username = principal.getName();
                    userId = userService.findByUsername(username).getId();
                    // Update session mapping
                    sessionId = secureSessionManager.getOrCreateSecureSessionId(userId);
                } catch (Exception e) {
                    log.warn("Could not resolve user ID for principal: {}", principal.getName());
                }
            }

            // Process message with user context
            AIChatService.ChatMessage chatMessage = aiChatService.processMessage(
                    sessionId,
                    request.message(),
                    userId
            );

            MessageResponse response = new MessageResponse(
                    chatMessage.getMessageId(),
                    chatMessage.getUserMessage(),
                    chatMessage.getAiResponse(),
                    chatMessage.getLanguage(),
                    chatMessage.getTimestamp()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing message", e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse(
                            UUID.randomUUID().toString(),
                            request.message(),
                            "I apologize, but I encountered an error. Please try again.",
                            "en",
                            new Date()
                    ));
        }
    }

    /**
     * Get chat history for a session
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<List<MessageResponse>> getChatHistory(@PathVariable String sessionId) {
        try {
            List<AIChatService.ChatMessage> history = aiChatService.getChatHistory(sessionId);

            List<MessageResponse> responses = history.stream()
                    .map(msg -> new MessageResponse(
                            msg.getMessageId(),
                            msg.getUserMessage(),
                            msg.getAiResponse(),
                            msg.getLanguage(),
                            msg.getTimestamp()
                    ))
                    .toList();

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            log.error("Error getting chat history", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Close a chat session
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> closeSession(@PathVariable String sessionId) {
        try {
            aiChatService.closeSession(sessionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error closing session", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get active session count (for monitoring)
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Integer>> getStats() {
        try {
            Map<String, Integer> stats = new HashMap<>();
            stats.put("activeSessions", aiChatService.getActiveSessionCount());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting stats", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Reservation booking endpoint - requires authentication
     * Processes next step in reservation booking conversation
     */
    @PostMapping("/reservation/next")
    public ResponseEntity<MessageResponse> processReservationMessage(
            @RequestBody MessageRequest request,
            Principal principal) {
        try {
            // Must be logged in to book
            if (principal == null) {
                return ResponseEntity.status(401)
                        .body(new MessageResponse(
                                UUID.randomUUID().toString(),
                                request.message(),
                                "Please log in to book a table. You can log in at /login",
                                "en",
                                new Date()
                        ));
            }

            String username = principal.getName();
            Long userId = userService.findByUsername(username).getId();

            // Use secure session ID (not exposing userId)
            String sessionId = request.sessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = secureSessionManager.getOrCreateSecureSessionId(userId);
            }

            // Process message through reservation booking flow
            AIChatService.ChatMessage chatMessage = aiChatService.processMessage(
                    sessionId,
                    request.message(),
                    userId
            );

            MessageResponse response = new MessageResponse(
                    chatMessage.getMessageId(),
                    chatMessage.getUserMessage(),
                    chatMessage.getAiResponse(),
                    chatMessage.getLanguage(),
                    chatMessage.getTimestamp()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing reservation message", e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse(
                            UUID.randomUUID().toString(),
                            request.message(),
                            "I apologize, but I encountered an error. Please try again.",
                            "en",
                            new Date()
                    ));
        }
    }

    // DTO Classes
    public record SessionResponse(String sessionId, Date createdAt) {
    }

    public record MessageRequest(String sessionId, String message) {
    }

    public record MessageResponse(
            String messageId,
            String userMessage,
            String aiResponse,
            String language,
            Date timestamp) {
    }
}
