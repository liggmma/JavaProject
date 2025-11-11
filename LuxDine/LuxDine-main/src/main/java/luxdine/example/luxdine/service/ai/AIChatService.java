package luxdine.example.luxdine.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main service for AI Chatbot functionality
 * Manages chat sessions (in-memory only, no database persistence)
 * and coordinates with GeminiService for responses
 */
@Service
public class AIChatService {

    private static final Logger logger = LoggerFactory.getLogger(AIChatService.class);

    private final GeminiService geminiService;
    private final ChatbotContextService contextService;
    private final ReservationBookingService reservationBookingService;
    private final luxdine.example.luxdine.service.user.UserService userService;

    // In-memory storage for active chat sessions (no database)
    private final Map<String, ChatSession> activeSessions = new ConcurrentHashMap<>();

    @Autowired
    public AIChatService(GeminiService geminiService, ChatbotContextService contextService,
                        ReservationBookingService reservationBookingService,
                        luxdine.example.luxdine.service.user.UserService userService) {
        this.geminiService = geminiService;
        this.contextService = contextService;
        this.reservationBookingService = reservationBookingService;
        this.userService = userService;
    }

    /**
     * Create or retrieve a chat session
     *
     * @param sessionId Unique session identifier (userId for logged-in, UUID for guests)
     * @return Session ID
     */
    public String createOrGetSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }

        activeSessions.computeIfAbsent(sessionId, id -> {
            logger.info("Creating new chat session: {}", id);
            return new ChatSession(id);
        });

        return sessionId;
    }

    /**
     * Process a user message and generate AI response
     *
     * @param sessionId   Session identifier
     * @param userMessage User's message
     * @return AI generated response
     */
    public ChatMessage processMessage(String sessionId, String userMessage) {
        return processMessage(sessionId, userMessage, null);
    }

    /**
     * Process a user message with user context and generate AI response
     *
     * @param sessionId   Session identifier
     * @param userMessage User's message
     * @param userId      Optional user ID for personalized responses
     * @return AI generated response
     */
    public ChatMessage processMessage(String sessionId, String userMessage, Long userId) {
        // Null safety checks
        if (sessionId == null || sessionId.trim().isEmpty()) {
            logger.error("Invalid sessionId provided: {}", sessionId);
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        
        if (userMessage == null || userMessage.trim().isEmpty()) {
            logger.warn("Empty user message for session: {}", sessionId);
            throw new IllegalArgumentException("User message cannot be null or empty");
        }
        
        logger.info("Processing message for session {} (userId: {}): {}", sessionId, userId, userMessage);

        ChatSession session = activeSessions.get(sessionId);
        if (session == null) {
            session = new ChatSession(createOrGetSession(sessionId));
            activeSessions.put(sessionId, session);
        }

        // Store user ID in session for future requests
        if (userId != null) {
            session.setUserId(userId);
        }

        // Check if user has active reservation booking in progress
        boolean hasActiveBooking = reservationBookingService.hasActiveBooking(sessionId);

        // Detect reservation booking intent
        boolean isReservationBookingIntent = isReservationBookingIntent(userMessage);

        String aiResponse;

        // Route to reservation booking service if active or new booking intent
        if (hasActiveBooking || isReservationBookingIntent) {
            logger.info("Routing to reservation booking service for session: {}", sessionId);

            // Get username from userId if available
            String username = userId != null ? getUsernameFromUserId(userId) : null;

            if (isReservationBookingIntent && !hasActiveBooking) {
                // Start new booking
                aiResponse = reservationBookingService.startReservationBooking(sessionId, userId, username);
            } else {
                // Continue existing booking
                aiResponse = reservationBookingService.processReservationMessage(sessionId, userMessage, userId, username);
            }
        } else {
            // Normal chatbot flow
            // Classify query intent to build focused context (12 categories)
            String queryType = classifyQueryIntent(userMessage);
            logger.info("Classified query type: {}", queryType);

            // Build context with user information if available
            String context = contextService.buildContextForQuery(queryType, userId, userMessage);

            // Generate response using Gemini
            aiResponse = geminiService.generateResponse(userMessage, context);
        }

        // Detect language for response (once, used for message storage)
        String language = geminiService.detectLanguage(userMessage);
        logger.info("Detected language: {}", language);

        // Create chat message
        ChatMessage message = new ChatMessage(
                UUID.randomUUID().toString(),
                sessionId,
                userMessage,
                aiResponse,
                language,
                new Date()
        );

        // Add to session history
        session.addMessage(message);

        // Auto-cleanup old messages (keep only last 100 messages per session)
        if (session.getMessages().size() > 100) {
            List<ChatMessage> messages = session.getMessages();
            session.setMessages(new ArrayList<>(messages.subList(messages.size() - 100, messages.size())));
        }

        return message;
    }

    /**
     * Detect if user message indicates reservation booking intent (English and Vietnamese)
     */
    private boolean isReservationBookingIntent(String message) {
        if (message == null) return false;
        String lower = message.toLowerCase();

        // English patterns
        boolean englishIntent = lower.matches(".*(book|reserve|table|reservation|make a reservation|" +
                "i want to book|i'd like to book|can i book|need a table|want a table|book a table).*");

        // Vietnamese patterns
        boolean vietnameseIntent = lower.matches(".*(đặt bàn|tôi muốn đặt|cho tôi đặt|muốn đặt bàn|" +
                "giúp tôi đặt|giúp đặt|đặt chỗ|tôi cần đặt|cần đặt bàn|em muốn đặt|" +
                "book bàn|đăng ký bàn|order bàn|đặt cọc|đặt trước).*");

        return englishIntent || vietnameseIntent;
    }

    /**
     * Get username from userId (helper method)
     */
    private String getUsernameFromUserId(Long userId) {
        if (userId == null) return null;
        try {
            var user = userService.getUserById(userId);
            if (user != null) {
                // Return first name if available, otherwise username
                String firstName = user.getFirstName();
                return (firstName != null && !firstName.isEmpty()) ? firstName : user.getUsername();
            }
            return "Guest";
        } catch (Exception e) {
            logger.error("Error retrieving username for userId: {}", userId, e);
            return "Guest";
        }
    }

    /**
     * Get chat history for a session
     *
     * @param sessionId Session identifier
     * @return List of chat messages
     */
    public List<ChatMessage> getChatHistory(String sessionId) {
        ChatSession session = activeSessions.get(sessionId);
        if (session == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(session.getMessages());
    }

    /**
     * Close a chat session
     *
     * @param sessionId Session identifier
     */
    public void closeSession(String sessionId) {
        ChatSession removed = activeSessions.remove(sessionId);
        if (removed != null) {
            logger.info("Closed chat session: {}", sessionId);
        }
    }

    /**
     * Classify the intent of user's query into expanded categories
     * Supports both Vietnamese and English keywords
     * Now covers all 22+ entities in the database
     * Optimized using contains() for better performance
     */
    private String classifyQueryIntent(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "general";
        }
        
        String lowerMessage = message.toLowerCase();

        // Orders - status, items, modify, cancel
        if (containsAny(lowerMessage, "order", "my order", "order status", "order number", "track",
                "đơn hàng", "đơn", "mã đơn", "theo dõi", "trạng thái đơn", "bought", "purchase", "mua")) {
            return "orders";
        }

        // Payment - methods, process, refunds
        if (containsAny(lowerMessage, "payment", "pay", "card", "cash", "refund", "deposit", "charge",
                "thanh toán", "trả tiền", "hoàn tiền", "cọc", "phí", "bill", "receipt", "hóa đơn", "biên lai")) {
            return "payments";
        }

        // Feedback & Ratings
        if (containsAny(lowerMessage, "feedback", "rating", "review", "comment", "đánh giá", "nhận xét", "phản hồi")) {
            return "feedback";
        }

        // Bundles & Combos
        if (containsAny(lowerMessage, "bundle", "combo", "set", "deal", "promotion", "gói", "khuyến mãi")) {
            return "bundles";
        }

        // Categories (requires both category and menu keywords)
        if (containsAny(lowerMessage, "category", "danh mục", "loại", "type", "nhóm") &&
                containsAny(lowerMessage, "menu", "food", "món")) {
            return "categories";
        }

        // Work Schedule (for staff)
        if (containsAny(lowerMessage, "work schedule", "lịch làm việc", "shift", "ca", "my schedule", "lịch của tôi")) {
            return "work_schedule";
        }

        // Attendance (for staff)
        if (containsAny(lowerMessage, "attendance", "điểm danh", "chấm công", "check in", "check out")) {
            return "attendance";
        }

        // Chat History (customer-staff chat)
        if (containsAny(lowerMessage, "chat history", "lịch sử chat", "conversation", "tin nhắn", "support chat", "previous chat")) {
            return "chat";
        }

        // VIP Tiers
        if (containsAny(lowerMessage, "vip", "tier", "hạng", "membership", "thành viên", "loyalty", "điểm thưởng")) {
            return "vip_tiers";
        }

        // Banners & Promotions
        if (containsAny(lowerMessage, "banner", "promotion", "khuyến mãi", "quảng cáo", "advertisement", "deal")) {
            return "banners";
        }

        // Restaurant Info - hours, address, phone, parking, WiFi (exclude menu/reservation keywords)
        if (containsAny(lowerMessage, "address", "location", "where", "phone", "contact", "parking", "wifi",
                "facility", "facilities", "địa chỉ", "ở đâu", "số điện thoại", "liên hệ", "đỗ xe", "chỗ đậu xe", "tiện ích") &&
                !containsAny(lowerMessage, "menu", "food", "dish", "book", "reserve")) {
            return "restaurant_info";
        }

        // Menu & Dishes - prices, ingredients, bestsellers, specials
        if (containsAny(lowerMessage, "menu", "food", "dish", "eat", "meal", "ingredient", "bestseller",
                "popular", "special", "thực đơn", "món ăn", "đồ ăn", "món", "nguyên liệu", "bán chạy", "phổ biến", "đặc biệt")) {
            return "menu";
        }

        // Reservations - check availability, create, modify, cancel
        if (containsAny(lowerMessage, "book", "reserve", "reservation", "table", "seat", "availability", "available",
                "cancel reservation", "đặt bàn", "đặt chỗ", "bàn", "ghế", "còn chỗ", "có chỗ", "hủy đặt")) {
            return "reservations";
        }

        // Allergies & Dietary - filter by allergen, dietary restrictions
        if (containsAny(lowerMessage, "allerg", "dietary", "vegetarian", "vegan", "gluten", "peanut", "shellfish",
                "dairy", "dị ứng", "chay", "thuần chay", "không gluten", "đậu phộng", "hải sản", "sữa", "ăn kiêng")) {
            return "allergies";
        }

        // Suggestions - recommendations, combos, pairings
        if (containsAny(lowerMessage, "recommend", "suggest", "what should", "best", "favorite", "pairing", "wine",
                "gợi ý", "đề xuất", "nên", "tốt nhất", "yêu thích", "kết hợp", "món gì", "ăn gì")) {
            return "suggestions";
        }

        // Service Questions - takeout, delivery, wait times
        if (containsAny(lowerMessage, "takeout", "delivery", "pickup", "catering", "wait time", "service",
                "mang về", "giao hàng", "đặt mang đi", "tiệc", "thời gian chờ", "phục vụ")) {
            return "service";
        }

        // Pricing & Combos - bundles, set menus, average cost
        if (containsAny(lowerMessage, "price", "cost", "how much", "expensive", "cheap", "budget",
                "giá", "giá cả", "bao nhiêu", "đắt", "rẻ", "ngân sách", "hết bao nhiêu")) {
            return "pricing";
        }

        // Time & Schedule - preparation time, wait times, holiday hours
        if (containsAny(lowerMessage, "hour", "time", "open", "close", "schedule", "preparation", "holiday", "weekend",
                "giờ", "thời gian", "mở cửa", "đóng cửa", "lịch", "chuẩn bị", "lễ", "cuối tuần")) {
            return "schedule";
        }

        // General Support - help, errors, contact
        if (containsAny(lowerMessage, "help", "support", "problem", "issue", "complaint",
                "giúp", "hỗ trợ", "vấn đề", "khiếu nại")) {
            return "support";
        }

        // Default: provide general context
        return "general";
    }

    /**
     * Helper method to check if message contains any of the given keywords
     * More efficient than regex matching for simple keyword detection
     */
    private boolean containsAny(String message, String... keywords) {
        for (String keyword : keywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get count of active sessions
     */
    public int getActiveSessionCount() {
        return activeSessions.size();
    }

    /**
     * Clean up inactive sessions (sessions older than 1 hour with no activity)
     */
    public void cleanupInactiveSessions() {
        long oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000);
        activeSessions.entrySet().removeIf(entry -> {
            ChatSession session = entry.getValue();
            if (session.getLastActivity().getTime() < oneHourAgo) {
                logger.info("Removing inactive session: {}", entry.getKey());
                return true;
            }
            return false;
        });
    }

    /**
     * Inner class representing a chat session (in-memory only)
     */
    public static class ChatSession {
        private final String sessionId;
        private final Date createdAt;
        private Date lastActivity;
        private List<ChatMessage> messages;
        private Long userId; // For personalized responses

        public ChatSession(String sessionId) {
            this.sessionId = sessionId;
            this.createdAt = new Date();
            this.lastActivity = new Date();
            this.messages = new ArrayList<>();
            this.userId = null;
        }

        public void addMessage(ChatMessage message) {
            this.messages.add(message);
            this.lastActivity = new Date();
        }

        public String getSessionId() {
            return sessionId;
        }

        public Date getCreatedAt() {
            return createdAt;
        }

        public Date getLastActivity() {
            return lastActivity;
        }

        public List<ChatMessage> getMessages() {
            return messages;
        }

        public void setMessages(List<ChatMessage> messages) {
            this.messages = messages;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }
    }

    /**
     * Inner class representing a chat message
     */
    public static class ChatMessage {
        private final String messageId;
        private final String sessionId;
        private final String userMessage;
        private final String aiResponse;
        private final String language;
        private final Date timestamp;

        public ChatMessage(String messageId, String sessionId, String userMessage,
                           String aiResponse, String language, Date timestamp) {
            this.messageId = messageId;
            this.sessionId = sessionId;
            this.userMessage = userMessage;
            this.aiResponse = aiResponse;
            this.language = language;
            this.timestamp = timestamp;
        }

        public String getMessageId() {
            return messageId;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getUserMessage() {
            return userMessage;
        }

        public String getAiResponse() {
            return aiResponse;
        }

        public String getLanguage() {
            return language;
        }

        public Date getTimestamp() {
            return timestamp;
        }
    }
}
