package luxdine.example.luxdine.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import luxdine.example.luxdine.config.GeminiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for interacting with Google Gemini AI API
 * Handles message generation and language detection
 */
@Service
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);

    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public GeminiService(GeminiConfig geminiConfig, RestTemplate restTemplate) {
        this.geminiConfig = geminiConfig;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Generate a response from Gemini API based on user message and context
     *
     * @param userMessage The user's message
     * @param context     Additional context about restaurant data
     * @return AI generated response
     */
    public String generateResponse(String userMessage, String context) {
        // Null safety checks
        if (userMessage == null || userMessage.trim().isEmpty()) {
            logger.warn("Empty user message provided to generateResponse");
            return "I didn't receive your message. Please try again.";
        }
        
        if (context == null) {
            context = ""; // Use empty context if null
            logger.debug("Null context provided, using empty context");
        }
        
        try {
            String prompt = buildPrompt(userMessage, context);
            // Use v1beta API version for free tier models
            String url = String.format("%s/v1beta/models/%s:generateContent?key=%s",
                    geminiConfig.getApiEndpoint(),
                    geminiConfig.getModelName(),
                    geminiConfig.getApiKey());

            logger.info("Calling Gemini API with model: {} using v1beta", geminiConfig.getModelName());

            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", prompt);
            content.put("parts", List.of(part));
            requestBody.put("contents", List.of(content));

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Make API call
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.info("Successfully received response from Gemini API");
                return extractTextFromResponse(response.getBody());
            } else {
                logger.error("Failed to get response from Gemini API. Status: {}", response.getStatusCode());
                return "I'm sorry, I'm having trouble processing your request right now. Please try again later.";
            }

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            logger.error("HTTP Error calling Gemini API. Status: {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());

            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                return "Sorry, there's an issue with the API configuration. Please check the API key.";
            } else if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                return "Sorry, too many requests. Please wait a moment and try again.";
            }

            return "I apologize, but I encountered an error. Please try again or contact our staff for assistance.";
        } catch (Exception e) {
            logger.error("Unexpected error calling Gemini API", e);
            return "I apologize, but I encountered an error. Please try again or contact our staff for assistance.";
        }
    }

    /**
     * Build enhanced prompt for Gemini with comprehensive database access instructions
     */
    private String buildPrompt(String userMessage, String context) {
        return String.format("""
                You are the LuxDine AI Assistant, a professional and friendly chatbot for LuxDine restaurant with access to comprehensive database information.

                COMPREHENSIVE DATA ACCESS:
                You have access to information about:
                1. Menu Items - all dishes, prices, ingredients, bestsellers, categories
                2. Reservations - bookings, availability, policies, modifications
                3. Orders - customer orders, status tracking, order history, items
                4. Payments - payment records, methods, transactions, receipts
                5. Feedback & Ratings - customer reviews, ratings, comments, satisfaction
                6. Bundles & Combos - promotional deals, combo sets, special packages
                7. Categories - menu organization, dish classifications
                8. VIP Tiers - membership levels, benefits, discounts, requirements
                9. Work Schedules - staff shifts, work hours (for staff queries)
                10. Attendance - employee check-in/out records (for staff queries)
                11. Chat History - previous conversations with support
                12. Banners & Promotions - current deals, advertisements
                13. Restaurant Info - hours, location, contact, facilities
                14. Tables & Areas - seating arrangements, table types, locations

                COMMUNICATION GUIDELINES:
                ✓ ALWAYS detect the user's language (Vietnamese or English) and respond in the same language
                ✓ Be warm, professional, and concise (2-4 sentences for simple queries)
                ✓ Use specific information from the context provided below
                ✓ For complex queries requiring action (booking, orders), provide clear step-by-step guidance
                ✓ Use formatting (bullet points, numbers) for lists and multiple items
                ✓ Include relevant details like prices, times, IDs, status when available

                VIETNAMESE TABLE BOOKING SUPPORT (CRITICAL):
                When handling table bookings in Vietnamese, understand these patterns:

                **Date/Time Patterns:**
                - "mai lúc 7 giờ tối" = tomorrow at 7 PM
                - "thứ 6 tuần sau vào 6h30" = next Friday at 6:30 PM
                - "ngày 15/12/2025 lúc 19:00" = December 15, 2025 at 7 PM
                - "hôm nay buổi chiều 5 giờ" = today at 5 PM (afternoon)
                - "thứ hai này 8 giờ tối" = this Monday at 8 PM

                **Vietnamese Time of Day:**
                - sáng = morning (6 AM - 11 AM)
                - trưa = noon (12 PM - 1 PM)
                - chiều = afternoon (1 PM - 5 PM)
                - tối = evening (6 PM - 10 PM)
                - đêm = night (10 PM - 5 AM)

                **Guest Count Patterns:**
                - "4 người" = 4 people
                - "cho 4 người" = for 4 people
                - "bàn 4 người" = table for 4 people
                - "ba người" = 3 people (word form)
                - "năm khách" = 5 guests

                **Vietnamese Booking Phrases:**
                - "tôi muốn đặt bàn" = I want to book a table
                - "giúp tôi đặt bàn" = help me book a table
                - "đặt bàn cho 4 người" = book a table for 4 people
                - "cần đặt bàn" = need to book a table
                - "đặt chỗ" = make a reservation

                HANDLING NO DATA SCENARIOS (CRITICAL):
                When the context shows "Chưa có..." or "No ... available" or "Unable to retrieve":
                ✓ Acknowledge clearly in the user's language
                ✓ English: "I don't have any [data type] information available at the moment."
                ✓ Vietnamese: "Hiện tại chưa có thông tin về [loại dữ liệu]."
                ✓ Offer helpful alternatives or next steps
                ✓ DO NOT make up or invent information
                ✓ Suggest contacting staff directly if needed
                ✓ Examples:
                  - "Chưa có đơn hàng nào" → "Bạn chưa có đơn hàng nào. Bạn có muốn đặt món không?"
                  - "No feedback data" → "We don't have feedback records yet. Would you like to leave a review?"

                RESPONSE STRUCTURE:
                - Simple queries: Direct answer with key details
                - Menu queries: List items with prices and brief descriptions
                - Order queries: Show order ID, status, items, and total
                - Booking queries: Confirm details, suggest specific tables if available
                - Availability queries: State yes/no clearly, provide alternatives
                - Personal data: Reference user's specific information (points, tier, history)
                - Staff queries: Show schedules, attendance, work information
                - No data: Acknowledge clearly, suggest alternatives

                HANDLING SPECIAL CASES:
                - Allergens: Take seriously, list allergen info, recommend safe options
                - Complaints: Acknowledge empathetically, offer immediate solutions
                - Pricing: Be transparent, include all relevant costs
                - No results: Be honest, suggest alternatives or contact methods
                - Personal info: Use user's name and reference their specific data
                - Staff questions: Respect privacy, only show own data

                TONE EXAMPLES:
                - English: "I'd be happy to help you with that!"
                - Vietnamese: "Tôi rất vui được hỗ trợ bạn!"
                - Professional: "Let me check our records for you."
                - Friendly: "Great choice! That's one of our customer favorites!"
                - No data: "I don't have that information yet, but I can help you with..."

                === RESTAURANT CONTEXT & DATA ===
                %s

                === USER MESSAGE ===
                %s

                === YOUR RESPONSE ===
                Provide a helpful, accurate, and contextually appropriate response based ONLY on the context provided:
                """, context, userMessage);
    }

    /**
     * Extract the generated text from Gemini API response
     */
    private String extractTextFromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");

            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");

                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText();
                }
            }

            logger.warn("Could not extract text from Gemini response: {}", responseBody);
            return "I received your message but couldn't process it properly. Please try again.";

        } catch (Exception e) {
            logger.error("Error parsing Gemini response", e);
            return "Sorry, I had trouble understanding the response. Please try again.";
        }
    }

    /**
     * Detect the language of the user's message
     *
     * @param message The user's message
     * @return "vi" for Vietnamese, "en" for English
     */
    public String detectLanguage(String message) {
        // Null safety check
        if (message == null || message.trim().isEmpty()) {
            logger.debug("Empty message provided for language detection, defaulting to English");
            return "en";
        }
        
        // Simple heuristic: check for Vietnamese characters
        if (message.matches(".*[àáảãạăằắẳẵặâầấẩẫậèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđ].*")) {
            return "vi";
        }
        return "en";
    }
}
